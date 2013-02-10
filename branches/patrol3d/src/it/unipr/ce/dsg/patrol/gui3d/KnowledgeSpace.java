package it.unipr.ce.dsg.patrol.gui3d;

import it.unipr.ce.dsg.patrol.platform.GamePlayerResponsible;
import it.unipr.ce.dsg.patrol.platform.GameResourceMobileResponsible;
import it.unipr.ce.dsg.patrol.platform.bot.VirtualResource;

/**
 * This class rapresent one piece, a tile, of information about the space
 * @author Michael Benassi Giorgio Micconi
 */
public class KnowledgeSpace
{
    enum SpaceType
    {
        UNKNOW,
	SPACE,
	PLANET, //TODO check this, maybe it can be deleted beacuse the other 3 at end are now used
	STARSHIP,
        UNCONQUEREDPLANET,
        MYCONQUEREDPLANET,
        ENEMYCONQUEREDPLANET
    };

    public KnowledgeSpace()
    {
        this.type = SpaceType.UNKNOW;
	this.element = null;
    }

    public SpaceType getType()
    {
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
        
    private SpaceType type;
    private Object element;
}
