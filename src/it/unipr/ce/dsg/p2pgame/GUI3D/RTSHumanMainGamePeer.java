package it.unipr.ce.dsg.p2pgame.GUI3D;

import it.unipr.ce.dsg.p2pgame.Engine.MainGamePeer;

/**
 *
 * @author giorgio
 * Class that launch MainGamePeer for human player
 */
public class RTSHumanMainGamePeer 
{
    public static void main(String [] arg)
	{
		MainGamePeer mainpeer=new MainGamePeer(9998);
		mainpeer.start();
	}
}