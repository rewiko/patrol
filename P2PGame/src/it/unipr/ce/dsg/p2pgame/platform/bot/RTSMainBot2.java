package it.unipr.ce.dsg.p2pgame.platform.bot;

public class RTSMainBot2 {
	
	public  static void main(String [] arg)
	{
		Thread th=new Thread(new RTSGameBot2("profiles/profile1.txt","conf/gameconf.txt",6700,"usr1",9990));
		th.start();
		
	}

}