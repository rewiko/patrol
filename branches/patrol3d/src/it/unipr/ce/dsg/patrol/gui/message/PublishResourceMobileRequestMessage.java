package it.unipr.ce.dsg.patrol.gui.message;

import it.simplexml.message.Message;

public class PublishResourceMobileRequestMessage extends Message{

	public PublishResourceMobileRequestMessage()
	{
		
		super("","",0);
        this.setMessageType("PUBLISHRESOURCEREQUEST");
        this.PARAMETERS_NUM=3;
	}
	
	public PublishResourceMobileRequestMessage(Message message)
	{
		super(message.getSourceName(), message.getSourceSocketAddr(), message.getSourcePort());
        this.setMessageType("PUBLISHRESOURCEREQUEST");
        this.PARAMETERS_NUM=3;
	}

}
