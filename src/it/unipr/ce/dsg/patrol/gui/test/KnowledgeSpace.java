package it.unipr.ce.dsg.patrol.gui.test;

import it.unipr.ce.dsg.patrol.platform.GamePlayerResponsible;
import it.unipr.ce.dsg.patrol.platform.GameResourceMobileResponsible;
import it.unipr.ce.dsg.patrol.rtsdemo.*;

public class KnowledgeSpace {

	enum SpaceType{

		UNKNOW,
		SPACE,
		PLANET,
		STARSHIP

	};

	private SpaceType type;
	private Object element;

	public KnowledgeSpace(){
		this.type = SpaceType.UNKNOW;
		this.element = null;
	}



	public SpaceType getType() {
		return type;
	}

	public Object getElement() {
		return element;
	}



	public void setToUnknow(){
		this.type = SpaceType.UNKNOW;
		this.element = null;
	}

	public void setToSpace(){
		this.type = SpaceType.SPACE;
		this.element = null;
	}

	public void setToPlanet(GamePlayerResponsible player){
		this.type = SpaceType.PLANET;
		this.element = player;
	}

	public void setToShip(GameResourceMobileResponsible starship){
		this.type = SpaceType.STARSHIP;
		this.element = starship;
	}
}
