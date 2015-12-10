package crs.commerce.pricing;

import java.util.List;

import atg.commerce.pricing.Qualifier;

public class CRSQualifierService extends Qualifier{
	

	/* (non-Javadoc)
	 * Changing access modifier
	 */
	@Override
	public List wrapCommerceItems(List pItems, List pPriceQuotes) {
		return super.wrapCommerceItems(pItems, pPriceQuotes);
	}

}
