package it.unipr.ce.dsg.p2pgame.platform.bot;

import java.util.ArrayList;

import it.unipr.ce.dsg.p2pgame.platform.GamePeer;
import it.unipr.ce.dsg.p2pgame.platform.GameResourceMobile;

public interface InterfaceBot {

	GameResourceMobile getResourceMobilebyID(String resid);

	void setMovStatus(String resid, boolean b);

	int getProbattack();

	VirtualResource getVResourcebyCoordinates(int k, int j);

	String getOwnerid();

	
	GamePeer getMyGamePeer();
	
	ArrayList<VirtualResource> getPlanets();
	
	void setPlanetOwner(String idPlanet,String idOwner);
	
	VirtualResource getPlanetbyID(String idPlanet);

	
}
