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

public class GamePeerDescriptionRequest extends Message{

    public GamePeerDescriptionRequest()
    {
         super("","",0);
        this.setMessageType("GPDESCREQUEST");
        this.PARAMETERS_NUM=3;

    }

    public GamePeerDescriptionRequest(Message message)
    {
        super(message.getSourceName(), message.getSourceSocketAddr(), message.getSourcePort());

        this.setMessageType("GPDESCREQUEST");
        this.PARAMETERS_NUM=3;
    }



}
