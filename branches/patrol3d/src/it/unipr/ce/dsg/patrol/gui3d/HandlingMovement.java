/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.unipr.ce.dsg.patrol.gui3d;

import com.jme3.export.JmeExporter;
import com.jme3.export.JmeImporter;
import com.jme3.export.Savable;
import com.jme3.scene.Spatial;
import it.unipr.ce.dsg.patrol.gui.message.content.Point;
import it.unipr.ce.dsg.patrol.platform.GameResourceMobile;
import java.io.IOException;
import java.util.Iterator;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Handler of the movement of the ship. Implements Savable because everything used as UserData for Spatial must implments that class.
 * @author Michael Benassi Giorgio Micconi
 */
public class HandlingMovement implements Savable
{   
    /**
     * Constructor without specific path
     * @param gui reference to the GUI istance
     * @param id id of the ship. TODO the id is also in spatial object, this can be deleted
     * @param period_movement period of the movement. TODO it's unused, it can be deleted
     * @param selected the spatial of the ship
     * @param sender reference of the object that communicates with the net
     * @param delta displacement from PATROL reference system to jMonkeyEngine reference system
     * @param gran granularity value
     */
    public HandlingMovement(RTSGameGUI gui, String id, int period_movement, Spatial selected, MessageSender sender,double delta,double gran)
    {
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
        this.grm=this.sender.getMobileResource(this.resId);
        this.firstTime=true;
        this.mapping=new CoordinatesMapping();
        this.delta=delta;
        this.gran=(int) gran;
        this.travelsQueue=new LinkedBlockingQueue<Point2PointData>();
    }
    
    /**
     * Constructor with a path
     * @param gui reference to the GUI istance
     * @param id id of the ship. TODO the id is also in spatial object, this can be deleted
     * @param start departure Coordinates
     * @param arrival arrival Coordinates
     * @param period_movement period of the movement. TODO it's unused, it can be deleted
     * @param selected the spatial of the ship
     * @param sender reference of the object that communicates with the net
     * @param delta displacement from PATROL reference system to jMonkeyEngine reference system
     * @param gran granularity value
     */
    public HandlingMovement(RTSGameGUI gui, String id, Coordinate start, Coordinate arrival, int period_movement, Spatial selected, MessageSender sender,double delta,double gran)
    {
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
        this.grm=this.sender.getMobileResource(this.resId);
        this.firstTime=true;
        this.mapping=new CoordinatesMapping();
        this.delta=delta;
        this.gran=(int) gran;
        this.travelsQueue=new LinkedBlockingQueue<Point2PointData>();
        this.travelsQueue.offer(new Point2PointData(start,arrival));
    }
    
    /**
     * Add new movement for the spatial
     * @param start new departure Coordinate
     * @param arrival new arrival Coordinate
     * @return true if the spatial has been setted, false otherwise
     */
    public boolean addMovement(Coordinate start, Coordinate arrival)
    {
        if(this.spatial==null)
            return false;
        if(this.travelsQueue.isEmpty())
            this.travelsQueue.offer(new Point2PointData(start,arrival));
        else
            {
            Iterator<Point2PointData> iter=this.travelsQueue.iterator();
            Point2PointData last=null;
            while(iter.hasNext())
                last=iter.next();
            this.travelsQueue.offer(new Point2PointData(last.getArrival(),arrival));
            }
        return true;
    }
    
    /**
     * Calculates the next point that has to be reached by the spatial on its path to the arrival
     * @return the next point
     */
    public Coordinate calculateNextWayPoint()
    {
        if(this.travelsQueue.isEmpty() && this.arrived==true)
            return null;
        if(this.firstTime)
            {
            this.sender.setMobileReourceStatus(this.resId, true);
            this.firstTime=false;
            this.arrived=false;
            }
        if(this.arrived)
            return null;
        if(this.spatial.getControl(MyShipControl.class).isMoving())
            return null;
        if(this.current==null && this.arrival==null)
            {
            Point2PointData path=this.travelsQueue.poll();
            this.current=path.getStart();
            this.arrival=path.getArrival();
            }
        if(this.current.equalTo(this.arrival,"integer"))
            {
            System.out.println("Travel complete");
            grm=this.sender.getMobileResource(this.resId);
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
            if(directionX==1)//towards west
                {
                movx=this.gran;
                this.previousX=1;
                this.previousZ=0;
                }
            else if(directionX==2) //towards east
                {
                movx=-this.gran;
                this.previousX=2;
                this.previousZ=0;
                }
            else if(directionX==0)//no horizontal movement
                {
                movx=0;
                this.previousX=0;
                this.previousZ=0;
                }
            }
        else    //Z movement
            {
            if(directionZ==1)//towards north
                {
                movz=this.gran;
                this.previousZ=1;
                this.previousX=0;
                }
            else if(directionZ==2)//towards south
                {
                movz=-this.gran;
                this.previousZ=2;
                this.previousX=0;
                }
            else if(directionZ==0)//no vertical movement
                {
                movz=0;
                this.previousZ=0;
                this.previousX=0;
                }
            }
        double patrolOldX=grm.getX();
        double patrolOldY=grm.getY();
        this.sender.moveMobileResource(this.resId, movx, movz);
        grm=this.sender.getMobileResource(this.resId);
        double patrolNewX=grm.getX();
        double patrolNewY=grm.getY();
        if(patrolOldX!=patrolNewX || patrolOldY!=patrolNewY)
            {
            this.previous=this.current;
            Point newPoint=new Point(patrolNewX,patrolNewY);
            this.current=this.mapping.patrolTojMonkey(newPoint,this.delta);
            System.out.println("Next step has been calculated");
            return this.current;
            }
        else
            return null;
    }
    
    /**
     * Calcualtes the horizontal displacement of the next way point
     * @return the direction, 1=West, 0=No displacement, 2=East
     */
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

    /**
     * Calcualtes the vertical displacement of the next way point
     * @return the direction, 1=North, 0=No displacement, 2=South
     */
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

    /**
     * It isn't used, it is necessary only to save game but must be present in each class that is used as an UserData of a Spatial
     * @param ex
     * @throws IOException 
     */
    public void write(JmeExporter ex) throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * It isn't used, it is necessary only to load game but must be present in each class that is used as an UserData of a Spatial
     * @param ex
     * @throws IOException 
     */
    public void read(JmeImporter im) throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
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
}
