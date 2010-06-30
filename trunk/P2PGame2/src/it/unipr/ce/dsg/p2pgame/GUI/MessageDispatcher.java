/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package it.unipr.ce.dsg.p2pgame.GUI;

/**
 *
 * @author pelito
 */
import it.simplexml.message.Message;
import it.simplexml.message.MessageReader;
import it.unipr.ce.dsg.p2pgame.GUI.message.*;
import it.unipr.ce.dsg.p2pgame.GUI.message.content.GamePeerInfo;
import it.unipr.ce.dsg.p2pgame.GUI.message.content.Point;
import it.unipr.ce.dsg.p2pgame.platform.GamePlayerResponsible;
import it.unipr.ce.dsg.p2pgame.platform.GameResource;
import it.unipr.ce.dsg.p2pgame.platform.GameResourceEvolve;
import it.unipr.ce.dsg.p2pgame.platform.GameResourceMobile;
import it.unipr.ce.dsg.p2pgame.platform.GameResourceMobileResponsible;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;

public class MessageDispatcher{

    private Socket socket;

    public MessageDispatcher()
    {


    }

    private String sendMessage(Message message) throws UnknownHostException, IOException
    {
        this.socket=new Socket("jose",1000); // da modificare il nome del pc

        DataInputStream is = new DataInputStream(socket.getInputStream());
	DataOutputStream os = new DataOutputStream(socket.getOutputStream());

        os.write(message.generateXmlMessageString().getBytes());
        byte buffer[]=new byte[100000];

	is.read(buffer);

        String response=new String(buffer);

        return response;
    }

    public String getGamePeerId() throws UnknownHostException, IOException
    {
       

        Message message=new IDRequestMessage();

        String response=this.sendMessage(message);

       

        MessageReader messageReader = new MessageReader();
        Message receivedMessage = messageReader.readMessageFromString(response.trim());
        if (receivedMessage.getMessageType().equals("PEERID"))
        {
            //IDMessage idmessage=(IDMessage)receivedMessage;
            IDMessage idmessage=new IDMessage(receivedMessage);

            return idmessage.getID();
        }

   
        return null;

    }

    public String getGamePeerDescription() throws UnknownHostException, IOException
    {
        Message message=new GamePeerDescriptionRequest();

        String response=this.sendMessage(message);

        MessageReader messageReader = new MessageReader();
        Message receivedMessage = messageReader.readMessageFromString(response.trim());

        if (receivedMessage.getMessageType().equals("GPDESC"))
        {
            //GamePeerDescription description=(GamePeerDescription)receivedMessage;
            GamePeerDescription description=new GamePeerDescription(receivedMessage);


            return description.getDescription();
        }



        return null;
    }

    public boolean moveResourceMobile(String resId,double movX,double movY,double movZ,String threadId) throws UnknownHostException, IOException
    {
        Message message=new MoveResourceMessage(resId,movX,movY,movZ,threadId);

        String response=this.sendMessage(message);

        MessageReader messageReader = new MessageReader();
        Message receivedMessage = messageReader.readMessageFromString(response.trim());


         if (receivedMessage.getMessageType().equals("SUCCESSMESSAGE"))
         {
            //SuccessMessage success=(SuccessMessage)receivedMessage;
             SuccessMessage success=new SuccessMessage(receivedMessage);

            return success.getSuccess();

         }

         return false;

    }
    
    public Point getGamePlayerPosition() throws UnknownHostException, IOException
    {
        Message message=new PositionRequestMessage();
        
        String response=this.sendMessage(message);
        
        MessageReader messageReader=new MessageReader();
        Message receivedMessage = messageReader.readMessageFromString(response.trim());

        Point point=null;
        if(receivedMessage.getMessageType().equals("POSITION"))
        {
            //PositionMessage position=(PositionMessage)receivedMessage;
            PositionMessage position=new PositionMessage(receivedMessage);
            
            point=new Point(position.getX(),position.getY());
        
        }

        return point;
    
    }

    public void registerOnServer(String username, String password) throws UnknownHostException, IOException
    {
        Message message=new RegisterRequestMessage(username,password);


        String response=this.sendMessage(message);

        MessageReader messageReader=new MessageReader();
        Message receivedMessage = messageReader.readMessageFromString(response.trim());
        if(receivedMessage.getMessageType().equals("REGISTERRESPONSE"))
        {
            //....

        }


    }

