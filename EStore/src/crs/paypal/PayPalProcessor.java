package crs.paypal;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import org.apache.http.HttpException;

import crs.commerce.order.PayPalPaymentGroup;
import crs.paypal.beans.ErrorBean;
import crs.paypal.beans.ResponseBean;
import crs.paypal.beans.SetExpressCheckoutRequest;
import crs.paypal.beans.ShippingInfoBean;
import atg.commerce.CommerceException;
import atg.commerce.multisite.CommerceSitePropertiesManager;
import atg.commerce.order.CommerceItem;
import atg.commerce.order.HardgoodShippingGroup;
import atg.commerce.order.Order;
import atg.commerce.order.OrderManager;
import atg.commerce.order.PaymentGroup;
import atg.commerce.order.PaymentGroupManager;
import atg.commerce.order.ShippingGroup;
import atg.commerce.pricing.DetailedItemPriceInfo;
import atg.commerce.pricing.OrderPriceInfo;
import atg.commerce.pricing.PricingAdjustment;
import atg.commerce.pricing.PricingException;
import atg.commerce.pricing.PricingModelHolder;
import atg.commerce.pricing.PricingTools;
import atg.commerce.pricing.ShippingPriceInfo;
import atg.commerce.pricing.ShippingPricingEngine;
import atg.core.util.Address;
import atg.core.util.ContactInfo;
import atg.core.util.StringUtils;
import atg.droplet.GenericFormHandler;
import atg.multisite.SiteContextManager;
import atg.repository.MutableRepositoryItem;
import atg.repository.RepositoryItem;

public class PayPalProcessor extends GenericFormHandler {

	private String ecSuccessUrl, ecCancelUrl, defaultCurrencyCode,
			successDestURL, cancelDestURL, handoffURL, merchantDescriptor;
	private PaymentGroupManager paymentGroupManager;
	private PricingTools pricingTools;
	private OrderManager orderManager;
	private boolean submitOrderOnPayPalSite, addNote, doInstantUpdateTaxCalc,
			captureBillingAddress, sendLineItems, useReferenceTransactions;
	private ShippingPricingEngine shippingPricingEngine;
	private PayPalProcessorHelper payPalProcessorHelper;
	private CommerceSitePropertiesManager commerceSitePropertiesManager;
	private PayPalConnection payPalConnection;

	/**
	 * @param expressCheckoutRequest
	 * @return
	 */
	public String callSetExpressCheckout(
			SetExpressCheckoutRequest expressCheckoutRequest) {
		vlogDebug("Inside callSetExpressCheckout");
		ResponseBean response = callSetExpressCheckoutDetailedResponse(expressCheckoutRequest);
		vlogDebug("Exting from callSetExpressCheckout token : {0}",
				response.getToken());
		return response.getToken();
	}

