package com.dhc.api.domain.web.rest;

import static org.elasticsearch.index.query.QueryBuilders.queryStringQuery;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.codahale.metrics.annotation.Timed;
import com.dhc.api.domain.domain.Transaction;
import com.dhc.api.domain.repository.TransactionRepository;
import com.dhc.api.domain.repository.search.TransactionSearchRepository;
import com.dhc.api.domain.web.rest.errors.BadRequestAlertException;
import com.dhc.api.domain.web.rest.util.HeaderUtil;
import com.eway.payment.rapid.sdk.RapidClient;
import com.eway.payment.rapid.sdk.RapidSDK;
import com.eway.payment.rapid.sdk.beans.external.Refund;
import com.eway.payment.rapid.sdk.beans.internal.RefundDetails;
import com.eway.payment.rapid.sdk.output.RefundResponse;
import com.stripe.Stripe;
import com.stripe.exception.APIConnectionException;
import com.stripe.exception.APIException;
import com.stripe.exception.AuthenticationException;
import com.stripe.exception.CardException;
import com.stripe.exception.InvalidRequestException;

import io.github.jhipster.web.util.ResponseUtil;

/**
 * REST controller for managing Transaction.
 */
@RestController
@RequestMapping("/api")
public class TransactionResource2 {

    private final Logger log = LoggerFactory.getLogger(TransactionResource2.class);

    private static final String ENTITY_NAME = "transaction";

    private final TransactionRepository transactionRepository;

    private final TransactionSearchRepository transactionSearchRepository;
    
    private static String apiKey="C3AB9CgeTmTPFcVeKnxZXT8QKu26px4oz6JxeWTC9fY9O6C8NFiNc5fYJ6OqOFzo9YrJVp";
	private static String password="0I5TbujC";
	private static String rapidEnd="Sandbox";

	private static String stripe_apiKey="sk_test_MUVjvax4c6FLnvNpZHPVDCPP";
	
    public TransactionResource2(TransactionRepository transactionRepository, TransactionSearchRepository transactionSearchRepository) {
        this.transactionRepository = transactionRepository;
        this.transactionSearchRepository = transactionSearchRepository;
    }

