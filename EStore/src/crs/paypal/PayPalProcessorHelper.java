package crs.paypal;

import java.util.HashMap;
import java.util.List;

import atg.commerce.order.Order;
import atg.commerce.order.ShippingGroup;
import atg.nucleus.GenericService;
import atg.repository.RepositoryItem;
import atg.userprofiling.Profile;

public class PayPalProcessorHelper extends GenericService {

	public List<String> filterShippingMethods(
			List<String> pAvailableShippingMethods,
			ShippingGroup pShippingGroup, Order pOrder, RepositoryItem profile) {
		return pAvailableShippingMethods;
	}

	public HashMap<String, String> filterNVPForSetExpressCheckout(
			HashMap<String, String> pNameValuePairs) {
		return pNameValuePairs;
	}
}
