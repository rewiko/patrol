package it.unipr.ce.dsg.patrol.gui3d;

import it.unipr.ce.dsg.patrol.platform.bot.RTSGameBot2;

/**
 * Class that launch bot players. (Only main)
 * If there isn't the argument, a random value will be used for the communication ports.
 * @author Michael Benassi Giorgio Micconi
 */
public class RTSGameBotLauncher 
{
    /**
     * TODO checks the last port value, portReq
     * @param arg arg[0] contains the port used for communication. If there isn't the argument, a random value will be used.
     */
    public static void main(String [] arg)
    {
        Thread th;
        if(arg.length>=1)
        {
            System.out.println("Porta associata: "+arg[0]);
            th=new Thread(new RTSGameBot2("profiles/profile1.txt","conf/gameconf.txt",Integer.parseInt(arg[0]),"jose",9999));
        }
        else
        {
            System.out.println("Porta non trovata, calcolata random");
            th=new Thread(new RTSGameBot2("profiles/profile1.txt","conf/gameconf.txt",(int)(Math.random()*60000+1025),"jose",9990));
        }
        th.start();
    }
}
