package crs.projects.store.order;

import java.util.List;

import atg.commerce.CommerceException;
import atg.commerce.order.CreditCard;
import atg.commerce.order.Order;
import atg.commerce.order.PaymentGroup;
import atg.commerce.order.PaymentGroupRelationship;
import atg.commerce.order.RelationshipTypes;
import atg.commerce.order.StoreCredit;
import atg.projects.store.order.StorePaymentGroupManager;
import atg.repository.RepositoryItem;

public class CRSPaymentGroupManager extends StorePaymentGroupManager{

	private String paypalPaymentType;
	
	@SuppressWarnings("unchecked")
	public void removePaypalPaymentGroup(Order order) {
		
		PaymentGroup paymentGroup = null;
		List<PaymentGroup> allPaymentGrps = order.getPaymentGroups();
		
		for (PaymentGroup group : allPaymentGrps) {
			if (group.getPaymentGroupClassType().equals(getPaypalPaymentType())) {
				paymentGroup = group;
				break;
			}
		}
		
		vlogDebug("paymentGroup :{0}", paymentGroup);
		if (paymentGroup == null) {
			return;
		}
		
		try {
			removePaymentGroupFromOrder(order, paymentGroup.getId());
		} catch (CommerceException e) {
			vlogError("error in removing paypal payement group", e);
		}
		
	}

	@SuppressWarnings("unchecked")
	public boolean initializePaypalPaymentGroup(Order order, RepositoryItem profile) {

		removePaypalPaymentGroup(order);

		PaymentGroup storeCreditGrp = null;
		CreditCard card = ((CRSOrderTools) getOrderTools()).getCreditCard(order);
		List<StoreCredit> storeCreditGroups = ((CRSOrderTools) getOrderTools()).getStoreCreditPaymentGroups(order);

		try {
			for (StoreCredit storeCredit : storeCreditGroups) {
				if (storeCredit.getOrderRelationship().getRelationshipType() == RelationshipTypes.ORDERAMOUNTREMAINING) {
					storeCreditGrp = storeCredit;
					break;
				}
			}
			if (card != null) {
				getOrderManager().removeRemainingOrderAmountFromPaymentGroup(order, card.getId());
			} 
			if (storeCreditGrp != null) {
				getOrderManager().removeRemainingOrderAmountFromPaymentGroup(order, storeCreditGrp.getId());
			}
		} catch (CommerceException e) {
			vlogError(e, "Error in initializePaypalPaymentGroup : {0}");
			return false;
		}
		
		return true;
	}
	
	//Getter/Setter starts

	public String getPaypalPaymentType() {
		return paypalPaymentType;
	}

	public void setPaypalPaymentType(String paypalPaymentType) {
		this.paypalPaymentType = paypalPaymentType;
	}


	
}
