package it.unipr.ce.dsg.patrol.gui3d;

import it.unipr.ce.dsg.patrol.platform.MainGamePeer;

/**
 * Class that launch MainGamePeer for human players. (Only main)
 * It manages only the game node without user interface.
 * If there isn't the argument, the default value 9998 will be used for the communication port.
 * @author Michael Benassi Giorgio Micconi
 */
public class RTSHumanMainGamePeer 
{
    /**
     * 
     * @param arg arg arg[0] contains the port used for communication. If there isn't the argument the default value 9998 will be used.
     */
    public static void main(String [] arg)
    {
        int port = 9998;
    	if (arg.length >= 1)
            port = Integer.parseInt(arg[0].trim());
	MainGamePeer mainpeer=new MainGamePeer(port);
	mainpeer.start();
    }
}