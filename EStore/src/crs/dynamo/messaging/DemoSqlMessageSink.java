package crs.dynamo.messaging;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.ObjectMessage;
import javax.jms.TextMessage;

import atg.dms.patchbay.MessageSink;
import atg.nucleus.GenericService;

public class DemoSqlMessageSink extends GenericService implements MessageSink{

	@Override
	public void receiveMessage(String pPort, Message pMessage)throws JMSException {
		vlogDebug("Inside DemoSqlMessageSink.receiveMessage");
		System.out.printf("Message of type:%s recieved at port:%s", pMessage.getJMSType(),pPort);
		if (pMessage instanceof ObjectMessage){
			System.out.printf("Message recieved :%s",((ObjectMessage) pMessage).getObject());
		}
		
		vlogDebug("Exting DemoSqlMessageSink.receiveMessage");
	}

}
