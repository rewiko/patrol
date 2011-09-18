
package it.unipr.ce.dsg.p2pgame.Engine;

import it.simplexml.message.Message;
import it.simplexml.message.MessageReader;
import it.unipr.ce.dsg.p2pgame.GUI.message.*;
import it.unipr.ce.dsg.p2pgame.platform.GamePeer;
import it.unipr.ce.dsg.p2pgame.platform.GamePlayer;
import it.unipr.ce.dsg.p2pgame.platform.GamePlayerResponsible;
import it.unipr.ce.dsg.p2pgame.platform.GameResource;
import it.unipr.ce.dsg.p2pgame.platform.GameResourceEvolve;
import it.unipr.ce.dsg.p2pgame.platform.GameResourceMobile;
import it.unipr.ce.dsg.p2pgame.platform.GameResourceMobileResponsible;
import it.unipr.ce.dsg.p2pgame.util.CheckOutput;
import it.unipr.ce.dsg.p2pgame.util.MultiLog;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import weka.gui.SysErrLog;

/**
 *
 * @author jose murga
 */
public class GUIMessageListener extends Thread{

    private ServerSocket server;
    private GamePeer gp;
    private CheckOutput check;
    

    public GUIMessageListener(GamePeer gp)
    {
        super();
        this.gp=gp;
        
        this.server=null;
        check=new CheckOutput("output.txt");
    }

