package crs.paypal;

public class PayPalCertException extends Exception {
	private static final long serialVersionUID = 4819708233968907466L;

	public PayPalCertException() {
	}

	public PayPalCertException(String pMessage, Throwable pCause) {
		super(pMessage, pCause);
	}

	public PayPalCertException(String pMessage) {
		super(pMessage);
	}

	public PayPalCertException(Throwable pCause) {
		super(pCause);
	}
}