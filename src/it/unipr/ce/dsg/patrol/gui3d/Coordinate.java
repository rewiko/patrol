/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.unipr.ce.dsg.patrol.gui3d;

import com.jme3.math.Vector3f;
import it.unipr.ce.dsg.patrol.gui.message.content.Point;

/**
 * Class that rapresent jMonkeyEngine coordinates
 * TODO check if the conversion of the value of the coordinates is useful. Some cases return float, but the number is int so as to maintain the uniformity of the data
 * @author Michael Benassi Giorgio Micconi
 */
public class Coordinate
{       
    /**
     * Constructor with the single coordinates in jMonkeyEngine reference system
     * @param x in jMonkey it is at your left
     * @param y in jMonkey it is in front of you, towards above 
     * @param z in jMonkey it is in front of you, towards the screen
     */
    public Coordinate(double x,double y,double z)
    {
        this.x=x;
        this.y=y;
        this.z=z;
    }
    
    /**
     * This constructor is for PATROL' reference sistem
     * @param p y coordinate is the z coordinate of jMonkey. The jMonkey's y coordinate is set to 0.0f
     */
    public Coordinate(Point p)
    {
        this.x=p.getX();
        this.y=0.0;
        this.z=p.getY();
    }
        
    public double getX()
    {
        return this.x;
    }
        
    public float getFX()
    {
        return (float)this.x;
    }
        
    public float getIX()
    {
        return (int)this.x;
    }
        
    public double getY()
    {
        return this.y;
    }
        
    public float getIY()
    {
        return (int)this.y;
    }
        
    public float getFY()
    {
        return (float)this.y;
    }  
      
    public double getZ()
    {
        return this.z;
    }
        
    public float getIZ()
    {
        return (int)this.z;
    }
        
    public float getFZ()
    {
        return (float)this.z;
    }  
        
    public Point getPoint()
    {
        return new Point(this.x,this.z);
    }
        
    public Vector3f getVector3f()
    {
        return new Vector3f((float)this.x,(float)this.y,(float)this.z);
    }
        
    public void setX(double x)
    {
        this.x=x;
    }
        
    public void setY(double y)
    {
        this.y=y;
    }
        
    public void setZ(double z)
    {
        this.z=z;
    }
        
    /**
     * Comparison the actual point with the one passed
     * @param other other point
     * @param precision if the comparison has to be done between int or float, "float" -> float, "integer" -> int
     * @return true if they are equal, false otherwise
     */
    public boolean equalTo(Coordinate other,String precision)
    {
        if(precision.equals("integer"))
        {
            if((int)this.x==(int)other.getX() && (int)this.y==(int)other.getY() && (int)this.z==(int)other.getZ())
                return true;
            return false;
        }
        else if(precision.equals("float"))
        {
            if((float)this.x==(float)other.getX() && (float)this.y==(float)other.getY() && (float)this.z==(float)other.getZ())
                return true;
            return false;
        }
        else    //default behaviour, double precision
        {
            if(this.x==other.getX() && this.y==other.getY() && this.z==other.getZ())
                return true;
            return false;
        }
    }
        
    private double x;
    private double y;
    private double z;
}