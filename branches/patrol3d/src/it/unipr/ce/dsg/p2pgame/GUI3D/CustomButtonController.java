/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.unipr.ce.dsg.p2pgame.GUI3D;

import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.controls.Controller;
import de.lessvoid.nifty.elements.Element;
import de.lessvoid.nifty.input.NiftyInputEvent;
import de.lessvoid.nifty.elements.render.TextRenderer;
import de.lessvoid.nifty.screen.Screen;
import de.lessvoid.xml.xpp3.Attributes;
import java.util.Properties;

/**
 *
 * @author giorgio
 */
public class CustomButtonController implements Controller
{

    private Nifty nifty;
    private Screen screen;
    private Element element;
    
    public void bind(Nifty nifty, Screen screen, Element element, Properties parameter, Attributes controlDefinitionAttributes)
    {
        this.nifty=nifty;
        this.screen=screen;
        this.element=element;
    }

    public void init(Properties parameter, Attributes controlDefinitionAttributes)
    {

    }

    public void onStartScreen()
    {
        
    }

    public void onFocus(boolean getFocus)
    {

    }

    public boolean inputEvent(NiftyInputEvent inputEvent)
    {
        return false;
    }
    
    public String getText()
    {
        TextRenderer app;
        int i=0;
        do{
            app=this.element.getElements().get(i++).getRenderer(TextRenderer.class);
        }while(app==null);
        return app.getOriginalText();
    }
    
    public void setText(String text)
    {
        TextRenderer app;
        int i=0;
        do{
            app=this.element.getElements().get(i++).getRenderer(TextRenderer.class);
        }while(app==null);
        app.setText(text);
    }
}