	public ResponseBean callSetExpressCheckoutDetailedResponse(
			SetExpressCheckoutRequest expressCheckoutRequest) {
		vlogDebug("Inside callSetExpressCheckoutDetailedResponse");
		String token = null;
		try {
			setupPayPalPG(expressCheckoutRequest.getOrder());
		} catch (CommerceException e) {
			vlogError(e,
					"Exception in callSetExpressCheckoutDetailedResponse while setting PG :: {0}");
		}

		NumberFormat DEFAULT_DECIMAL_FORMAT = new DecimalFormat("#0.00");

		HashMap<String, String> nameValuePairs = new HashMap<String, String>();
		HashMap<String, String> responseMap = new HashMap<>();
		HashMap<String, String> customFieldMap = new HashMap<>();

		nameValuePairs.put("METHOD", "SetExpressCheckout");
		nameValuePairs.put("RETURNURL", expressCheckoutRequest.getReturnURL());
		nameValuePairs.put("CANCELURL", expressCheckoutRequest.getCancelURL());
		if (isSubmitOrderOnPayPalSite()) {
			nameValuePairs.put("useraction", "commit");
		} else {
			nameValuePairs.put("useraction", "continue");
		}
		nameValuePairs.put("PAYMENTREQUEST_0_PAYMENTACTION", "Order");
		nameValuePairs.put("PAYMENTREQUEST_0_CURRENCYCODE",
				expressCheckoutRequest.getCurrencyCode());
		nameValuePairs.put("PAYMENTREQUEST_0_INVNUM", expressCheckoutRequest
				.getOrder().getId());
		nameValuePairs.put("PAYMENTREQUEST_0_PAYMENTREQUESTID",
				expressCheckoutRequest.getOrder().getId());

		if (isAddNote())
			nameValuePairs.put("ALLOWNOTE", "1");
		else {
			nameValuePairs.put("ALLOWNOTE", "0");
		}
		double maxShippingAmount = 0.0D;

		if (expressCheckoutRequest.getShippingAddress() != null) {
			nameValuePairs.put("ADDROVERRIDE", "1");
			nameValuePairs.put("PAYMENTREQUEST_0_SHIPTONAME",
					expressCheckoutRequest.getShippingAddress().getFirstName()
							+ " "
							+ expressCheckoutRequest.getShippingAddress()
									.getLastName());
			nameValuePairs.put("PAYMENTREQUEST_0_SHIPTOSTREET",
					expressCheckoutRequest.getShippingAddress().getAddress1());

			StringBuffer street2 = new StringBuffer();
			if ((StringUtils.isNotBlank(expressCheckoutRequest
					.getShippingAddress().getAddress2()))) {
				street2.append(expressCheckoutRequest.getShippingAddress()
						.getAddress2());
			}
			if ((StringUtils.isNotBlank(expressCheckoutRequest
					.getShippingAddress().getAddress3()))) {
				if (street2.length() > 0) {
					street2.append(' ');
				}
				street2.append(expressCheckoutRequest.getShippingAddress()
						.getAddress3());
			}
			if (street2.length() > 0) {
				nameValuePairs.put("PAYMENTREQUEST_0_SHIPTOSTREET2",
						street2.toString());
			}
			nameValuePairs.put("PAYMENTREQUEST_0_SHIPTOCITY",
					expressCheckoutRequest.getShippingAddress().getCity());
			nameValuePairs.put("PAYMENTREQUEST_0_SHIPTOSTATE",
					expressCheckoutRequest.getShippingAddress().getState());
			nameValuePairs.put("PAYMENTREQUEST_0_SHIPTOCOUNTRYCODE",
					expressCheckoutRequest.getShippingAddress().getCountry());
			nameValuePairs
					.put("PAYMENTREQUEST_0_SHIPTOZIP", expressCheckoutRequest
							.getShippingAddress().getPostalCode());
			nameValuePairs.put("EMAIL", expressCheckoutRequest.getEmail());

			int index;
			if ((expressCheckoutRequest.getOrder().getShippingGroupCount() > 0)
					&& (StringUtils
							.isNotBlank(((ShippingGroup) expressCheckoutRequest
									.getOrder().getShippingGroups().get(0))
									.getShippingMethod()))) {
				nameValuePairs.put(
						"PAYMENTREQUEST_0_SHIPPINGAMT",
						DEFAULT_DECIMAL_FORMAT.format(expressCheckoutRequest
								.getOrder().getPriceInfo().getShipping()));

				maxShippingAmount = expressCheckoutRequest.getOrder()
						.getPriceInfo().getShipping();
			} else {
				ArrayList<ShippingInfoBean> shippingPrices = getShippingPrices(
						expressCheckoutRequest.getOrder(),
						expressCheckoutRequest.getProfile(),
						expressCheckoutRequest.getShippingAddress());

				if (shippingPrices != null) {
					index = 0;
					for (ShippingInfoBean shipOption : shippingPrices) {
						String lShippingOptionLabel = "L_SHIPPINGOPTIONLABEL"
								+ index;
						String lShippingOptionAmount = "L_SHIPPINGOPTIONAMOUNT"
								+ index;
						String lShippingOptionTax = "L_TAXAMT" + index;
						String lShippingOptionIsDefault = "L_SHIPPINGOPTIONISDEFAULT"
								+ index;

						nameValuePairs.put(lShippingOptionLabel,
								shipOption.getMethod());
						nameValuePairs.put(lShippingOptionAmount, new Double(
								shipOption.getPrice()).toString());
						nameValuePairs.put(lShippingOptionTax, new Double(
								shipOption.getTax()).toString());
						if (index == 0)
							nameValuePairs
									.put(lShippingOptionIsDefault, "true");
						else {
							nameValuePairs.put(lShippingOptionIsDefault,
									"false");
						}

						if (shipOption.getPrice() > maxShippingAmount) {
							maxShippingAmount = shipOption.getPrice();
						}
						index++;
					}
				}
			}

			PaymentGroup paypalPG = findPayPalPG(expressCheckoutRequest
					.getOrder());
			nameValuePairs.put("PAYMENTREQUEST_0_AMT",
					DEFAULT_DECIMAL_FORMAT.format(paypalPG.getAmount()));
			nameValuePairs.put(
					"PAYMENTREQUEST_0_TAXAMT",
					DEFAULT_DECIMAL_FORMAT.format(expressCheckoutRequest
							.getOrder().getPriceInfo().getTax()));
		} else {
			// Shipping address not available
		}

		if (isCaptureBillingAddress()) {
			nameValuePairs.put("REQBILLINGADDRESS", "1");
		}
		processCartItems(nameValuePairs, expressCheckoutRequest.getOrder());

		String orderTotalString = DEFAULT_DECIMAL_FORMAT
				.format(expressCheckoutRequest.getOrder().getPriceInfo()
						.getAmount());

		BigDecimal orderTotal = new BigDecimal(orderTotalString);

		String maxShippingString = DEFAULT_DECIMAL_FORMAT
				.format(maxShippingAmount);
		BigDecimal maxShipping = new BigDecimal(maxShippingString);

		String taxString = DEFAULT_DECIMAL_FORMAT.format(expressCheckoutRequest
				.getOrder().getPriceInfo().getTax());

		BigDecimal tax = new BigDecimal(taxString);

		BigDecimal total = orderTotal.add(maxShipping).add(tax)
				.multiply(BigDecimal.valueOf(1.2D));
		nameValuePairs.put("MAXAMT",
				DEFAULT_DECIMAL_FORMAT.format(total.doubleValue()));

		customFieldMap
				.put("sid", expressCheckoutRequest.getOrder().getSiteId());
		vlogDebug(
				"PayPalProcessor.callSetExpressCheckoutDetailedResponse:about to create custom "
						+ "field based on the following map: {0}",
				customFieldMap.toString());

		String customField = PayPalUtils.mapToString(customFieldMap);
		vlogDebug(
				"PayPalProcessor.callSetExpressCheckoutDetailedResponse:customField String: {0}",
				customField);

		if (customField.length() > 250) {
			vlogError(
					"PayPalProcessor.callSetExpressCheckoutDetailedResponse:The custom field String length is greater "
							+ "than the 250 allowed characters! Not setting the param. customField:{0}",
					customField);
		} else {
			nameValuePairs.put("PAYMENTREQUEST_0_CUSTOM",
					PayPalUtils.mapToString(customFieldMap));
		}

		if (isUseReferenceTransactions()) {
			nameValuePairs.put("L_BILLINGTYPE0",
					"MerchantInitiatedBillingSingleAgreement");
			nameValuePairs.put("L_BILLINGAGREEMENTDESCRIPTION0",
					"Billing Agreement for future purchases with "
							+ getMerchantDescriptor());

			nameValuePairs.put("L_PAYMENTTYPE0", "InstantOnly");
		}

		HashMap<String, String> filteredNVPs = getPayPalProcessorHelper()
				.filterNVPForSetExpressCheckout(nameValuePairs);

		try {
			vlogDebug(
					"PayPalProcessor.callSetExpressCheckoutDetailedResponse request map filteredNVPs :{0}",
					filteredNVPs);
			responseMap = getPayPalConnection().call(filteredNVPs);
			vlogDebug(
					"PayPalProcessor.callSetExpressCheckoutDetailedResponse response map:{0}",
					responseMap);
			token = (String) responseMap.get("TOKEN");
		} catch (HttpException he) {
			vlogError(
					he,
					"PayPalProcessor.callSetExpressCheckoutDetailedResponse: caught HttpException during call {0}",
					he.getMessage());
		} catch (IOException ioe) {
			vlogError(
					ioe,
					"PayPalProcessor.callSetExpressCheckoutDetailedResponse: caught IOException during call {0}",
					ioe);
		}

		ResponseBean response = new ResponseBean();
		String ack = (String) responseMap.get("ACK");
		if ((!StringUtils.isBlank(ack)) && (ack.equals("Success"))) {
			response.setSuccess(true);
			response.setToken(token);
		} else {
			response.setSuccess(false);
			int index = 0;
			while (!StringUtils.isBlank((String) responseMap.get("L_ERRORCODE"
					+ index))) {
				ErrorBean currentError = new ErrorBean();
				currentError.setErrorCode((String) responseMap
						.get("L_ERRORCODE" + index));
				currentError.setSeveritycode((String) responseMap
						.get("L_SEVERITYCODE" + index));
				currentError.setShortMsg((String) responseMap
						.get("L_SHORTMESSAGE" + index));
				currentError.setLongMsg((String) responseMap
						.get("L_LONGMESSAGE" + index));
				response.getErrorBeans().add(currentError);
				index++;
			}
		}
		return response;
	}

