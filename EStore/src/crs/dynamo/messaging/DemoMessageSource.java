package crs.dynamo.messaging;

import javax.jms.JMSException;
import javax.jms.ObjectMessage;

import atg.dms.patchbay.MessageSource;
import atg.dms.patchbay.MessageSourceContext;
import atg.nucleus.GenericService;
import atg.nucleus.ServiceException;

public class DemoMessageSource extends GenericService implements MessageSource {

	private MessageSourceContext messageSourceContext;
	private boolean started;
	
	@Override
	public void setMessageSourceContext(MessageSourceContext messageSourceContext) {
		vlogDebug("setMessageSourceContext(MessageSourceContext messageSourceContext) is being called");
		this.messageSourceContext = messageSourceContext;
	}

	@Override
	public void startMessageSource() {
		vlogDebug("startMessageSource() is being called");
		setStarted(true);
	}

	@Override
	public void stopMessageSource() {
		vlogDebug("stopMessageSource() is being called");
		setStarted(false);
	}
	
	/** This method will do actual work
	 * @throws JMSException
	 */
	public void sendMessage() throws JMSException {
		vlogDebug("Inside DemoMessageSource.sendMessage");
		if (started && messageSourceContext != null) {
			ObjectMessage msg = getMessageSourceContext().createObjectMessage("myPort");
			msg.setJMSType("crs.msg.local");
			
			DummyMessage dummyMessage = new DummyMessage();
			dummyMessage.setMessage("Hello Charli.. Hello !!");
			msg.setObject(dummyMessage);
			getMessageSourceContext().sendMessage("myPort",msg);
			
			ObjectMessage sqlMsg = getMessageSourceContext().createObjectMessage("sqlJMSPort");
			sqlMsg.setJMSType("crs.msg.sql");
			
			DummyMessage msg2 = new DummyMessage();
			msg2.setMessage("ELEPHANT OBJECT");
			sqlMsg.setObject(msg2);
			getMessageSourceContext().sendMessage("sqlJMSPort", sqlMsg);
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

	public boolean isStarted() {
		return started;
	}

	public void setStarted(boolean started) {
		this.started = started;
	}

	public MessageSourceContext getMessageSourceContext() {
		return messageSourceContext;
	}

	
}
