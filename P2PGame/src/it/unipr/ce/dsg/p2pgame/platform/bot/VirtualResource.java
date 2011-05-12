package it.unipr.ce.dsg.p2pgame.platform.bot;

public class VirtualResource {
	
	String resType;
	String ownerID;
	int x;
	int y;
	
	
	public VirtualResource(String resType, String ownerID, int x, int y) {
		
		this.resType = resType;
		this.ownerID = ownerID;
		this.x = x;
		this.y = y;
	}
	
public VirtualResource() {
		
		
	}
	
	
	
	public String getResType() {
		return resType;
	}
	public void setResType(String resType) {
		this.resType = resType;
	}
	public String getOwnerID() {
		return ownerID;
	}
	public void setOwnerID(String ownerID) {
		this.ownerID = ownerID;
	}
	public int getX() {
		return x;
	}
	public void setX(int x) {
		this.x = x;
	}
	public int getY() {
		return y;
	}
	public void setY(int y) {
		this.y = y;
	}
	
	
	

}