	@SuppressWarnings("unchecked")
	private void processCartItems(HashMap<String, String> pNameValuePairs,
			Order pOrder) {
		vlogDebug(
				"Inside PayPalProcessor.processCartItems pNameValuePairs {0}",
				pNameValuePairs);

		NumberFormat DEFAULT_DECIMAL_FORMAT = new DecimalFormat("#0.00");
		List<CommerceItem> items = pOrder.getCommerceItems();

		int index = 0;
		BigDecimal itemTotal = new BigDecimal(0.0D);
		for (CommerceItem item : items) {
			List<DetailedItemPriceInfo> priceInfoList = item.getPriceInfo()
					.getCurrentPriceDetailsSorted();
			if ((priceInfoList != null) && (priceInfoList.size() > 0))
				for (DetailedItemPriceInfo priceInfo : priceInfoList) {
					if (isSendLineItems()) {
						String lName = "L_PAYMENTREQUEST_0_NAME" + index;
						String lNumber = "L_PAYMENTREQUEST_0_NUMBER" + index;
						String lDesc = "L_PAYMENTREQUEST_0_DESC" + index;
						String lAmt = "L_PAYMENTREQUEST_0_AMT" + index;
						String lQty = "L_PAYMENTREQUEST_0_QTY" + index;

						RepositoryItem catalogRef = (RepositoryItem) item
								.getAuxiliaryData().getCatalogRef();
						pNameValuePairs.put(lName,
								catalogRef.getItemDisplayName());
						pNameValuePairs.put(lNumber,
								catalogRef.getRepositoryId());
						pNameValuePairs.put(lDesc, (String) catalogRef
								.getPropertyValue("description"));
						String itemPriceString = DEFAULT_DECIMAL_FORMAT
								.format(priceInfo.getDetailedUnitPrice());
						pNameValuePairs.put(lAmt, itemPriceString);
						pNameValuePairs.put(lQty,
								Long.toString(priceInfo.getQuantity()));
					}
					String itemAmountString = DEFAULT_DECIMAL_FORMAT
							.format(priceInfo.getAmount());
					BigDecimal itemAmount = new BigDecimal(itemAmountString);
					itemTotal = itemTotal.add(itemAmount);
					index++;
				}
		}
		List<PaymentGroup> paymentGroups = pOrder.getPaymentGroups();
		index = 0;
		for (PaymentGroup paymentGroup : paymentGroups) {
			if ((!(paymentGroup instanceof PayPalPaymentGroup))
					&& (paymentGroup.getAmount() > 0.0D)) {
				if (isSendLineItems()) {
					String lName = "L_PAYMENTREQUEST_0_NAME" + index;
					String lNumber = "L_PAYMENTREQUEST_0_NUMBER" + index;
					String lDesc = "L_PAYMENTREQUEST_0_DESC" + index;
					String lAmt = "L_PAYMENTREQUEST_0_AMT" + index;
					String lQty = "L_PAYMENTREQUEST_0_QTY" + index;
					pNameValuePairs.put(lName, paymentGroup.getPaymentMethod()
							+ " Payment");
					pNameValuePairs.put(lNumber, paymentGroup.getId());
					pNameValuePairs.put(lDesc, paymentGroup.getPaymentMethod()
							+ " Payment");
					pNameValuePairs.put(lQty, "1");
					String itemPriceString = DEFAULT_DECIMAL_FORMAT
							.format(paymentGroup.getAmount() * -1.0D);
					pNameValuePairs.put(lAmt, itemPriceString);
				}
				BigDecimal itemAmount = new BigDecimal(
						DEFAULT_DECIMAL_FORMAT.format(paymentGroup.getAmount()
								* -1.0D));

				itemTotal = itemTotal.add(itemAmount);
				index++;
			}

		}

		List<PricingAdjustment> pricingAdjustments = pOrder.getPriceInfo()
				.getAdjustments();
		index = 0;
		if ((pricingAdjustments != null) && (pricingAdjustments.size() > 0)) {
			for (PricingAdjustment pricingAdjustment : pricingAdjustments) {
				if ((pricingAdjustment.getPricingModel() != null)
						&& (pricingAdjustment.getTotalAdjustment() != 0.0D)) {
					if (isSendLineItems()) {
						String lName = "L_PAYMENTREQUEST_0_NAME" + index;
						String lNumber = "L_PAYMENTREQUEST_0_NUMBER" + index;
						String lDesc = "L_PAYMENTREQUEST_0_DESC" + index;
						String lAmt = "L_PAYMENTREQUEST_0_AMT" + index;
						String lQty = "L_PAYMENTREQUEST_0_QTY" + index;
						pNameValuePairs.put(lName,
								pricingAdjustment.getAdjustmentDescription());
						pNameValuePairs.put(lNumber,
								pricingAdjustment.getAdjustmentDescription());
						pNameValuePairs.put(lDesc, "Order Pricing Adjustment");
						String itemPriceString = DEFAULT_DECIMAL_FORMAT
								.format(pricingAdjustment.getTotalAdjustment());
						pNameValuePairs.put(lAmt, itemPriceString);
						pNameValuePairs.put(lQty, "1");
					}
					BigDecimal itemAmount = new BigDecimal(
							DEFAULT_DECIMAL_FORMAT.format(pricingAdjustment
									.getTotalAdjustment()));

					itemTotal = itemTotal.add(itemAmount);
					index++;
				}

			}

		}

		pNameValuePairs.put("PAYMENTREQUEST_0_ITEMAMT",
				Double.toString(itemTotal.doubleValue()));

		if (!isSendLineItems()) {
			pNameValuePairs.put(
					"L_PAYMENTREQUEST_0_DESC0",
					"Ulta.com Order #" + pOrder.getId() + " ("
							+ pOrder.getTotalCommerceItemCount() + " items)");

			pNameValuePairs.put("L_PAYMENTREQUEST_0_NUMBER0", pOrder.getId());
			pNameValuePairs.put("L_PAYMENTREQUEST_0_QTY0", "1");
			pNameValuePairs.put("L_PAYMENTREQUEST_0_AMT0",
					Double.toString(itemTotal.doubleValue()));
		}
		vlogDebug(
				"Exting from PayPalProcessor.processCartItems pNameValuePairs {0}",
				pNameValuePairs);
	}