    @Override
    public void run() {

        Socket clientSocket = null;



        try {
		if (this.server == null)
                	this.server = new ServerSocket(2424);

            } catch (IOException e) {
		e.printStackTrace();
            }

        while(true){
            try {
                clientSocket = server.accept();
                DataInputStream is = new DataInputStream(clientSocket.getInputStream());
                DataOutputStream os = new DataOutputStream(clientSocket.getOutputStream());
                String message = null;
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

                    try {
						checkIncomingMessage(message, os);
					} catch (InterruptedException e) {
						
						System.err.println("GUIMessageListener InterruptedException");
						e.printStackTrace();
					}

                    is.close();
		    os.close();
		    clientSocket.close();
		    break;
                }

            } catch (IOException ex) {
                Logger.getLogger(GUIMessageListener.class.getName()).log(Level.SEVERE, null, ex);
            }

        }

    }

    /*
     * checkIncomingMessage:
     * transforms the received message, string format to the appropriate data structure and
     * verifies the received message type, then invokes the appropriate method to develop response
     * Parameters:
     * messageString : String
     * os : DataOutputStream
     */

    public void checkIncomingMessage(String messageString, DataOutputStream os) throws IOException, InterruptedException {

        MessageReader messageReader = new MessageReader();
	Message receivedMessage = messageReader.readMessageFromString(messageString.trim());

        if(receivedMessage.getMessageType().equals("IDREQUEST"))
        {
            this.GamePeerIDAction(receivedMessage, os);
        }
        else if(receivedMessage.getMessageType().equals("GPDESCREQUEST"))
        {
            this.GamePeerDescriptionAction(receivedMessage, os);

        }
        else if(receivedMessage.getMessageType().equals("MOVERESOURCE"))
        {
            this.MoveResourceMobileAction(receivedMessage, os);
        }
        else if(receivedMessage.getMessageType().equals("POSITIONREQUEST"))
        {
            this.PositionRequestAction(receivedMessage, os);
        }
        else if(receivedMessage.getMessageType().equals("REGISTERREQUEST"))
        {
            this.RegisterRequestAction(receivedMessage, os);
        }
        else if(receivedMessage.getMessageType().equals("START"))
        {
            this.StartMessageAction(receivedMessage, os);
        }
        else if(receivedMessage.getMessageType().equals("CREATERESOURCE"))
        {
            this.CreateResourceAction(receivedMessage, os);
        }
        else if(receivedMessage.getMessageType().equals("CREATEMOBILERESOURCE"))
        {
            this.CreateMobileResourceAction( receivedMessage,os);
        }
        else if(receivedMessage.getMessageType().equals("INFOGPREQUEST"))
        {
            this.infoGamePeerAction(receivedMessage, os);
        }
        else if(receivedMessage.getMessageType().equals("GPRESOURCESREQ"))
        {
            this.GamePeerResourcesAction(receivedMessage, os);
        }
        else if(receivedMessage.getMessageType().equals("RESOURCEMOBILEBYIDREQUEST"))
        {
            this.MobileResourceFromIDAction(receivedMessage, os);
        }
        else if(receivedMessage.getMessageType().equals("GPVISIONSREQ"))
        {
            this.GamePeerVisionAction(receivedMessage, os);


        }
        else if(receivedMessage.getMessageType().equals("RESOURCEBYIDREQUEST"))
        {
            this.ResourceFromIDAction(receivedMessage, os);
        }
        else if(receivedMessage.getMessageType().equals("CREATERESOURCEEVOLVE"))
        {

            this.CreateResourceEvolveAction(receivedMessage, os);
        }
        else if(receivedMessage.getMessageType().equals("RESOURCESSIZEREQ"))
        {
            this.ResourcesSizeAction(receivedMessage, os);

        }
        else if(receivedMessage.getMessageType().equals("GPLAYERREQ"))
        {
            this.GamePlayerAction(receivedMessage, os);
        }
        else if(receivedMessage.getMessageType().equals("GRANULARITYREQUEST"))
        {
            this.GranularityAction(receivedMessage, os);
        }
        else if(receivedMessage.getMessageType().equals("UPDATERESEVOLVEMESSAGE"))
        {
            this.UpdateResourceEvolveAction(receivedMessage, os);
        }
        else if(receivedMessage.getMessageType().equals("MOVEMENTREQUEST"))
        {
            this.MovementAction(receivedMessage, os);
        }
        else if(receivedMessage.getMessageType().equals("IPADDRESSREQUEST"))
        {
            this.IpAddresRequestAction(receivedMessage, os);
        }






    }


    /*
     * GamePeerIDAction
     * returns the Game Peer's ID
     * Parameters:
     * receivedMessage: Message
     * os: DataOutputStream     *
     */
   public void GamePeerIDAction(Message receivedMessage, DataOutputStream os) throws IOException
   {
            String id=this.gp.getMyId();

            IDMessage idmessage=new IDMessage(id);

            os.write(idmessage.generateXmlMessageString().getBytes());


   }

   /*
    * GamePeerDescriptionAction
    * returns a Game peer's description
    * Parameters:
    * receivedMessage: Message
    * os: DataOutputStream        *
    */
   public void GamePeerDescriptionAction(Message receivedMessage, DataOutputStream os) throws IOException
   {
            String description=this.gp.toString();

            GamePeerDescription gpdesc=new GamePeerDescription(description);

            os.write(gpdesc.generateXmlMessageString().getBytes());
   }


   /*
    * MoveResourceMobileAction
    * Moves a Resource mobile to the new position (x,y,z)
    * Parameters:
    * receivedMessage: Message
    * os: DataOutputStream      *
    */
   public void MoveResourceMobileAction(Message receivedMessage, DataOutputStream os) throws IOException, InterruptedException
   {
           
            MoveResourceMessage moveresourcemessage=new MoveResourceMessage(receivedMessage);

            double x=moveresourcemessage.getX();
            double y=moveresourcemessage.getY();
            double z=moveresourcemessage.getZ();
            String resourceId=moveresourcemessage.getResID();
            String threadId=moveresourcemessage.getThreadID();

            SuccessMessage success;
            if(this.gp.moveResourceMobile(resourceId, x, y, z, threadId))
            {
                success=new SuccessMessage(true);
            }
            else
            {
                success=new SuccessMessage(false);
            }


            os.write(success.generateXmlMessageString().getBytes());



   }

   /*
    * PositionRequestAction
    * returs the current game player's position (x,y)
    * Parameters:
    * receivedMessage: Message
    * os: DataOutputStream  
    */

   public void PositionRequestAction(Message receivedMessage, DataOutputStream os) throws IOException
   {
            double x;
            double y;
            x=gp.getPlayer().getPosX();
            y=gp.getPlayer().getPosY();

            PositionMessage position=new PositionMessage(x,y);

            os.write(position.generateXmlMessageString().getBytes());
   }

   /*
    * RegisterRequestAction
    * logs the new user on the server
    * Parameters:
    * receivedMessage: Message
    * os: DataOutputStream  
    */
   public void RegisterRequestAction(Message receivedMessage, DataOutputStream os) throws IOException
   {
            
            RegisterRequestMessage register=new RegisterRequestMessage(receivedMessage);

            String user=register.getUserName();
            String password=register.getPassword();

            this.gp.registerOnServer(user, password);


            RegisterResponseMessage response=new RegisterResponseMessage("yes");

            os.write(response.generateXmlMessageString().getBytes());

   }

   /*
    * StartMessageAction
    * stars game for the new user
    * Parameters:
    * receivedMessage: Message
    * os: DataOutputStream 
    */
   public void StartMessageAction(Message receivedMessage, DataOutputStream os) throws IOException
   {
               
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

                SuccessMessage success=new SuccessMessage(true);

                os.write(success.generateXmlMessageString().getBytes());

   }

   /*
    * CreateResourceAction
    * Creates a new resource of the game peer
    * Parameters:
    * receivedMessage: Message
    * os: DataOutputStream 
    */
   public void CreateResourceAction(Message receivedMessage, DataOutputStream os) throws IOException
   {
            
            CreateResourceMessage createresource=new CreateResourceMessage(receivedMessage);

            String id=createresource.getResourceID();
            String description=createresource.getDescription();
            double quantity=createresource.getQuantity();

            GameResource resource= new GameResource(id,description,quantity);
            this.gp.addToMyResource(resource);
            this.gp.printVision();
            //System.out.println("RISORSA AGGIUNTA "+ id);

            SuccessMessage success=new SuccessMessage(true);

            os.write(success.generateXmlMessageString().getBytes());
   }

   /*
    * CreateResourceEvolveAction
    * Creates a new resource evolve of the game peer
    * Parameters:
    * receivedMessage: Message
    * os: DataOutputStream     *
    */
   public void CreateResourceEvolveAction(Message receivedMessage, DataOutputStream os) throws IOException
   {
            CreateResourceEvolveMessage createresource=new CreateResourceEvolveMessage(receivedMessage);

            String id=createresource.getResourceID();
            String description=createresource.getDescription();
            double quantity=createresource.getQuantity();
            long period=createresource.getPeriod();
            double offset=createresource.getOffset();


            GameResourceEvolve resource=new GameResourceEvolve(id,description,quantity,period,offset);

            this.gp.addToMyResource(resource);
            //System.out.println("RISORSA AGGIUNTA "+ id);
            //MultiLog.println(GUIMessageListener.class.toString(), "RISORSA AGGIUNTA "+ id );

            SuccessMessage success=new SuccessMessage(true);

            os.write(success.generateXmlMessageString().getBytes());


   }

   /*
    * CreateMobileResourceAction
    * Creates a new resource mobile of the game peer
    * Parameters:
    * receivedMessage: Message
    * os: DataOutputStream 
    */

   public void CreateMobileResourceAction(Message receivedMessage, DataOutputStream os) throws IOException
   {
         
         CreateMobileResourceMessage createresource=new CreateMobileResourceMessage(receivedMessage);
         String type=createresource.getType();
         double quantity=createresource.getQuantity();

         this.gp.createMobileResource(type, quantity);
        // System.out.println("RISORSA mobile AGGIUNTA ");

         SuccessMessage success=new SuccessMessage(true);

         os.write(success.generateXmlMessageString().getBytes());


   }

   /*
    * infoGamePeerAction
    * returns game player's information (id, name position (x,y,z)
    * velocity, visibility )
    * Parameters:
    * receivedMessage: Message
    * os: DataOutputStream 
    */
   public void infoGamePeerAction(Message receivedMessage, DataOutputStream os) throws IOException
   {
        String id=this.gp.getPlayer().getId();
        String name=this.gp.getPlayer().getName();
        double posx=this.gp.getPlayer().getPosX();
        double posy=this.gp.getPlayer().getPosY();
        double posz=this.gp.getPlayer().getPosZ();
        double vel=this.gp.getPlayer().getVelocity();
        double vis=this.gp.getPlayer().getVisibility();

        InfoGamePeerMessage infomsg=new InfoGamePeerMessage(id,name,posx,posy,posz,vel,vis);
        os.write(infomsg.generateXmlMessageString().getBytes());
   }

   /*
    * GamePeerResourcesAction
    * Returns the game peer's resources list
    * Parameters:
    * receivedMessage: Message
    * os: DataOutputStream
    */
   public void GamePeerResourcesAction(Message receivedMessage, DataOutputStream os) throws IOException{

        ArrayList<Object> Resources = new ArrayList<Object>();

        Resources=this.gp.getMyResources();
        this.gp.printVision();


        int n=Resources.size();
        Object resource;

        String str_resources="";

        /*
         * this loop creates a String with all the game peer's resources
         *
         */

        for(int i=0;i<n;i++)
        {
            resource=Resources.get(i);


            if(resource instanceof GameResourceEvolve)
            {
                GameResourceEvolve gre=(GameResourceEvolve)resource;
                 String id=gre.getId();
                 String description=gre.getDescription();
                 double quantity=gre.getQuantity();
                 double period=gre.getPeriod();
                 double offset=gre.getOffset();

                 str_resources+="GameResourceEvolve";
                 str_resources+="#";

                 str_resources+=id;
                 str_resources+="#";

                 str_resources+=description;
                 str_resources+="#";

                 str_resources+=Double.toString(quantity);
                 str_resources+="#";

                 str_resources+=Double.toString(period);
                 str_resources+="#";

                 str_resources+=Double.toString(offset);

            }
            else if(resource instanceof GameResourceMobile)
            {
                GameResourceMobile grm=(GameResourceMobile)resource;
                String id=grm.getId();
                String description=grm.getDescription();
                double quantity=grm.getQuantity();
                double x=grm.getX();
                double y=grm.getY();
                double z=grm.getZ();
                double velocity=grm.getVelocity();
                double vision=grm.getVision();
                String owner=grm.getOwner();
                String ownerid=grm.getOwnerId();

                ArrayList<Object> resourcevision=grm.getResourceVision();

                str_resources+="GameResourceMobile";
                str_resources+="#";

                str_resources+=id;
                str_resources+="#";

                str_resources+=description;
                str_resources+="#";

                str_resources+=Double.toString(quantity);
                str_resources+="#";

                str_resources+=Double.toString(x);
                str_resources+="#";

                str_resources+=Double.toString(y);
                str_resources+="#";

                str_resources+=Double.toString(z);
                str_resources+="#";

                str_resources+=Double.toString(velocity);
                str_resources+="#";

                str_resources+=Double.toString(vision);
                str_resources+="#";

                str_resources+=owner;
                str_resources+="#";

                str_resources+=ownerid;
                str_resources+="#";

                int nr=resourcevision.size();

                String str_rvision="";
                Object res;

                /*
                 * If it's a resource mobile, then this loop creates a String with all
                 * the mobile resource's vision information
                 */
                for(int j=0;j<nr;j++)
                {
                    res=resourcevision.get(j);

                    if(res instanceof GamePlayerResponsible)
                    {
                        GamePlayerResponsible gpr=(GamePlayerResponsible) res;

                        int pos=j;
                        String vid=gpr.getId();
                        String vname=gpr.getName();
                        double vx=gpr.getPosX();
                        double vy=gpr.getPosY();
                        double vz=gpr.getPosZ();
                        double vvel=gpr.getVelocity();
                        double vvis=gpr.getVisibility();
                        long vtime=gpr.getTimestamp();
                        String poshash=gpr.getPositionHash();
                        String oldpos=gpr.getOldPos();





                        str_rvision+="GamePlayerResponsible";
                        str_rvision+=";";

                        str_rvision+=Integer.toString(pos);
                        str_rvision+=";";

                        str_rvision+=vid;
                        str_rvision+=";";

                        str_rvision+=vname;
                        str_rvision+=";";

                        str_rvision+=Double.toString(vx);
                        str_rvision+=";";

                        str_rvision+=Double.toString(vy);
                        str_rvision+=";";

                        str_rvision+=Double.toString(vz);
                        str_rvision+=";";

                        str_rvision+=Double.toString(vvel);
                        str_rvision+=";";

                        str_rvision+=Double.toString(vvis);
                        str_rvision+=";";

                        str_rvision+=Long.toString(vtime);
                        str_rvision+=";";

                        str_rvision+=poshash;
                        str_rvision+=";";

                        str_rvision+=oldpos;


                    }
                    else if(res instanceof GameResourceMobileResponsible)
                    {
                        GameResourceMobileResponsible grmr=(GameResourceMobileResponsible)res;

                        int pos =j;
                        String vid=grmr.getId();
                        String vdesc=grmr.getDescription();
                        String vowner=grmr.getOwner();
                        String vownerid=grmr.getOwnerId();
                        double vq=grmr.getQuantity();
                        double vx=grmr.getX();
                        double vy=grmr.getY();
                        double vz=grmr.getZ();
                        double vvel=grmr.getVelocity();
                        double vvis=grmr.getVision();
                        long vtime=grmr.getTimestamp();
                        String poshash=grmr.getPositionHash();
                        String oldpos=grmr.getOldPos();


                        str_rvision+="GameResourceMobileResponsible";
                        str_rvision+=";";

                        str_rvision+=Integer.toString(pos);
                        str_rvision+=";";

                        str_rvision+=vid;
                        str_rvision+=";";

                        str_rvision+=vdesc;
                        str_rvision+=";";

                        str_rvision+=vowner;
                        str_rvision+=";";

                        str_rvision+=vownerid;
                        str_rvision+=";";

                        str_rvision+=Double.toString(vq);
                        str_rvision+=";";

                        str_rvision+=Double.toString(vx);
                        str_rvision+=";";

                        str_rvision+=Double.toString(vy);
                        str_rvision+=";";

                        str_rvision+=Double.toString(vz);
                        str_rvision+=";";

                        str_rvision+=Double.toString(vvel);
                        str_rvision+=";";

                        str_rvision+=Double.toString(vvis);
                        str_rvision+=";";

                        str_rvision+=Long.toString(vtime);
                        str_rvision+=";";

                        str_rvision+=poshash;
                        str_rvision+=";";

                        str_rvision+=oldpos;


                    }
                    else
                    {
                    	
                    	int pos=j;
                    	str_rvision+="null";
                        str_rvision+=";";

                        str_rvision+=Integer.toString(pos);
                        

                    	
                    }

                    if(j!=nr-1) // serves to not add the token at the end of the String
                    {
                        str_rvision+="$";
                    }


                }


                
                //add the resource's vision string to the resource's string
                //System.out.println("RESOURCE_VISION LISTENER "+ str_rvision);
                //this.check.print_msg(GUIMessageListener.class.getName(), "RESOURCE_VISION LISTENER "+ str_rvision);
                str_resources+=str_rvision;

            }
            else if(resource instanceof GameResource)
            {
                GameResource gr=(GameResource)resource;
                String id=gr.getId();
                String description=gr.getDescription();
                double quantity=gr.getQuantity();

                str_resources+="GameResource";
                str_resources+="#";

                str_resources+=id;
                str_resources+="#";

                str_resources+=description;
                str_resources+="#";

                str_resources+=Double.toString(quantity);

            }

            if(i!=n-1) // serves to not add the token at the end of the String
            {
                str_resources+=":";
            }
        }

       // System.out.println("\n\n"+str_resources);

        GamePeerResources response= new GamePeerResources(str_resources);
        os.write(response.generateXmlMessageString().getBytes());


   }

   /*
    * MobileResourceFromIDAction
    * returns a game peer'r mobile resource by ID
    *  Parameters:
    * receivedMessage: Message
    * os: DataOutputStream     
    */

   public void MobileResourceFromIDAction(Message receivedMessage, DataOutputStream os) throws IOException
   {
        
        MobileResourceFromIDRequestMessage request=new MobileResourceFromIDRequestMessage(receivedMessage);

        String id=request.getResourceID();
        String str_resources="";
        GameResourceMobile grm=this.gp.getMyMobileResourceFromId(id);

        /*
         * creates a String with the mobile resource's structure
         */

        String description=grm.getDescription();
                double quantity=grm.getQuantity();
                double x=grm.getX();
                double y=grm.getY();
                double z=grm.getZ();
                double velocity=grm.getVelocity();
                double vision=grm.getVision();
                String owner=grm.getOwner();
                String ownerid=grm.getOwnerId();

                ArrayList<Object> resourcevision=grm.getResourceVision();

                str_resources+="GameResourceMobile";
                str_resources+="#";

                str_resources+=id;
                str_resources+="#";

                str_resources+=description;
                str_resources+="#";

                str_resources+=Double.toString(quantity);
                str_resources+="#";

                str_resources+=Double.toString(x);
                str_resources+="#";

                str_resources+=Double.toString(y);
                str_resources+="#";

                str_resources+=Double.toString(z);
                str_resources+="#";

                str_resources+=Double.toString(velocity);
                str_resources+="#";

                str_resources+=Double.toString(vision);
                str_resources+="#";

                str_resources+=owner;
                str_resources+="#";

                str_resources+=ownerid;
                str_resources+="#";

                int nr=resourcevision.size();

                String str_rvision="";
                Object res;
                /*
                 * this loop creates a String with all
                 * the mobile resource's vision information
                 */
                for(int j=0;j<nr;j++)
                {
                    res=resourcevision.get(j);

                    if(res instanceof GamePlayerResponsible)
                    {
                        GamePlayerResponsible gpr=(GamePlayerResponsible) res;

                        int pos=j;
                        String vid=gpr.getId();
                        String vname=gpr.getName();
                        double vx=gpr.getPosX();
                        double vy=gpr.getPosY();
                        double vz=gpr.getPosZ();
                        double vvel=gpr.getVelocity();
                        double vvis=gpr.getVisibility();
                        long vtime=gpr.getTimestamp();
                        String poshash=gpr.getPositionHash();
                        String oldpos=gpr.getOldPos();

                        str_rvision+="GamePlayerResponsible";
                        str_rvision+=";";

                        str_rvision+=Integer.toString(pos);
                        str_rvision+=";";

                        str_rvision+=vid;
                        str_rvision+=";";

                        str_rvision+=vname;
                        str_rvision+=";";

                        str_rvision+=Double.toString(vx);
                        str_rvision+=";";

                        str_rvision+=Double.toString(vy);
                        str_rvision+=";";

                        str_rvision+=Double.toString(vz);
                        str_rvision+=";";

                        str_rvision+=Double.toString(vvel);
                        str_rvision+=";";

                        str_rvision+=Double.toString(vvis);
                        str_rvision+=";";

                        str_rvision+=Long.toString(vtime);
                        str_rvision+=";";

                        str_rvision+=poshash;
                        str_rvision+=";";

                        str_rvision+=oldpos;
                        


                    }
                    else if(res instanceof GameResourceMobileResponsible)
                    {
                        GameResourceMobileResponsible grmr=(GameResourceMobileResponsible)res;

                        int pos =j;
                        String vid=grmr.getId();
                        String vdesc=grmr.getDescription();
                        String vowner=grmr.getOwner();
                        String vownerid=grmr.getOwnerId();
                        double vq=grmr.getQuantity();
                        double vx=grmr.getX();
                        double vy=grmr.getY();
                        double vz=grmr.getZ();
                        double vvel=grmr.getVelocity();
                        double vvis=grmr.getVision();
                        long vtime=grmr.getTimestamp();
                        String poshash=grmr.getPositionHash();
                        String oldpos=grmr.getOldPos();


                        str_rvision+="GameResourceMobileResponsible";
                        str_rvision+=";";

                        str_rvision+=Integer.toString(pos);
                        str_rvision+=";";

                        str_rvision+=vid;
                        str_rvision+=";";

                        str_rvision+=vdesc;
                        str_rvision+=";";

                        str_rvision+=vowner;
                        str_rvision+=";";

                        str_rvision+=vownerid;
                        str_rvision+=";";

                        str_rvision+=Double.toString(vq);
                        str_rvision+=";";

                        str_rvision+=Double.toString(vx);
                        str_rvision+=";";

                        str_rvision+=Double.toString(vy);
                        str_rvision+=";";

                        str_rvision+=Double.toString(vz);
                        str_rvision+=";";

                        str_rvision+=Double.toString(vvel);
                        str_rvision+=";";

                        str_rvision+=Double.toString(vvis);
                        str_rvision+=";";

                        str_rvision+=Long.toString(vtime);
                        str_rvision+=";";

                        str_rvision+=poshash;
                        str_rvision+=";";

                        str_rvision+=oldpos;


                    }
                    else
                    {
                    	
                    	int pos=j;
                    	str_rvision+="null";
                        str_rvision+=";";

                        str_rvision+=Integer.toString(pos);
                        

                    	
                    }

                    if(j!=nr-1) // serves to not add the token at the end of the String
                    {
                        str_rvision+="$";
                    }


                }


                //adds the resource's vision string to the resource's string
                str_resources+=str_rvision;

                //System.out.println("RESOURCE_VISION LISTENER "+ str_rvision);
                //this.check.print_msg(GUIMessageListener.class.getName(), "RESOURCE_VISION LISTENER "+ str_rvision);

                MobileResourceFromIDMessage res_message=new MobileResourceFromIDMessage(str_resources);

                os.write(res_message.generateXmlMessageString().getBytes());


   }

   /*
    * ResourceFromIDAction
    * Returns a game peer's rosurce by ID
    * Parameters:
    * receivedMessage: Message
    * os: DataOutputStream
    */
   public void ResourceFromIDAction(Message receivedMessage, DataOutputStream os) throws IOException
   {
            ResourceFromIDRequestMessage req=new ResourceFromIDRequestMessage(receivedMessage);

            String r_id=req.getResourceID();

            GameResource resource=this.gp.getMyResourceFromId(r_id);
            /*
             * creates a String with the resource's structure
             */

            String str_resources="";
            if(resource instanceof GameResourceEvolve)
            {
                GameResourceEvolve gre=(GameResourceEvolve)resource;
                 String id=gre.getId();
                 String description=gre.getDescription();
                 double quantity=gre.getQuantity();
                 double period=gre.getPeriod();
                 double offset=gre.getOffset();

                 str_resources+="GameResourceEvolve";
                 str_resources+="#";

                 str_resources+=id;
                 str_resources+="#";

                 str_resources+=description;
                 str_resources+="#";

                 str_resources+=Double.toString(quantity);
                 str_resources+="#";

                 str_resources+=Double.toString(period);
                 str_resources+="#";

                 str_resources+=Double.toString(offset);
            }
            else if(resource instanceof GameResourceMobile)
            {
                                GameResourceMobile grm=(GameResourceMobile)resource;
                String id=grm.getId();
                String description=grm.getDescription();
                double quantity=grm.getQuantity();
                double x=grm.getX();
                double y=grm.getY();
                double z=grm.getZ();
                double velocity=grm.getVelocity();
                double vision=grm.getVision();
                String owner=grm.getOwner();
                String ownerid=grm.getOwnerId();

                ArrayList<Object> resourcevision=grm.getResourceVision();

                str_resources+="GameResourceMobile";
                str_resources+="#";

                str_resources+=id;
                str_resources+="#";

                str_resources+=description;
                str_resources+="#";

                str_resources+=Double.toString(quantity);
                str_resources+="#";

                str_resources+=Double.toString(x);
                str_resources+="#";

                str_resources+=Double.toString(y);
                str_resources+="#";

                str_resources+=Double.toString(z);
                str_resources+="#";

                str_resources+=Double.toString(velocity);
                str_resources+="#";

                str_resources+=Double.toString(vision);
                str_resources+="#";

                str_resources+=owner;
                str_resources+="#";

                str_resources+=ownerid;
                str_resources+="#";

                int nr=resourcevision.size();

                String str_rvision="";
                Object res;
                /*
                 * If it's a resource mobile, this loop creates a String with
                 * the rsource mobile's vision information list
                 */
                for(int j=0;j<nr;j++)
                {
                    res=resourcevision.get(j);

                    if(res instanceof GamePlayerResponsible)
                    {
                        GamePlayerResponsible gpr=(GamePlayerResponsible) res;

                        int pos=j;
                        String vid=gpr.getId();
                        String vname=gpr.getName();
                        double vx=gpr.getPosX();
                        double vy=gpr.getPosY();
                        double vz=gpr.getPosZ();
                        double vvel=gpr.getVelocity();
                        double vvis=gpr.getVisibility();
                        long vtime=gpr.getTimestamp();
                        String poshash=gpr.getPositionHash();
                        String oldpos=gpr.getOldPos();





                        str_rvision+="GamePlayerResponsible";
                        str_rvision+=";";

                        str_rvision+=Integer.toString(pos);
                        str_rvision+=";";

                        str_rvision+=vid;
                        str_rvision+=";";

                        str_rvision+=vname;
                        str_rvision+=";";

                        str_rvision+=Double.toString(vx);
                        str_rvision+=";";

                        str_rvision+=Double.toString(vy);
                        str_rvision+=";";

                        str_rvision+=Double.toString(vz);
                        str_rvision+=";";

                        str_rvision+=Double.toString(vvel);
                        str_rvision+=";";

                        str_rvision+=Double.toString(vvis);
                        str_rvision+=";";

                        str_rvision+=Long.toString(vtime);
                        str_rvision+=";";

                        str_rvision+=poshash;
                        str_rvision+=";";

                        str_rvision+=oldpos;


                    }
                    else if(res instanceof GameResourceMobileResponsible)
                    {
                        GameResourceMobileResponsible grmr=(GameResourceMobileResponsible)res;

                        int pos =j;
                        String vid=grmr.getId();
                        String vdesc=grmr.getDescription();
                        String vowner=grmr.getOwner();
                        String vownerid=grmr.getOwnerId();
                        double vq=grmr.getQuantity();
                        double vx=grmr.getX();
                        double vy=grmr.getY();
                        double vz=grmr.getZ();
                        double vvel=grmr.getVelocity();
                        double vvis=grmr.getVision();
                        long vtime=grmr.getTimestamp();
                        String poshash=grmr.getPositionHash();
                        String oldpos=grmr.getOldPos();


                        str_rvision+="GameResourceMobileResponsible";
                        str_rvision+=";";

                        str_rvision+=Integer.toString(pos);
                        str_rvision+=";";

                        str_rvision+=vid;
                        str_rvision+=";";

                        str_rvision+=vdesc;
                        str_rvision+=";";

                        str_rvision+=vowner;
                        str_rvision+=";";

                        str_rvision+=vownerid;
                        str_rvision+=";";

                        str_rvision+=Double.toString(vq);
                        str_rvision+=";";

                        str_rvision+=Double.toString(vx);
                        str_rvision+=";";

                        str_rvision+=Double.toString(vy);
                        str_rvision+=";";

                        str_rvision+=Double.toString(vz);
                        str_rvision+=";";

                        str_rvision+=Double.toString(vvel);
                        str_rvision+=";";

                        str_rvision+=Double.toString(vvis);
                        str_rvision+=";";

                        str_rvision+=Long.toString(vtime);
                        str_rvision+=";";

                        str_rvision+=poshash;
                        str_rvision+=";";

                        str_rvision+=oldpos;


                    }
                    else
                    {
                    	
                    	int pos=j;
                    	str_rvision+="null";
                        str_rvision+=";";

                        str_rvision+=Integer.toString(pos);
                        

                    	
                    }

                    if(j!=nr-1) // serves to not add the token at the end of the String
                    {
                        str_rvision+="$";
                    }


                }


                //adds the resource's vision string to the resource's string
                str_resources+=str_rvision;
            }
            else if(resource instanceof GameResource)
            {
                GameResource gr=(GameResource)resource;
                String id=gr.getId();
                String description=gr.getDescription();
                double quantity=gr.getQuantity();

                str_resources+="GameResource";
                str_resources+="#";

                str_resources+=id;
                str_resources+="#";

                str_resources+=description;
                str_resources+="#";

                str_resources+=Double.toString(quantity);
            }

           // System.out.println(str_resources);

            ResourceFromIDMessage response=new ResourceFromIDMessage(str_resources);

            os.write(response.generateXmlMessageString().getBytes());
   }

   /*
    * GamePeerVisionAction
    * Returns a game peer's vision information list
    * Parameters:
    * receivedMessage: Message
    * os: DataOutputStream
    */
   public void GamePeerVisionAction(Message receivedMessage, DataOutputStream os) throws IOException
   {
     ArrayList<Object> vision=gp.getVision();

     int nr=vision.size();

     String str_rvision="";
     Object res;
                /*
                 * this loop creates a String with the game peer's vision information list
                 */
                for(int j=0;j<nr;j++)
                {
                    res=vision.get(j);

                    if(res instanceof GamePlayerResponsible)
                    {
                        GamePlayerResponsible gpr=(GamePlayerResponsible) res;

                        int pos=j;
                        String vid=gpr.getId();
                        String vname=gpr.getName();
                        double vx=gpr.getPosX();
                        double vy=gpr.getPosY();
                        double vz=gpr.getPosZ();
                        double vvel=gpr.getVelocity();
                        double vvis=gpr.getVisibility();
                        long vtime=gpr.getTimestamp();
                        String poshash=gpr.getPositionHash();
                        String oldpos=gpr.getOldPos();

                        str_rvision+="GamePlayerResponsible";
                        str_rvision+=";";

                        str_rvision+=Integer.toString(pos);
                        str_rvision+=";";

                        str_rvision+=vid;
                        str_rvision+=";";

                        str_rvision+=vname;
                        str_rvision+=";";

                        str_rvision+=Double.toString(vx);
                        str_rvision+=";";

                        str_rvision+=Double.toString(vy);
                        str_rvision+=";";

                        str_rvision+=Double.toString(vz);
                        str_rvision+=";";

                        str_rvision+=Double.toString(vvel);
                        str_rvision+=";";

                        str_rvision+=Double.toString(vvis);
                        str_rvision+=";";

                        str_rvision+=Long.toString(vtime);
                        str_rvision+=";";

                        str_rvision+=poshash;
                        str_rvision+=";";

                        str_rvision+=oldpos;


                    }
                    else if(res instanceof GameResourceMobileResponsible)
                    {
                        GameResourceMobileResponsible grmr=(GameResourceMobileResponsible)res;

                        int pos =j;
                        String vid=grmr.getId();
                        String vdesc=grmr.getDescription();
                        String vowner=grmr.getOwner();
                        String vownerid=grmr.getOwnerId();
                        double vq=grmr.getQuantity();
                        double vx=grmr.getX();
                        double vy=grmr.getY();
                        double vz=grmr.getZ();
                        double vvel=grmr.getVelocity();
                        double vvis=grmr.getVision();
                        long vtime=grmr.getTimestamp();
                        String poshash=grmr.getPositionHash();
                        String oldpos=grmr.getOldPos();


                        str_rvision+="GameResourceMobileResponsible";
                        str_rvision+=";";

                        str_rvision+=Integer.toString(pos);
                        str_rvision+=";";

                        str_rvision+=vid;
                        str_rvision+=";";

                        str_rvision+=vdesc;
                        str_rvision+=";";

                        str_rvision+=vowner;
                        str_rvision+=";";

                        str_rvision+=vownerid;
                        str_rvision+=";";

                        str_rvision+=Double.toString(vq);
                        str_rvision+=";";

                        str_rvision+=Double.toString(vx);
                        str_rvision+=";";

                        str_rvision+=Double.toString(vy);
                        str_rvision+=";";

                        str_rvision+=Double.toString(vz);
                        str_rvision+=";";

                        str_rvision+=Double.toString(vvel);
                        str_rvision+=";";

                        str_rvision+=Double.toString(vvis);
                        str_rvision+=";";

                        str_rvision+=Long.toString(vtime);
                        str_rvision+=";";

                        str_rvision+=poshash;
                        str_rvision+=";";

                        str_rvision+=oldpos;


                    }

                    if(j!=nr-1) // serves to not add the token at the end of the String
                    {
                        str_rvision+="$";
                    }


                }

    // System.out.println(str_rvision);

     GamePeerVision gpv=new GamePeerVision(str_rvision);

     os.write(gpv.generateXmlMessageString().getBytes());





   }

  /*
   * ResourcesSizeAction
   * Returns the number of game peer resurces
   * Parameters:
   * receivedMessage: Message
   * os: DataOutputStream
   */
   public void ResourcesSizeAction(Message receivedMessage, DataOutputStream os) throws IOException
   {
        //ResourcesSizeRequestMessage req=new ResourcesSizeRequestMessage(receivedMessage);

        int size=this.gp.getMyResources().size();

        ResourcesSizeMessage resp=new ResourcesSizeMessage(size);

        os.write(resp.generateXmlMessageString().getBytes());

   }

   /*
    * GamePlayerAction
    * Returns the game player informations (id, name position (x,y,z), velocity, visibility)
    * Parameters:
    * receivedMessage: Message
    * os: DataOutputStream
    */
   public void GamePlayerAction(Message receivedMessage, DataOutputStream os) throws IOException
   {
        GamePlayer gplayer=this.gp.getPlayer();

        String id=gplayer.getId();
        String name=gplayer.getName();
        double posx=gplayer.getPosX();
        double posy=gplayer.getPosY();
        double posz=gplayer.getPosZ();
        double vel=gplayer.getVelocity();
        double vis=gplayer.getVisibility();

        GamePlayerMessage message=new GamePlayerMessage(id,name,posx,posy,posz,vel,vis);

        os.write(message.generateXmlMessageString().getBytes());


   }

   /*
    * GranularityAction
    * Returns the world's granularity
    * Parameters:
    * receivedMessage: Message
    * os: DataOutputStream
    */
   public void GranularityAction(Message receivedMessage, DataOutputStream os) throws IOException
   {
        double gran=this.gp.getWorld().getGranularity();

        GranularityMessage granmsg=new GranularityMessage(gran);

        os.write(granmsg.generateXmlMessageString().getBytes());
   }

   /*
    * UpdateResourceEvolveAction
    * Updates the resource evolve information (quantity), by ID
    * Parameters:
    * receivedMessage: Message
    * os: DataOutputStream
    */
   public void UpdateResourceEvolveAction(Message receivedMessage, DataOutputStream os) throws IOException
   {
        UpdateResourceEvolveMessage msg=new UpdateResourceEvolveMessage(receivedMessage);

        double qt=msg.getQuantity();

        this.gp.getMyResourceFromId("moneyEvolveble").setQuantity(qt);

        SuccessMessage message=new SuccessMessage(true);

        os.write(message.generateXmlMessageString().getBytes());
   }

   /*
    * MovementAction
    * creates and launches a thread to perform the movement of the mobile resource
    * Parameters:
    * receivedMessage: Message
    * os: DataOutputStream
    */
   public void MovementAction(Message receivedMessage, DataOutputStream os) throws IOException
   {
        final MovementRequestMessage request=new MovementRequestMessage(receivedMessage);

        SuccessMessage message=new SuccessMessage(true); 


      
       
        //creates a thread
        Thread move=new Thread(new Runnable(){

            public void run() { //implements a run method of runnable

                    

                    try {

                        double target_x=request.getX(); //poition target
                        double target_y=request.getY();
                        String resId=request.getResID(); // resource id
                        String threadId=request.getThreadID(); // thread id

                        GameResourceMobile res = gp.getMyMobileResourceFromId(resId);

                        while((res.getX()!=target_x )|| (res.getY()!=target_y)) //while the current resource mobile postion is different form the target position
                        {
                            Thread.sleep(500);
                            double movX = target_x - res.getX(); //movement distance x
			    double movY = target_y - res.getY(); //movement distance y

                            if (movX != 0.0){ // if the resource x postion  is different from the target x position
				if (movX > 0.0 && movX > res.getVelocity() / 2.0){ //move in the right direction
					movX = res.getVelocity() / 2.0;
				}
				else if (movX < 0.0 && Math.abs(movX) > res.getVelocity() / 2.0){ // move in the left direction
					movX = - (res.getVelocity() / 2.0);
				}
                            }

                            if (movY != 0.0){ // if the resource y postion  is different from the target y position
				if (movY > 0.0 && movY > res.getVelocity() / 2.0){
					movY = res.getVelocity() / 2.0;
				}
				else if (movY < 0.0 && Math.abs(movY) > res.getVelocity() / 2.0){
					movY = - (res.getVelocity() / 2.0);
				}
                            }

                            MultiLog.println(GUIMessageListener.class.toString(), "RISORSA MOSSA di " + movX + " , " + movY);

                            if( (movX + movY) > res.getVelocity() ){
				MultiLog.println(GUIMessageListener.class.toString(), "FINE PER MOVIMENTO ECCESSIVO ------------------------------------------------" + res.getVelocity());
				
				System.exit(1);
			    }

                            gp.moveResourceMobile(resId, movX, movY, 0, threadId);

                            
                           // System.out.println("MOV_RESOURCE "+ res.getX() + "  "+res.getY());
                        }

                    } catch (InterruptedException ex) {
                        Logger.getLogger(GUIMessageListener.class.getName()).log(Level.SEVERE, null, ex);
                    }



            }


        }
                );

                
                move.start();

       os.write(message.generateXmlMessageString().getBytes());




   }

   /*
    * IpAddresRequestAction
    * Returns the game peer's IP address
    * Parameters:
    * receivedMessage: Message
    * os: DataOutputStream
    */
   public void IpAddresRequestAction(Message receivedMessage, DataOutputStream os) throws IOException
   {

       String ipAddress=this.gp.getMyPeer().getIpAddress();

       IpAddressMessage ipAdd=new IpAddressMessage(ipAddress);

       os.write(ipAdd.generateXmlMessageString().getBytes());


   }



public static void main(String [] arg)
{
    GUIMessageListener listener=new GUIMessageListener(null);
    listener.start();

}



}


