package crs.dynamo.messaging;

import java.io.Serializable;

public class DummyMessage implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String message;

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	@Override
	public String toString() {
		return "DummyMessage [message=" + message + "]";
	}
}
