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

public class PositionRequestMessage extends Message{

    public PositionRequestMessage()
    {
         super("","",0);
        this.setMessageType("POSITIONREQUEST");
        this.PARAMETERS_NUM=3;
    }

    public PositionRequestMessage(Message message)
    {
        super(message.getSourceName(), message.getSourceSocketAddr(), message.getSourcePort());
        this.setMessageType("POSITIONREQUEST");
        this.PARAMETERS_NUM=3;

    }

}