    public void startGame(double minX, double maxX, double minY, double maxY, double minZ, double maxZ, double vel, double vis, double gran) throws UnknownHostException, IOException
    {
        Message message=new StartMessage(minX,maxX,minY,maxY,minZ,maxZ, vel,vis,gran);

        String response=this.sendMessage(message);

        MessageReader messageReader=new MessageReader();
        Message receivedMessage = messageReader.readMessageFromString(response.trim());

         if(receivedMessage.getMessageType().equals("SUCCESSMESSAGE"))
        {
            //....

        }


    }

    public void Multilog(String id,String text) throws UnknownHostException, IOException
    {
        Message message=new MultilogMessage(id,text);
        String response=this.sendMessage(message);

        MessageReader messageReader=new MessageReader();
        Message receivedMessage = messageReader.readMessageFromString(response.trim());

         if(receivedMessage.getMessageType().equals("SUCCESSMESSAGE"))
        {
            //....

        }

    }

    public void addResource(String id, String description, double quantity) throws UnknownHostException, IOException
    {
        Message message=new CreateResourceMessage(id, description,quantity);
        String response=this.sendMessage(message);

        MessageReader messageReader=new MessageReader();
        Message receivedMessage = messageReader.readMessageFromString(response.trim());

         if(receivedMessage.getMessageType().equals("SUCCESSMESSAGE"))
        {
            //....

        }


    }

    public void createMobileResource(String type, double qt) throws UnknownHostException, IOException
    {
        Message message=new CreateMobileResourceMessage(type,qt);
        String response=this.sendMessage(message);

        MessageReader messageReader=new MessageReader();
        Message receivedMessage = messageReader.readMessageFromString(response.trim());

         if(receivedMessage.getMessageType().equals("SUCCESSMESSAGE"))
        {
            //....

        }

    }

    public ArrayList<Object> getResources() throws UnknownHostException, IOException
    {

        Message message=new GamePeerResourcesRequest();
        String response= this.sendMessage(message);

        MessageReader messageReader=new MessageReader();
        Message receivedMessage = messageReader.readMessageFromString(response.trim());

        if(receivedMessage.getMessageType().equals("GPRESOURCES"))
        {
            //....
            ArrayList<Object> Resources = new ArrayList<Object>();
            //GamePeerResources gpr=(GamePeerResources)receivedMessage;
            GamePeerResources gpr=new GamePeerResources(receivedMessage);
            String resources=gpr.getResources();

            String []Array_resources=resources.split("||");
            int n=Array_resources.length;

            for(int i=0;i<n;i++)
            {
                String []tmp=Array_resources[i].split("#");

                if(tmp[0].equals("GameResource"))
                {
                    String id=tmp[1];
                    String description=tmp[2];
                    double quantity=Double.parseDouble(tmp[3]);

                    GameResource gr=new GameResource(id,description,quantity);

                    Resources.add(gr);

                }
                else if(tmp[0].equals("GameResourceEvolve"))
                {
                    String id=tmp[1];
                    String description=tmp[2];
                    double quantity=Double.parseDouble(tmp[3]);
                    long period=Long.parseLong(tmp[4]);
                    double offset=Double.parseDouble(tmp[5]);

                    GameResourceEvolve gre=new GameResourceEvolve(id,description,quantity,period,offset);

                    Resources.add(gre);
                }
                else if(tmp[0].equals("GameResourceMobile"))
                {
                    String id=tmp[1];
                    String description=tmp[2];
                    double quantity=Double.parseDouble(tmp[3]);
                    double x=Double.parseDouble(tmp[4]);
                    double y=Double.parseDouble(tmp[5]);
                    double z=Double.parseDouble(tmp[6]);
                    double velocity=Double.parseDouble(tmp[7]);
                    double vision=Double.parseDouble(tmp[8]);
                    String owner=tmp[9];
                    String ownerid=tmp[10];
                    String rvision=tmp[11];

                    GameResourceMobile grm=new GameResourceMobile(id,description,owner,ownerid,quantity,x,y,z,velocity,vision);



                    String []Array_v=rvision.split("*");
                    int nv=Array_v.length;

                    for(int j=0;j<nv;j++)
                    {
                        String [] Array_vision=Array_v[j].split("\\");
                        String type=Array_vision[0];

                        if(type.equals("GamePlayerResponsible"))
                        {
                            int pos=Integer.parseInt(Array_vision[1]);
                            String vid=Array_vision[2];
                            String vname=Array_vision[3];
                            double vx=Double.parseDouble(Array_vision[4]);
                            double vy=Double.parseDouble(Array_vision[5]);
                            double vz=Double.parseDouble(Array_vision[6]);
                            double vvel=Double.parseDouble(Array_vision[7]);
                            double vvis=Double.parseDouble(Array_vision[8]);
                            long vtime=Long.parseLong(Array_vision[9]);
                            String poshash=Array_vision[10];
                            String oldpos=Array_vision[11];

                            GamePlayerResponsible gpresp=new GamePlayerResponsible(vid,vname,vx,vy,vz,vvel,vvis,vtime,poshash,oldpos);
                            grm.addToResourceVision(gpresp, pos);
                        }
                        else if(type.equals("GameResourceMobileResponsible"))
                        {
                            int pos=Integer.parseInt(Array_vision[1]);
                            String vid=Array_vision[2];
                            String vdesc=Array_vision[3];
                            String vowner=Array_vision[4];
                            String vownerid=Array_vision[5];
                            double vq=Double.parseDouble(Array_vision[5]);
                            double vx=Double.parseDouble(Array_vision[6]);
                            double vy=Double.parseDouble(Array_vision[7]);
                            double vz=Double.parseDouble(Array_vision[8]);
                            double vvel=Double.parseDouble(Array_vision[9]);
                            double vvis=Double.parseDouble(Array_vision[10]);
                            long vtime=Long.parseLong(Array_vision[11]);
                            String poshash=Array_vision[12];
                            String oldpos=Array_vision[13];

                            GameResourceMobileResponsible grmresp=new GameResourceMobileResponsible(vid,vdesc,vowner,vownerid,vq,vx,vy,vz,vvel,vvis,vtime,poshash,oldpos);

                             grm.addToResourceVision(grmresp, pos);
                        }


                    }

                    Resources.add(grm);

                }
            }

            return Resources;

        }

        return null;
    }



