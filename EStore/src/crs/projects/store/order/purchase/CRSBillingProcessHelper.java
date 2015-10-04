package crs.projects.store.order.purchase;

import java.util.List;

import atg.commerce.CommerceException;
import atg.commerce.order.Order;
import atg.commerce.order.PaymentGroup;
import atg.commerce.order.PaymentGroupRelationship;
import atg.commerce.order.RelationshipTypes;
import atg.projects.store.order.purchase.StoreBillingProcessHelper;
import atg.repository.RepositoryItem;
import crs.commerce.order.PaypalPayment;
import crs.projects.store.order.CRSPaymentGroupManager;

public class CRSBillingProcessHelper extends StoreBillingProcessHelper{
	
	private String payPalPaymentType;

	@SuppressWarnings("unchecked")
	public void setupPaypalPaymentGrpForOrder(Order order, RepositoryItem profile) {
		removePaypalPaymentGrp(order);
		initializePaypalPaymentGroup(order, profile);

		boolean exists = false;
		List<PaymentGroupRelationship> relationShips = order.getPaymentGroupRelationships();
		for (PaymentGroupRelationship paymentGroupRelationship : relationShips) {
			if (paymentGroupRelationship.getRelationshipType() == RelationshipTypes.ORDERAMOUNTREMAINING) {
				exists = true;
				break;
			}
		}

		if(!exists) {
			addOrderAmountRemainingToPaypalPaymentGroup(order);
		}


	}

	private void addOrderAmountRemainingToPaypalPaymentGroup(Order order) {
		try {
			double orderRemainingAmount = getOrderRemaningAmount(order);
			if (orderRemainingAmount > 0) {
				PaypalPayment payPal = (PaypalPayment) getPaymentGroupManager().createPaymentGroup(getPayPalPaymentType());
				//payPal.setPropertyValue("paymentMethod", "paypal");
				getOrderManager().addRemainingOrderAmountToPaymentGroup(order, payPal.getId());
				getOrderManager().updateOrder(order);
			}
		} catch (CommerceException e) {
			vlogError(e,"addOrderAmountRemainingToPaypalPaymentGroup : {0}");
		}
		
	}

	private void initializePaypalPaymentGroup(Order order,
			RepositoryItem profile) {
		getPaymentGroupManager().initializePaypalPaymentGroup(order, profile);

	}

	private void removePaypalPaymentGrp(Order order) {
		getPaymentGroupManager().removePaypalPaymentGroup(order);
	}

	// Getter/Setter starts
	@Override
	public CRSPaymentGroupManager getPaymentGroupManager() {
		// TODO Auto-generated method stub
		return (CRSPaymentGroupManager) super.getPaymentGroupManager();
	}

	public String getPayPalPaymentType() {
		return payPalPaymentType;
	}

	public void setPayPalPaymentType(String payPalPaymentType) {
		this.payPalPaymentType = payPalPaymentType;
	}

}
