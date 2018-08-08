package com.dhc.api.domain.web.rest;

import com.dhc.api.domain.AimsbillingApp;

import com.dhc.api.domain.domain.Transaction;
import com.dhc.api.domain.repository.TransactionRepository;
import com.dhc.api.domain.repository.search.TransactionSearchRepository;
import com.dhc.api.domain.web.rest.errors.ExceptionTranslator;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.util.Collections;
import java.util.List;


import static com.dhc.api.domain.web.rest.TestUtil.createFormattingConversionService;
import static org.assertj.core.api.Assertions.assertThat;
import static org.elasticsearch.index.query.QueryBuilders.queryStringQuery;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test class for the TransactionResource REST controller.
 *
 * @see TransactionResource
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = AimsbillingApp.class)
public class TransactionResourceIntTest {

    private static final String DEFAULT_TRANSACTION_ID = "AAAAAAAAAA";
    private static final String UPDATED_TRANSACTION_ID = "BBBBBBBBBB";

    private static final String DEFAULT_APP_TRANSACTION_ID = "AAAAAAAAAA";
    private static final String UPDATED_APP_TRANSACTION_ID = "BBBBBBBBBB";

    private static final Long DEFAULT_AMOUNT = 1L;
    private static final Long UPDATED_AMOUNT = 2L;

    private static final String DEFAULT_CURRENCY = "AAAAAAAAAA";
    private static final String UPDATED_CURRENCY = "BBBBBBBBBB";

    private static final String DEFAULT_TRANSACTION_TYPE = "AAAAAAAAAA";
    private static final String UPDATED_TRANSACTION_TYPE = "BBBBBBBBBB";

    private static final String DEFAULT_STATUS = "AAAAAAAAAA";
    private static final String UPDATED_STATUS = "BBBBBBBBBB";

    private static final String DEFAULT_TRANSACTION_DATETIME = "AAAAAAAAAA";
    private static final String UPDATED_TRANSACTION_DATETIME = "BBBBBBBBBB";

    private static final String DEFAULT_GATEWAY = "AAAAAAAAAA";
    private static final String UPDATED_GATEWAY = "BBBBBBBBBB";

    @Autowired
    private TransactionRepository transactionRepository;


    /**
     * This repository is mocked in the com.dhc.api.domain.repository.search test package.
     *
     * @see com.dhc.api.domain.repository.search.TransactionSearchRepositoryMockConfiguration
     */
    @Autowired
    private TransactionSearchRepository mockTransactionSearchRepository;

    @Autowired
    private MappingJackson2HttpMessageConverter jacksonMessageConverter;

    @Autowired
    private PageableHandlerMethodArgumentResolver pageableArgumentResolver;

    @Autowired
    private ExceptionTranslator exceptionTranslator;

    @Autowired
    private EntityManager em;

    private MockMvc restTransactionMockMvc;

