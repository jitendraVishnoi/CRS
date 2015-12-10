package crs.paypal;

import java.io.Serializable;

import atg.commerce.order.Order;
import atg.core.util.Address;
import atg.userprofiling.Profile;

public class PayPalInfo implements Serializable {

	private static final long serialVersionUID = -1925761224523223839L;
	public static final String PAYMENTACTION_SALE = "Sale";
	public static final String PAYMENTACTION_AUTHORIZATION = "Authorization";
	public static final String PAYMENTACTION_ORDER = "Order";
	public static final String METHOD_SETEXPRESSCHECKOUT = "SetExpressCheckout";
	public static final String METHOD_GETDETAILS = "GetExpressCheckoutDetails";
	public static final String METHOD_GETTRANSACTIONDETAILS = "GetTransactionDetails";
	public static final String METHOD_DOEXPRESSCHECKOUT = "DoExpressCheckoutPayment";
	public static final String METHOD_PAYMENT = "DoExpressCheckoutPayment";
	public static final String METHOD_REFUND = "RefundTransaction";
	public static final String METHOD_AUTH = "DoAuthorization";
	public static final String METHOD_CAPTURE = "DoCapture";
	public static final String METHOD_VOID = "DoVoid";
	public static final String METHOD_BAUPDATE = "BillAgreementUpdate";
	public static final String METHOD_DOREFERENCETRANSACTION = "DoReferenceTransaction";
	public static final String LOCALECODE_AU = "AU";
	public static final String LOCALECODE_AT = "AT";
	public static final String LOCALECODE_BE = "BE";
	public static final String LOCALECODE_CA = "CA";
	public static final String LOCALECODE_CH = "CH";
	public static final String LOCALECODE_CN = "CN";
	public static final String LOCALECODE_DE = "DE";
	public static final String LOCALECODE_FR = "FR";
	public static final String LOCALECODE_GB = "GB";
	public static final String LOCALECODE_IT = "IT";
	public static final String LOCALECODE_ES = "ES";
	public static final String LOCALECODE_NL = "NL";
	public static final String LOCALECODE_PL = "PL";
	public static final String LOCALECODE_US = "US";
	private Order mOrder;
	private String mPaymentId;
	private double mAmount;
	private String mCurrencyCode;
	private Address mBillingAddress;
	private Address mShippingAddress;
	private String mToken;
	private String mBillingAgreementId;
	private String mReturnURL;
	private String mCancelURL;
	private String mLocaleCode;
	private boolean mIsAddressOverride = false;

	private boolean mIsRequireConfirmShipping = false;

	private boolean mIsNoShipping = false;

	private boolean mIsCallBack = false;
	private String mCallbackURL;
	private String mEmail;
	private Profile mProfile;
	private String mTransactionId;
	private String mPayerId;

	public void setOrder(Order pOrder) {
		this.mOrder = pOrder;
	}

	public Order getOrder() {
		return this.mOrder;
	}

	public String getPaymentId() {
		return this.mPaymentId;
	}

	public void setPaymentId(String pPaymentId) {
		this.mPaymentId = pPaymentId;
	}

	public double getAmount() {
		return this.mAmount;
	}

	public void setAmount(double pAmount) {
		this.mAmount = pAmount;
	}

	public String getCurrencyCode() {
		return this.mCurrencyCode;
	}

	public void setCurrencyCode(String pCurrencyCode) {
		this.mCurrencyCode = pCurrencyCode;
	}

	public Address getBillingAddress() {
		return this.mBillingAddress;
	}

	public void setBillingAddress(Address pBillingAddress) {
		this.mBillingAddress = pBillingAddress;
	}

	public void setShippingAddress(Address pShippingAddress) {
		this.mShippingAddress = pShippingAddress;
	}

	public Address getShippingAddress() {
		return this.mShippingAddress;
	}

	public void setToken(String pToken) {
		this.mToken = pToken;
	}

	public String getToken() {
		return this.mToken;
	}

	public String getReturnURL() {
		return this.mReturnURL;
	}

	public void setReturnURL(String pReturnURL) {
		this.mReturnURL = pReturnURL;
	}

	public String getCancelURL() {
		return this.mCancelURL;
	}

	public void setCancelURL(String pCancelURL) {
		this.mCancelURL = pCancelURL;
	}

	public void setLocaleCode(String pLocaleCode) {
		this.mLocaleCode = pLocaleCode;
	}

	public String getLocaleCode() {
		return this.mLocaleCode == null ? "US" : this.mLocaleCode;
	}

	public boolean isAddressOverride() {
		return this.mIsAddressOverride;
	}

	public void setIsAddressOverride(boolean pIsAddressOverride) {
		this.mIsAddressOverride = pIsAddressOverride;
	}

	public boolean isRequireConfirmShipping() {
		return this.mIsRequireConfirmShipping;
	}

	public void setIsRequireConfirmShipping(boolean pIsRequireConfirmShipping) {
		this.mIsRequireConfirmShipping = pIsRequireConfirmShipping;
	}

	public boolean isNoShipping() {
		return this.mIsNoShipping;
	}

	public void setIsNoShipping(boolean pIsNoShipping) {
		this.mIsNoShipping = pIsNoShipping;
	}

	public void setIsCallBack(boolean pIsCallBack) {
		this.mIsCallBack = pIsCallBack;
	}

	public boolean isCallBack() {
		return this.mIsCallBack;
	}

	public void setCallbackURL(String pCallbackURL) {
		this.mCallbackURL = pCallbackURL;
	}

	public String getCallbackURL() {
		return this.mCallbackURL;
	}

	public void setEmail(String pEmail) {
		this.mEmail = pEmail;
	}

	public String getEmail() {
		return this.mEmail;
	}

	public void setProfile(Profile pProfile) {
		this.mProfile = pProfile;
	}

	public Profile getProfile() {
		return this.mProfile;
	}

	public void setTransactionId(String pTransactionId) {
		this.mTransactionId = pTransactionId;
	}

	public String getTransactionId() {
		return this.mTransactionId;
	}

	public String getPayerId() {
		return this.mPayerId;
	}

	public void setPayerId(String pPayerId) {
		this.mPayerId = pPayerId;
	}

	public String getBillingAgreementId() {
		return this.mBillingAgreementId;
	}

	public void setBillingAgreementId(String pBillingAgreementId) {
		this.mBillingAgreementId = pBillingAgreementId;
	}

	@Override
	public String toString() {
		return "PayPalInfo [mOrder=" + mOrder.getId() + ", mPaymentId=" + mPaymentId
				+ ", mAmount=" + mAmount + ", mCurrencyCode=" + mCurrencyCode
				+ ", mBillingAddress=" + mBillingAddress
				+ ", mShippingAddress=" + mShippingAddress + ", mToken="
				+ mToken + ", mBillingAgreementId=" + mBillingAgreementId
				+ ", mReturnURL=" + mReturnURL + ", mCancelURL=" + mCancelURL
				+ ", mLocaleCode=" + mLocaleCode + ", mIsAddressOverride="
				+ mIsAddressOverride + ", mIsRequireConfirmShipping="
				+ mIsRequireConfirmShipping + ", mIsNoShipping="
				+ mIsNoShipping + ", mIsCallBack=" + mIsCallBack
				+ ", mCallbackURL=" + mCallbackURL + ", mEmail=" + mEmail
				+ ", mProfile=" + mProfile + ", mTransactionId="
				+ mTransactionId + ", mPayerId=" + mPayerId + "]";
	}
	
	
}
