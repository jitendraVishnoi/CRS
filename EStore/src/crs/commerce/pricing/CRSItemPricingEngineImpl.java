package crs.commerce.pricing;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import atg.commerce.pricing.AmountInfo;
import atg.commerce.pricing.ItemPricingEngineImpl;
import atg.commerce.pricing.PricingContext;
import atg.commerce.pricing.PricingException;
import atg.commerce.pricing.PromotionProcessingComponent;
import atg.repository.RepositoryItem;

public class CRSItemPricingEngineImpl extends ItemPricingEngineImpl{
	
	/* (non-Javadoc)
	 * changing access modifier for non package classes
	 */
	@Override
	public AmountInfo createPriceInfo() throws PricingException {
		
		return super.createPriceInfo();
	}
	
	@Override
	public void applyCalculator(Object pCalc, int pPricingMethod,
			List pItemPriceQuotes, List pItems, PricingContext pPricingContext,
			Map pExtraParameters) throws PricingException {
		// TODO Auto-generated method stub
		super.applyCalculator(pCalc, pPricingMethod, pItemPriceQuotes, pItems,
				pPricingContext, pExtraParameters);
	}
	
	@Override
	public void applyPromotionProcessing(
			PromotionProcessingComponent[] pPromotionProcessingComponents,
			Collection<RepositoryItem> pPricingModels,
			PricingContext pPricingContext, Map pExtraParameters)
			throws PricingException {
		// TODO Auto-generated method stub
		super.applyPromotionProcessing(pPromotionProcessingComponents, pPricingModels,
				pPricingContext, pExtraParameters);
	}
	
	@Override
	public Collection<RepositoryItem> vetoPromotionsForEvaluation(
			PricingContext pPricingContext, Map pExtraParametersMap,
			Collection<RepositoryItem> pPricingModels) {
		// TODO Auto-generated method stub
		return super.vetoPromotionsForEvaluation(pPricingContext, pExtraParametersMap,
				pPricingModels);
	}

}
