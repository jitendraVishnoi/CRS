package crs.paypal;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import atg.commerce.order.HardgoodShippingGroup;
import atg.commerce.order.Order;
import atg.commerce.order.ShippingGroup;

public class PaypalManager {
	

	private PaypalTools tools;
	
	public HashMap callExpressCheckout(Order order, HardgoodShippingGroup defaultAddress, Map<String, String> details) {
		
		return getTools().expressCheckout(order, defaultAddress, details);
	}

	public PaypalTools getTools() {
		return tools;
	}

	public void setTools(PaypalTools tools) {
		this.tools = tools;
	}
	
}
