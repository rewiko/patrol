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
public class IpAddressRequestMessage extends Message{

    public IpAddressRequestMessage()
    {
        super("","",0);
        this.setMessageType("IPADDRESSREQUEST");
        this.PARAMETERS_NUM=3;

    }

    public IpAddressRequestMessage(Message message)
    {
        super(message.getSourceName(), message.getSourceSocketAddr(), message.getSourcePort());
        this.setMessageType("IPADDRESSREQUEST");
        this.PARAMETERS_NUM=3;

    }

}
