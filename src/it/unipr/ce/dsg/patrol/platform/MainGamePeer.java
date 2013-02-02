/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package it.unipr.ce.dsg.patrol.platform;


import it.simplexml.message.Message;
import it.simplexml.message.MessageReader;
import it.unipr.ce.dsg.patrol.gui.message.CreateGamePeerRequestMessage;
import it.unipr.ce.dsg.patrol.gui.message.RegisterRequestMessage;
import it.unipr.ce.dsg.patrol.gui.message.RegisterResponseMessage;
import it.unipr.ce.dsg.patrol.gui.message.StartMessage;
import it.unipr.ce.dsg.patrol.gui.message.SuccessMessage;


import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * Class that create a GamePeer manager that interacts with the GUI component.
 * It is the backend of a Game Node
 *
 * @author jose murga
 * @author Stefano Sebastio
 */
public class MainGamePeer extends Thread{

    private GamePeer gp = null;
    ServerSocket server=null;
    private GUIMessageListener message_listener;
    private int portnumber;
    
    /**
     * The port number is the port where the GamePeer responds to GUI requests.
     * It is an internal port not an external communication one
     * 
     * @param port
     */
    public MainGamePeer(int port)
    {
    	this.gp=null;
    	this.portnumber=port;
    }
    
	/**
	 * Manages all the requests coming from the GUI module
	 */
    public void run()
    {
        System.out.println("waiting...");
        Socket clientSocket = null;
        

        try {
        	if (server == null){
	            try{    	
		            server = new ServerSocket(this.portnumber);//(9999);
		        } catch (BindException e){
		            	System.err.println("The specified address port ("+ this.portnumber + ") is already in use. Try a different one");
		            	e.printStackTrace();
		            	System.exit(5);
		        }
        	}
        } catch (IOException e) {
        	e.printStackTrace();
        }

        while(true){
            try {
                clientSocket = server.accept(); //it is a blocking method. Thus it waits until a GUI is created 
                DataInputStream is = new DataInputStream(clientSocket.getInputStream());
                DataOutputStream os = new DataOutputStream(clientSocket.getOutputStream());
                String message = null;
              //  while(true){

               // int current = 0;
                boolean read = false;
                byte[] buf = new byte[100000];

                //while (current < 1) {
                while (!read) {

					int reader = is.read(buf);
		
					if (reader != -1){
						message = new String(buf);
					
						//current++;
						read = true;
					}
		        }
		
		        checkIncomingMessage(message, os);
		
		        is.close();
		        os.close();
		        clientSocket.close();
		        break;
			    //  }
			
	            } catch (IOException ex) {
	                Logger.getLogger(MainGamePeer.class.getName()).log(Level.SEVERE, null, ex);
	            }

        }
    }

    /**
     * checkIncomingMessage(): Dispatch the GUI message request to the appropriate handler
     * 
     * transforms the received message, string format to the appropriate data structure and
     * verifies the received message type, then invokes the appropriate method to develop response
     * 
     * @param messageString : String, os : DataOutputStream 
     * 
     **/

    public void checkIncomingMessage(String messageString, DataOutputStream os) throws IOException {

        MessageReader messageReader = new MessageReader();
        Message receivedMessage = messageReader.readMessageFromString(messageString.trim());
        
        
        if(receivedMessage.getMessageType().equals("CREATEGAMEPEERREQUEST"))
        {
            this.CreateGamePeerAction(receivedMessage, os);
        }
        else if(receivedMessage.getMessageType().equals("REGISTERREQUEST"))
        {
            this.RegisterRequestAction(receivedMessage, os);
        }
        else if(receivedMessage.getMessageType().equals("START"))
        {
            this.StartMessageAction(receivedMessage, os);
        }
         else if(receivedMessage.getMessageType().equals("GPEXISTREQUEST"))
        {
            this.GamePeerExistAction(receivedMessage, os);
        }



    }