    private Transaction transaction;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        final TransactionResource transactionResource = new TransactionResource(transactionRepository, mockTransactionSearchRepository);
        this.restTransactionMockMvc = MockMvcBuilders.standaloneSetup(transactionResource)
            .setCustomArgumentResolvers(pageableArgumentResolver)
            .setControllerAdvice(exceptionTranslator)
            .setConversionService(createFormattingConversionService())
            .setMessageConverters(jacksonMessageConverter).build();
    }

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Transaction createEntity(EntityManager em) {
        Transaction transaction = new Transaction()
            .transactionId(DEFAULT_TRANSACTION_ID)
            .appTransactionId(DEFAULT_APP_TRANSACTION_ID)
            .amount(DEFAULT_AMOUNT)
            .currency(DEFAULT_CURRENCY)
            .transactionType(DEFAULT_TRANSACTION_TYPE)
            .status(DEFAULT_STATUS)
            .transactionDatetime(DEFAULT_TRANSACTION_DATETIME)
            .gateway(DEFAULT_GATEWAY);
        return transaction;
    }

    @Before
    public void initTest() {
        transaction = createEntity(em);
    }

    @Test
    @Transactional
    public void createTransaction() throws Exception {
        int databaseSizeBeforeCreate = transactionRepository.findAll().size();

        // Create the Transaction
        restTransactionMockMvc.perform(post("/api/transactions")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(transaction)))
            .andExpect(status().isCreated());

        // Validate the Transaction in the database
        List<Transaction> transactionList = transactionRepository.findAll();
        assertThat(transactionList).hasSize(databaseSizeBeforeCreate + 1);
        Transaction testTransaction = transactionList.get(transactionList.size() - 1);
        assertThat(testTransaction.getTransactionId()).isEqualTo(DEFAULT_TRANSACTION_ID);
        assertThat(testTransaction.getAppTransactionId()).isEqualTo(DEFAULT_APP_TRANSACTION_ID);
        assertThat(testTransaction.getAmount()).isEqualTo(DEFAULT_AMOUNT);
        assertThat(testTransaction.getCurrency()).isEqualTo(DEFAULT_CURRENCY);
        assertThat(testTransaction.getTransactionType()).isEqualTo(DEFAULT_TRANSACTION_TYPE);
        assertThat(testTransaction.getStatus()).isEqualTo(DEFAULT_STATUS);
        assertThat(testTransaction.getTransactionDatetime()).isEqualTo(DEFAULT_TRANSACTION_DATETIME);
        assertThat(testTransaction.getGateway()).isEqualTo(DEFAULT_GATEWAY);

        // Validate the Transaction in Elasticsearch
        verify(mockTransactionSearchRepository, times(1)).save(testTransaction);
    }

    @Test
    @Transactional
    public void createTransactionWithExistingId() throws Exception {
        int databaseSizeBeforeCreate = transactionRepository.findAll().size();

        // Create the Transaction with an existing ID
        transaction.setId(1L);

        // An entity with an existing ID cannot be created, so this API call must fail
        restTransactionMockMvc.perform(post("/api/transactions")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(transaction)))
            .andExpect(status().isBadRequest());

        // Validate the Transaction in the database
        List<Transaction> transactionList = transactionRepository.findAll();
        assertThat(transactionList).hasSize(databaseSizeBeforeCreate);

        // Validate the Transaction in Elasticsearch
        verify(mockTransactionSearchRepository, times(0)).save(transaction);
    }

    @Test
    @Transactional
    public void getAllTransactions() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList
        restTransactionMockMvc.perform(get("/api/transactions?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(transaction.getId().intValue())))
            .andExpect(jsonPath("$.[*].transactionId").value(hasItem(DEFAULT_TRANSACTION_ID.toString())))
            .andExpect(jsonPath("$.[*].appTransactionId").value(hasItem(DEFAULT_APP_TRANSACTION_ID.toString())))
            .andExpect(jsonPath("$.[*].amount").value(hasItem(DEFAULT_AMOUNT.intValue())))
            .andExpect(jsonPath("$.[*].currency").value(hasItem(DEFAULT_CURRENCY.toString())))
            .andExpect(jsonPath("$.[*].transactionType").value(hasItem(DEFAULT_TRANSACTION_TYPE.toString())))
            .andExpect(jsonPath("$.[*].status").value(hasItem(DEFAULT_STATUS.toString())))
            .andExpect(jsonPath("$.[*].transactionDatetime").value(hasItem(DEFAULT_TRANSACTION_DATETIME.toString())))
            .andExpect(jsonPath("$.[*].gateway").value(hasItem(DEFAULT_GATEWAY.toString())));
    }
    

    @Test
    @Transactional
    public void getTransaction() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get the transaction
        restTransactionMockMvc.perform(get("/api/transactions/{id}", transaction.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.id").value(transaction.getId().intValue()))
            .andExpect(jsonPath("$.transactionId").value(DEFAULT_TRANSACTION_ID.toString()))
            .andExpect(jsonPath("$.appTransactionId").value(DEFAULT_APP_TRANSACTION_ID.toString()))
            .andExpect(jsonPath("$.amount").value(DEFAULT_AMOUNT.intValue()))
            .andExpect(jsonPath("$.currency").value(DEFAULT_CURRENCY.toString()))
            .andExpect(jsonPath("$.transactionType").value(DEFAULT_TRANSACTION_TYPE.toString()))
            .andExpect(jsonPath("$.status").value(DEFAULT_STATUS.toString()))
            .andExpect(jsonPath("$.transactionDatetime").value(DEFAULT_TRANSACTION_DATETIME.toString()))
            .andExpect(jsonPath("$.gateway").value(DEFAULT_GATEWAY.toString()));
    }
    @Test
    @Transactional
    public void getNonExistingTransaction() throws Exception {
        // Get the transaction
        restTransactionMockMvc.perform(get("/api/transactions/{id}", Long.MAX_VALUE))
            .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updateTransaction() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        int databaseSizeBeforeUpdate = transactionRepository.findAll().size();

        // Update the transaction
        Transaction updatedTransaction = transactionRepository.findById(transaction.getId()).get();
        // Disconnect from session so that the updates on updatedTransaction are not directly saved in db
        em.detach(updatedTransaction);
        updatedTransaction
            .transactionId(UPDATED_TRANSACTION_ID)
            .appTransactionId(UPDATED_APP_TRANSACTION_ID)
            .amount(UPDATED_AMOUNT)
            .currency(UPDATED_CURRENCY)
            .transactionType(UPDATED_TRANSACTION_TYPE)
            .status(UPDATED_STATUS)
            .transactionDatetime(UPDATED_TRANSACTION_DATETIME)
            .gateway(UPDATED_GATEWAY);

        restTransactionMockMvc.perform(put("/api/transactions")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(updatedTransaction)))
            .andExpect(status().isOk());

        // Validate the Transaction in the database
        List<Transaction> transactionList = transactionRepository.findAll();
        assertThat(transactionList).hasSize(databaseSizeBeforeUpdate);
        Transaction testTransaction = transactionList.get(transactionList.size() - 1);
        assertThat(testTransaction.getTransactionId()).isEqualTo(UPDATED_TRANSACTION_ID);
        assertThat(testTransaction.getAppTransactionId()).isEqualTo(UPDATED_APP_TRANSACTION_ID);
        assertThat(testTransaction.getAmount()).isEqualTo(UPDATED_AMOUNT);
        assertThat(testTransaction.getCurrency()).isEqualTo(UPDATED_CURRENCY);
        assertThat(testTransaction.getTransactionType()).isEqualTo(UPDATED_TRANSACTION_TYPE);
        assertThat(testTransaction.getStatus()).isEqualTo(UPDATED_STATUS);
        assertThat(testTransaction.getTransactionDatetime()).isEqualTo(UPDATED_TRANSACTION_DATETIME);
        assertThat(testTransaction.getGateway()).isEqualTo(UPDATED_GATEWAY);

        // Validate the Transaction in Elasticsearch
        verify(mockTransactionSearchRepository, times(1)).save(testTransaction);
    }

    @Test
    @Transactional
    public void updateNonExistingTransaction() throws Exception {
        int databaseSizeBeforeUpdate = transactionRepository.findAll().size();

        // Create the Transaction

        // If the entity doesn't have an ID, it will be created instead of just being updated
        restTransactionMockMvc.perform(put("/api/transactions")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(transaction)))
            .andExpect(status().isBadRequest());

        // Validate the Transaction in the database
        List<Transaction> transactionList = transactionRepository.findAll();
        assertThat(transactionList).hasSize(databaseSizeBeforeUpdate);

        // Validate the Transaction in Elasticsearch
        verify(mockTransactionSearchRepository, times(0)).save(transaction);
    }

    @Test
    @Transactional
    public void deleteTransaction() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        int databaseSizeBeforeDelete = transactionRepository.findAll().size();

        // Get the transaction
        restTransactionMockMvc.perform(delete("/api/transactions/{id}", transaction.getId())
            .accept(TestUtil.APPLICATION_JSON_UTF8))
            .andExpect(status().isOk());

        // Validate the database is empty
        List<Transaction> transactionList = transactionRepository.findAll();
        assertThat(transactionList).hasSize(databaseSizeBeforeDelete - 1);

        // Validate the Transaction in Elasticsearch
        verify(mockTransactionSearchRepository, times(1)).deleteById(transaction.getId());
    }

    @Test
    @Transactional
    public void searchTransaction() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);
        when(mockTransactionSearchRepository.search(queryStringQuery("id:" + transaction.getId())))
            .thenReturn(Collections.singletonList(transaction));
        // Search the transaction
        restTransactionMockMvc.perform(get("/api/_search/transactions?query=id:" + transaction.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(transaction.getId().intValue())))
            .andExpect(jsonPath("$.[*].transactionId").value(hasItem(DEFAULT_TRANSACTION_ID.toString())))
            .andExpect(jsonPath("$.[*].appTransactionId").value(hasItem(DEFAULT_APP_TRANSACTION_ID.toString())))
            .andExpect(jsonPath("$.[*].amount").value(hasItem(DEFAULT_AMOUNT.intValue())))
            .andExpect(jsonPath("$.[*].currency").value(hasItem(DEFAULT_CURRENCY.toString())))
            .andExpect(jsonPath("$.[*].transactionType").value(hasItem(DEFAULT_TRANSACTION_TYPE.toString())))
            .andExpect(jsonPath("$.[*].status").value(hasItem(DEFAULT_STATUS.toString())))
            .andExpect(jsonPath("$.[*].transactionDatetime").value(hasItem(DEFAULT_TRANSACTION_DATETIME.toString())))
            .andExpect(jsonPath("$.[*].gateway").value(hasItem(DEFAULT_GATEWAY.toString())));
    }

    @Test
    @Transactional
    public void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Transaction.class);
        Transaction transaction1 = new Transaction();
        transaction1.setId(1L);
        Transaction transaction2 = new Transaction();
        transaction2.setId(transaction1.getId());
        assertThat(transaction1).isEqualTo(transaction2);
        transaction2.setId(2L);
        assertThat(transaction1).isNotEqualTo(transaction2);
        transaction1.setId(null);
        assertThat(transaction1).isNotEqualTo(transaction2);
    }
}
