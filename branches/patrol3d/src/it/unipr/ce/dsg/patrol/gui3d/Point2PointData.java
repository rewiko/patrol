/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.unipr.ce.dsg.patrol.gui3d;

/**
 * Class used to store the points of departure and arrival used in the method of the tracking path.
 * @author Michael Benassi Giorgio Micconi
 */
public class Point2PointData 
{
    /**
     * Constructor of the class, couple the two coordinate
     * @param start coordinates of departure
     * @param arrival coordinates of arrival 
     */
    public Point2PointData(Coordinate start, Coordinate arrival)
    {
        this.start = start;
        this.arrival = arrival;
    }

    /**
     * Gets the arrival coordinates
     * @return arrival coordinates
     */
    public Coordinate getArrival()
    {
        return arrival;
    }

    /**
     * Gets the departure coordinates
     * @return departure coordinates
     */
    public Coordinate getStart()
    {
        return start;
    }

    /**
     * Sets the arrival coordinates
     * @param arrival coordinates of arrival to set
     */
    public void setArrival(Coordinate arrival)
    {
        this.arrival = arrival;
    }

    /**
     * Sets the departure coordinates
     * @param start coordinates of departure to set
     */
    public void setStart(Coordinate start)
    {
        this.start = start;
    }
    
    private Coordinate start;
    private Coordinate arrival;
}
