package it.unipr.ce.dsg.patrol.gui3d;

import it.unipr.ce.dsg.patrol.platform.MainGamePeer;

/**
 *
 * @author giorgio
 * Class that launch MainGamePeer for human player.
 * 
 * It manages only the game node without user interface
 */
public class RTSHumanMainGamePeer 
{
    public static void main(String [] arg)
	{
    	int port = 9998;
    	if (arg.length >= 1)
    		port = Integer.parseInt(arg[0].trim());
    	
	MainGamePeer mainpeer=new MainGamePeer(port);
	mainpeer.start();
	}
}