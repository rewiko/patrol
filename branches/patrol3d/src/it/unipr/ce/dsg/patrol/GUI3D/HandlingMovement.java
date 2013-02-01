/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.unipr.ce.dsg.patrol.GUI3D;

import com.jme3.export.JmeExporter;
import com.jme3.export.JmeImporter;
import com.jme3.export.Savable;
import com.jme3.scene.Spatial;
import it.unipr.ce.dsg.patrol.GUI.message.content.Point;
import it.unipr.ce.dsg.patrol.platform.GameResourceMobile;
import it.unipr.ce.dsg.patrol.util.MultiLog;

import java.io.IOException;
import java.util.Iterator;
import java.util.concurrent.LinkedBlockingQueue;

/**
 *
 * @author giorgio
 */
public class HandlingMovement implements Savable
{
    private String resId;
    private RTSGameGUI gui;
    private int period;
    private Spatial spatial;
    private Coordinate current;
    private Coordinate previous;
    private Coordinate arrival;
    private int previousX;
    private int previousZ;
    private CoordinatesMapping mapping;
    private boolean arrived;
    private MessageSender sender;
    private GameResourceMobile grm;
    private boolean firstTime;
    private double delta;
    private int gran;
    private LinkedBlockingQueue<Point2PointData> travelsQueue;
    
    /**
     * Constructor without specific path
     * @param gui
     * @param id
     * @param period_movement
     * @param selected
     * @param sender
     * @param delta
     * @param gran 
     */
    public HandlingMovement(RTSGameGUI gui, String id, int period_movement, Spatial selected, MessageSender sender,double delta,double gran)
    {
        //MultiLog.println(RTSGameGUI.class.toString(),"HandlingMovement constructor");
        this.resId=id;
	this.gui=gui;
	this.period=period_movement;
        this.spatial=selected;
        this.previous=null;
        this.current=null;
        this.arrival=null;
        this.previousX=0;
        this.previousZ=0;
        this.arrived=false;
        this.sender=sender;
        //MultiLog.println(RTSGameGUI.class.toString(),"Request for resource "+this.resId);
        this.grm=this.sender.getMobileResource(this.resId);
        this.firstTime=true;
        this.mapping=new CoordinatesMapping();
        this.delta=delta;
        this.gran=(int) gran;
        this.travelsQueue=new LinkedBlockingQueue<Point2PointData>();
        //MultiLog.println(RTSGameGUI.class.toString(),"End HandlingMovement constructor");
    }
    
    /**
     * Constructor with a path
     * @param gui
     * @param id
     * @param start
     * @param arrival
     * @param period_movement
     * @param selected
     * @param sender
     * @param delta
     * @param gran 
     */
    public HandlingMovement(RTSGameGUI gui, String id, Coordinate start, Coordinate arrival, int period_movement, Spatial selected, MessageSender sender,double delta,double gran)
    {
        //MultiLog.println(RTSGameGUI.class.toString(),"HandlingMovement constructor");
        this.resId=id;
	this.gui=gui;
	this.period=period_movement;
        this.spatial=selected;
        this.previous=null;
        this.current=null;
        this.arrival=null;
        this.previousX=0;
        this.previousZ=0;
        this.arrived=false;
        this.sender=sender;
        //MultiLog.println(RTSGameGUI.class.toString(),"Request for resource "+this.resId);
        this.grm=this.sender.getMobileResource(this.resId);
        this.firstTime=true;
        this.mapping=new CoordinatesMapping();
        this.delta=delta;
        this.gran=(int) gran;
        this.travelsQueue=new LinkedBlockingQueue<Point2PointData>();
        this.travelsQueue.offer(new Point2PointData(start,arrival));
        //MultiLog.println(RTSGameGUI.class.toString(),"End HandlingMovement constructor");
    }
    
    public boolean addMovement(Coordinate start, Coordinate arrival)
    {
        //MultiLog.println(RTSGameGUI.class.toString(),"Add movement");
        if(this.spatial==null)
            {
            //MultiLog.println(RTSGameGUI.class.toString(),"Spatial wasn't set");    
            return false;
            }
        if(this.travelsQueue.isEmpty())
            {
            //MultiLog.println(RTSGameGUI.class.toString(),"Queue empty, add first Point2PointData element");
            this.travelsQueue.offer(new Point2PointData(start,arrival));
            //MultiLog.println(RTSGameGUI.class.toString(),"\t Added new Point2PointData element");
            }
        else
            {
            //MultiLog.println(RTSGameGUI.class.toString(),"Queue not empty, "+this.travelsQueue.size()+" elements");
            Iterator<Point2PointData> iter=this.travelsQueue.iterator();
            Point2PointData last=null;
            while(iter.hasNext())
                last=iter.next();
            this.travelsQueue.offer(new Point2PointData(last.getArrival(),arrival));
            //MultiLog.println(RTSGameGUI.class.toString(),"\t Added new Point2PointData element");
            }
        return true;
    }
    
