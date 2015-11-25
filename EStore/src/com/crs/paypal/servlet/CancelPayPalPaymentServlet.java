package com.crs.paypal.servlet;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;

import atg.commerce.CommerceException;
import atg.commerce.order.Order;
import atg.commerce.order.OrderHolder;
import atg.commerce.order.OrderImpl;
import atg.commerce.order.OrderManager;
import atg.commerce.order.PaymentGroup;
import atg.commerce.order.PaymentGroupManager;
import atg.commerce.order.purchase.PaymentGroupContainerService;
import atg.nucleus.servlet.HttpServletService;
import atg.servlet.ServletUtil;
import crs.paypal.PayPalProcessor;

public class CancelPayPalPaymentServlet extends HttpServletService {

	private String shoppingCart;
	private PaymentGroupManager paymentGroupManager;
	private OrderManager orderManager;
	private TransactionManager transactionManager;
	private String paymentGroupContainerService;
	private PayPalProcessor payPalProcessor;

	public void service(HttpServletRequest pRequest,
			HttpServletResponse pResponse) throws ServletException, IOException {
		vlogDebug("CancelPayPalPaymentServlet.service:entering method...");

		PaymentGroupManager paymentGroupManager = getPaymentGroupManager();

		OrderHolder shoppingCart = (OrderHolder) ServletUtil.getDynamoRequest(pRequest)
				.resolveName(getShoppingCart());
		Order currentOrder = shoppingCart.getCurrent();

		PaymentGroupContainerService paymentGroupContainerService = (PaymentGroupContainerService) ServletUtil
				.getDynamoRequest(pRequest).resolveName(
						getPaymentGroupContainerService());
		
		OrderManager orderManager = getOrderManager();
		PaymentGroup defaultPaymentGroup = paymentGroupContainerService
				.getPaymentGroup(orderManager.getOrderTools()
						.getDefaultPaymentGroupType());
		vlogDebug("currentOrder ::{0} defaultPaymentGroup::{1}", currentOrder, defaultPaymentGroup);
		Transaction transaction = null;
		try {
			transaction = ensureTransaction();
			synchronized (currentOrder) {
				try {
					@SuppressWarnings("unchecked")
					List<PaymentGroup> nonModifiablePaymentGroups = paymentGroupManager
							.getNonModifiablePaymentGroups(currentOrder);

					vlogDebug("nonModifiablePaymentGroups ::{0} in order ::{1}", nonModifiablePaymentGroups, currentOrder.getId());
					paymentGroupManager.removeAllPaymentGroupsFromOrder(
							currentOrder, nonModifiablePaymentGroups);
					applyPaymentGroup(currentOrder, defaultPaymentGroup);
					paymentGroupManager
							.recalculatePaymentGroupAmounts(currentOrder);
					orderManager.updateOrder(currentOrder);
				} catch (CommerceException ce) {
					if (isLoggingError())
						logError("CancelPayPalPaymentServlet.service: CommerceException caught"
								+ ce.getMessage());
				}
			}
		} finally {
			if (transaction != null) {
				commitTransaction(transaction, currentOrder);
			}
		}
		if ("EC".equals(pRequest.getParameter("type")))
			pResponse.sendRedirect(getPayPalProcessor().getEcSuccessUrl());
		else
			pResponse.sendRedirect(getPayPalProcessor().getEcCancelUrl());
	}

	protected void applyPaymentGroup(Order pOrder, PaymentGroup pPaymentGroup) {
		vlogDebug("Inside CancelPayPalPaymentServlet.applyPaymentGroup order :{0} paymentGroup:{1}", pOrder, pPaymentGroup);
		OrderManager orderManager = getOrderManager();
		PaymentGroupManager paymentGroupManager = getPaymentGroupManager();

		if (pPaymentGroup == null) {
			if (isLoggingDebug()) {
				logDebug("CancelPayPalPaymentServlet.applyDefaultPaymentGroup: no default payment group");
			}
			return;
		}
		vlogDebug("applying DefaultPaymentGroup" + pPaymentGroup);
		String pgId = pPaymentGroup.getId();
		try {
			if (!paymentGroupManager.isPaymentGroupInOrder(pOrder, pgId)) {
				paymentGroupManager.addPaymentGroupToOrder(pOrder,
						pPaymentGroup);
			}
			orderManager.addRemainingOrderAmountToPaymentGroup(pOrder, pgId);
		} catch (CommerceException ce) {
			if (isLoggingError())
				logError("CancelPayPalPaymentServlet.applyDefaultPaymentGroup: CommerceException caught setting up default payment group. "
						+ ce.getMessage());
		}
	}

	protected Transaction ensureTransaction() {
		TransactionManager transactionManager = getTransactionManager();
		Transaction transaction = null;
		try {
			transaction = transactionManager.getTransaction();
			if (transaction == null) {
				transactionManager.begin();
				transaction = transactionManager.getTransaction();
			}
		} catch (NotSupportedException nse) {
			vlogError(
					"CancelPayPalPaymentServlet.ensureTransaction: Caught NotSupportedException. {0}",
					nse.getMessage());
		} catch (SystemException se) {
			vlogError(
					"CancelPayPalPaymentServlet.ensureTransaction: Caught SystemException. {0}",
					se.getMessage());
		}
		return transaction;
	}

	protected void commitTransaction(Transaction pTransaction, Order pOrder) {
		boolean exception = false;

		if (pTransaction != null)
			try {
				TransactionManager transactionManager = getTransactionManager();
				if (transactionManager.getStatus() == 1) {
					transactionManager.rollback();
					if ((pOrder instanceof OrderImpl))
						((OrderImpl) pOrder).invalidateOrder();
				} else {
					transactionManager.commit();
				}
			} catch (RollbackException exc) {
				exception = true;
				if (isLoggingError())
					logError(exc);
			} catch (HeuristicMixedException exc) {
				exception = true;
				if (isLoggingError())
					logError(exc);
			} catch (HeuristicRollbackException exc) {
				exception = true;
				if (isLoggingError())
					logError(exc);
			} catch (SystemException exc) {
				exception = true;
				if (isLoggingError())
					logError(exc);
			} finally {
				if ((exception) && ((pOrder instanceof OrderImpl)))
					((OrderImpl) pOrder).invalidateOrder();
			}
	}

	public String getShoppingCart() {
		return shoppingCart;
	}

	public void setShoppingCart(String shoppingCart) {
		this.shoppingCart = shoppingCart;
	}

	public PaymentGroupManager getPaymentGroupManager() {
		return paymentGroupManager;
	}

	public void setPaymentGroupManager(PaymentGroupManager paymentGroupManager) {
		this.paymentGroupManager = paymentGroupManager;
	}

	public OrderManager getOrderManager() {
		return orderManager;
	}

	public void setOrderManager(OrderManager orderManager) {
		this.orderManager = orderManager;
	}

	public TransactionManager getTransactionManager() {
		return transactionManager;
	}

	public void setTransactionManager(TransactionManager transactionManager) {
		this.transactionManager = transactionManager;
	}

	public String getPaymentGroupContainerService() {
		return paymentGroupContainerService;
	}

	public void setPaymentGroupContainerService(
			String paymentGroupContainerService) {
		this.paymentGroupContainerService = paymentGroupContainerService;
	}

	public PayPalProcessor getPayPalProcessor() {
		return payPalProcessor;
	}

	public void setPayPalProcessor(PayPalProcessor payPalProcessor) {
		this.payPalProcessor = payPalProcessor;
	}

}
