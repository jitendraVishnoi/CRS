package crs.commerce.order.purchase;

import atg.commerce.CommerceException;
import atg.commerce.order.CreditCard;
import atg.commerce.order.InvalidParameterException;
import atg.commerce.order.Order;
import atg.projects.store.order.purchase.*;

public class CRSStoreBillingProcessHelper extends StoreBillingProcessHelper {

	@Override
	public void addOrderAmountRemainingToCreditPaymentGroup(Order pOrder)
			throws CommerceException, InvalidParameterException {
		vlogDebug("Inside CRSStoreBillingProcessHelper.addOrderAmountRemainingToCreditPaymentGroup order::{0}", pOrder.getId());
		
		CreditCard creditCard = getCreditCard(pOrder);
		if (creditCard == null) {
			vlogError("No credit card payment group to handle exting from CRSStoreBillingProcessHelper.addOrderAmountRemainingToCreditPaymentGroup");
			return;
		} else {
			super.addOrderAmountRemainingToCreditPaymentGroup(pOrder);
		}
	}
}
