/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.unipr.ce.dsg.patrol.gui3d;

/**
 *
 * @author giorgio
 */
public class Point2PointData 
{

    public Point2PointData(Coordinate start, Coordinate arrival) {
        this.start = start;
        this.arrival = arrival;
    }

    public Coordinate getArrival() {
        return arrival;
    }

    public Coordinate getStart() {
        return start;
    }

    public void setArrival(Coordinate arrival) {
        this.arrival = arrival;
    }

    public void setStart(Coordinate start) {
        this.start = start;
    }
    private Coordinate start;
    private Coordinate arrival;
}
