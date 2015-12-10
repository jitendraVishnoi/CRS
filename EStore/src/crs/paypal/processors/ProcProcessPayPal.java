package crs.paypal.processors;

import crs.commerce.order.PayPalPaymentGroup;
import crs.paypal.PayPalInfo;
import crs.paypal.PayPalProcessor;
import crs.paypal.PayPalStatus;
import crs.paypal.PayPalUtils;
import atg.commerce.CommerceException;
import atg.commerce.payment.PaymentManagerPipelineArgs;
import atg.commerce.payment.processor.ProcProcessPaymentGroup;
import atg.payment.PaymentStatus;

public class ProcProcessPayPal extends ProcProcessPaymentGroup{

	private PayPalProcessor payPalProcessor;

	@Override
	public PaymentStatus authorizePaymentGroup(PaymentManagerPipelineArgs pParams)
			throws CommerceException {
		vlogDebug("ProcProcessPayPal.authorizePaymentGroup:called");
		PayPalInfo payPalInfo = (PayPalInfo)pParams.getPaymentInfo();
		PayPalPaymentGroup pg = (PayPalPaymentGroup)pParams.getPaymentGroup();

		if (!PayPalUtils.isPayPalPGUsingBillingAgreement(pg))
		{
			getPayPalProcessor().callDoExpressCheckout(payPalInfo);
			pg.setTransactionId(payPalInfo.getTransactionId());
		}

		PayPalStatus status = getPayPalProcessor().callDoAuthorization(payPalInfo);
		return status;

	}

	@Override
	public PaymentStatus debitPaymentGroup(PaymentManagerPipelineArgs paramPaymentManagerPipelineArgs)
			throws CommerceException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PaymentStatus creditPaymentGroup(PaymentManagerPipelineArgs paramPaymentManagerPipelineArgs)
			throws CommerceException {
		// TODO Auto-generated method stub
		return null;
	}

	public PayPalProcessor getPayPalProcessor() {
		return payPalProcessor;
	}

	public void setPayPalProcessor(PayPalProcessor payPalProcessor) {
		this.payPalProcessor = payPalProcessor;
	}

}
