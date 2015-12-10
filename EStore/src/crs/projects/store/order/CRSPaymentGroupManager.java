package crs.projects.store.order;

import atg.commerce.CommerceException;
import atg.commerce.order.Order;
import atg.commerce.order.PaymentGroup;
import atg.projects.store.order.StorePaymentGroupManager;

public class CRSPaymentGroupManager extends StorePaymentGroupManager{
	
	@Override
	public void addPaymentGroupToOrder(Order pOrder, PaymentGroup pPaymentGroup)
			throws CommerceException {
		vlogDebug("CRSPaymentGroupManager.addPaymentGroupToOrder order::{0} paymentGroup::{1}", pOrder.getId(), pPaymentGroup);
		super.addPaymentGroupToOrder(pOrder, pPaymentGroup);
	}
	
	@Override
	public PaymentGroup createPaymentGroup() throws CommerceException {
		vlogDebug("Inside CRSPaymentGroupManager.createPaymentGroup");
		return super.createPaymentGroup();
	}
	
	@Override
	public PaymentGroup createPaymentGroup(String pPaymentGroupType)
			throws CommerceException {
		vlogDebug("Inside CRSPaymentGroupManager.createPaymentGroup pPaymentGroupType::{0}", pPaymentGroupType);
		return super.createPaymentGroup(pPaymentGroupType);
	}

}
