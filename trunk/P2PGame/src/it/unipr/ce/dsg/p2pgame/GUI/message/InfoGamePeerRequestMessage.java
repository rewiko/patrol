/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package it.unipr.ce.dsg.p2pgame.GUI.message;

/**
 *
 * @author pelito
 */
import it.simplexml.message.Message;
public class InfoGamePeerRequestMessage extends Message{

    public InfoGamePeerRequestMessage()
    {
         super("","",0);
        this.setMessageType("INFOGPREQUEST");
        this.PARAMETERS_NUM=3;
    }
    
    public InfoGamePeerRequestMessage(Message message)
    {
        super(message.getSourceName(), message.getSourceSocketAddr(), message.getSourcePort());
        this.setMessageType("INFOGPREQUEST");
        this.PARAMETERS_NUM=3;
        
    }



}
