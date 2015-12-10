package crs.commerce.order.purchase;

import crs.commerce.order.PayPalPaymentGroup;
import crs.paypal.PayPalProcessor;
import atg.commerce.CommerceException;
import atg.commerce.order.CreditCard;
import atg.commerce.order.InvalidParameterException;
import atg.commerce.order.Order;
import atg.projects.store.order.purchase.*;

public class CRSStoreBillingProcessHelper extends StoreBillingProcessHelper {

	private PayPalProcessor payPalProcessor;
	
	
	@Override
	public void addOrderAmountRemainingToCreditPaymentGroup(Order pOrder)
			throws CommerceException, InvalidParameterException {
		vlogDebug("Inside CRSStoreBillingProcessHelper.addOrderAmountRemainingToCreditPaymentGroup order::{0}", pOrder.getId());
		
		CreditCard creditCard = getCreditCard(pOrder);
		if (creditCard == null) {
			vlogError("No credit card payment group to handle exting from CRSStoreBillingProcessHelper.addOrderAmountRemainingToCreditPaymentGroup");
			return;
		} else {
			PayPalPaymentGroup payPalPaymentGroup = getPayPalProcessor().findPayPalPG(pOrder);
			//going to remove PayPalPayment Group as it is remainingType 
			if (payPalPaymentGroup != null) {
				getPaymentGroupManager().removePaymentGroupFromOrder(pOrder, payPalPaymentGroup.getId());
			}
			super.addOrderAmountRemainingToCreditPaymentGroup(pOrder);
		}
	}


	public PayPalProcessor getPayPalProcessor() {
		return payPalProcessor;
	}


	public void setPayPalProcessor(PayPalProcessor payPalProcessor) {
		this.payPalProcessor = payPalProcessor;
	}
	
	
}