    /*
     * CreateGamePeerAction:
     * creates a new instance of the game peer class with the message's info
     * creates a new instance of the MessageListener class thread an  starts it
     * 
     * @param messageString : String
     * @param os : DataOutputStream
     **/
    private void CreateGamePeerAction(Message receivedMessage, DataOutputStream os) throws IOException
    {
    	System.out.println("CreateGamePeer ");
    	System.exit(2);
        CreateGamePeerRequestMessage request=new CreateGamePeerRequestMessage(receivedMessage);
        
        int inPort=request.getInPort();
        int outPort=request.getOutPort();
        int idBitLength=request.getIdBitLength();
        String id=request.getId();
        String serverAddr=request.getServerAddr();
        int serverPort=request.getServerPort();
        int gameInPort=request.getGameInPort();
        String gameServerAddr=request.getGameServerAddr();
        int gameOutPort=request.getGameOutPort();
        int gameServerPort=request.getGameServerPort();
        int stab=request.getStab();
        
        //int fix=request.getFix();
        int fix=50;
        System.out.println("fix " + fix);
        int check=request.getCheck();
        int pub=request.getPub();

        this.gp=new GamePeer(inPort,outPort,idBitLength,id,serverAddr,serverPort,gameInPort,gameOutPort,gameServerAddr,gameServerPort, stab,fix,check, pub);

        this.message_listener=new GUIMessageListener(gp,(/*2424*/this.portnumber+5));
        //this.message_listener.start();

        Thread listenerThread=new Thread(this.message_listener);
        listenerThread.start();
        // System.out.println("creato gamepeer e messagelistener");

        SuccessMessage success=new SuccessMessage(true);

        if(this.gp==null){
        	success=new SuccessMessage(false);
        }

        os.write(success.generateXmlMessageString().getBytes());

    }

    /**
     *  RegisterRequestAction
     *  register the user on the bootstrap server
     * 
     * @param receivedMessage: Message
     * @param os: DataOutputStream
     **/
    private void RegisterRequestAction(Message receivedMessage, DataOutputStream os) throws IOException
    {
    	System.out.println("RegisterReq");
         System.exit(3);
         
            RegisterRequestMessage register=new RegisterRequestMessage(receivedMessage);

            String user=register.getUserName();
            String password=register.getPassword();


            System.out.println("registering user on server");
            boolean resp = this.gp.registerOnServer(user, password);

            RegisterResponseMessage response = null;
           if (resp)
        	   response=new RegisterResponseMessage("yes");
           else
        	   response=new RegisterResponseMessage("no");

            os.write(response.generateXmlMessageString().getBytes());

    }

    /*
     * StartMessageAction
     * stars game for the new user
     * Parameters:
     * receivedMessage: Message
     * os: DataOutputStream
     */
    private void StartMessageAction(Message receivedMessage, DataOutputStream os) throws IOException
    {
    	System.out.println("Start Message received - for debug purpose only (Called by MessageSender)");
    	System.exit(1);
               StartMessage start =new StartMessage(receivedMessage);
               double minx=start.getMinX();
               double maxx=start.getMaxX();
               double miny=start.getMinY();
               double maxy=start.getMaxY();
               double minz=start.getMinZ();
               double maxz=start.getMaxZ();
               double vel=start.getVel();
               double gran=start.getGran();
               double vis=start.getVis();


               this.gp.startGame(minx, maxx, miny, maxy, minz, maxz, vel, vis, gran);

               System.out.println("game started");

                SuccessMessage success=new SuccessMessage(true);

                os.write(success.generateXmlMessageString().getBytes());

    }

    /**
     *  GamePeerExistAction
     *  returns true if exist an instance of game peer class. Returns false, otherwise.
     *  
     *  
     * @param receivedMessage: Message coming from the GUI
     * @param os: DataOutputStream 
     **/

    public void GamePeerExistAction(Message receivedMessage, DataOutputStream os) throws IOException
    {
          SuccessMessage success;
          

          if(this.gp==null){
        	//System.out.println("GAMEPEER false");  
        	success=new SuccessMessage(false);
          }
          else
          {
        	  //System.out.println("GAMEPEER true");
        	  success=new SuccessMessage(true);
          }

          os.write(success.generateXmlMessageString().getBytes());


    }

    /**
     * Creates and launches a new instance of Main Game peer thread class. The node backend
     **/
    public static void main(String [] arg)
    {
    	
    	int port = 9998;
    	if (arg.length >= 1)
    		port = Integer.parseInt(arg[0].trim());
    	
		MainGamePeer mainpeer=new MainGamePeer(port);
		mainpeer.start();


    }




}

    
    
   




