/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.unipr.ce.dsg.patrol.GUI3D;

import com.jme3.app.SimpleApplication;
import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.elements.Element;
import de.lessvoid.nifty.screen.Screen;
import de.lessvoid.nifty.screen.ScreenController;
import java.util.Iterator;

/**
 *
 * @author Benassi Michael Micconi Giorgio
 */
public class HUDScreenController extends SimpleApplication implements ScreenController
{

    @Override
    public void simpleInitApp() 
    {
        //throw new UnsupportedOperationException("Not supported yet.");
    }

    public void bind(Nifty nifty, Screen screen) 
    {
        this.nifty=nifty;
    }

    public void onStartScreen() 
    {
        //throw new UnsupportedOperationException("Not supported yet.");
    }

    public void onEndScreen() 
    {
        //throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public void disableHUD()
    {
        for(Iterator<Element> i=this.nifty.getScreen("hud").getLayerElements().iterator(); i.hasNext();)
            i.next().setVisible(false);
    }
    
    public void enableHUD()
    {
        for(Iterator<Element> i=this.nifty.getScreen("hud").getLayerElements().iterator(); i.hasNext();)
            i.next().setVisible(true);
    }
    
    private Nifty nifty;
}
