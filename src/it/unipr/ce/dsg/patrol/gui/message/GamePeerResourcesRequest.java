/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package it.unipr.ce.dsg.patrol.gui.message;

/**
 *
 * @author pelito
 */
import it.simplexml.message.Message;

public class GamePeerResourcesRequest extends Message{


    public GamePeerResourcesRequest()
    {
             super("","",0);
            this.setMessageType("GPRESOURCESREQ");
            this.PARAMETERS_NUM=3;

    }

    public  GamePeerResourcesRequest(Message message)
    {
        super(message.getSourceName(), message.getSourceSocketAddr(), message.getSourcePort());
        this.setMessageType("GPRESOURCESREQ");
        this.PARAMETERS_NUM=3;

    }

}
