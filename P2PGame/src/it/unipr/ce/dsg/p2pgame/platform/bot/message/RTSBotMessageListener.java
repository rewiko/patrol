package it.unipr.ce.dsg.p2pgame.platform.bot.message;

import it.simplexml.message.AckMessage;
import it.simplexml.message.Message;
import it.simplexml.message.MessageReader;
import it.unipr.ce.dsg.p2pgame.platform.bot.InterfaceBot;
import it.unipr.ce.dsg.p2pgame.platform.bot.RTSGameBot;
import it.unipr.ce.dsg.p2pgame.platform.bot.RTSGameBot2;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class RTSBotMessageListener implements Runnable{
	
	private ServerSocket server;
	private int port;
	private String listenerId;
	private String listenerAddr;
	
	private RTSGameBot2 mybot;
	
	public RTSBotMessageListener(InterfaceBot bot,String id,String address, int port)
	{
		this.mybot=(RTSGameBot2)bot;
		this.port=port;
		this.listenerId=id;
		this.listenerAddr=address;
		
		
		System.out.println("ip "+address+" , "+port);
	}

	@Override
	public void run() {
		Socket clientSocket = null;
		
		try {
			if (this.server == null)
	                	this.server = new ServerSocket(this.port);

	    } catch (IOException e) {
			e.printStackTrace();
	    }
	    
	    System.out.println("-----------------RTSBotMessageListener-----------------");
	    while(true)
	    {
	    	
	    	try {
				clientSocket = server.accept();
				String message = null;

				DataInputStream is = new DataInputStream(clientSocket.getInputStream());
				DataOutputStream os = new DataOutputStream(clientSocket.getOutputStream());
				
		        while(true){

                    int current = 0;
                    byte[] buf = new byte[100000];

                    while (current < 1) {

                    	int reader = is.read(buf);

                    	if (reader != -1){
                    		message = new String(buf);
                    		current++;
                    	}
                    }

                    checkIncomingMessage(message, os);

                    is.close();
                    os.close();
                    clientSocket.close();
                    break;
                }
		        
				
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
	    	
	    	
	    }
	            
	
	}
	
	 public void checkIncomingMessage(String messageString, DataOutputStream os) throws IOException {
		 
		 MessageReader messageReader = new MessageReader();
		 Message receivedMessage = messageReader.readMessageFromString(messageString.trim());
		 
		 if(receivedMessage.getMessageType().equals("PLANETCONQUERED"))
	        {
	            this.planetConqueredAction(receivedMessage, os);
	        }
		 
	 }
	 
	 private void planetConqueredAction(Message receivedMessage, DataOutputStream os) throws IOException
	 {
		 PlanetConqueredMessage planetmessage=new PlanetConqueredMessage(receivedMessage);
		 
		 String playerID=planetmessage.getIdPlayer();
		 String planetID=planetmessage.getIdPlanet();
		 String playerName=planetmessage.getNamePlayer();
		 
		 this.mybot.setPlanetOwner(planetID, playerID, playerName);
		 System.out.println("GIOCATORE "+playerID+"HA CONQUISTATO "+planetID);
		 
			os.write((new AckMessage(this.listenerId, this.listenerAddr, this.port, 0, "")).generateXmlMessageString().getBytes());
		 
		 
	 }

}