	@SuppressWarnings({ "unchecked" })
	private ArrayList<ShippingInfoBean> getShippingPrices(Order order,
			RepositoryItem profile, Address shippingAddress) {
		vlogDebug(
				"Inside PayPalProcessor.getShippingPrices profile::{0}, shippingAddress::{1}",
				profile, shippingAddress);

		ArrayList<ShippingInfoBean> shippingPrices = new ArrayList<>();
		List<String> availableShippingMethods = null;
		Collection<PricingModelHolder> shippingPricingModels = null;

		List<ShippingGroup> shippingGroups = order.getShippingGroups();
		for (ShippingGroup shippingGroup : shippingGroups) {
			if (shippingGroup instanceof HardgoodShippingGroup) {
				vlogDebug(
						"PayPalProcessor.getShippingPrices:shippingGroup has {0} CI relationships.",
						shippingGroup.getCommerceItemRelationshipCount());
				if (shippingAddress != null) {
					((HardgoodShippingGroup) shippingGroup)
							.setShippingAddress(shippingAddress);
				}
				if (profile != null) {
					shippingPricingModels = getShippingPricingEngine()
							.getPricingModels(profile);
					try {
						availableShippingMethods = getShippingPricingEngine()
								.getAvailableMethods(shippingGroup,
										availableShippingMethods, null,
										profile, null);
					} catch (PricingException e) {
						vlogError(
								e,
								"PayPalProcessor.getShippingPrices exception while retrieving shipping methods {0}");
					}
				}
				// filter shipping methods
				availableShippingMethods = getPayPalProcessorHelper()
						.filterShippingMethods(availableShippingMethods,
								shippingGroup, order, profile);

				if (availableShippingMethods != null) {
					for (String shippingMethod : availableShippingMethods) {
						ShippingInfoBean shippingInfoBean = new ShippingInfoBean();
						shippingInfoBean.setMethod(shippingMethod);
						shippingGroup.setShippingMethod(shippingMethod);

						try {
							ShippingPriceInfo shippingPriceInfo = getShippingPricingEngine()
									.priceShippingGroup(order, shippingGroup,
											shippingPricingModels, null,
											profile, null);
							vlogDebug(
									"PayPalProcessor.getShippingPrices shippingPriceInfo.getAmount() {0}",
									shippingPriceInfo.getAmount());
							shippingInfoBean.setPrice(shippingPriceInfo
									.getAmount());
							shippingGroup.setPriceInfo(shippingPriceInfo);
						} catch (PricingException e) {
							vlogError(
									e,
									"PayPalProcessor.getShippingPrices exception while getting shippingPriceInfo {0}");
						}
						if (isDoInstantUpdateTaxCalc()) {
							Collection<PricingModelHolder> itemPricingModels = getPricingTools()
									.getItemPricingEngine().getPricingModels(
											profile);
							Collection<PricingModelHolder> orderPricingModels = getPricingTools()
									.getOrderPricingEngine().getPricingModels(
											profile);
							Collection<PricingModelHolder> taxPricingModels = getPricingTools()
									.getTaxPricingEngine().getPricingModels(
											profile);

							RepositoryItem priceList = (RepositoryItem) SiteContextManager
									.getCurrentSite()
									.getPropertyValue(
											getCommerceSitePropertiesManager()
													.getDefaultListPriceListPropertyName());
							((MutableRepositoryItem) profile).setPropertyValue(
									"priceList", priceList);
							((MutableRepositoryItem) profile).setPropertyValue(
									"myPriceList", priceList);

							if (StringUtils.isEmpty((String) profile
									.getPropertyValue("locale"))) {
								((MutableRepositoryItem) profile)
										.setPropertyValue("locale", priceList
												.getPropertyValue("locale"));
							}

							try {
								getPricingTools().priceOrderTotal(order,
										itemPricingModels, orderPricingModels,
										shippingPricingModels,
										taxPricingModels, null, profile, null);
							} catch (PricingException e) {
								vlogError(e,
										"PayPalProcessor.getShippingPrices exception while priceOrderTotal {0}");
							}

							OrderPriceInfo orderPriceInfo = order
									.getPriceInfo();
							vlogDebug("ConfirmPayPalPaymentServlet.service:order total:"
									+ orderPriceInfo.getTotal()
									+ " order tax: " + orderPriceInfo.getTax());
							shippingInfoBean.setTax(orderPriceInfo.getTax());
						}
						shippingPrices.add(shippingInfoBean);
						vlogDebug(
								"PayPalProcessor.getShippingPrices shippingPrices::{0}",
								shippingPrices);
					}
				}
			}
		}

		return shippingPrices;
	}