    public Coordinate calculateNextWayPoint()
    {
        //MultiLog.println(RTSGameGUI.class.toString(),"HandlingMovement calculateNextWayPoint");
        if(this.travelsQueue.isEmpty() && this.arrived==true)
            {
            //MultiLog.println(RTSGameGUI.class.toString(),"\tNo path to follow was set");
            return null;
            }
        if(this.firstTime)
            {
            //MultiLog.println(RTSGameGUI.class.toString(),"\tFirst time waypoint");
            this.sender.setMobileReourceStatus(this.resId, true);
            this.firstTime=false;
            this.arrived=false;
            }
        if(this.arrived)
            return null;
        if(this.spatial.getControl(MyShipControl.class).isMoving())
            {
            //MultiLog.println(RTSGameGUI.class.toString(),"\tShip is still moving, can't calculate next way point");
            return null;
            }
        if(this.current==null && this.arrival==null)
            {
            Point2PointData path=this.travelsQueue.poll();
            this.current=path.getStart();
            this.arrival=path.getArrival();
            }
        //MultiLog.println(RTSGameGUI.class.toString(), "Current: "+this.current.getIX()+" "+this.current.getIY()+" "+this.current.getIZ());
        //MultiLog.println(RTSGameGUI.class.toString(), "Arrival: "+this.arrival.getIX()+" "+this.arrival.getIY()+" "+this.arrival.getIZ());
        if(this.current.equalTo(this.arrival,"integer"))
            {
            System.out.println("Travel complete");
            grm=this.sender.getMobileResource(this.resId);
            //MultiLog.println(RTSGameGUI.class.toString(),"\tTravel complete x:"+grm.getX()+" z:"+grm.getY());
            this.sender.setMobileReourceStatus(this.resId, false);
            this.current=null;
            this.arrival=null;
            this.firstTime=true;
            this.arrived=true;
            return null;
            }
        int directionX=longitudeMovement();
        int directionZ=latitudeMovement();
        int movx=0;
	int movz=0;
        if(directionX!=0 && !(previousX!=0 && directionZ!=0))
            {
            if(directionX==1)//verso ovest
                {
                movx=this.gran;
                this.previousX=1;
                this.previousZ=0;
                }
            else if(directionX==2) //verso est
                {
                movx=-this.gran;
                this.previousX=2;
                this.previousZ=0;
                }
            else if(directionX==0)//non mi muovo in orizontale
                {
                movx=0;
                this.previousX=0;
                this.previousZ=0;
                }
            }
        else    //Z movement
            {
            if(directionZ==1)//verso nord
                {
                movz=this.gran;
                this.previousZ=1;
                this.previousX=0;
                }
            else if(directionZ==2)//verso sud
                {
                movz=-this.gran;
                this.previousZ=2;
                this.previousX=0;
                }
            else if(directionZ==0)//non mi muovo in verticale
                {
                movz=0;
                this.previousZ=0;
                this.previousX=0;
                }
            }
        double patrolOldX=grm.getX();
        double patrolOldY=grm.getY();
        this.sender.moveMobileResource(this.resId, movx, movz);
        //MultiLog.println(RTSGameGUI.class.toString(), "Move: "+movx+" "+movz);
        grm=this.sender.getMobileResource(this.resId);
        double patrolNewX=grm.getX();
        double patrolNewY=grm.getY();
        if(patrolOldX!=patrolNewX || patrolOldY!=patrolNewY)
            {
            //MultiLog.println(RTSGameGUI.class.toString(),"Previus: "+current.getFX()+" "+current.getFY()+" "+current.getFZ());
            //MultiLog.println(RTSGameGUI.class.toString(),"Patrol new: "+patrolNewX+" "+patrolNewY);
            this.previous=this.current;
            Point newPoint=new Point(patrolNewX,patrolNewY);
            this.current=this.mapping.patrolTojMonkey(newPoint,this.delta);
           //MultiLog.println(RTSGameGUI.class.toString(),"Current: "+current.getFX()+" "+current.getFY()+" "+current.getFZ());
            System.out.println("Next step has been calculated");
            //MultiLog.println(RTSGameGUI.class.toString(),"Next step has been calculated");
            return this.current;
            }
        else
            return null;
    }
    
    private int longitudeMovement() 
    {
        if((int)this.current.getIX()<(int)this.arrival.getIX())
        {
            return 1;
        }
        else if((int)this.current.getIX()==(int)this.arrival.getIX())
        {
            return 0;
        }
        else if((int)this.current.getIX()>(int)this.arrival.getIX())
        {
            return 2;
        }
        return 0;
    }

    private int latitudeMovement() 
    {
        if((int)this.current.getIZ()<(int)this.arrival.getIZ())
        {
            return 1;
        }
        else if((int)this.current.getIZ()==(int)this.arrival.getIZ())
        {
            return 0;
        }
        else if((int)this.current.getIZ()>(int)this.arrival.getIZ())
        {
            return 2;
        }
        return 0;
    }

    public void write(JmeExporter ex) throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void read(JmeImporter im) throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
