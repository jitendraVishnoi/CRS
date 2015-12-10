package crs.commerce.order;

import atg.commerce.CommerceException;
import atg.commerce.order.Order;
import atg.projects.store.mobile.order.MobileStoreOrderManager;

public class CRSOrderManager extends MobileStoreOrderManager{
	
	@Override
	public void addRemainingOrderAmountToPaymentGroup(Order pOrder,
			String pPaymentGroupId) throws CommerceException {
		vlogDebug("Inside CRSOrderManager.addRemainingOrderAmountToPaymentGroup order :: {0}", pOrder.getId());
		super.addRemainingOrderAmountToPaymentGroup(pOrder, pPaymentGroupId);
	}

}
