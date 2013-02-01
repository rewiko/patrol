/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.unipr.ce.dsg.patrol.GUI3D;

import com.jme3.animation.LoopMode;
import com.jme3.cinematic.MotionPath;
import com.jme3.cinematic.MotionPathListener;
import com.jme3.cinematic.events.MotionEvent;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.AbstractControl;
import com.jme3.scene.control.Control;

/**
 *
 * @author giorgio
 */
public class MyShipControl extends AbstractControl
{
    private float movementSpeed;
    private boolean move,ready;
    private Coordinate arrival;
    //private MotionTrack motionControl;
    private MotionEvent motionControl;
    private MotionPath path;
    
    public MyShipControl()
    {
        this.movementSpeed=1.0f;
        this.move=false;
        this.ready=false;
    }
    
    public MyShipControl(float movementSpeed)
    {
        this.movementSpeed=movementSpeed;
        this.move=false;
        this.ready=false;
    }
     
    @Override
    public void setSpatial(Spatial spatial) 
    {
        super.setSpatial(spatial);
    }
    
    public boolean isMoving()
    {
        return this.move;
    }
    
    public boolean setMovement(Coordinate arrival)
    {
        if(/*move ||*/ (spatial == null))    //if move==true then we can't do another movement, same for spatial==null
            return false;
        this.arrival=arrival;
        this.path=new MotionPath();
        path.addListener(new MotionPathListener()
            {
            public void onWayPointReach(MotionEvent control, int wayPointIndex)
                {
                if (path.getNbWayPoints() == wayPointIndex + 1)
                    {
                        move=false;
                        ready=false;
                        System.out.println("Movement finished");
                    }
                else
                    {
                    
                    }
                }
            });
        Vector3f intStart=new Vector3f((int)this.spatial.getLocalTranslation().getX(),(int)this.spatial.getLocalTranslation().getY(),(int)this.spatial.getLocalTranslation().getZ());
        Vector3f intArrival=new Vector3f((int)this.arrival.getX(),(int)this.arrival.getY(),(int)this.arrival.getZ());
        if(intStart.equals(intArrival))
            {
            this.ready=false;
            return false;
            }
        this.path.addWayPoint(intStart);
        this.path.addWayPoint(intArrival);
        this.path.setCurveTension(0.0f);
        System.out.println("Arrival coordinate: "+this.arrival.getVector3f().getX()+" "+this.arrival.getVector3f().getY()+" "+this.arrival.getVector3f().getZ());
        System.out.println("Actual coordinate(path): "+this.path.getWayPoint(0).getX()+" "+this.path.getWayPoint(0).getY()+" "+this.path.getWayPoint(0).getZ());
        System.out.println("Arrival coordinate(path): "+this.path.getWayPoint(1).getX()+" "+this.path.getWayPoint(1).getY()+" "+this.path.getWayPoint(1).getZ());
        motionControl = new MotionEvent(this.spatial,path);
        //motionControl.setRotation(new Quaternion().fromAngleNormalAxis(-FastMath.HALF_PI, Vector3f.UNIT_Y));
        motionControl.setDirectionType(MotionEvent.Direction.PathAndRotation);
        motionControl.setRotation(new Quaternion().fromAngleNormalAxis(-FastMath.HALF_PI, Vector3f.UNIT_Y));
        motionControl.setInitialDuration(1.0f);
        motionControl.setSpeed(this.movementSpeed);
        motionControl.setLoopMode(LoopMode.DontLoop);
        this.ready=true;
        this.move=true;
        System.out.println("Path configuration ready");
        return true;
    }
    
    /*public boolean move()
    {
        if(this.path.getNbWayPoints()>0 && (spatial != null))
                {
                this.move=true;
                motionControl = new MotionTrack(this.spatial,path);
                motionControl.setDirectionType(MotionTrack.Direction.PathAndRotation);
                //motionControl.setRotation(new Quaternion().fromAngleNormalAxis(-FastMath.HALF_PI, Vector3f.UNIT_Y));
                motionControl.setInitialDuration(10f);
                motionControl.setSpeed(this.movementSpeed);
                System.out.println("Play");
                motionControl.play();
                return true;
                }
        return false;
    }*/        
            
    /** Implement your spatial's behaviour here.
    * From here you can modify the scene graph and the spatial
    * (transform them, get and set userdata, etc).
    * This loop controls the spatial while the Control is enabled. */
    @Override
    protected void controlUpdate(float tpf)
    {
        //System.out.println("Cycle");
        if(spatial != null)
            if(this.ready/* && !this.move*/)
                {
                    this.move=true;
                    this.motionControl.play();
                    System.out.println("Movement");
                }
    }

    @Override
    protected void controlRender(RenderManager rm, ViewPort vp) {
        //throw new UnsupportedOperationException("Not supported yet.");
    }

    public Control cloneForSpatial(Spatial spatial) 
    {
        final MyShipControl control = new MyShipControl();
        /* Optional: use setters to copy userdata into the cloned control */
        // control.setIndex(i); // example
        control.setSpatial(spatial);
        return control;
    }
}
