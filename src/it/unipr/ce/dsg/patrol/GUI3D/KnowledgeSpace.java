package it.unipr.ce.dsg.patrol.GUI3D;

import it.unipr.ce.dsg.patrol.platform.GamePlayerResponsible;
import it.unipr.ce.dsg.patrol.platform.GameResourceMobileResponsible;
import it.unipr.ce.dsg.patrol.platform.bot.VirtualResource;

public class KnowledgeSpace {

	enum SpaceType{

		UNKNOW,
		SPACE,
		PLANET,
		STARSHIP,
                UNCONQUEREDPLANET, //it's a try
                MYCONQUEREDPLANET, //it's a try
                ENEMYCONQUEREDPLANET //it's a try
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
        
        public void setToUnconqueredPlanet(VirtualResource planet)
        {
            this.type=SpaceType.UNCONQUEREDPLANET;
            this.element=planet;
        }
        
        public void setToMyConqueredPlanet(VirtualResource planet)
        {
            this.type=SpaceType.MYCONQUEREDPLANET;
            this.element=planet;
        }
        
        public void setToEnemyConqueredPlanet(VirtualResource planet)
        {
            this.type=SpaceType.ENEMYCONQUEREDPLANET;
            this.element=planet;
        }
}