	/**
	 * @param order
	 * @return
	 * @throws CommerceException
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public PayPalPaymentGroup setupPayPalPG(Order order)
			throws CommerceException {
		vlogDebug("Inside PayPalProcessor.setupPayPalPG order::{0}", order);

		if (findPayPalPG(order) == null) {
			vlogDebug("creating paypal payment group");
			PayPalPaymentGroup newPayPalPaymentGroup = (PayPalPaymentGroup) getPaymentGroupManager()
					.createPaymentGroup("paypal");

			List<PaymentGroup> nonModifiablePaymentGroup = getPaymentGroupManager()
					.getNonModifiablePaymentGroups(order);
			List<PaymentGroup> paymentGroups = order.getPaymentGroups();

			if (!paymentGroups.isEmpty()) {
				List<String> removeIds = new ArrayList();
				for (PaymentGroup group : paymentGroups) {
					if (group.getAmount() == 0d
							&& !nonModifiablePaymentGroup.contains(group)) {
						removeIds.add(group.getId());
					}
				}
				for (String removeId : removeIds) {
					vlogDebug("removeId ::{0}", removeId);
					getPaymentGroupManager().removePaymentGroupFromOrder(order,
							removeId);
				}
			}
			vlogDebug("Adding new PayPalPaymentGroup to order");
			getPaymentGroupManager().addPaymentGroupToOrder(order,
					newPayPalPaymentGroup);
		}

		// Calculate total price for order(item,order,shipping,tax)
		getPricingTools().priceOrderTotal(order);
		getOrderManager().addRemainingOrderAmountToPaymentGroup(order,
				findPayPalPG(order).getId());
		vlogDebug("PG is added to order {0} with remaining amount ::{1}",
				order.getId(),findPayPalPG(order).getAmount());
		getPaymentGroupManager().recalculatePaymentGroupAmounts(order);
		vlogDebug("PG recalculated amount ::{0} for order ::{1}", findPayPalPG(order)
				.getAmount(),order.getId());

		if (order.isTransient()) {
			getOrderManager().addOrder(order);
		}
		getOrderManager().updateOrder(order);
		vlogDebug("Exting from PayPalProcessor.setupPayPalPG PG::{0}",
				findPayPalPG(order));
		return findPayPalPG(order);
	}

	/**
	 * @param order
	 * @return
	 */
	private PayPalPaymentGroup findPayPalPG(Order order) {
		vlogDebug("Inside findPayPalPG order :: {0}", order);

		@SuppressWarnings("unchecked")
		List<PaymentGroup> paymentGroups = order.getPaymentGroups();
		PayPalPaymentGroup payPalPaymentGroup = null;
		for (PaymentGroup paymentGroup : paymentGroups) {
			if (paymentGroup instanceof PayPalPaymentGroup) {
				payPalPaymentGroup = (PayPalPaymentGroup) paymentGroup;
				break;
			}
		}

		vlogDebug("Exting from findPayPalPG payPalPaymentGroup::{0}",
				payPalPaymentGroup);
		return payPalPaymentGroup;
	}

