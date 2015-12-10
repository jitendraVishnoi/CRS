package crs.commerce.endeca.index.accessor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import atg.commerce.CommerceException;
import atg.commerce.order.CommerceItem;
import atg.commerce.order.OrderTools;
import atg.commerce.pricing.AmountInfo;
import atg.commerce.pricing.ItemPricingCalculator;
import atg.commerce.pricing.PricingContext;
import atg.commerce.pricing.PricingException;
import atg.commerce.pricing.PricingModelHolder;
import atg.commerce.pricing.PricingTools;
import atg.commerce.pricing.PromotionProcessingComponent;
import atg.core.util.StringUtils;
import atg.nucleus.naming.ComponentName;
import atg.repository.RepositoryItem;
import atg.repository.search.indexing.Context;
import atg.repository.search.indexing.PropertyAccessorImpl;
import atg.repository.search.indexing.specifier.PropertyTypeEnum;
import atg.servlet.ServletUtil;
import atg.userprofiling.Profile;
import crs.commerce.pricing.CRSItemPricingEngineImpl;
import crs.commerce.pricing.CRSQualifierService;

public class PromotionAccessor extends PropertyAccessorImpl {

	private OrderTools orderTools;
	private PricingTools pricingTools;
	private CRSQualifierService pricingQualifierService;
	private CRSItemPricingEngineImpl itemPricingEngine;
	private String pricingModelHolderPath;

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	protected Object getTextOrMetaPropertyValue(Context pContext,
			RepositoryItem pItem, String pPropertyName, PropertyTypeEnum pType) {

		List<RepositoryItem> childSkus = (List<RepositoryItem>) pItem.getPropertyValue("childSKUs");
		
		if (childSkus.isEmpty()) {
			vlogError("No sku found for product :: {0}",pItem.getRepositoryId());
			return null;
		}
		RepositoryItem sku = childSkus.get(0);
		CommerceItem commerceItem = null;
		try {
			commerceItem = getPricingTools().
					createPricingCommerceItem(sku.getRepositoryId(), pItem.getRepositoryId(), 1);
		} catch (CommerceException e) {
			vlogError(e,"Exception in PromotionAccessor.getTextOrMetaPropertyValue :: {0}");
		}
		vlogDebug("commerceItem :: {0}", commerceItem);
		
		List<CommerceItem> items = new ArrayList<>();
		List<AmountInfo> priceQuotes = new ArrayList<>();
		items.add(commerceItem);
		try {
			priceQuotes.add(getItemPricingEngine().createPriceInfo());
		} catch (PricingException e) {
			vlogError(e,"Exception in PromotionAccessor.getTextOrMetaPropertyValue :: {0}");
		}

		List wrappedItems = getPricingQualifierService()
				.wrapCommerceItems(items, priceQuotes);
		
		PricingContext pricingContext = getPricingTools().getPricingContextFactory()
				.createPricingContext(wrappedItems, null, new Profile(), null,
						null, null);

		HashMap extraParameters = new HashMap();
		Collection pricingModels = getItemPricingEngine().getGlobalPromotions();

		if (getItemPricingEngine().getPreCalculators() != null) {
			int num = getItemPricingEngine().getPreCalculators().length;
			for (int c = 0; c < num; ++c) {
				ItemPricingCalculator calc = getItemPricingEngine()
						.getPreCalculators()[c];
				try {
					getItemPricingEngine().applyCalculator(calc, 0,
							priceQuotes, items, pricingContext,
							extraParameters);
				} catch (PricingException e) {
					vlogError(e,"Exception in PromotionAccessor.getTextOrMetaPropertyValue :: {0}");
				}
			}

		}

		PromotionProcessingComponent[] prePromotionProcessing = getItemPricingEngine()
				.getPrePromotionProcessing();
		if (prePromotionProcessing != null) {
			try {
				getItemPricingEngine().applyPromotionProcessing(
						prePromotionProcessing, pricingModels,
						pricingContext, extraParameters);
			} catch (PricingException e) {
				vlogError(e,"Exception in PromotionAccessor.getTextOrMetaPropertyValue :: {0}");
			}
		}

		pricingModels = getItemPricingEngine()
				.vetoPromotionsForEvaluation(pricingContext, null,
						pricingModels);
		List<RepositoryItem> promotions = new ArrayList<>();
		for (Object pricingModel : pricingModels) {
			pricingContext.setPricingModel((RepositoryItem)pricingModel);

			Collection qualifyingItems = null;
			try {
				qualifyingItems = getPricingQualifierService()
						.findQualifyingItems(pricingContext, null);
			} catch (PricingException e) {
				vlogError(e,"Exception in PromotionAccessor.getTextOrMetaPropertyValue :: {0}");
			}

			if ((qualifyingItems != null)
					&& (!(qualifyingItems.isEmpty()))) {
				promotions.add((RepositoryItem)pricingModel);
			}
		}
		
		if (promotions.isEmpty()) {
			return false;
		} else {
			return true;
		}
	}

	public OrderTools getOrderTools() {
		return orderTools;
	}

	public void setOrderTools(OrderTools orderTools) {
		this.orderTools = orderTools;
	}

	public PricingTools getPricingTools() {
		return pricingTools;
	}

	public void setPricingTools(PricingTools pricingTools) {
		this.pricingTools = pricingTools;
	}

	public CRSQualifierService getPricingQualifierService() {
		return pricingQualifierService;
	}

	public void setPricingQualifierService(
			CRSQualifierService pricingQualifierService) {
		this.pricingQualifierService = pricingQualifierService;
	}

	public CRSItemPricingEngineImpl getItemPricingEngine() {
		return itemPricingEngine;
	}

	public void setItemPricingEngine(CRSItemPricingEngineImpl itemPricingEngine) {
		this.itemPricingEngine = itemPricingEngine;
	}

	public String getPricingModelHolderPath() {
		return pricingModelHolderPath;
	}

	public void setPricingModelHolderPath(String pricingModelHolderPath) {
		this.pricingModelHolderPath = pricingModelHolderPath;
	}

}
