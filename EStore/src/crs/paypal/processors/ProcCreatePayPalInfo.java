package crs.paypal.processors;

import java.util.List;

import crs.commerce.order.PayPalPaymentGroup;
import crs.paypal.PayPalInfo;
import atg.commerce.order.HardgoodShippingGroup;
import atg.commerce.order.Order;
import atg.commerce.order.ShippingGroup;
import atg.commerce.payment.PaymentManagerPipelineArgs;
import atg.core.util.StringUtils;
import atg.nucleus.GenericService;
import atg.service.pipeline.PipelineProcessor;
import atg.service.pipeline.PipelineResult;

public class ProcCreatePayPalInfo extends GenericService implements PipelineProcessor{

	public static final int SUCCESS = 1;
	public static final int FAILURE = 0;

	@Override
	public int[] getRetCodes() {
		int[] retCodes = { 1, 0 };
		return retCodes;
	}

	@SuppressWarnings("unchecked")
	@Override
	public int runProcess(Object pParam, PipelineResult pResult) throws Exception {
		vlogDebug("ProcCreatePayPalInfo.runProcess:called");

		PaymentManagerPipelineArgs params = (PaymentManagerPipelineArgs)pParam;
		PayPalPaymentGroup payPalPaymentGroup = (PayPalPaymentGroup)params.getPaymentGroup();
		Order order = params.getOrder();
		if (payPalPaymentGroup != null) {
			PayPalInfo payPalInfo = new PayPalInfo();
			payPalInfo.setOrder(order);
			payPalInfo.setAmount(params.getAmount());
			payPalInfo.setBillingAddress(payPalPaymentGroup.getBillingAddress());

			List<ShippingGroup> hsg = order.getShippingGroups();
			for (ShippingGroup shippingGroup : hsg) {
				if ((shippingGroup instanceof HardgoodShippingGroup)) {
					payPalInfo.setShippingAddress(((HardgoodShippingGroup)shippingGroup).getShippingAddress());
					break;
				}
			}
			payPalInfo.setPaymentId(payPalPaymentGroup.getId());
			payPalInfo.setToken(payPalPaymentGroup.getToken());
			payPalInfo.setTransactionId(payPalPaymentGroup.getTransactionId());
			payPalInfo.setPayerId(payPalPaymentGroup.getPayerId());
			if (StringUtils.isNotEmpty(payPalPaymentGroup.getBillingId())) {
				payPalInfo.setBillingAgreementId(payPalPaymentGroup.getBillingId());
			}
			params.setPaymentInfo(payPalInfo);

			vlogDebug("ProcCreatePayPalInfo.runProcess:success!");
			return SUCCESS;
		}
		vlogDebug("ProcCreatePayPalInfo.runProcess:failure!");
		return FAILURE;
	}

}