    public GamePeerInfo getGamePeerInfo() throws UnknownHostException, IOException
    {
        Message message=new InfoGamePeerRequestMessage();

        String response=this.sendMessage(message);

        MessageReader messageReader=new MessageReader();
        Message receivedMessage = messageReader.readMessageFromString(response.trim());

        if(receivedMessage.getMessageType().equals("GAMEPEERINFO"))
        {
            //InfoGamePeerMessage infogp=(InfoGamePeerMessage)receivedMessage;
            InfoGamePeerMessage infogp=new InfoGamePeerMessage(receivedMessage);

            String id=infogp.getPlayerID();
            String name=infogp.getPlayerName();
            double posx=infogp.getPosX();
            double posy=infogp.getPosY();
            double posz=infogp.getPosZ();
            double vel=infogp.getVel();
            double vis=infogp.getVis();

            GamePeerInfo info=new GamePeerInfo(id,name,posx,posy,posz,vel,vis);

            return info;

        }

        return null;
    }


public GameResource getMobileResource(String resource_id) throws UnknownHostException, IOException
{
    Message message=new MobileResourceFromIDRequestMessage(resource_id);

    String response=this.sendMessage(message);

    MessageReader messageReader=new MessageReader();
    Message receivedMessage = messageReader.readMessageFromString(response.trim());

    if(receivedMessage.getMessageType().equals("GPRESOURCEMOBILE"))
    {
        //MobileResourceFromIDMessage resid=(MobileResourceFromIDMessage)receivedMessage;
        MobileResourceFromIDMessage resid=new MobileResourceFromIDMessage(receivedMessage);

        String resource=resid.getResource();
        String []tmp=resource.split("#");

        String id=tmp[0];
        String description=tmp[1];
        double quantity=Double.parseDouble(tmp[2]);
        double x=Double.parseDouble(tmp[3]);
        double y=Double.parseDouble(tmp[4]);
        double z=Double.parseDouble(tmp[5]);
        double velocity=Double.parseDouble(tmp[6]);
        double vision=Double.parseDouble(tmp[7]);
        String owner=tmp[8];
        String ownerid=tmp[9];
        String rvision=tmp[10];
        
        GameResourceMobile grm=new GameResourceMobile(id,description,owner,ownerid,quantity,x,y,z,velocity,vision);
        
        String []Array_v=rvision.split("*");
        int nv=Array_v.length;

        for(int j=0;j<nv;j++)
        {
            String [] Array_vision=Array_v[j].split("\\");
            String type=Array_vision[0];

                        if(type.equals("GamePlayerResponsible"))
                        {
                            int pos=Integer.parseInt(Array_vision[1]);
                            String vid=Array_vision[2];
                            String vname=Array_vision[3];
                            double vx=Double.parseDouble(Array_vision[4]);
                            double vy=Double.parseDouble(Array_vision[5]);
                            double vz=Double.parseDouble(Array_vision[6]);
                            double vvel=Double.parseDouble(Array_vision[7]);
                            double vvis=Double.parseDouble(Array_vision[8]);
                            long vtime=Long.parseLong(Array_vision[9]);
                            String poshash=Array_vision[10];
                            String oldpos=Array_vision[11];

                            GamePlayerResponsible gpresp=new GamePlayerResponsible(vid,vname,vx,vy,vz,vvel,vvis,vtime,poshash,oldpos);
                            grm.addToResourceVision(gpresp, pos);
                        }
                        else if(type.equals("GameResourceMobileResponsible"))
                        {
                            int pos=Integer.parseInt(Array_vision[1]);
                            String vid=Array_vision[2];
                            String vdesc=Array_vision[3];
                            String vowner=Array_vision[4];
                            String vownerid=Array_vision[5];
                            double vq=Double.parseDouble(Array_vision[5]);
                            double vx=Double.parseDouble(Array_vision[6]);
                            double vy=Double.parseDouble(Array_vision[7]);
                            double vz=Double.parseDouble(Array_vision[8]);
                            double vvel=Double.parseDouble(Array_vision[9]);
                            double vvis=Double.parseDouble(Array_vision[10]);
                            long vtime=Long.parseLong(Array_vision[11]);
                            String poshash=Array_vision[12];
                            String oldpos=Array_vision[13];

                            GameResourceMobileResponsible grmresp=new GameResourceMobileResponsible(vid,vdesc,vowner,vownerid,vq,vx,vy,vz,vvel,vvis,vtime,poshash,oldpos);

                             grm.addToResourceVision(grmresp, pos);
                        }


            }


            return grm;

    }



    return null;
}

public GameResource getMyResourceFromId(String r_id) throws UnknownHostException, IOException
{
    Message message=new ResourceFromIDRequestMessage(r_id);

    String response=this.sendMessage(message);

    MessageReader messageReader=new MessageReader();
    Message receivedMessage = messageReader.readMessageFromString(response.trim());

    if(receivedMessage.getMessageType().equals("GPRESOURCE"))
    {
        ResourceFromIDMessage res=new ResourceFromIDMessage(receivedMessage);

        String str_resource=res.getResource();

        GameResource game_resource=null;

       String []tmp=str_resource.split("\\#");

                if(tmp[0].equals("GameResource"))
                {
                    String id=tmp[1];
                    String description=tmp[2];
                    double quantity=Double.parseDouble(tmp[3]);

                    GameResource gr=new GameResource(id,description,quantity);

                    game_resource=gr;

                }
                else if(tmp[0].equals("GameResourceEvolve"))
                {
                    String id=tmp[1];
                    String description=tmp[2];
                    double quantity=Double.parseDouble(tmp[3]);
                    double period=Double.parseDouble(tmp[4]);
                    double offset=Double.parseDouble(tmp[5]);

                    GameResourceEvolve gre=new GameResourceEvolve(id,description,quantity,(long)period,offset);

                   game_resource=gre;
                }
                else if(tmp[0].equals("GameResourceMobile"))
                {
                    String id=tmp[1];
                    String description=tmp[2];
                    double quantity=Double.parseDouble(tmp[3]);
                    double x=Double.parseDouble(tmp[4]);
                    double y=Double.parseDouble(tmp[5]);
                    double z=Double.parseDouble(tmp[6]);
                    double velocity=Double.parseDouble(tmp[7]);
                    double vision=Double.parseDouble(tmp[8]);
                    String owner=tmp[9];
                    String ownerid=tmp[10];
                    String rvision=tmp[11];

                    GameResourceMobile grm=new GameResourceMobile(id,description,owner,ownerid,quantity,x,y,z,velocity,vision);



                    String []Array_v=rvision.split("\\$");
                    int nv=Array_v.length;

                    for(int j=0;j<nv;j++)
                    {
                        String [] Array_vision=Array_v[j].split("\\;");
                        String type=Array_vision[0];

                        if(type.equals("GamePlayerResponsible"))
                        {
                            int pos=Integer.parseInt(Array_vision[1]);
                            String vid=Array_vision[2];
                            String vname=Array_vision[3];
                            double vx=Double.parseDouble(Array_vision[4]);
                            double vy=Double.parseDouble(Array_vision[5]);
                            double vz=Double.parseDouble(Array_vision[6]);
                            double vvel=Double.parseDouble(Array_vision[7]);
                            double vvis=Double.parseDouble(Array_vision[8]);
                            long vtime=Long.parseLong(Array_vision[9]);
                            String poshash=Array_vision[10];
                            String oldpos=Array_vision[11];

                            GamePlayerResponsible gpresp=new GamePlayerResponsible(vid,vname,vx,vy,vz,vvel,vvis,vtime,poshash,oldpos);
                            grm.addToResourceVision(gpresp, pos);
                        }
                        else if(type.equals("GameResourceMobileResponsible"))
                        {
                            int pos=Integer.parseInt(Array_vision[1]);
                            String vid=Array_vision[2];
                            String vdesc=Array_vision[3];
                            String vowner=Array_vision[4];
                            String vownerid=Array_vision[5];
                            double vq=Double.parseDouble(Array_vision[6]);
                            double vx=Double.parseDouble(Array_vision[7]);
                            double vy=Double.parseDouble(Array_vision[8]);
                            double vz=Double.parseDouble(Array_vision[9]);
                            double vvel=Double.parseDouble(Array_vision[10]);
                            double vvis=Double.parseDouble(Array_vision[11]);
                            long vtime=Long.parseLong(Array_vision[12]);
                            String poshash=Array_vision[13];
                            String oldpos=Array_vision[14];

                            GameResourceMobileResponsible grmresp=new GameResourceMobileResponsible(vid,vdesc,vowner,vownerid,vq,vx,vy,vz,vvel,vvis,vtime,poshash,oldpos);

                             grm.addToResourceVision(grmresp, pos);
                        }


                    }

                   game_resource=grm;

                }

                return game_resource;
    }


    return null;
}




public ArrayList<Object> getVision() throws UnknownHostException, IOException
{
    Message message=new GamePeerVisionRequest();


    String response=this.sendMessage(message);

    MessageReader messageReader=new MessageReader();
    Message receivedMessage = messageReader.readMessageFromString(response.trim());

    if(receivedMessage.getMessageType().equals("GPVISION"))
    {
        //GamePeerVision gpv=(GamePeerVision)receivedMessage;
        GamePeerVision gpv=new GamePeerVision(receivedMessage);
        String str_vision=gpv.getVision();

        ArrayList<Object> vision=new ArrayList<Object>();

        String []Array_v=str_vision.split("*");
        int nv=Array_v.length;

        for(int j=0;j<nv;j++)
        {
            String [] Array_vision=Array_v[j].split("\\");
            String type=Array_vision[0];

                        if(type.equals("GamePlayerResponsible"))
                        {
                            int pos=Integer.parseInt(Array_vision[1]);
                            String vid=Array_vision[2];
                            String vname=Array_vision[3];
                            double vx=Double.parseDouble(Array_vision[4]);
                            double vy=Double.parseDouble(Array_vision[5]);
                            double vz=Double.parseDouble(Array_vision[6]);
                            double vvel=Double.parseDouble(Array_vision[7]);
                            double vvis=Double.parseDouble(Array_vision[8]);
                            long vtime=Long.parseLong(Array_vision[9]);
                            String poshash=Array_vision[10];
                            String oldpos=Array_vision[11];

                            GamePlayerResponsible gpresp=new GamePlayerResponsible(vid,vname,vx,vy,vz,vvel,vvis,vtime,poshash,oldpos);
                            
                            vision.add(pos,gpresp);
                        }
                        else if(type.equals("GameResourceMobileResponsible"))
                        {
                            int pos=Integer.parseInt(Array_vision[1]);
                            String vid=Array_vision[2];
                            String vdesc=Array_vision[3];
                            String vowner=Array_vision[4];
                            String vownerid=Array_vision[5];
                            double vq=Double.parseDouble(Array_vision[5]);
                            double vx=Double.parseDouble(Array_vision[6]);
                            double vy=Double.parseDouble(Array_vision[7]);
                            double vz=Double.parseDouble(Array_vision[8]);
                            double vvel=Double.parseDouble(Array_vision[9]);
                            double vvis=Double.parseDouble(Array_vision[10]);
                            long vtime=Long.parseLong(Array_vision[11]);
                            String poshash=Array_vision[12];
                            String oldpos=Array_vision[13];

                            GameResourceMobileResponsible grmresp=new GameResourceMobileResponsible(vid,vdesc,vowner,vownerid,vq,vx,vy,vz,vvel,vvis,vtime,poshash,oldpos);

                             vision.add(pos,grmresp);

                        }

            }


            return vision;
        
    }

    return null;
}






}
