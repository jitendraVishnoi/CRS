package crs.dynamo.messaging;

import javax.jms.JMSException;
import javax.jms.ObjectMessage;
import javax.jms.TextMessage;

import atg.dms.patchbay.MessageSource;
import atg.dms.patchbay.MessageSourceContext;
import atg.nucleus.GenericService;
import atg.nucleus.ServiceException;

public class DemoMessageSource extends GenericService implements MessageSource {

	MessageSourceContext messageSourceContext;
	boolean started;
	
	@Override
	public void setMessageSourceContext(MessageSourceContext messageSourceContext) {
		vlogDebug("setMessageSourceContext(MessageSourceContext messageSourceContext) is being called");
		this.messageSourceContext = messageSourceContext;
	}

	@Override
	public void startMessageSource() {
		vlogDebug("startMessageSource() is being called");
		started = true;
	}

	@Override
	public void stopMessageSource() {
		vlogDebug("stopMessageSource() is being called");
		started = false;
	}
	
	/** This method will do actual work
	 * @throws JMSException
	 */
	public void sendMessage() throws JMSException {
		vlogDebug("Inside DemoMessageSource.sendMessage");
		if (started && messageSourceContext != null) {
			TextMessage textMessage = messageSourceContext.createTextMessage("myPort");
			textMessage.setJMSType("crs.msg.local");
			textMessage.setText("Hello Charli.. Hello !!");
			messageSourceContext.sendMessage("myPort", textMessage);
			
			TextMessage message = messageSourceContext.createTextMessage("sqlJMSPort");
			message.setJMSType("crs.msg.sql");
			message.setText("ELEPHANT OBJECT");
			messageSourceContext.sendMessage("sqlJMSPort",message);
			vlogDebug("Exting DemoMessageSource.sendMessage");
		}
	}
	
	//Overriding GenericService methods
	
	@Override
	public void doStartService() throws ServiceException {
		vlogDebug("doStartService() is being called");
		super.doStartService();
	}
	
	@Override
	public void stopService() throws ServiceException {
		vlogDebug("stopService() is being called");
		super.stopService();
	}
	
	@Override
	public void doStopService() throws ServiceException {
		vlogDebug("doStopService() is being called");
		super.doStopService();
	}

}
