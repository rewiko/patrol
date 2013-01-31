package it.unipr.ce.dsg.p2pgame.GUI3D;

import it.unipr.ce.dsg.p2pgame.platform.bot.RTSGameBot2;

/**
 *
 * @author giorgio
 */
public class RTSGameBotLauncher 
{
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
