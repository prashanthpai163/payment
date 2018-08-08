/**
 * 
 */
package com.dhc.api.domain.domain;

/**
 * @author Prashanth.Pai
 *
 */
public class RefundParams {

	private String transactionID;
	private String amount;
	private int customer_ID;
	public String getTransactionID() {
		return transactionID;
	}
	public void setTransactionID(String transactionID) {
		this.transactionID = transactionID;
	}
	public String getAmount() {
		return amount;
	}
	public void setAmount(String amount) {
		this.amount = amount;
	}
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("RefundParams [transactionID=").append(transactionID).append(", amount=").append(amount)
				.append(", customer_ID=").append(customer_ID).append("]");
		return builder.toString();
	}
	public int getCustomer_ID() {
		return customer_ID;
	}
	public void setCustomer_ID(int customer_ID) {
		this.customer_ID = customer_ID;
	}
}