    /**
     * POST  /transactions : Create a new transaction.
     *
     * @param transaction the transaction to create
     * @return the ResponseEntity with status 201 (Created) and with body the new transaction, or with status 400 (Bad Request) if the transaction has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping("/transactions2")
    @Timed
    public ResponseEntity<Transaction> createTransaction(@RequestBody Transaction transaction) throws URISyntaxException {
        log.debug("REST request to save Transaction : {}", transaction);
        if (transaction.getId() != null) {
            throw new BadRequestAlertException("A new transaction cannot already have an ID", ENTITY_NAME, "idexists");
        }
        //To do the function based on transaction_type
        if(transaction.getTransactionType().equalsIgnoreCase("refund")){
        	//RefundParams refund = new RefundParams();
        	//to do refund
        	
        	if(transaction.getGateway().equalsIgnoreCase("eway")){
        		
        		String ref_id = transaction.getAppTransactionId();
        		ref_id=ref_id.substring(7);
        		System.out.println(ref_id);
        		
        		//prop = (Properties) session.getAttribute(Constants.PROPERTIES);
        	
        		RapidClient client = RapidSDK.newRapidClient(apiKey, password, rapidEnd);
        		RefundDetails refundDetails = new RefundDetails();
        		
        		refundDetails.setOriginalTransactionID(ref_id);
        		refundDetails.setTotalAmount(transaction.getAmount());
        		Refund refund = new Refund();
        		refund.setRefundDetails(refundDetails);
        		RefundResponse response = client.refund(refund);
        		System.out.println(response.getTransactionStatus().isStatus());
        		if (response.getTransactionStatus().isStatus()) {
        			System.out.println("Refund successful! ID: " + response.getTransactionStatus().getTransactionID());
        			int refID = response.getTransactionStatus().getTransactionID();
        			String app_ref = "ew_refund_" + refID;
        			transaction.transactionId(ref_id);
        			transaction.setAppTransactionId(app_ref);
        			transaction.setStatus("success");
        			
        			LocalDateTime now = LocalDateTime.now();
        			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        			String dateTime = now.format(formatter);
        			
        			transaction.setTransactionDatetime(dateTime);
        		}else {
        				if (!response.getErrors().isEmpty()) {
        					String errorCode=null;
        					for (String errorcode : response.getErrors()) {
        						System.out.println("Error Message: " + RapidSDK.userDisplayMessage(errorcode, "en"));
        						errorCode = errorcode.concat(RapidSDK.userDisplayMessage(errorcode, "en"));
        						        					}
        					log.error("Sorry, your refund failed with ErrorCode-->"+errorCode);
        					return ResponseEntity.created(new URI("/api/transactions/" + errorCode))
        				            .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, errorCode))
        				            .body(null);
        					
        				} else {
        					System.out.println("Sorry, your refund failed");
        					log.error("Sorry, your refund failed");
        				}
        				
        			}
        	}
        	
        	//for Stripe
        	else{
        		
        		com.stripe.model.Refund refund = null;
        		Stripe.apiKey = stripe_apiKey;
        		
        		String ref_id = transaction.getAppTransactionId();
        		ref_id=ref_id.substring(8);
        		System.out.println(ref_id);

        		Map<String, Object> refundParams = new HashMap<String, Object>();
        		refundParams.put("charge", ref_id);
        		refundParams.put("amount", transaction.getAmount());
        		try {
        			refund=com.stripe.model.Refund.create(refundParams);
        		} catch (AuthenticationException | InvalidRequestException | APIConnectionException | CardException
        				| APIException e1) {
        			// TODO Auto-generated catch block
        			e1.printStackTrace();
        		}
        		if(refund.getId()!=null){
        		String refundId=refund.getId();
        		//httpResp.setRefId(refundId);
        		String refund_app_refId="stp_refund_"+refundId;
        		transaction.setTransactionId(refundId);
        		transaction.setAppTransactionId(refund_app_refId);
        		transaction.setStatus("success");
        		
        		LocalDateTime now = LocalDateTime.now();
    			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    			String dateTime = now.format(formatter);
    			
    			transaction.setTransactionDatetime(dateTime);
    			
        		}
        		else{
        			
        			log.error("Sorry, your refund failed");
					return ResponseEntity.created(new URI("/api/transactions/"))
				            .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, "Refund Failed"))
				            .body(null);
        		}
        	}
        }
        
        
        //rest all same
        Transaction result = transactionRepository.save(transaction);
        transactionSearchRepository.save(result);
        return ResponseEntity.created(new URI("/api/transactions/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * PUT  /transactions : Updates an existing transaction.
     *
     * @param transaction the transaction to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated transaction,
     * or with status 400 (Bad Request) if the transaction is not valid,
     * or with status 500 (Internal Server Error) if the transaction couldn't be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PutMapping("/transactions2")
    @Timed
    public ResponseEntity<Transaction> updateTransaction(@RequestBody Transaction transaction) throws URISyntaxException {
        log.debug("REST request to update Transaction : {}", transaction);
        if (transaction.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        Transaction result = transactionRepository.save(transaction);
        transactionSearchRepository.save(result);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, transaction.getId().toString()))
            .body(result);
    }

    /**
     * GET  /transactions : get all the transactions.
     *
     * @return the ResponseEntity with status 200 (OK) and the list of transactions in body
     */
    @GetMapping("/transactions2")
    @Timed
    public List<Transaction> getAllTransactions() {
        log.debug("REST request to get all Transactions");
        return transactionRepository.findAll();
    }

    /**
     * GET  /transactions/:id : get the "id" transaction.
     *
     * @param id the id of the transaction to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the transaction, or with status 404 (Not Found)
     */
    @GetMapping("/transactions2/{id}")
    @Timed
    public ResponseEntity<Transaction> getTransaction(@PathVariable Long id) {
        log.debug("REST request to get Transaction : {}", id);
        Optional<Transaction> transaction = transactionRepository.findById(id);
        return ResponseUtil.wrapOrNotFound(transaction);
    }

    /**
     * DELETE  /transactions/:id : delete the "id" transaction.
     *
     * @param id the id of the transaction to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @DeleteMapping("/transactions2/{id}")
    @Timed
    public ResponseEntity<Void> deleteTransaction(@PathVariable Long id) {
        log.debug("REST request to delete Transaction : {}", id);

        transactionRepository.deleteById(id);
        transactionSearchRepository.deleteById(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, id.toString())).build();
    }

    /**
     * SEARCH  /_search/transactions?query=:query : search for the transaction corresponding
     * to the query.
     *
     * @param query the query of the transaction search
     * @return the result of the search
     */
    @GetMapping("/_search/transactions2")
    @Timed
    public List<Transaction> searchTransactions(@RequestParam String query) {
        log.debug("REST request to search Transactions for query {}", query);
        return StreamSupport
            .stream(transactionSearchRepository.search(queryStringQuery(query)).spliterator(), false)
            .collect(Collectors.toList());
    }

}
