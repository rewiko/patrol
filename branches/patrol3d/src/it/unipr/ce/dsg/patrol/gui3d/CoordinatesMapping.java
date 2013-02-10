/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.unipr.ce.dsg.patrol.gui3d;

import it.unipr.ce.dsg.patrol.gui.message.content.Point;

/**
 * Provide mapping between PATROL reference system and jMonkeyEngine reference system.
 * @author Benassi Michael Micconi Giorgio
 */
public class CoordinatesMapping 
{   
    /**
     * This constructor does nothing
     */
    public CoordinatesMapping()
    {
        
    }
    
    /**
     * Calculate the jMonkeyEngine coordinates from the PATROL coordinates
     * @param p PATROL coordinates
     * @param delta displacement from the two reference system
     * @return jMonkeyEngine coordinates
     */
    public Coordinate patrolTojMonkey(Point p,double delta)
    {
        return new Coordinate(p.getX()-delta,0.0,p.getY()-delta);
    }
    
    /**
     * Calculate the PATROL coordinates from the jMonkeyEngine coordinates
     * @param p jMonkeyEngine coordinates
     * @param delta displacement from the two reference system
     * @return PATROL coordinates
     */
    public Point jMonkeyToPatrol(Coordinate p,double delta)
    {
        return new Point(p.getX()+delta,p.getZ()+delta);
    }
}

