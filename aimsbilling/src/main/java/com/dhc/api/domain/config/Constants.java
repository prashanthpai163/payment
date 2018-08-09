package com.dhc.api.domain.config;

/**
 * Application constants.
 */
public class Constants {

    // Regex for acceptable logins
    public static final String LOGIN_REGEX = "^[_.@A-Za-z0-9-]*$";

    public static final String SYSTEM_ACCOUNT = "system";
    public static final String ANONYMOUS_USER = "anonymoususer";
    public static final String DEFAULT_LANGUAGE = "en";
    
    
    //Application-Specific
    public static final String CARD_DETAILS="cardDetails";
	public static final String GATEWAY="gateway";
	public static final String TRANSACTION="transaction";
	public static final String PROPERTIES="properties";
	public static final String REFERENCE="reference";
	public static final String AMOUNT="amount";
	public static final String CURRENCY="currency";
	public static final String DESCRIPTION="description";
	public static final String EWAY_API_KEY="eway_api_key";
	public static final String EWAY_PASSWORD="eway_password";
	public static final String EWAY_RAPIDENDPOINT="eway_rapidEndPoint";
	public static final String STRIPE_API_KEY="stripe_api_key";
	public static final String SOURCE="source";
	public static final String CUSTOMER="customer";
	public static final String SCHEMA="schema";
    
    private Constants() {
    }
}
