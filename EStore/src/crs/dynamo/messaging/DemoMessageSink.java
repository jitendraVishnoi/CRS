package crs.dynamo.messaging;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.ObjectMessage;
import javax.jms.StreamMessage;
import javax.jms.TextMessage;

import atg.dms.patchbay.MessageSink;
import atg.nucleus.GenericService;

public class DemoMessageSink extends GenericService implements MessageSink{

	@Override
	public void receiveMessage(String pPort, Message pMessage) throws JMSException {
		
		System.out.printf("Message recived at port:%s with JMSType:%s",pPort, pMessage.getJMSType());
		
		if (pMessage instanceof TextMessage) {
			System.out.printf("TextMessage::%s", pMessage);
		} else if (pMessage instanceof ObjectMessage) {
			System.out.printf("ObjectMessage::%s", pMessage);
		} else if (pMessage instanceof StreamMessage) {
			System.out.printf("StreamMessage::%s", pMessage);
		} 
		
	}

}
