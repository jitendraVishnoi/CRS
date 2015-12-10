package crs.paypal;

import java.util.Date;

import atg.payment.PaymentStatusImpl;

public class PayPalStatus extends PaymentStatusImpl {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private String mCorrelationId;
	private Date mTimeStamp;
	private String mProtectionEligibility;

	public void setTimeStamp(Date pTimeStamp) {
		this.mTimeStamp = pTimeStamp;
	}

	public Date getTimeStamp() {
		return this.mTimeStamp;
	}

	public String getCorrelationId() {
		return this.mCorrelationId;
	}

	public void setCorrelationId(String pCorrelationId) {
		this.mCorrelationId = pCorrelationId;
	}

	public String getProtectionEligibility() {
		return this.mProtectionEligibility;
	}

	public void setProtectionEligibility(String pProtectionEligibility) {
		this.mProtectionEligibility = pProtectionEligibility;
	}
}
