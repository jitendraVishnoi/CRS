package com.crs.paypal.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
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
import atg.commerce.order.HardgoodShippingGroup;
import atg.commerce.order.Order;
import atg.commerce.order.OrderHolder;
import atg.commerce.order.OrderImpl;
import atg.commerce.order.OrderManager;
import atg.commerce.order.PaymentGroup;
import atg.commerce.order.PaymentGroupManager;
import atg.core.util.StringUtils;
import atg.nucleus.servlet.HttpServletService;
import atg.service.pipeline.PipelineResult;
import atg.servlet.ServletUtil;
import crs.commerce.order.PayPalPaymentGroup;
import crs.paypal.PayPalDetails;
import crs.paypal.PayPalProcessor;

public class ConfirmPayPalPaymentServlet extends HttpServletService{

	private PayPalProcessor payPalProcessor;
	private String shoppingCart;
	private PaymentGroupManager paymentGroupManager;
	private OrderManager orderManager;
	private TransactionManager transactionManager;
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void service(HttpServletRequest pRequest, HttpServletResponse pResponse)
		    throws ServletException, IOException {
		    
		vlogDebug("Inside ConfirmPayPalPaymentServlet.service");
		
		boolean errors = false;

		OrderHolder shoppingCart = (OrderHolder) ServletUtil.getDynamoRequest(pRequest).resolveName(getShoppingCart());
		    Order currentOrder = shoppingCart.getCurrent();
		    OrderManager orderManager = getOrderManager();

		    Transaction transaction = null;
		    try {
		      transaction = ensureTransaction();
		      synchronized (currentOrder) {
		        try {
		          PayPalPaymentGroup paypalPaymentGroup = null;
		          List<PaymentGroup> paymentGroups = currentOrder.getPaymentGroups();
		          for (PaymentGroup paymentGroup : paymentGroups) {
		            if ((paymentGroup instanceof PayPalPaymentGroup)) {
		              paypalPaymentGroup = (PayPalPaymentGroup)paymentGroup;
		              break;
		            }
		          }
		          if (paypalPaymentGroup != null) {
		            PayPalDetails details = getPayPalProcessor().callGetExpressCheckoutDetails(paypalPaymentGroup.getToken());

		            if (details == null) {
		              vlogError("ConfirmPayPalPaymentServlet.service:Could not retrieve transaction details back from PayPal for token: {0}", paypalPaymentGroup.getToken());
		              throw new CommerceException("Could not retrieve transaction details back from PayPal.");
		            }

		            paypalPaymentGroup.setTransactionId(details.getTransactionId());
		            paypalPaymentGroup.setPayerId(details.getPayerid());
		            paypalPaymentGroup.setPayerStatus(details.getPayerStatus());

		            vlogDebug("ConfirmPayPalPaymentServlet.service:payerId: {0}",  paypalPaymentGroup.getPayerId());

		            vlogDebug("ConfirmPayPalPaymentServlet.service:is changed: {0}", paypalPaymentGroup.isChanged());

		              vlogDebug("ConfirmPayPalPaymentServlet.service:changed properties:{0}", paypalPaymentGroup.getChangedProperties());

		            if (!StringUtils.isBlank(details.getShippingOption())) {
		              for (Object shipGroup : currentOrder.getShippingGroups()) {
		                if ((shipGroup instanceof HardgoodShippingGroup)) {
		                  ((HardgoodShippingGroup)shipGroup).setShippingMethod(details.getShippingOption());
		                }
		              }
		            }

		            if (details.getShippingAddress() != null) {
		              for (Object shipGroup : currentOrder.getShippingGroups()) {
		                if ((shipGroup instanceof HardgoodShippingGroup)) {
		                  ((HardgoodShippingGroup)shipGroup).setShippingAddress(details.getShippingAddress());
		                }
		              }
		            }

		            if (getPayPalProcessor().isCaptureBillingAddress()) {
		              paypalPaymentGroup.setBillingAddress(details.getBillingAddress());
		            }

		            getPayPalProcessor().getPricingTools().priceOrderTotal(currentOrder);
		            vlogDebug("ConfirmPayPalPaymentServlet.service:order total:{0} order tax: {1}" +currentOrder.getPriceInfo().getTotal(),currentOrder.getPriceInfo().getTax());

		            if (currentOrder.isTransient()) {
		              getOrderManager().addOrder(currentOrder);
		            }
		            //((OrderImpl)currentOrder).updateVersion();
		            getOrderManager().updateOrder(currentOrder);

		            HashMap params = new HashMap();
		            params.put("Locale", ServletUtil.getUserLocale());

		            if (getPayPalProcessor().isSubmitOrderOnPayPalSite()) {
		              PipelineResult processOrderResult = orderManager.processOrderWithReprice(currentOrder, null, params);

		              if (processOrderResult.hasErrors()) {
		                if (isLoggingError()) {
		                  Object[] pipelineErrors = processOrderResult.getErrors();
		                  vlogError("ConfirmpayPalPaymentServlet.service: errors processing order - ");
		                  for (int i = 0; i < pipelineErrors.length; i++) {
		                    logError(pipelineErrors[i].toString());
		                  }
		                }
		                throw new CommerceException("ConfirmpayPalPaymentServlet.service: errors processing order");
		              }
		            }
		          }
		        }
		        catch (CommerceException ce) {
		          if (isLoggingError()) {
		            logError("ConfirmPayPalPaymentServlet.service: CommerceException caught. " + ce.getMessage(), ce);
		          }

		          errors = true;
		        }
		      }
		    } finally {
		      if (transaction != null) {
		        commitTransaction(transaction, currentOrder);
		      }
		    }
		    if (!errors) {
		      if ("EC".equals(pRequest.getParameter("type"))) {
		          vlogDebug("ConfirmPayPalPaymentServlet.service:redirecting to: {0}", getPayPalProcessor().getEcSuccessUrl());
		        pResponse.sendRedirect(getPayPalProcessor().getEcSuccessUrl());
		      } else {
		        vlogDebug("ConfirmPayPalPaymentServlet.service:redirecting to: {0}", getPayPalProcessor().getSuccessDestURL());

		        pResponse.sendRedirect(getPayPalProcessor().getSuccessDestURL());
		      }
		    } else {
		      vlogError("ConfirmPayPalPaymentServlet.service:Error occured!!!");
		      PrintWriter out = pResponse.getWriter();
		      out.print("There was a problem submitting your order");
		    }
		  }

