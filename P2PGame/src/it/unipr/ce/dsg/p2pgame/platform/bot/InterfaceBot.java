package it.unipr.ce.dsg.p2pgame.platform.bot;

import it.unipr.ce.dsg.p2pgame.platform.GameResourceMobile;

public interface InterfaceBot {

	GameResourceMobile getResourceMobilebyID(String resid);

	void setMovStatus(String resid, boolean b);

	int getProbattack();

	VirtualResource getVResourcebyCoordinates(int k, int j);

	String getOwnerid();

	
	
	
	

	
}
