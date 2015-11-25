package crs.paypal.beans;

public class ShippingInfoBean {
	private String mMethod;
	private double mPrice;
	private double mTax;

	public String getMethod() {
		return this.mMethod;
	}

	public void setMethod(String pMethod) {
		this.mMethod = pMethod;
	}

	public double getPrice() {
		return this.mPrice;
	}

	public void setPrice(double pPrice) {
		this.mPrice = pPrice;
	}

	public double getTax() {
		return this.mTax;
	}

	public void setTax(double pTax) {
		this.mTax = pTax;
	}

	public String toString() {
		return "Shipping Method: " + getMethod() + ". Shipping Price: "
				+ getPrice() + ". Tax: " + getTax();
	}
}
