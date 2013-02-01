/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package it.unipr.ce.dsg.patrol.GUI.message;

/**
 *
 * @author pelito
 */
import it.simplexml.message.Message;
public class ResourcesSizeRequestMessage extends Message {

    public ResourcesSizeRequestMessage()
    {
        super("","",0);
        this.setMessageType("RESOURCESSIZEREQ");
        this.PARAMETERS_NUM=3;

    }

    public ResourcesSizeRequestMessage(Message message)
    {

        super(message.getSourceName(), message.getSourceSocketAddr(), message.getSourcePort());

        this.setMessageType("RESOURCESSIZEREQ");
        this.PARAMETERS_NUM=3;
    }



}
