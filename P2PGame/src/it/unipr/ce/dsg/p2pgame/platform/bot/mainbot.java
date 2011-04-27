package it.unipr.ce.dsg.p2pgame.platform.bot;

public class mainbot {
	
	public static void main(String [] arg)
	{
		Thread th=new Thread(new Bot());
		th.start();
		
	}

}
