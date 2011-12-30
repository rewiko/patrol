package it.unipr.ce.dsg.p2pgame.platform.bot.message;

import it.simplexml.message.Message;
import it.simplexml.message.Parameter;
public class BotStartMatchRequestMessage extends Message {

	private String id;

	public BotStartMatchRequestMessage(String id)
	{
		
		
	}
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
}