		  protected Transaction ensureTransaction()
		  {
		    TransactionManager transactionManager = getTransactionManager();
		    Transaction transaction = null;
		    try {
		      transaction = transactionManager.getTransaction();
		      if (transaction == null) {
		        transactionManager.begin();
		        transaction = transactionManager.getTransaction();
		      }
		    } catch (NotSupportedException nse) {
		      if (isLoggingError())
		        logError("ConfirmPayPalPaymentServlet.ensureTransaction: Caught NotSupportedException. " + nse.getMessage());
		    }
		    catch (SystemException se)
		    {
		      if (isLoggingError()) {
		        logError("ConfirmPayPalPaymentServlet.ensureTransaction: Caught SystemException. " + se.getMessage());
		      }
		    }
		    return transaction;
		  }

		  protected void commitTransaction(Transaction pTransaction, Order pOrder)
		  {
		    boolean exception = false;

		    if (pTransaction != null)
		      try {
		        TransactionManager transactionManager = getTransactionManager();
		        if (transactionManager.getStatus() == 1) {
		          transactionManager.rollback();
		          if ((pOrder instanceof OrderImpl))
		            ((OrderImpl)pOrder).invalidateOrder();
		        }
		        else {
		          transactionManager.commit();
		        }
		      } catch (RollbackException exc) {
		        exception = true;
		        if (isLoggingError())
		          logError(exc);
		      }
		      catch (HeuristicMixedException exc) {
		        exception = true;
		        if (isLoggingError())
		          logError(exc);
		      }
		      catch (HeuristicRollbackException exc) {
		        exception = true;
		        if (isLoggingError())
		          logError(exc);
		      }
		      catch (SystemException exc) {
		        exception = true;
		        if (isLoggingError())
		          logError(exc);
		      }
		      finally {
		        if ((exception) && 
		          ((pOrder instanceof OrderImpl)))
		          ((OrderImpl)pOrder).invalidateOrder();
		      }
		  }

	
	

	public PayPalProcessor getPayPalProcessor() {
		return payPalProcessor;
	}

	public void setPayPalProcessor(PayPalProcessor payPalProcessor) {
		this.payPalProcessor = payPalProcessor;
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

}
