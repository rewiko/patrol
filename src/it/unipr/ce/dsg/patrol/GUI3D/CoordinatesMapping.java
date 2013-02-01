/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.unipr.ce.dsg.patrol.GUI3D;

import it.unipr.ce.dsg.patrol.GUI.message.content.Point;

/**
 *
 * @author Benassi Michael Micconi Giorgio
 */
public class CoordinatesMapping 
{
    public static String Patrol="patrol";
    public static String jMonkey="jmonkey";
    
    public CoordinatesMapping()
    {
        
    }
    
    public Coordinate patrolTojMonkey(Point p,double delta)
    {
        //return new Coordinate(p.getX()-delta,-(p.getY()-delta));
        return new Coordinate(p.getX()-delta,0.0,p.getY()-delta);
    }
    
    public Point jMonkeyToPatrol(Coordinate p,double delta)
    {
        //return new Point(p.getX()+delta,Math.abs(p.getY()-delta));
        return new Point(p.getX()+delta,p.getZ()+delta);
    }
}

