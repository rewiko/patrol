/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.unipr.ce.dsg.patrol.gui3d;

import com.jme3.scene.Spatial;
import it.unipr.ce.dsg.patrol.platform.bot.VirtualResource;
import it.unipr.ce.dsg.patrol.util.MultiLog;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author Giorgio
 */
public class StatusPrinter
{
    MultiLog multiLog;
    String id;
    
    public StatusPrinter(String id)
        {
        if(MultiLog.getInstance()==null)
            multiLog = new MultiLog("ConfigFile.txt",true,true);
        this.id=id;
        }
    
    public void stampSingleText(String text)
        {
        MultiLog.println(this.id, text);
        }
    
    public void stampGUIStatus(RTSGameGUI gui)
        {
        MultiLog.println(this.id,"My home planet");
        MultiLog.println(this.id, "\t"+gui.homePlanetCoord.getX()+" "+gui.homePlanetCoord.getY()+" "+gui.homePlanetCoord.getZ());
        
        MultiLog.println(this.id,"Planets:");
        for(Iterator<VirtualResource> iter=gui.planets.iterator();iter.hasNext();)
            {
            VirtualResource planet=iter.next();
            MultiLog.println(this.id,"\t"+planet.getId()+" "+planet.getOwnerID()+" "+planet.getOwnerName()+" "+planet.getResType()+" "+planet.getX()+" "+planet.getY()+" "+planet.getZ());
            }
        MultiLog.println(this.id,"Knowledges:");
        for(int i=0;i<gui.knowledges.size();i++)
            {
            MultiLog.println(this.id,"\t"+i);
            MultiLog.println(this.id,"\t"+gui.knowledges.get(i).getType().toString());
            MultiLog.println(this.id,"\t"+gui.knowledges.get(i).getElement().getClass().toString());
            }
        HashMap map=gui.enemyShipMap;
        MultiLog.println(this.id,"Enemy ship:");
        for(int i=0;i<gui.enemyShipMap.size();i++)
            {
            MultiLog.println(this.id,"\t"+i);
            Spatial spa=(Spatial)map.get(map.keySet().toArray()[i]);
            Collection<String> data=spa.getUserDataKeys();
            for(Iterator<String> iter=data.iterator();iter.hasNext();)
                MultiLog.println(this.id,"\t\t"+spa.getUserData(iter.next())); 
            }
        MultiLog.println(this.id,"Gui attached nodes:");
        for(int i=0;i<gui.getRootNode().getChildren().size();i++)
            {
            Collection<String> data=gui.getRootNode().getChild(i).getUserDataKeys();
            Spatial spa=gui.getRootNode().getChild(i);
            for(Iterator<String> iter=data.iterator();iter.hasNext();)
                MultiLog.println(this.id,"\t\t"+spa.getUserData(iter.next()));
            }
        }
}
