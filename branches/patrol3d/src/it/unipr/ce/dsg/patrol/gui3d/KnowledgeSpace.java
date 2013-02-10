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
    /**
     * It contains the possible types of the resource
     */
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

    /**
     * Constructor, it sets the type to UNKNOWN and the element to null
     */
    public KnowledgeSpace()
    {
        this.type = SpaceType.UNKNOW;
	this.element = null;
    }

    /**
     * Gets the type of the resource
     * @return the type
     */
    public SpaceType getType()
    {
        return type;
    }

    /**
     * Gets the element of the resource
     * @return 
     */
    public Object getElement()
    {
        return element;
    }

    /**
     * Sets the type to UNKNOWN and the element to null
     */
    public void setToUnknow()
    {
        this.type = SpaceType.UNKNOW;
        this.element = null;
    }

    /**
     * Sets the type to SPACE and the element to null
     */
    public void setToSpace()
    {
        this.type = SpaceType.SPACE;
	this.element = null;
    }

    /**
     * Sets the type to PLANET and the element to the home planet 'player'
     * TODO maybe it can be deleted
     * @param player the home planet
     */
    public void setToPlanet(GamePlayerResponsible player)
    {
        this.type = SpaceType.PLANET;
	this.element = player;
    }

    /**
     * Sets the type to STARSHIP and the element to 'starship'
     * @param starship the starship
     */
    public void setToShip(GameResourceMobileResponsible starship)
    {
        this.type = SpaceType.STARSHIP;
	this.element = starship;
    }
    
    /**
     * Sets the type to UNCONQUEREDPLANET and the element to 'planet'
     * @param planet the planet
     */
    public void setToUnconqueredPlanet(VirtualResource planet)
    {
        this.type=SpaceType.UNCONQUEREDPLANET;
        this.element=planet;
    }
    
    /**
     * Sets the type to MYCONQUEREDPLANET and the element to 'planet'
     * @param planet the planet
     */
    public void setToMyConqueredPlanet(VirtualResource planet)
    {
        this.type=SpaceType.MYCONQUEREDPLANET;
        this.element=planet;
    }
        
    /**
     * Sets the type to ENEMYCONQUEREDPLANET and the element to 'planet'
     * @param planet the planet
     */
    public void setToEnemyConqueredPlanet(VirtualResource planet)
    {
        this.type=SpaceType.ENEMYCONQUEREDPLANET;
        this.element=planet;
    }
        
    private SpaceType type;
    private Object element;
}