	public void addTokenToPayPalPG(Order pOrder, String pToken)
			throws CommerceException {
		vlogDebug("Inside addTokenToPayPalPG pOrder:{0} pToken:{1}", pOrder,
				pToken);

		PayPalPaymentGroup payPalPaymentGroup = findPayPalPG(pOrder);

		vlogDebug("payPalPaymentGroup:{0}", payPalPaymentGroup);
		if (payPalPaymentGroup == null) {
			setupPayPalPG(pOrder);
		}
		payPalPaymentGroup.setToken(pToken);
		getOrderManager().updateOrder(pOrder);

		vlogDebug("Exiting from addTokenToPayPalPG");
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public PayPalDetails callGetExpressCheckoutDetails(String pToken) {

		vlogDebug("Inside callGetExpressCheckoutDetails token : {0}", pToken);

		HashMap nameValuePairs = new HashMap();
		HashMap responseMap = new HashMap();
		PayPalDetails response = new PayPalDetails();
		nameValuePairs.put("METHOD", "GetExpressCheckoutDetails");
		nameValuePairs.put("Token", pToken);

		try {
			responseMap = getPayPalConnection().call(nameValuePairs);
			vlogDebug("callGetExpressCheckoutDetails responseMap :: {0}",
					responseMap);

			response.setOrderid((String) responseMap
					.get("PAYMENTREQUEST_0_INVNUM"));
			response.setTransactionId((String) responseMap
					.get("PAYMENTREQUEST_0_TRANSACTIONID"));
			response.setCheckoutStatus((String) responseMap
					.get("CHECKOUTSTATUS"));
			response.setPayerid((String) responseMap.get("PAYERID"));
			response.setPayerStatus((String) responseMap.get("PAYERSTATUS"));
			if (((String) responseMap.get("PAYERSTATUS"))
					.equalsIgnoreCase("verified")) {
				response.setPayerStatusCertified(true);
			}

			if (!StringUtils.isBlank((String) responseMap
					.get("SHIPPINGOPTIONAMOUNT"))) {
				response.setShippingAmount(new Double((String) responseMap
						.get("SHIPPINGOPTIONAMOUNT")).doubleValue());
				response.setShippingOption((String) responseMap
						.get("SHIPPINGOPTIONNAME"));
			}

			ContactInfo shipAddress = new ContactInfo();
			shipAddress.setFirstName((String) responseMap.get("FIRSTNAME"));
			shipAddress.setLastName((String) responseMap.get("LASTNAME"));
			shipAddress.setPhoneNumber((String) responseMap
					.get("PAYMENTREQUEST_0_SHIPTOPHONENUM"));
			shipAddress.setAddress1((String) responseMap
					.get("PAYMENTREQUEST_0_SHIPTOSTREET"));
			shipAddress.setAddress2((String) responseMap
					.get("PAYMENTREQUEST_0_SHIPTOSTREET2"));
			shipAddress.setCity((String) responseMap
					.get("PAYMENTREQUEST_0_SHIPTOCITY"));
			shipAddress.setState((String) responseMap
					.get("PAYMENTREQUEST_0_SHIPTOSTATE"));
			shipAddress.setPostalCode((String) responseMap
					.get("PAYMENTREQUEST_0_SHIPTOZIP"));
			shipAddress.setCountry((String) responseMap
					.get("PAYMENTREQUEST_0_SHIPTOCOUNTRYCODE"));
			shipAddress.setEmail((String) responseMap.get("EMAIL"));
			response.setShippingAddress(shipAddress);
			if ((responseMap.get("ADDRESSSTATUS") != null)
					&& (((String) responseMap.get("ADDRESSSTATUS"))
							.equalsIgnoreCase("Confirmed"))) {
				response.setAddressConfirmed(true);
			}
			if (isCaptureBillingAddress()) {
				ContactInfo billAddress = new ContactInfo();
				billAddress.setFirstName((String) responseMap.get("FIRSTNAME"));
				billAddress.setLastName((String) responseMap.get("LASTNAME"));
				billAddress.setPhoneNumber((String) responseMap
						.get("PAYMENTREQUEST_0_SHIPTOPHONENUM"));
				billAddress.setAddress1((String) responseMap
						.get("PAYMENTREQUEST_0_SHIPTOSTREET"));
				billAddress.setAddress2((String) responseMap
						.get("PAYMENTREQUEST_0_SHIPTOSTREET2"));
				billAddress.setCity((String) responseMap
						.get("PAYMENTREQUEST_0_SHIPTOCITY"));
				billAddress.setState((String) responseMap
						.get("PAYMENTREQUEST_0_SHIPTOSTATE"));
				billAddress.setPostalCode((String) responseMap
						.get("PAYMENTREQUEST_0_SHIPTOZIP"));
				billAddress.setCountry((String) responseMap
						.get("PAYMENTREQUEST_0_SHIPTOCOUNTRYCODE"));
				billAddress.setEmail((String) responseMap.get("EMAIL"));
				response.setBillingAddress(billAddress);
			}
		} catch (HttpException he) {
			if (isLoggingError())
				logError("PayPalProcessor.getDetails: HttpException caught - "
						+ he.getMessage(), he);
		} catch (IOException ioe) {
			if (isLoggingError()) {
				logError("PayPalProcessor.getDetails: IOException caught - "
						+ ioe.getMessage(), ioe);
			}
		}
		return response;

	}

	// Getter Setter starts
	public String getEcSuccessUrl() {
		return ecSuccessUrl;
	}

	public void setEcSuccessUrl(String ecSuccessUrl) {
		this.ecSuccessUrl = ecSuccessUrl;
	}

	public String getEcCancelUrl() {
		return ecCancelUrl;
	}

	public void setEcCancelUrl(String ecCancelUrl) {
		this.ecCancelUrl = ecCancelUrl;
	}

	public String getDefaultCurrencyCode() {
		return defaultCurrencyCode;
	}

	public void setDefaultCurrencyCode(String defaultCurrencyCode) {
		this.defaultCurrencyCode = defaultCurrencyCode;
	}

	public PaymentGroupManager getPaymentGroupManager() {
		return paymentGroupManager;
	}

	public void setPaymentGroupManager(PaymentGroupManager paymentGroupManager) {
		this.paymentGroupManager = paymentGroupManager;
	}

	public PricingTools getPricingTools() {
		return pricingTools;
	}

	public void setPricingTools(PricingTools pricingTools) {
		this.pricingTools = pricingTools;
	}

	public OrderManager getOrderManager() {
		return orderManager;
	}

	public void setOrderManager(OrderManager orderManager) {
		this.orderManager = orderManager;
	}

	public boolean isSubmitOrderOnPayPalSite() {
		return submitOrderOnPayPalSite;
	}

	public void setSubmitOrderOnPayPalSite(boolean submitOrderOnPayPalSite) {
		this.submitOrderOnPayPalSite = submitOrderOnPayPalSite;
	}

	public boolean isAddNote() {
		return addNote;
	}

	public void setAddNote(boolean addNote) {
		this.addNote = addNote;
	}

	public ShippingPricingEngine getShippingPricingEngine() {
		return shippingPricingEngine;
	}

	public void setShippingPricingEngine(
			ShippingPricingEngine shippingPricingEngine) {
		this.shippingPricingEngine = shippingPricingEngine;
	}

	public PayPalProcessorHelper getPayPalProcessorHelper() {
		return payPalProcessorHelper;
	}

	public void setPayPalProcessorHelper(
			PayPalProcessorHelper payPalProcessorHelper) {
		this.payPalProcessorHelper = payPalProcessorHelper;
	}

	public boolean isDoInstantUpdateTaxCalc() {
		return doInstantUpdateTaxCalc;
	}

	public void setDoInstantUpdateTaxCalc(boolean doInstantUpdateTaxCalc) {
		this.doInstantUpdateTaxCalc = doInstantUpdateTaxCalc;
	}

	public CommerceSitePropertiesManager getCommerceSitePropertiesManager() {
		return commerceSitePropertiesManager;
	}

	public void setCommerceSitePropertiesManager(
			CommerceSitePropertiesManager commerceSitePropertiesManager) {
		this.commerceSitePropertiesManager = commerceSitePropertiesManager;
	}

	public boolean isCaptureBillingAddress() {
		return captureBillingAddress;
	}

	public void setCaptureBillingAddress(boolean captureBillingAddress) {
		this.captureBillingAddress = captureBillingAddress;
	}

	public PayPalConnection getPayPalConnection() {
		return payPalConnection;
	}

	public void setPayPalConnection(PayPalConnection payPalConnection) {
		this.payPalConnection = payPalConnection;
	}

	public boolean isSendLineItems() {
		return sendLineItems;
	}

	public void setSendLineItems(boolean sendLineItems) {
		this.sendLineItems = sendLineItems;
	}

	public String getSuccessDestURL() {
		return successDestURL;
	}

	public void setSuccessDestURL(String successDestURL) {
		this.successDestURL = successDestURL;
	}

	public String getCancelDestURL() {
		return cancelDestURL;
	}

	public void setCancelDestURL(String cancelDestURL) {
		this.cancelDestURL = cancelDestURL;
	}

	public String getHandoffURL() {
		return handoffURL;
	}

	public void setHandoffURL(String handoffURL) {
		this.handoffURL = handoffURL;
	}

	public boolean isUseReferenceTransactions() {
		return useReferenceTransactions;
	}

	public void setUseReferenceTransactions(boolean useReferenceTransactions) {
		this.useReferenceTransactions = useReferenceTransactions;
	}

	public String getMerchantDescriptor() {
		return merchantDescriptor;
	}

	public void setMerchantDescriptor(String merchantDescriptor) {
		this.merchantDescriptor = merchantDescriptor;
	}

}
