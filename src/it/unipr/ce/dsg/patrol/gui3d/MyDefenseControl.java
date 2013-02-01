/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.unipr.ce.dsg.patrol.gui3d;

import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.AbstractControl;
import com.jme3.scene.control.Control;

/**
 *
 * @author giorgio
 */
public class MyDefenseControl extends AbstractControl
{
    
    private float rotSpeed=1/15f;
    
    public MyDefenseControl()
    {
        this.rotSpeed=1/15f;
    }
    
    /**
     * 
     * @param rotSpeed is the value that multiply tpf parameter in controlUpdate
     */
    public MyDefenseControl(float rotSpeed)
    {
        this.rotSpeed=rotSpeed;
    }

    @Override
    public void setSpatial(Spatial spatial) 
    {
        super.setSpatial(spatial);
    }
    
    /** Implement your spatial's behaviour here.
    * From here you can modify the scene graph and the spatial
    * (transform them, get and set userdata, etc).
    * This loop controls the spatial while the Control is enabled. */
    @Override
    protected void controlUpdate(float tpf)
    {
        if(spatial != null)
            {
            spatial.rotate(0.0f,tpf*this.rotSpeed,0.0f);
            }
    }

    @Override
    protected void controlRender(RenderManager rm, ViewPort vp) {
        //throw new UnsupportedOperationException("Not supported yet.");
    }

    public Control cloneForSpatial(Spatial spatial) 
    {
        final MyDefenseControl control = new MyDefenseControl();
        /* Optional: use setters to copy userdata into the cloned control */
        // control.setIndex(i); // example
        control.setSpatial(spatial);
        return control;
    }
}
