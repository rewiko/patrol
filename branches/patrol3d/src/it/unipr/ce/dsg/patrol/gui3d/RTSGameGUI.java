/*
 * RTSGameGUI.java
 *
 * Created on 10-feb-2012, 11.12.34
 */

package it.unipr.ce.dsg.patrol.gui3d;

//jMonkey's import
import com.jme3.app.FlyCamAppState;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.ScreenshotAppState;
import com.jme3.collision.CollisionResults;
import com.jme3.input.KeyInput;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.AnalogListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.MouseAxisTrigger;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.material.RenderState.BlendMode;
import com.jme3.material.RenderState.FaceCullMode;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Plane;
import com.jme3.math.Quaternion;
import com.jme3.math.Ray;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.niftygui.NiftyJmeDisplay;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.queue.RenderQueue.Bucket;
import com.jme3.renderer.queue.RenderQueue.ShadowMode;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Box;
import com.jme3.system.AppSettings;
import com.jme3.terrain.geomipmap.TerrainLodControl;
import com.jme3.terrain.heightmap.AbstractHeightMap;
import com.jme3.terrain.geomipmap.TerrainQuad;
import com.jme3.terrain.heightmap.ImageBasedHeightMap;
import com.jme3.texture.Texture;
import com.jme3.texture.Texture.WrapMode;
//NiftyGUI's import
import de.lessvoid.nifty.Nifty;
//PATROL's import
import de.lessvoid.nifty.elements.Element;
import de.lessvoid.nifty.elements.render.TextRenderer;
import it.simplexml.message.AckMessage;
import it.simplexml.message.Message;
import it.simplexml.message.MessageReader;
import it.unipr.ce.dsg.patrol.gui.message.content.Point;
import it.unipr.ce.dsg.patrol.gui3d.KnowledgeSpace.SpaceType;
import it.unipr.ce.dsg.patrol.platform.GamePlayer;
import it.unipr.ce.dsg.patrol.platform.GamePlayerResponsible;
import it.unipr.ce.dsg.patrol.platform.GameResourceMobile;
import it.unipr.ce.dsg.patrol.platform.GameResourceMobileResponsible;
import it.unipr.ce.dsg.patrol.platform.bot.UserInfo;
import it.unipr.ce.dsg.patrol.platform.bot.VirtualResource;
import it.unipr.ce.dsg.patrol.platform.bot.message.BotStartMatchRequestMessage;
import it.unipr.ce.dsg.patrol.platform.bot.message.BotStartMatchResponseMessage;
import it.unipr.ce.dsg.patrol.platform.bot.message.PlanetConqueredMessage;
import it.unipr.ce.dsg.patrol.util.MultiLog;
//other import
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

/**
 * PATROL's game
 * @author Michael Benassi Giorgio Micconi
 */
public class RTSGameGUI extends SimpleApplication 
{	
    /**
     * Main method for game's GUI
     * @param argv argv[0]=true -> show settings of the graphics at launch. argv will be passed to RTSGameGUI constructor.
     */
    @SuppressWarnings("ResultOfObjectAllocationIgnored")
    public static void main(String[] argv)
    {
        new MultiLog("ConfigFile.txt",true,true);  //initialization of MultiLog object
        //read the first argument to decide if the initial settings must be shown
        if (argv.length < 4)
        {
            System.err.println("RTSGameGUI launched without required (4) parameters");
            System.exit(1);
        }
        if(argv[0].equals("true"))
            showSettingsContr=true;
        else if(argv[0].equals("false"))
            showSettingsContr=false;
        RTSGameGUI app = new RTSGameGUI(argv);
        AppSettings settings = new AppSettings(true);
        settings.setRenderer(AppSettings.LWJGL_OPENGL2);
        settings.setWidth(width);
        settings.setHeight(height);
        settings.setFullscreen(fullScreen);
        settings.setDepthBits(colorDepth);
        settings.setSamples(samples);
        settings.setVSync(vSynch);
        settings.setTitle(title);
        app.setShowSettings(showSettingsContr);
        app.setSettings(settings);
        app.start();
    }

    /**
     * Constructor of RTSGameGUI, initializes parameters
     * @param argv argv[1]=true -> show welcome, login and loading gui, game mode. argv[2]=true -> fps rate will be shown. argv[3]=true -> state view information will be shown.
     */
    public RTSGameGUI (String[] argv)
    {
        //read the second argument to decide if the initial GUI must be shown
        if(argv[1].equals("true"))
            this.showGUI=true;
        else if(argv[1].equals("false"))
            this.showGUI=false;
        //read the third argument to decide if fps must be shown
        if(argv[2].equals("true"))
            this.displayFPS=true;
        else if(argv[2].equals("false"))
            this.displayFPS=false;
        //read the second argument to decide if state view information must be shown
        if(argv[3].equals("true"))
            this.displayStatView=true;
        else if(argv[3].equals("false"))
            this.displayStatView=false;
        /*initialize screenController class, values shown in login screen
        They will be surely used in development mode.
        In game mode they will be shown to the user and he will can change them*/
        this.screenController=new MyScreenController(this);
        this.screenController.setUser("user");
        this.screenController.setPwd("pwd");
        this.screenController.setServerAddress("127.0.0.1");
        this.screenController.setServerPort(1235);
        this.screenController.setOutPort(40000);
        this.screenController.setMessagePort(9998);
        this.screenController.setActualCentralPosition("0 0");
        this.screenController.setSelection("Null");
    }
    
    /**
     * jMonkey's fundamental method override, it creates the scene at startup.
     */
    @Override
    public void simpleInitApp() 
    {
        ScreenshotAppState state = new ScreenshotAppState();  //ScreenshotAppState class allows to take a screenshot
        this.stateManager.attach(state);
        stateManager.detach( stateManager.getState( FlyCamAppState.class ) );   //detaching of FlyCamAppState so the user can't move the camera in login step.
        this.setDisplayFps(displayFPS);
        this.setDisplayStatView(displayStatView);
        flyCam.setMoveSpeed(150); //set cam speed
        this.transformer=new CoordinatesMapping();
        this.rootNode.setCullHint(Spatial.CullHint.Dynamic);    //enable cutting hidden faces 
        this.rootNode.setShadowMode(ShadowMode.Off);    //shadow disabled
        this.enemyShipMap=new HashMap<String,Spatial>();
        this.ready=false;
        if(this.showGUI)
            {
            //all screens are visible
            //initialization of all gui's screens
            NiftyJmeDisplay niftyDisplay=new NiftyJmeDisplay(assetManager, inputManager, audioRenderer, guiViewPort);
            this.nifty=niftyDisplay.getNifty();
            this.nifty.fromXml("Interface/welcome.xml","start",this.screenController);
            this.nifty.addXml("Interface/login.xml");
            this.nifty.addXml("Interface/loadingGeneral.xml");
            this.nifty.addXml("Interface/loadingNetwork.xml");
            this.nifty.addXml("Interface/loadingKnowledges.xml");
            this.nifty.addXml("Interface/loadingTerrain.xml");
            this.nifty.addXml("Interface/loadingGrid.xml");
            this.nifty.addXml("Interface/loadingLights.xml");
            this.nifty.addXml("Interface/loadingHome.xml");
            this.nifty.addXml("Interface/loadingKeys.xml");
            this.nifty.addXml("Interface/HUD.xml");
            this.nifty.addXml("Interface/purchaseDefenseWindow.xml");
            this.nifty.addXml("Interface/purchaseShipWindow.xml");
            this.nifty.addXml("Interface/endGame.xml");
            this.stateManager.attach(this.screenController);    //the screen controller must be attach to state manager otherwise we can't use the GUI
            guiViewPort.addProcessor(niftyDisplay);
            flyCam.setDragToRotate(true); //disable mouse interaction in graphic scene
            }
        else
            {
            //developers mode, default settings, no welcome/login/loading screens
            NiftyJmeDisplay niftyDisplay=new NiftyJmeDisplay(assetManager, inputManager, audioRenderer, guiViewPort);
            this.nifty=niftyDisplay.getNifty();
            this.nifty.fromXml("Interface/HUD_start.xml","start",this.screenController);
            this.nifty.addXml("Interface/HUD.xml");
            this.nifty.addXml("Interface/purchaseDefenseWindow.xml");
            this.nifty.addXml("Interface/purchaseShipWindow.xml");
            this.nifty.addXml("Interface/endGame.xml");
            this.stateManager.attach(this.screenController);    //the screen controller must be attach to state manager otherwise we can't use the GUI
            guiViewPort.addProcessor(niftyDisplay);
            //initialization method
            this.connect();
            this.initializeKnowledges();
            this.initTerrain();
            this.initGroundGrid();
            this.addLightToScene();
            this.getHomePlanetAndDefense();
            this.initKeys();
            this.updatePositionGUI();
            this.screenController.startGameWithoutGUI();
            this.getFlyByCamera().setDragToRotate(false);  //to allow the user to interact using mouse
            this.setReadyState(true);
            }
    }
    
    /**
     * Provides connection among GUI and game peer (Stefano) 
     */
    public void connect()
    {
        if(this.showGUI)
            {
            //in game mode
            //takes values inserted by the user in login screen, from the GUI
            this.outPort=Integer.parseInt(this.screenController.getOutPort());
            this.serverAddr=this.screenController.getServerAddress();
            this.serverPort=Integer.parseInt(this.screenController.getServerPort());
            this.user=this.screenController.getUser();
            this.pwd=this.screenController.getPwd();
            this.gamePeerPort=Integer.parseInt(this.screenController.getMessagePort());
            this.shipModel="Models/"+this.screenController.getShipModel()+"/"+this.screenController.getShipModel()+".j3o";
            }
        else
            {
            //TODO delete this part, assign these values in the constructor, before passing the same values to nifty
            //in developerment mode
            this.outPort=40000;
            this.serverAddr="127.0.0.1";
            this.serverPort=1235;
            this.user="giorgio";
            this.pwd="ggg";
            this.gamePeerPort=9998;
            this.shipModel="Models/spaceship-red/spaceship-red.j3o";
            }
        //the following values are the fixed, not to change
        this.inPort=this.outPort;
        this.id="";
        this.idLength=160;
        this.gameInPort=this.outPort+2/*3*/;
        this.gameOutPort=this.outPort+2;
        this.gameServerAddr=this.serverAddr;
        this.gameServerPort=this.serverPort+2; 
        this.stab=4000;
        this.fix=1000;
        this.check=64000;
        this.pub=2000;
        System.out.println(this.user+" "+this.pwd+" "+this.serverAddr+" "+this.serverPort+" "+this.outPort+" "+this.gamePeerPort);
        this.request=new MessageSender(this.gamePeerPort);
        //start of communication between bootstrap server and GUI
        MultiLog.println(RTSGameGUI.class.toString(), "Try to connect and register...");
        this.request.CreateGamePeer(inPort, outPort, idLength, id, serverAddr, serverPort, gameInPort, gameOutPort, gameServerAddr, gameServerPort, stab, fix, check, pub);
        MultiLog.println(RTSGameGUI.class.toString(), "Peer is created");
        this.request.registerOnServer(this.user,this.pwd);
        MultiLog.println(RTSGameGUI.class.toString(), "Peer is registrated into the network");
        this.confRead();    //reading of configuration values
        MultiLog.println(RTSGameGUI.class.toString(), "Speed: "+this.speedPlat+" Visibility: "+this.vis+" Granularity: "+this.gran);
        this.request.startGame(this.minXField, this.maxXField, this.minYField, this.maxYField, this.minZField, this.maxZField, this.speedPlat, this.vis, this.gran);
        this.playerId=this.request.getGamePeerId();
        this.playerName=this.playerId;  //TODO playerName is equal to playerId
        MultiLog.println(RTSGameGUI.class.toString(),"Player ID: "+this.playerId);
        this.initMoneyResource();
        this.screenController.setResMinCost((float)this.resMinCost);    //sets the min cost of resource in GUI for sliders
    }
    
    /**
     * Sets the coordinates of the point of view of the player
     */
    public void updatePositionGUI()
    {
        Vector3f v=this.cam.getLocation();
        this.screenController.setActualCentralPosition(v.x+" "+v.z);
    }
    
    /**
    * Reads configuration value and the list of unknwon planet from file
    * Read file READMEgameconf.txt to know what they means
    */
    @SuppressWarnings("CallToThreadDumpStack")
    private void confRead()
    {
        String configuration="conf/gameconf.txt";
        try
            {
            File fconf=new File(configuration);
            FileInputStream fisconf=new FileInputStream(fconf);
            InputStreamReader isrconf=new InputStreamReader(fisconf);
	    BufferedReader brconf=new BufferedReader(isrconf);
	    String straux=brconf.readLine();
            this.minXField=Double.parseDouble(straux);
            straux=brconf.readLine();
            this.maxXField=Double.parseDouble(straux);
            straux=brconf.readLine();
            this.minYField=Double.parseDouble(straux);
            straux=brconf.readLine();
            this.maxYField=Double.parseDouble(straux);
            straux=brconf.readLine();
            this.minZField=Double.parseDouble(straux);
            straux=brconf.readLine();
            this.maxZField=Double.parseDouble(straux);
            straux=brconf.readLine();
            this.speedPlat=Float.parseFloat(straux);
            straux=brconf.readLine();
            this.vis=Double.parseDouble(straux);
            straux=brconf.readLine();
            this.gran=Double.parseDouble(straux);
            this.widthField=this.maxXField-this.minXField;
            this.heightField=this.maxYField-this.minYField;
            straux=brconf.readLine();
            this.resMinCost=Double.parseDouble(straux);
            straux=brconf.readLine();
	    int nplanets=Integer.parseInt(straux);
            this.planets=new ArrayList<VirtualResource>();
	    for(int i=0;i<nplanets;i++)
	        {
                straux=brconf.readLine();	        	
	        String [] str_cord=straux.split(",");
	        VirtualResource planet=new VirtualResource();
	        planet.setOwnerID("null");
	        planet.setOwnerName("null");
	        planet.setId("planet"+i);
	        planet.setResType("planet");
	        planet.setX(Double.parseDouble(str_cord[0]));
	        planet.setY(Double.parseDouble(str_cord[1]));
	        planet.setZ(Double.parseDouble(str_cord[2]));
	        this.planets.add(planet);	
	        }
            this.delta=(this.maxXField-this.minXField)/2;
            MultiLog.println(RTSGameGUI.class.toString(),this.minXField+" "+this.maxXField+" "+this.minYField+" "+this.maxYField+" "+this.minZField+" "+this.maxZField+" delta:"+((this.maxXField-this.minXField)/2));
            }
        catch (FileNotFoundException e1)
            {
            // TODO Auto-generated catch block
            e1.printStackTrace();
            }
        catch (IOException e)
            {			
            // TODO Auto-generated catch block
            e.printStackTrace();
            }
    }
    
    /**
     * Terrain's initialization function
     */
    public void initTerrain()
    {
        // 1. Create terrain material and load four textures into it.
        mat_terrain = new Material(assetManager, "Common/MatDefs/Terrain/Terrain.j3md");
        // 1.1) Add ALPHA map (for red-blue-green coded splat textures)
        //this map defines position of different textures on the ground, using colours. In this case we have only 1 texture for the ground, so it's completely red
        mat_terrain.setTexture("Alpha", assetManager.loadTexture("Textures/Terrain/splat/groundmap.png"));
        // 1.2) Add sky texture into the red layer (Tex1).
        Texture sky = assetManager.loadTexture("Textures/Terrain/splat/sky3.jpg");
        sky.setWrap(WrapMode.Repeat);   //this means that the texture will be repeated whether is needed
        mat_terrain.setTexture("Tex1", sky);    //application of the texture on the ground
        mat_terrain.setFloat("Tex1Scale", 1f);   //scale of the texture
        @SuppressWarnings("UnusedAssignment")
        AbstractHeightMap heightmap = null;
        //This map defines the 2D altimetric profile of the space, is completely red because it has to be flat everywhere
        Texture heightMapImage = assetManager.loadTexture("Textures/Terrain/splat/ground.png");
        heightmap = new ImageBasedHeightMap(heightMapImage.getImage());
        heightmap.load();
        /* 3. We have prepared material and heightmap. 
         Now we create the actual terrain:
         3.1) Create a TerrainQuad and name it "my terrain".
         3.2) A good value for terrain tiles is 64x64 -- so we supply 64+1=65.
         3.3) We prepared a heightmap of size 512x512 -- so we supply 512+1=513.
         3.4) As LOD step scale we supply Vector3f(1,1,1).
         3.5) We supply the prepared heightmap itself.
        */
        int patchSize = 1025; //risolution of the background's image + 1
        terrain = new TerrainQuad("my terrain", patchSize, /*513*/1025, heightmap.getHeightMap());
        // 4. We give the terrain its material, position & scale it, and attach it.
        terrain.setMaterial(mat_terrain);
        terrain.setLocalTranslation(0.0f, -5.0f, 0.0f);
        terrain.setLocalScale((float)(this.widthField)/1025,1.0f,(float)(this.heightField)/1025);//it's scaled to reach the right dimensions
        terrain.setName("terrain");
        rootNode.attachChild(terrain);
        // 5. The LOD (level of detail) depends on were the camera is:
        TerrainLodControl control = new TerrainLodControl(terrain, getCamera());
        terrain.addControl(control);
    }
    
    /**
     * Initialization of the game's grid
     */
    public void initGroundGrid()
    {
        //creations of the tile's material
        tileMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        tileMat.setTexture("ColorMap",assetManager.loadTexture("Textures/Grid/grid.png"));
        tileMat.getAdditionalRenderState().setBlendMode(BlendMode.Alpha); // activate transparency
        tileMat.setReceivesShadows(false);
        tileMat.preload(renderManager); //for matter of efficiency
        tileMat.getAdditionalRenderState().setFaceCullMode(FaceCullMode.Back);  //not render back faces
        //creations of the fog of war's model
        fogMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        fogMat.setTexture("ColorMap",assetManager.loadTexture("Textures/FogOfWar/fog4.png"));
        fogMat.setReceivesShadows(false);
        fogMat.preload(renderManager);  //for matter of efficiency
        fogMat.getAdditionalRenderState().setFaceCullMode(FaceCullMode.Back);   //not render back faces
        //creations of the tile's model
        Box tile = new Box(Vector3f.ZERO, (float)this.gran/2, 0.01f, (float)this.gran/2);
        Geometry tileGeom = new Geometry("tile", tile);
        tileGeom.setMaterial(fogMat);
        MultiLog.println(RTSGameGUI.class.toString(),"Initialize grid of the ground, tile dimension is "+this.gran+", field dimensions are "+this.widthField+"x"+this.heightField);
        //creations of the entire grid and the node of the grid
        gridNode=new Node("gridNode");
        for(int i=0;i<(this.widthField/this.gran);i++)
            for(int j=0;j<(this.heightField/this.gran);j++)
                {
                Geometry other=tileGeom.clone();
                other.setLocalTranslation(new Vector3f((float)((-(this.widthField)/2+this.gran/2)+this.gran*i),0.0f,(float)((-(this.heightField)/2+this.gran/2)+this.gran*j)));
                other.setUserData("index",(j*this.numTileX + i));
                other.setName("tile"+(j*this.numTileX + i));
                gridNode.attachChild(other);
                }
        gridNode.setLocalTranslation(gridNode.getLocalTranslation().getX()-(float)(this.gran/2),gridNode.getLocalTranslation().getY(),gridNode.getLocalTranslation().getZ()-(float)(this.gran/2));
        rootNode.attachChild(gridNode);
        MultiLog.println(RTSGameGUI.class.toString(),"Created "+(int)((this.widthField/this.gran)*(this.heightField/this.gran))+" tile");
    }
    
    /**
     * Initialization of the money generator resource
     */
    private void initMoneyResource()
    {
        this.request.addResourceEvolve("moneyEvolveble","Money",0,1000,1);
    }
    
    /**
     * Initial creation of the Home Planet and acquisition of the first resource of defence
     */
    public void getHomePlanetAndDefense()
    {
        System.out.println("Home planet");
        Point point=this.request.getGamePlayerPosition();
        MultiLog.println(RTSGameGUI.class.toString(),"Home planet position: "+point.getX()+" "+point.getY()+" delta:"+(this.maxXField-this.minXField)/2);
        Coordinate pos=this.transformer.patrolTojMonkey(point,(this.maxXField-this.minXField)/2);
        this.homePlanetCoord=pos;
        MyPlanetControl planetControl=new MyPlanetControl(this.planetRotSpeed);
        MyDefenseControl defenseControl=new MyDefenseControl(this.defenseRotSpeed);
        Spatial homePlanet=assetManager.loadModel("Models/planet1-1/planet1-1.j3o");
        homePlanet.addControl(planetControl);
        homePlanet.setName("home");
        homePlanet.setUserData("type","fixed");
        homePlanet.setUserData("ownerId",this.playerId);
        homePlanet.setUserData("hasDefense", true);
        homePlanet.setLocalScale(planetScale);
        homePlanet.setLocalTranslation(pos.getIX(),pos.getIY(),pos.getIZ());
        rootNode.attachChild(homePlanet);
        //at the begining, the home planet is selected by default
        this.crown=assetManager.loadModel("Models/crown3/crown3.j3o");
        this.crown.setName("SelectionCrown");
        this.crown.setLocalScale(2.5f, 1.0f, 2.5f);
        this.crown.setLocalTranslation(homePlanet.getLocalTranslation());
        rootNode.attachChild(crown);
        //at the begining, the point of view is centered on the home planet
        this.cam.setLocation(new Vector3f(pos.getIX(),20.0f+pos.getIY(),(pos.getIZ()-this.distCamera)));
        this.cam.lookAt(new Vector3f(pos.getIX(),pos.getIY(),pos.getIZ()), Vector3f.UNIT_Y);
        MultiLog.println(RTSGameGUI.class.toString(),"pianeta: "+pos.getX()+" "+pos.getY()+" "+pos.getZ()+" io "+pos.getX()+" "+(20.0f+(float) pos.getY())+" "+(pos.getZ()-this.distCamera));
        //there must be at least one defensive resource
        System.out.println("COMPRATA DIFESA");
        String timestamp = Long.toString(System.currentTimeMillis());
        this.request.addResource("strdef" + timestamp, "Defense" + timestamp, 1.0);
        Spatial defense = assetManager.loadModel("Models/defense/defense.j3o");
        defense.setLocalScale(1.6f);
        defense.setName("defense");
        defense.setUserData("type","fixed");
        defense.setUserData("numDefense", 1);
        defense.addControl(defenseControl);
        defense.setLocalTranslation(pos.getIX(),pos.getIY(),pos.getIZ());
        rootNode.attachChild(defense);
        homePlanet.setUserData("defenseName", defense.getName());
    }
    
    /**
     * Sets the member ready to state's value. This member indicates when the GUI has finished the initialization
     * @param state 
     */
    public void setReadyState(boolean state)
    {
        this.ready=state;
    }
    
    /**
     * Function that buy and create a mobile resource, if there is enough money
     * @param value power of the starship and its cost
     * @return true if the ship was created, false otherwise
     */
    public boolean buyAndCreateMobileResource(Float value)
    {
        MultiLog.println(RTSGameGUI.class.toString(),"value="+value+" min="+this.resMinCost);
        if(value<this.resMinCost)
            return false;
        MultiLog.println(RTSGameGUI.class.toString(),"actual money="+this.request.getMyResourceFromId("moneyEvolveble").getQuantity());
        if(this.request.getMyResourceFromId("moneyEvolveble").getQuantity()-value<0)
            return false;
        MultiLog.println(RTSGameGUI.class.toString(),"Creating a ship spatial");
        String timestamp = Long.toString(System.currentTimeMillis());
        this.request.createMobileResource("Attack"+timestamp, value);
        this.request.UpdateResourceEvolve(this.request.getMyResourceFromId("moneyEvolveble").getQuantity()-value); 
        ArrayList<Object> myResources=this.request.getResources();
        //TODO check if last resource is a mobile resource, for safety
        GameResourceMobile grm=(GameResourceMobile) myResources.get(myResources.size()-1);
        MultiLog.println(RTSGameGUI.class.toString(),"Comprata astronave: x:"+grm.getX()+" z:"+grm.getY());
        Coordinate pos=this.transformer.patrolTojMonkey(this.request.getGamePlayerPosition(),this.delta);
        MyShipControl shipControl=new MyShipControl(1.0f);   
        MultiLog.println(RTSGameGUI.class.toString(),"model to load="+this.shipModel);
        Spatial spaceship =assetManager.loadModel(this.shipModel);
        spaceship.setName("spaceship_"+timestamp);
        spaceship.setUserData("id", grm.getId());
        spaceship.setUserData("owner", this.playerId);
        spaceship.setUserData("ownerID", this.playerId);
        spaceship.setUserData("type", "mobile");
        spaceship.setLocalScale(shipScale);
        spaceship.addControl(shipControl);
        spaceship.setLocalTranslation((int)(pos.getX()),(int)(pos.getY()),(int)(pos.getZ()));
        rootNode.attachChild(spaceship);
        MultiLog.println(RTSGameGUI.class.toString(),"Moving ship out of planet");
        if(pos.getIZ()>(-this.delta))
            this.moveOnCreation(spaceship,new Vector3f((int)(pos.getX()),(int)(pos.getY()),(int)(pos.getZ()-this.gran)));
        else
            this.moveOnCreation(spaceship,new Vector3f((int)(pos.getX()),(int)(pos.getY()),(int)(pos.getZ()+this.gran)));
        MultiLog.println(RTSGameGUI.class.toString(),"Ship was created and moved");
        return true;
    }
    
    /**
     * Function that buy and create a defensive resource, if there is enough money
     * @param value power of the defence and its cost
     * @return true if the defence was created, false otherwise
     */
    boolean buyAndCreateDefence(Float value)
    {
        if(value<this.resMinCost)
            return false;
        if(this.request.getMyResourceFromId("moneyEvolveble").getQuantity()-value<0)
            return false;
        String timestamp = Long.toString(System.currentTimeMillis());
        this.request.addResource("strdef" + timestamp, "Defense" + timestamp, value);
        this.request.UpdateResourceEvolve(this.request.getMyResourceFromId("moneyEvolveble").getQuantity()-value); 
        return true;
    }
    
    /**
     * Creates illumination for the scene
     */
    public void addLightToScene()
    {
        DirectionalLight sun = new DirectionalLight();
        sun.setDirection(new Vector3f(-0.5f,-0.5f,-0.5f).normalizeLocal());
        sun.setColor(new ColorRGBA(1.0f,1.0f,1.0f,1.0f));
        rootNode.addLight(sun);
        DirectionalLight sun2 = new DirectionalLight();
        sun2.setDirection(new Vector3f(0.5f,-0.5f,0.5f).normalizeLocal());
        sun2.setColor(new ColorRGBA(1.0f,1.0f,1.0f,1.0f));
        rootNode.addLight(sun2);
        AmbientLight al = new AmbientLight();
        al.setColor(ColorRGBA.White.mult(1.0f));
        rootNode.addLight(al);
    }
    
    /**
     * Initialization of every single cell of the space, at the begining it's all empty
     */
    public void initializeKnowledges()
    {
        this.numTileX = (int)(this.widthField / this.gran);
        this.numTileY = (int)(this.heightField / this.gran);
        this.knowledges = new ArrayList<KnowledgeSpace>();
        for (int y=0; y < this.numTileY; y++)
            for (int x=0; x < this.numTileX; x++)
                {
                KnowledgeSpace known = new KnowledgeSpace();
		this.knowledges.add(known);
		}
	}
    
    /**
     * Provides vision around the home planet of the player and updates the knowledgeSpace with what is within
     */
    private void getPlanetVision() 
    {
        ArrayList<Object> vision=this.request.getVision();  //list of all visible things to user
        GamePlayer player=this.request.getGamePlayer();
        //double gran=this.request.getGranularity();
        //TODO visibility and granularity parameter should be taken from the net, using request method
        for (int i_x = (int) (player.getPosX() - this.vis); i_x <= player.getPosX() + this.vis; i_x += this.gran)
            for (int i_y = (int) (player.getPosY() - this.vis); i_y <= player.getPosY() + this.vis; i_y += this.gran)
                if(i_x>=this.minXField && i_y>=this.minYField && i_x<this.maxXField && i_y<this.maxYField)
                    if ( !(i_x == player.getPosX() && i_y == player.getPosY()) )
                        this.addNewInfo(i_x, i_y, null);
    	for (int i = 0; i < vision.size(); i++)
            if (vision.get(i) instanceof GamePlayerResponsible)
                {
        	GamePlayerResponsible playerResp = (GamePlayerResponsible) vision.get(i);
       		this.addNewInfo((int) playerResp.getPosX(), (int) playerResp.getPosY(), playerResp);
        	}
            else if (vision.get(i) instanceof GameResourceMobileResponsible)
                {
        	GameResourceMobileResponsible resource = (GameResourceMobileResponsible) vision.get(i);
        	this.addNewInfo( (int) resource.getX(), (int) resource.getY(), resource);
                if(!resource.getOwnerId().equals(this.playerId))
                    {
                    MultiLog.println(RTSGameGUI.class.toString(), "ENEMY SHIP OWNER ID="+resource.getOwnerId());
                    if(!this.enemyShipMap.containsKey(resource.getId()))
                        this.createEnemyStarship((int) resource.getX(), (int) resource.getY(), resource);
                    else
                        this.enemyShipMap.get(resource.getId()).setUserData("visible", "true");
                    }
                }
        Vector3f center=new Vector3f((float)player.getPosX(),(float)player.getPosY(),(float)player.getPosZ());
        for(int i=0;i<this.planets.size();i++)
            {
            VirtualResource planet=this.planets.get(i);
            Vector3f planetPos=new Vector3f((float)planet.getX(),(float)planet.getY(),(float)planet.getZ());
            if(this.isInRange(planetPos,center,this.vis))
                this.addNewInfo((int)planetPos.getX(),(int)planetPos.getY(),planet);
            }
    	GamePlayerResponsible planet = new GamePlayerResponsible(player.getId(), player.getName(), player.getPosX(),player.getPosY(),0, 0,10,0,"","" );
	this.addMyPlanetPosition( (int) Math.round(player.getPosX()), (int) Math.round(player.getPosY()),planet);
    }
    
    /**
     * Provides vision around starship of the player and updates the knowledgeSpace with what is within
     * @param mob the starship considered
     * @param gran granularity value
     * @param player player descriptor
     */
    private void getStarShipVision(GameResourceMobile mob, double gran, GamePlayer player)
    {
    //vision parameter must be taken by net
    for (int i_x = (int) (mob.getX() - this.vis); i_x <= mob.getX() + this.vis; i_x += gran)
	for (int i_y = (int) (mob.getY() - this.vis); i_y <= mob.getY() + this.vis; i_y += gran )
            if(i_x>=this.minXField && i_y>=this.minYField && i_x<this.maxXField && i_y<this.maxYField)
                if ( !(i_x == player.getPosX() && i_y == player.getPosY()) && !(i_x == mob.getX() && i_y == mob.getY()))
                    this.addNewInfo(i_x, i_y, null);
    ArrayList<Object> mobVis = mob.getResourceVision();
    for (int k = 0; k < mobVis.size(); k++)
	if (mobVis.get(k) instanceof GamePlayerResponsible)
        {
            GamePlayerResponsible playerResp = (GamePlayerResponsible) mobVis.get(k);
            this.addNewInfo( (int) playerResp.getPosX() , (int) playerResp.getPosY(), playerResp);
        }
	else if (mobVis.get(k) instanceof GameResourceMobileResponsible)
        {
            GameResourceMobileResponsible resource = (GameResourceMobileResponsible) mobVis.get(k);
            this.addNewInfo((int) resource.getX(), (int) resource.getY(), resource);
            if(!resource.getOwnerId().equals(this.playerId))
            {
                MultiLog.println(RTSGameGUI.class.toString(), "ENEMY SHIP OWNER ID="+resource.getOwnerId());
                if(!this.enemyShipMap.containsKey(resource.getId()))
                    this.createEnemyStarship((int) resource.getX(), (int) resource.getY(), resource);
                else
                    this.enemyShipMap.get(resource.getId()).setUserData("visible", "true");
            }
        }
    Vector3f center=new Vector3f((float)mob.getX(),(float)mob.getY(),(float)mob.getZ());
    for(int i=0;i<this.planets.size();i++)
    {
        VirtualResource planet=this.planets.get(i);
        Vector3f planetPos=new Vector3f((float)planet.getX(),(float)planet.getY(),(float)planet.getZ());
        if(this.isInRange(planetPos,center,this.vis))
            this.addNewInfo((int)planetPos.getX(),(int)planetPos.getY(),planet);
    }
    GameResourceMobileResponsible ship = new GameResourceMobileResponsible(mob.getId(), mob.getDescription(), mob.getOwner(), mob.getOwnerId(),mob.getQuantity(), mob.getX(), mob.getY(), 0, mob.getVelocity(), /*mob.getVision()*/this.vis, 0, "", "");
    if ( !(mob.getX() == player.getPosX() && mob.getY() == player.getPosY()) )
        this.addNewInfo((int) mob.getX(), (int) mob.getY(), ship);
    }
    
    /**
     * Add home planet position to knowledgeSpace
     * @param x x coordinate of home planet
     * @param y y coordinate of home planet
     * @param myPlanet the home planet
     */
    public void addMyPlanetPosition(int x, int y, GamePlayerResponsible myPlanet)
    {
	int x_pos = (int) Math.round( x / this.gran - 0.5);
	int y_pos = (int) Math.round( y / this.gran - 0.5);
	KnowledgeSpace planet = new KnowledgeSpace();
	planet.setToPlanet(myPlanet);
	this.knowledges.set((int)Math.round(y_pos*this.numTileX + x_pos), planet);
    }
    
    /**
     * Add new information about the obj Object
     * @param x x coordinate of obj
     * @param y y coordinate of obj
     * @param obj the object
     */
    public void addNewInfo(int x, int y,Object obj)
    {
        int x_pos = (int) Math.round( (double) x/(double) this.gran - 0.5);
	int y_pos = (int) Math.round( (double) y/(double) this.gran - 0.5);
	//checks the type of the istance obj e creates the right knowledege
        if((int)Math.round(y_pos*this.numTileX + x_pos)>=this.knowledges.size() || (int)Math.round(y_pos*this.numTileX + x_pos)<0)
            return;
	if (obj == null)
            {
            KnowledgeSpace space = new KnowledgeSpace();
            space.setToSpace();
            this.knowledges.set((int)Math.round(y_pos*this.numTileX + x_pos), space);
            }
	else if (obj instanceof GameResourceMobileResponsible)
            {
            KnowledgeSpace ship = new KnowledgeSpace();
            ship.setToShip( (GameResourceMobileResponsible) obj);
            this.knowledges.set((int)Math.round(y_pos*this.numTileX + x_pos), ship);
            }
	else if (obj instanceof GamePlayerResponsible)
            {
            KnowledgeSpace planet = new KnowledgeSpace();
            planet.setToPlanet( (GamePlayerResponsible) obj);
            this.knowledges.set((int)Math.round(y_pos*this.numTileX + x_pos), planet);
            }
        else if (obj instanceof VirtualResource)
            {
            KnowledgeSpace planetKs = new KnowledgeSpace();
            VirtualResource planet=(VirtualResource) obj;
            if(planet.getOwnerID().equals("null"))
                planetKs.setToUnconqueredPlanet(planet);
            else if(planet.getOwnerID().equals(this.playerId))
                planetKs.setToMyConqueredPlanet(planet);
            else
                planetKs.setToEnemyConqueredPlanet(planet);
            this.knowledges.set((int)Math.round(y_pos*this.numTileX + x_pos), planetKs);
            for(int i=0;i<=this.planets.size();i++)
                if(this.planets.get(i).getId().equals(planet.getId()))
                    {
                    this.planets.get(i).setOwnerID(planet.getOwnerID());
                    this.planets.get(i).setOwnerName(planet.getOwnerName());
                    break;
                    }
            }
    }
    
    /**
     * Check if the pos coordinate is within the range vis of center
     * @param pos position to check
     * @param center center coordinates of the range
     * @param vis range of vision, from the center
     * @return true if pos is in range, false otherwise
     */
    private boolean isInRange(Vector3f pos,Vector3f center,double vis)
    {
        if((pos.getX()<=(center.getX()+vis) && pos.getX()>=(center.getX()-vis)) && (pos.getY()<=(center.getY()+vis) && pos.getY()>=(center.getY()-vis)))
            return true;
        else
            return false;
    }
      
    /**
     * Custom Keybinding: Map named actions to inputs.
     */
    public void initKeys() 
    {
        MultiLog.println(RTSGameGUI.class.toString(),"InitKeys");
        this.inputManager.clearMappings();
        this.inputManager.setCursorVisible(true);
        // You can map one or several inputs to one named action
        this.inputManager.addMapping("Exit", new KeyTrigger(KeyInput.KEY_ESCAPE));
        this.inputManager.addMapping("Mouse_Left_Button_click",new MouseButtonTrigger(MouseInput.BUTTON_LEFT));
        this.inputManager.addMapping("Mouse_Right_Button_click",new MouseButtonTrigger(MouseInput.BUTTON_RIGHT));
        this.inputManager.addMapping("Mouse_movement_X_l",new MouseAxisTrigger(MouseInput.AXIS_X,true));
        this.inputManager.addMapping("Mouse_movement_X_r",new MouseAxisTrigger(MouseInput.AXIS_X,false));
        this.inputManager.addMapping("Mouse_movement_Y_d",new MouseAxisTrigger(MouseInput.AXIS_Y,true));
        this.inputManager.addMapping("Mouse_movement_Y_u",new MouseAxisTrigger(MouseInput.AXIS_Y,false));
        this.inputManager.addMapping("Mouse_wheel_u",new MouseAxisTrigger(MouseInput.AXIS_WHEEL,false));
        this.inputManager.addMapping("Mouse_wheel_d",new MouseAxisTrigger(MouseInput.AXIS_WHEEL,true));
        this.inputManager.addMapping("Left_arrow",new KeyTrigger(KeyInput.KEY_LEFT));
        this.inputManager.addMapping("Right_arrow",new KeyTrigger(KeyInput.KEY_RIGHT));
        this.inputManager.addMapping("Up_arrow",new KeyTrigger(KeyInput.KEY_UP));
        this.inputManager.addMapping("Down_arrow",new KeyTrigger(KeyInput.KEY_DOWN));
        this.inputManager.addMapping("+",new KeyTrigger(KeyInput.KEY_ADD));
        this.inputManager.addMapping("-",new KeyTrigger(KeyInput.KEY_SUBTRACT));
        this.inputManager.addMapping("Pause", new KeyTrigger(KeyInput.KEY_P),new KeyTrigger(KeyInput.KEY_PAUSE));
        this.inputManager.addMapping("Toggle_HUD",new KeyTrigger(KeyInput.KEY_V));
        this.inputManager.addMapping("Shift_r_l", new KeyTrigger(KeyInput.KEY_LSHIFT),new KeyTrigger(KeyInput.KEY_RSHIFT));
        // Add the names to the action listener.
        inputManager.addListener(actionListener, new String[]{"Pause","Toggle_HUD","Mouse_Left_Button_click","Mouse_Right_Button_click","Shift_r_l"});
        inputManager.addListener(analogListener, new String[]{"Exit","Mouse_movement_X_l","Mouse_movement_X_r", "Mouse_movement_Y_d","Mouse_movement_Y_u","Mouse_wheel_u","Mouse_wheel_d", "Left_arrow", "Right_arrow", "Up_arrow", "Down_arrow","+","-"});
    }
 
    /**
     * Listener for keys and mouse buttons. Discrete inputs.
     */  
  private ActionListener actionListener = new ActionListener()
  {
      /**
       * Callback launched when a discrete event is occured.
       * @param name identifier assigned to this event in initKeys()
       * @param keyPressed true if a key of the keyboard or a button of the mouse was pressed
       * @param tpf time per frame
       */
    public void onAction(String name, boolean keyPressed, float tpf) 
    {
        //TODO Pause command can be deleted because it's not useful in multiplayer game
        if (name.equals("Pause") && !keyPressed)
            isRunning = !isRunning;
        else if(name.equals("Toggle_HUD") && !keyPressed)
            screenController.toggleHUD();
        else if(name.equals("Shift_r_l") && keyPressed)
            shiftPressed=true;
        else if(name.equals("Shift_r_l") && !keyPressed)
            shiftPressed=false;
        if(name.equals("Mouse_Left_Button_click") && !keyPressed) 
        {
            // Reset results list.
            CollisionResults results = new CollisionResults();
            // Convert screen click to 3d position
            Vector2f click2d = inputManager.getCursorPosition();
            Vector3f click3d = cam.getWorldCoordinates(new Vector2f(click2d.x, click2d.y), 0f).clone();
            Vector3f dir = cam.getWorldCoordinates(new Vector2f(click2d.x, click2d.y), 1f).subtractLocal(click3d).normalizeLocal();
            // Aim the ray from the clicked spot forwards.
            Ray ray = new Ray(click3d, dir);
            // Collect intersections between ray and all nodes in results list.
            rootNode.collideWith(ray, results);
            // (Print the results so we see what is going on:)
            for (int i = 0; i < results.size(); i++)
            {
                // (For each "hit", we know distance, impact point, geometry.)
                float dist = results.getCollision(i).getDistance();
                Vector3f pt = results.getCollision(i).getContactPoint();
                String target = results.getCollision(i).getGeometry().getName();
            }
            // Use the results -- we rotate the selected geometry.
            if (results.size() > 0)
            {
            // The closest result is the target that the player picked:
                Geometry target = results.getClosestCollision().getGeometry();
                if(target.getName().contains("terrain") || target.getName().contains("Torus") || target.getName().contains("tile"))
                {
                    //the user has selected nothing (terrain, grid or defence), detach of the selection crown
                    selected=null;
                    screenController.setSelection("Terrain");
                    if(rootNode.hasChild(crown))
                        rootNode.detachChild(crown);
                }
                else
                {
                    //the user has selected a ship or a planet
                    int i=rootNode.getChildIndex(target.getParent().getParent());
                    selected=rootNode.getChild(i);
                    screenController.setSelection(rootNode.getChild(i).getName());
                    if(rootNode.hasChild(crown))
                    {
                        rootNode.detachChild(crown);
                        i=rootNode.getChildIndex(target.getParent().getParent());
                    }
                    crown.setLocalTranslation(rootNode.getChild(i).getLocalTranslation());
                    rootNode.attachChild(crown);
                }
            }
            }
        if (name.equals("Mouse_Right_Button_click") && !keyPressed)
            {
            // Reset results list.
            CollisionResults results = new CollisionResults();
            // Convert screen click to 3d position
            Vector2f click2d = inputManager.getCursorPosition();
            Vector3f click3d = cam.getWorldCoordinates(new Vector2f(click2d.x, click2d.y), 0f).clone();
            Vector3f dir = cam.getWorldCoordinates(new Vector2f(click2d.x, click2d.y), 1f).subtractLocal(click3d).normalizeLocal();
            // Aim the ray from the clicked spot forwards.
            Ray ray = new Ray(click3d, dir);
            // Collect intersections between ray and all nodes in results list.
            rootNode.collideWith(ray, results);
            // (Print the results so we see what is going on:)
            for (int i = 0; i < results.size(); i++)
                {
                // (For each "hit", we know distance, impact point, geometry.)
                float dist = results.getCollision(i).getDistance();
                Vector3f pt = results.getCollision(i).getContactPoint();
                String target = results.getCollision(i).getGeometry().getName();
                }
            // Use the results -- we rotate the selected geometry.
            if (results.size() > 0)
                {
                // The closest result is the target that the player picked:
                Geometry target = results.getClosestCollision().getGeometry();
                // Here comes the action:
                if(target.getName().contains("terrain") || target.getName().contains("tile"))
                    {
                        Vector3f zeroPlanePoint=new Vector3f();
                        Plane zeroPlane=new Plane();
                        zeroPlane.setOriginNormal(new Vector3f(0.0f,0.0f,0.0f),new Vector3f(0.0f,1.0f,0.0f));
                        ray.intersectsWherePlane(zeroPlane, zeroPlanePoint);
                        move(selected,zeroPlanePoint,shiftPressed);
                    }
                }
            }    
        }
  };
  
  /**
     * Moves starship of the player
     * @param object the ship that has to be moved
     * @param contactPointPreCalc point of contact on the plane
     * @param multiPath indicates if the movement is part of multipoint path or simple path
     */
  private void move(Spatial object,Vector3f contactPointPreCalc,boolean multiPath)
  {
    if(object!=null)
        if(object.getUserData("type")!=null)
        {
            if(object.getUserData("type").equals("mobile"))
            {
                Vector3f contactPoint=this.findTile(contactPointPreCalc);
                this.crown.setLocalTranslation(contactPoint.getX(), 0.0f, contactPoint.getZ());
                int period_movement=500;//millisecond
                String resId=object.getUserData("id");
                Coordinate start=new Coordinate(object.getLocalTranslation().getX(),0,object.getLocalTranslation().getZ());
                Coordinate arrival=new Coordinate(contactPoint.getX(),0,contactPoint.getZ());
                if(multiPath==false || object.getUserData("movement")==null)
                    object.setUserData("movement",new HandlingMovement(this,resId,start,arrival,period_movement,object,this.request,this.delta,this.gran));
                else    //if the movement is multipath( more control points) add a point
                    ((HandlingMovement)object.getUserData("movement")).addMovement(start, arrival);
            }
            else
                System.out.println("Fixed resource");
        }
  }
  
  /**
     * Moves starship of the player, only for ship creation events
     * @param object the ship that has to be moved
     * @param contactPointPreCalc point of contact on the plane
     */
  private void moveOnCreation(Spatial object,Vector3f contactPointPreCalc)
  {
      if(object.getUserData("type")!=null && object.getUserData("type").equals("mobile"))
        {
        Vector3f contactPoint=this.findTile(contactPointPreCalc);
        System.out.println("Mobile resource");
        int period_movement=500;//millisecond
        String resId=object.getUserData("id");
        Coordinate start=new Coordinate(object.getLocalTranslation().getX(),0,object.getLocalTranslation().getZ());
        Coordinate arrival=new Coordinate(contactPoint.getX(),0,contactPoint.getZ());
        object.setUserData("movement",new HandlingMovement(this,resId,start,arrival,period_movement,object,this.request,this.delta,this.gran));
        }
      else
        {
        System.out.println("Fixed resource");
        MultiLog.println(RTSGameGUI.class.toString(),"Fixed resource");
        }
      MultiLog.println(RTSGameGUI.class.toString(),"End move on creation method");
  }
  
  /**
     * Finds the tile to which the contact point belongs
     * @param contactPoint point of contact on the plane
     * @return the coordinate of the right tile
     */
  private Vector3f findTile(Vector3f contactPoint)
  {
      int tileX;
      int tileZ;
      if(contactPoint.getX()>=0)
        tileX=(int)Math.floor((contactPoint.getX()+this.gran/2)/this.gran);
      else
          tileX=(int)Math.ceil((contactPoint.getX()-this.gran/2)/this.gran);
      if(contactPoint.getZ()>=0)
        tileZ=(int)Math.floor((contactPoint.getZ()+this.gran/2)/this.gran);
      else
          tileZ=(int)Math.ceil((contactPoint.getZ()-this.gran/2)/this.gran);
      //TODO check bottom code
      if(contactPoint.getX()>=0 && contactPoint.getZ()>=0)
        return new Vector3f((float)(this.gran*tileX),0.0f,(float)(this.gran*tileZ));
      else if(contactPoint.getX()<0 && contactPoint.getZ()>0)
        return new Vector3f((float)(this.gran*tileX),0.0f,(float)(this.gran*tileZ));
      else if(contactPoint.getX()<0 && contactPoint.getZ()<0)
        return new Vector3f((float)(this.gran*tileX),0.0f,(float)(this.gran*tileZ));
      else //if(contactPoint.getX()>0 && contactPoint.getZ()<0)
        return new Vector3f((float)(this.gran*tileX),0.0f,(float)(this.gran*tileZ));
  }
 
  /**
     * Listener for keys and mouse buttons. Continuous inputs.
     */
  private AnalogListener analogListener = new AnalogListener() 
  {
    public void onAnalog(String name, float value, float tpf)
        {
        if(!isRunning)
            return;
        if(name.equals("Exit"))
            {
            /*TODO check if there is something to close*/
            stop();
            }
        Vector3f v=cam.getLocation();
        float deltaTranslation=1.5f;
        float camLimitXp=(float) (maxXField/2);
        float camLimitXn=(float) -(maxXField/2);
        float camLimitZp=(float) ((maxYField/2)-distCamera);
        float camLimitZn=(float) (-(maxYField/2)-distCamera);
        if (name.equals("Left_arrow") && v.x<camLimitXp)
            cam.setLocation(new Vector3f(v.x+deltaTranslation,v.y,v.z));
        else if (name.equals("Right_arrow") && v.x>camLimitXn) 
            cam.setLocation(new Vector3f(v.x-deltaTranslation,v.y,v.z));
        else if (name.equals("Up_arrow") && v.z<camLimitZp)//forward
            cam.setLocation(new Vector3f(v.x,v.y,v.z+deltaTranslation));
        else if (name.equals("Down_arrow") && v.z>camLimitZn)//backwards
            cam.setLocation(new Vector3f(v.x,v.y,v.z-deltaTranslation));
        else if(name.equals("Mouse_movement_X_l"))
            {
            if(right)
                right=false;
            if(inputManager.getCursorPosition().x<=minX && v.x<camLimitXp)
                left=true;
            else
                left=false;
            }
        else if(name.equals("Mouse_movement_X_r") && v.x>camLimitXn)
            {
            if(left)
                left=false;
            if(inputManager.getCursorPosition().x>=maxX)
                right=true;
            else
                right=false;
            }
        else if(name.equals("Mouse_movement_Y_u") && v.z<camLimitZp)
            {
            if(down)
                down=false;
            if(inputManager.getCursorPosition().y>=maxY)
                up=true;
            else
                up=false;
            }
        else if(name.equals("Mouse_movement_Y_d") && v.z>camLimitZn)
            {
            if(up)
                up=false;
            if(inputManager.getCursorPosition().y<=minY)
                down=true;
            else
                down=false;
            }
        else if(v.y>maxZoom && (name.equals("Mouse_wheel_u") || name.equals("+")))
                cam.setLocation(new Vector3f(v.x,v.y-zoom,v.z+zoom));
        else if(v.y<minZoom && (name.equals("Mouse_wheel_d") || name.equals("-")))
                cam.setLocation(new Vector3f(v.x,v.y+zoom,v.z-zoom));
            }
  };
  
  /**
   * Cycle of application update
   * @param tpf speed of refresh
   */
  @Override
  public void simpleUpdate(float tpf) 
  {
    if(!this.isRunning)
        return;
    if(this.ready)
    {
        updateGameView(tpf);
        updateFieldVisibility();
        updatePosition(tpf);
        checkEndGame();
    }
  }
  
  /**
   * Check if the game has ended and who win
   */
  private void checkEndGame()
  {
    boolean endGame=true;
    ArrayList<Integer> numberPlanets=new ArrayList<Integer>();			
    this.UpdateLoggedUsers();
    ArrayList<String> players=this.request.getLoggedUsersList();
    System.out.println("Players.size()="+players.size());
    for(int i=0;i<players.size();i++)
    {
        int count=0;
        String playerid=players.get(i);
        for(int j=0;j<planets.size();j++)
        {
            VirtualResource planet=planets.get(j);
            String planetOwnerID=planet.getOwnerID();
            planetOwnerID=planetOwnerID.substring(0,(planetOwnerID.indexOf(',')==-1)?planetOwnerID.length():planetOwnerID.indexOf(','));
            if(playerid.substring(0,(playerid.indexOf(',')==-1)?playerid.length():playerid.indexOf(',')).equals(planetOwnerID))
                count++;
            else if(planet.getOwnerID().equals("null"))
                endGame=false;
        }
	System.out.println("PLAYER "+playerid+" NUM PLANETS= "+count);
	numberPlanets.add(new Integer(count));
    }
    if(!endGame)
        return;
    int index = 0;
    int max=Collections.max(numberPlanets);
    for(int i=0;i<numberPlanets.size();i++)
        if(max==numberPlanets.get(i))
        {
            index=i;
            break;
        }
    String idWinner=players.get(index);
    if(this.playerId.equals(idWinner.substring(0,(idWinner.indexOf(',')==-1)?idWinner.length():idWinner.indexOf(','))))
    {
        System.out.println("You Win!");
        this.screenController.goToEndGame(true);
    }
    else
    {
        System.out.println("You Lose!");
        this.screenController.goToEndGame(false);
    }
  }
    
  /**
   * Performs update of the positions of all the player ships
   * @param tpf time per frame
   */
  private void updatePosition(float tpf)
  {
    HashMap positions=new HashMap();
    for(int i=0;i<this.rootNode.getChildren().size();i++)
    {
        HandlingMovement handler=(HandlingMovement)this.rootNode.getChild(i).getUserData("movement");
        if(handler!=null)
        {
            Coordinate pos=handler.calculateNextWayPoint();
            if(pos!=null)
                positions.put(this.rootNode.getChild(i).getName(),pos);
        }
    }
    for(int i=0;i<positions.size();i++)
    {
        String index=(String) positions.keySet().iterator().next();
        Coordinate coord=(Coordinate) positions.get(index);
        Spatial ship=this.rootNode.getChild(index);
        MyShipControl shipControl=(MyShipControl) ship.getControl(0);
        if(shipControl.setMovement(coord))  //TODO the if statement is necessary only if you wanna check the return value, else it's useless
            System.out.println("Ready to go");
    }
  }
  
  /**
   * Detaching (and destruction by the garbage collector) of all enemy ships
   * Not used. It can be deleted.
   * TODO this way isn't efficient, better use the enemyShipMap to know what ship is visible e waht notTODO This is not efficient, it is better to use the enemyShipMap to know which ship is visible and which is not
   */
  private void detachEnemyShip()
  {
    for(Iterator<Spatial> iter=rootNode.getChildren().iterator();iter.hasNext();)
    {
        Spatial spatial=iter.next();
        if(spatial.getUserData("type")!=null)
            if(spatial.getUserData("type").equals("mobile"))
                if(spatial.getUserData("ownerID")!=null)
                    if(!spatial.getUserData("ownerID").equals(this.playerId))
                        rootNode.detachChild(spatial);
    }
  }
  
  /**
   * Update visibility around the player's resources
   */
  private void updateFieldVisibility()
  {
    Iterator iter=this.enemyShipMap.values().iterator();
    for(;iter.hasNext();)
    {
        Spatial enemyShip=(Spatial) iter.next();
        enemyShip.setUserData("visible","false");
    }
    this.getPlanetVision();
    GamePlayer gp=this.request.getGamePlayer();
    ArrayList<Object> myResources=this.request.getResources();
    for (int k = 0; k < myResources.size(); k++)
        if (myResources.get(k) instanceof GameResourceMobile)
            this.getStarShipVision((GameResourceMobile) myResources.get(k), gran, gp);
    for(int i=0;i<this.widthField/this.gran;i++)
        for(int j=0;j<this.heightField/this.gran;j++)
        {
            SpaceType type=this.knowledges.get((int)Math.round(j*this.numTileX + i)).getType();
            if(!type.equals(SpaceType.UNKNOW))
            {
                Spatial cell=gridNode.getChild("tile"+((int)Math.round(j*this.numTileX + i)));
                if(cell==null)  //TODO theory in this case should never happen, could be deleted
                    System.exit(20);
                cell.setMaterial(this.tileMat);
                cell.setQueueBucket(Bucket.Transparent);
                if(this.knowledges.get((int)Math.round(j*this.numTileX + i)).getElement()==null)
                    continue;
                if(type.equals(SpaceType.PLANET) && !matchCoordinates(i,j,this.homePlanetCoord) && (this.knowledges.get((int)Math.round(j*this.numTileX + i)).getElement() instanceof GamePlayerResponsible))
                    this.createEnemyHomePlanet(j, j, (GamePlayerResponsible)this.knowledges.get((int)Math.round(j*this.numTileX + i)).getElement());
                else if((type.equals(SpaceType.UNCONQUEREDPLANET) || type.equals(SpaceType.ENEMYCONQUEREDPLANET) || type.equals(SpaceType.MYCONQUEREDPLANET)) && !matchCoordinates(i,j,this.homePlanetCoord) && (this.knowledges.get((int)Math.round(j*this.numTileX + i)).getElement() instanceof VirtualResource))
                    this.createOtherPlanets(j, j, (VirtualResource)this.knowledges.get((int)Math.round(j*this.numTileX + i)).getElement());
                else if(type.equals(SpaceType.STARSHIP) && (this.knowledges.get((int)Math.round(j*this.numTileX + i)).getElement() instanceof GameResourceMobileResponsible))
                {
                    GameResourceMobileResponsible ship=(GameResourceMobileResponsible) this.knowledges.get((int)Math.round(j*this.numTileX + i)).getElement();
                    if(!ship.getOwnerId().equals(this.playerId))
                    {
                        Spatial enemyShip=this.enemyShipMap.get(ship.getId());
                        if(enemyShip.getUserData("visible").equals("true") && rootNode.hasChild(enemyShip))
                        {
                            Coordinate coord=this.transformer.patrolTojMonkey(new Point(i,j), this.delta);
                            enemyShip.setLocalTranslation(this.findTile(new Vector3f(coord.getIX(),coord.getIY(),coord.getIZ())));
                        }
                        else if(enemyShip.getUserData("visible").equals("true") && !rootNode.hasChild(enemyShip))
                        {
                            Coordinate coord=this.transformer.patrolTojMonkey(new Point(i,j), this.delta);
                            enemyShip.setLocalTranslation(this.findTile(new Vector3f(coord.getIX(),coord.getIY(),coord.getIZ())));
                            rootNode.attachChild(enemyShip);
                        }
                    }
                }
            }
        }
        for (int k = 0; k < myResources.size(); k++)
            if (myResources.get(k) instanceof GameResourceMobile)
            {
                this.verifyPlanets((GameResourceMobile) myResources.get(k));    //clash handling
                this.verifyEnemies((GameResourceMobile) myResources.get(k));    //clash handling
            }
        for(int i=0;i<rootNode.getQuantity();i++)
            if(rootNode.getChild(i).getUserData("visible")!=null && rootNode.getChild(i).getUserData("visible").equals("false"))
                rootNode.detachChildAt(i);
    }
    
  /**
   * Check if the coordinates coord are equivalent to (i,j)
   * @param i x coordinate
   * @param j y coordinate
   * @param coord coordinates to match with (i,j)
   * @return true if the coordinates are equivalent, false otherwise
   */
  private boolean matchCoordinates(int i,int j,Coordinate coord)
  {
    Point point=this.transformer.jMonkeyToPatrol(coord, this.delta);
    Point tile=new Point(point.getX()/this.gran,point.getY()/this.gran);
    if((int)Math.round(j*this.numTileX + i)==(tile.getY()*this.numTileX+tile.getX()))
        return true;
    return false;
  }
  
  /**
   * Check if there are planet to conquer near the ship grm
   * @param grm the ship in exam
   */
  public void verifyPlanets(GameResourceMobile grm)
  {
    String resID=grm.getId();
    double x=grm.getX();
    double y=grm.getY();
    for(int k=0;k<planets.size();k++)
    {
        VirtualResource planet=planets.get(k);  //for each planets checks if its coordinates are inside the window of vision of my mobile resource
        if(this.isInRange(new Vector3f((float)planet.getX(),(float)planet.getY(),(float)planet.getZ()),new Vector3f((float)x,(float)y,0),grm.getVision()))
        {
            System.out.println("PIANETA");
            System.out.println("Coordinate: x= "+planet.getX()+ " y= "+planet.getY());
            if(planet.getOwnerID().equals("null"))  //if the planet has no owner
            {
                if(this.createResource(planet.getId())) //if I have enough money to buy a defence
                { 
                    //conquer the planet
                    System.out.println("CONQUISTO IL PIANETA "+planet.getId());
                    this.screenController.setTextNotification("Planet "+planet.getId()+" conquered");
                    this.setPlanetOwner(planet.getId(),this.playerId,this.playerName); //lo conquisto 
                    this.UpdateLoggedUsers();
                    HashMap<String,UserInfo> userslist=this.getLoggedUsers();
                    Set<String> key_set=userslist.keySet();
                    Iterator<String> iterator=key_set.iterator();
                    System.out.println("USERS "+userslist.size());
                    //sends a message to all player that I conquered a planet
                    while(iterator.hasNext())
                    {		
                        String iduser=iterator.next();
                        UserInfo info=userslist.get(iduser);	
                        String user_id=info.getId();
                        String userip=info.getIp();
                        int userport=info.getPort();
                        if(!this.playerId.equals(user_id))											
                        {
                            System.out.println("INVIO MESSAGGIO A "+user_id);	
                            PlanetConqueredMessage message=new PlanetConqueredMessage(this.playerId,this.request.getIpAddress(),(/*this.portMin*/this.outPort+7),this.playerId,this.playerName,planet.getId());	
                            System.out.println("Invio messaggio a "+userip+" , "+((userport+1)+7));
                            String responseMessage=it.simplexml.sender.MessageSender.sendMessage(userip,((userport)+7),message.generateXmlMessageString());
                            if(responseMessage.contains("ERROR"))
                                System.out.println(" Sending Message ERROR!");
                            else
                            {
                                //ack message
                                MessageReader responseStartMessageReader = new MessageReader();
                                Message receivedStartMessageReader = responseStartMessageReader.readMessageFromString(responseMessage.trim());
                                AckMessage ackMessage = new AckMessage(receivedStartMessageReader);
                                if (ackMessage.getAckStatus() == 0)
                                    System.out.println(" Message received");
                            }
                        }
                    }
                }
                else
                    this.screenController.setTextNotification("Not enough money to conquer the planet");
            }
            else if(!planet.getOwnerID().equals(this.playerId))
            {
                //clash handling
		//un pianeta deve avere una risorsa associata. Alla conquista del pianeta devo acquistare una risorsa che difenda questo pianeta. Da fare
		//quando trovo un pianeta nemico inizio uno scontro con questa risorsa
		//ricavo id e port number del nemico
		UserInfo info=this.getLoggedUserInfo(planet.getOwnerID());
		if(info==null)
                {
                    this.UpdateLoggedUsers();
                    info=this.getLoggedUserInfo(planet.getOwnerID());
                }			
		if(!this.inClash)
                {
                    System.out.println("###########SCONTRO#############");
                    this.screenController.setTextNotification("Start invasion");
                    this.inClash=true;
                    //invio messaggio
                    long currentTime=System.currentTimeMillis();
                    System.out.println("#######################RTSGameGUI--->richiesta di scontro#############################");
                    Message message=new BotStartMatchRequestMessage(this.playerId,grm.getId(),planet.getId(),Long.toString(currentTime));
                    String responseMessage=it.simplexml.sender.MessageSender.sendMessage(info.getIp(),(info.getPort()+7),message.generateXmlMessageString());	
                    MessageReader messageReader=new MessageReader();
                    Message receivedMessage = messageReader.readMessageFromString(responseMessage.trim());
                    if(receivedMessage.getMessageType().equals("BOTSTARTMATCH"))
                    {
			System.out.println("##########RTSGameGUI-->Risposta############ ");
			BotStartMatchResponseMessage response=new BotStartMatchResponseMessage(receivedMessage);
			boolean isInMatch=response.getInMatch();
			if(isInMatch)
                        {
                            System.out.println("#############Scontro--->Decisione:INIZIA ALTRO PEER################");
                            //TODO handling of this case
                        }
			else //!inMatch
                        {
                            System.out.println("#############Scontro--->POSSO INIZIARE IO################");
                            System.out.println(planet.getOwnerID());
                            String result=request.startMatch(planet.getOwnerID(), planet.getOwnerName(),info.getIp(),info.getPort(), planet.getId(),grm.getId(),grm.getQuantity() , planet.getX(), planet.getY(), planet.getZ());
                            // se vinco conquisto il pianeta
                            // se perdo perdo la mia risorsa
                            System.out.println("TROVATO PIANETA NEMICO");
                            if(result.equals("win"))
                            {//ho vinto conquisto pianeta
				this.setPlanetOwner(planet.getId(), "null", "null");//prima di conquistare il pianeta cancello il proprietario precedente
				if(this.createResource(planet.getId()))
                                {
                                    System.out.println("CONQUISTO IL PIANETA "+planet.getId());
                                    this.screenController.setTextNotification("Planet "+planet.getId()+" conquered");
                                    this.setPlanetOwner(planet.getId(), this.playerId,this.playerName); //lo conquisto
                                    //ora devo comunicarlo a gli altri giocatori
                                    this.UpdateLoggedUsers();
                                    HashMap<String,UserInfo> userslist=this.getLoggedUsers();
                                    Set<String> key_set=userslist.keySet();
                                    Iterator<String> iterator=key_set.iterator();
                                    while(iterator.hasNext())
                                    {
					String iduser=iterator.next();
					UserInfo info2=userslist.get(iduser);	
					String user_id=info2.getId();
					String userip=info2.getIp();
					int userport=info2.getPort();
                                        if(!this.playerId.equals(user_id))											
                                        {
                                            System.out.println("Invio messaggio a "+user_id);
                                            PlanetConqueredMessage msg=new PlanetConqueredMessage(this.playerId,this.request.getIpAddress(),(this.outPort+7),this.playerId,this.playerName,planet.getId());
                                            String responseMess=it.simplexml.sender.MessageSender.sendMessage(userip,(userport+7),msg.generateXmlMessageString());
                                            if(responseMess.contains("ERROR"))
                                            {
						System.out.println(RTSGameGUI.class.toString()+ "Sending Message ERROR!");
                                            }	
                                            else
                                            {
                                                //ack message
                                                MessageReader responseStartMessageReader = new MessageReader();
                                                Message receivedStartMessageReader = responseStartMessageReader.readMessageFromString(responseMess.trim());
                                                AckMessage ackMessage = new AckMessage(receivedStartMessageReader);
                                                if (ackMessage.getAckStatus() == 0)
                                                {
                                                    System.out.println("Messaggio ricevuto da "+user_id);
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            else
                            {
                                //destroy ship after losing
                                for(Iterator<Spatial> iter=rootNode.getChildren().iterator();iter.hasNext();)
                                {
                                    Spatial spatial=iter.next();
                                    if(spatial.getUserData("type")!=null)
                                        if(spatial.getUserData("type").equals("mobile"))
                                            if(spatial.getUserData("id").equals(resID))
                                                rootNode.detachChild(spatial);
                                }
                            }
                        }
                    }
                    else if(receivedMessage.getMessageType().equals("ERROR"))
                    {
                        System.out.println(RTSGameGUI.class.toString()+ "Sending Message ERROR!");
                    }
                    this.inClash=false;
                    this.screenController.setTextNotification("End of invasion");
                }
		else
                {
                    System.out.println("############Sono in uno Scontro#############");
                }
            }
            //if the planets is mine, I do nothing
        }
    }
  }

  /**
   * Check if there are enemy ship to destroy near the ship grm
   * @param grm the ship in exam
   */
  public void verifyEnemies(GameResourceMobile grm)
  {
      String resID=grm.getId();
      //get visiblity value of the resource
      ArrayList<Object> vision=grm.getResourceVision();	
      //creo un arraylist dove salvo la posizione dentro l'array della visibilita' della risorsa mobile
      ArrayList<Integer> array_pos=new ArrayList<Integer>();	
      for(int z=0;z<vision.size();z++)
      {
          if(vision.get(z) instanceof GamePlayerResponsible)
          {
            GamePlayerResponsible gpr=(GamePlayerResponsible)vision.get(z);
            if(!this.playerId.equals(gpr.getId()))
            {
                System.out.println("@@@@@@@@@@@@@@@@@@@@base nemica@@@@@@@@@@@@@@@@@@@");			
                array_pos.add(new Integer(z));
            }
          }
          else if(vision.get(z) instanceof GameResourceMobileResponsible)
          {			
              GameResourceMobileResponsible grmr=(GameResourceMobileResponsible)vision.get(z);
              if(!this.playerId.equals(grmr.getOwnerId()))
              {
                  System.out.println("@@@@@@@@@@@@@@@@@@@@nave nemica@@@@@@@@@@@@@@@@@@@");
                  array_pos.add(new Integer(z));
              }
          }
      }
      if(!array_pos.isEmpty()) // se c'e' qualcuno da attaccare
      {
          int posres=array_pos.get(0);
          //recupero l'object
          Object res=vision.get(posres);		
          if(res instanceof GamePlayerResponsible)
          {
            //ho trovato la base di un giocatore
            GamePlayerResponsible player=(GamePlayerResponsible)res;			
            //ricavo id e port number del nemico
            this.UpdateLoggedUsers();
            UserInfo info=this.getLoggedUserInfo(player.getId());
            System.out.println("#################TROVATA BASE NEMICA+"+player.getId()+"+ #################");
            //inizio match
            if(!this.inClash) // se sono attualmente in uno scontro, ignoro questo scontro
            {
                //faccio lo scontro, altrimento lo ignoro
                this.inClash=true;		
                //invio messaggio
                long currentTime=System.currentTimeMillis();
                System.out.println("#######################RTSGameBot--->richiesta di scontro#############################");
                Message message=new BotStartMatchRequestMessage(this.playerId,grm.getId(),player.getId(),Long.toString(currentTime));				
                String responseMessage=it.simplexml.sender.MessageSender.sendMessage(info.getIp(),(info.getPort()+7),message.generateXmlMessageString());			
                MessageReader messageReader=new MessageReader();
                Message receivedMessage = messageReader.readMessageFromString(responseMessage.trim());			    
                if(receivedMessage.getMessageType().equals("BOTSTARTMATCH"))
                {
                    System.out.println("##########RTSGameBot-->Risposta############ ");
                    BotStartMatchResponseMessage response=new BotStartMatchResponseMessage(receivedMessage);
                    boolean isInMatch=response.getInMatch();
                    if(isInMatch)
                    {
                        System.out.println("#############Scontro--->Decisione:INIZIA ALTRO PEER################");
                        //TODO handle this case
                    }
                    else //!inMatch
                    {
                        System.out.println("#############Scontro--->POSSO INIZIARE IO################");
                        String result=this.request.startMatch(player.getId(), player.getName(),info.getIp(),info.getPort(), player.getId(),grm.getId(),grm.getQuantity() , player.getPosX(), player.getPosY(), player.getPosZ());
                        //attendere esito dello scontro
                        if(result.equals("win"))
                        {
                            System.out.println("Ho vinto");
                            this.screenController.setTextNotification("Enemy base conquered");
                        }
                        else if(result.equals("lose"))
                        {
                            System.out.println("Ho perso");
                            this.screenController.setTextNotification("Lost");
                            //destroy ship after losing
                            for(Iterator<Spatial> iter=rootNode.getChildren().iterator();iter.hasNext();)
                            {
                                Spatial spatial=iter.next();
                                if(spatial.getUserData("type")!=null)
                                    if(spatial.getUserData("type").equals("mobile"))
                                        if(spatial.getUserData("id").equals(resID))
                                            rootNode.detachChild(spatial);
                            }
                        }
                    }
                }
                else if(receivedMessage.getMessageType().equals("ERROR"))
                {
                    System.out.println(RTSGameGUI.class.toString()+ "Sending Message ERROR!");
                }
                this.inClash=false;
            }
            else
            {
                System.out.println("##########SONO IN CLASH#################");
            }
          }
          else if(res instanceof GameResourceMobileResponsible)
          {
            GameResourceMobileResponsible res_grm=(GameResourceMobileResponsible)res;
            UserInfo info=this.getLoggedUserInfo(res_grm.getOwnerId());
            if(info==null)
            {
                this.UpdateLoggedUsers();
                info=this.getLoggedUserInfo(res_grm.getOwnerId());
            }			
            System.out.println("###################### TROVATA RISORSA NEMICA +"+res_grm.getOwnerId()+" +  #################");
            //inizio match
            if(!this.inClash) // se sono attualmente in uno scontro, ignoro questo scontro
            {
                //faccio lo scontro, altrimento lo ignoro		
                this.inClash=true;				
                //invio messaggio
                long currentTime=System.currentTimeMillis();
                Message message=new BotStartMatchRequestMessage(this.playerId,grm.getId(),res_grm.getId(),Long.toString(currentTime));
                String responseMessage=it.simplexml.sender.MessageSender.sendMessage(info.getIp(),(info.getPort()+7),message.generateXmlMessageString());
                MessageReader messageReader=new MessageReader();
                Message receivedMessage = messageReader.readMessageFromString(responseMessage.trim());
                if(receivedMessage.getMessageType().equals("BOTSTARTMATCH"))
                {
                    System.out.println("##########RTSGameBot-->Risposta############ ");
                    BotStartMatchResponseMessage response=new BotStartMatchResponseMessage(receivedMessage);
                    boolean isInMatch=response.getInMatch();
                    if(isInMatch)
                    {
                        System.out.println("#############Scontro--->Decisione:INIZIA ALTRO PEER################");
                        //TODO handle this case
                    }
                    else //!inMatch
                    {
                        System.out.println("#############Scontro--->POSSO INIZIARE IO################");
                        String result=this.request.startMatch(res_grm.getOwnerId(), res_grm.getOwner(),info.getIp(),info.getPort(), res_grm.getId(),grm.getId(),grm.getQuantity() , res_grm.getX(), res_grm.getY(), res_grm.getZ());
                        //attendere esito dello scontro
                        if(result.equals("win"))
                        {
                            System.out.println("Ho vinto");
                            this.screenController.setTextNotification("Enemy ship conquered");
                        }
                        else if(result.equals("lose"))
                        {
                            System.out.println("Ho perso");
                            this.screenController.setTextNotification("Lost");
                            //destroy ship after losing
                            for(Iterator<Spatial> iter=rootNode.getChildren().iterator();iter.hasNext();)
                            {
                                 Spatial spatial=iter.next();
                                    if(spatial.getUserData("type")!=null)
                                        if(spatial.getUserData("type").equals("mobile"))
                                            if(spatial.getUserData("id").equals(resID))
                                                rootNode.detachChild(spatial);
                            }
                        }
                    }
                }
                else if(receivedMessage.getMessageType().equals("ERROR"))
                {			    	 
                    System.out.println(RTSGameGUI.class.toString()+ "Sending Message ERROR!");
                }				
                this.inClash=false;
            }
            else
            {
                System.out.println("###########SONO IN CLASH#####################");
            }
          }
      }
  }
  
  /**
   * Acquisition of defensive resource
   * @param idRes id of the player
   * @return true if it possible to buy the resource, false otherwise (not enough money)
   */
  public boolean createResource(String idRes)
  {
      double qt=this.request.getMyResourceFromId("moneyEvolveble").getQuantity();
      int multiplicity=(int) (qt/this.resMinCost);
      if(multiplicity>0)
      {
        qt=multiplicity*this.resMinCost; //uso la stessa variabile
        String timestamp = Long.toString(System.currentTimeMillis());
        this.request.addResource(idRes, "Defense" + timestamp, this.resMinCost);
        this.request.UpdateResourceEvolve(this.request.getMyResourceFromId("moneyEvolveble").getQuantity()-qt);
        return true;
      }
      else
        return false;
  }
  
  /**
   * Gets list of logged players
   * @return HashMap that contains player's Id and player's Information
   */
  public HashMap<String,UserInfo> getLoggedUsers()
  {
    return this.loggedusers;
  }
  
  /**
   * Sets new owner of the planet
   * @param idPlanet id of the planet
   * @param idOwner id of the new owner
   * @param nameOwner name of the new owner
   */
  public void setPlanetOwner(String idPlanet,String idOwner,String nameOwner)
  {
    this.getPlanetbyID(idPlanet).setOwnerID(idOwner);
    this.getPlanetbyID(idPlanet).setOwnerName(nameOwner);	
    rootNode.getChild("uPlanet_"+idPlanet).setUserData("ownerId",idOwner);
  }
  
  /**
   * Gets the information about the player id
   * @param id player in exam
   * @return information about the player
   */
  public UserInfo getLoggedUserInfo(String id)
  {
        return this.loggedusers.get(id);
  }
  
  /**
   * Gets the id of the player
   * @return the id of the player
   */  
  public String getOwnerid()
  {
        return ownerid;
  }
  
  /**
   * Gets the VirtualResource that represent the planet
   * @param idPlanet id of the planet
   * @return VirtualResource associated
   */
  public VirtualResource getPlanetbyID(String idPlanet)
  {
      for(int i=0;i<this.planets.size();i++)
      {
          VirtualResource planet=this.planets.get(i);
          if(planet.getId().equals(idPlanet))
              return planet;
      }
      return null;
  }
  
  /**
   * Creates planet that isn't a home planet for one of the players, if it doesn't exist
   * @param x coordinate x
   * @param y coordinate y
   * @param planet VirtualResource associated to the planet
   */ 
  private void createOtherPlanets(int x,int y,VirtualResource planet)
  {
      Coordinate coord=this.transformer.patrolTojMonkey(new Point(x*this.gran,y*this.gran),this.delta);
      String resOwnerId=planet.getOwnerID();
        if(rootNode.getChild("myPlanet_"+planet.getId())!=null || rootNode.getChild("uPlanet_"+planet.getId())!=null || rootNode.getChild("ePlanet_"+planet.getId())!=null)
            return;
        if(resOwnerId.equals(this.playerId))
        {
            MyPlanetControl planetControl=new MyPlanetControl(this.planetRotSpeed);
            MyDefenseControl defenseControl=new MyDefenseControl(this.defenseRotSpeed);
            Spatial myPlanet=assetManager.loadModel("Models/planet4-1/planet4-1.j3o");
            myPlanet.addControl(planetControl);
            myPlanet.setName("myPlanet_"+planet.getId());
            myPlanet.setUserData("type","fixed");
            myPlanet.setUserData("ownerId",planet.getOwnerID());
            myPlanet.setUserData("hasDefense", true);
            myPlanet.setLocalScale(planetScale);
            myPlanet.setLocalTranslation(coord.getIX(),coord.getIY(),coord.getIZ());
            rootNode.attachChild(myPlanet);
            String timestamp = Long.toString(System.currentTimeMillis());
            this.request.addResource("strdef" + timestamp, "Defense" + timestamp, 1.0);
            Spatial defense = assetManager.loadModel("Models/defense/defense.j3o");
            defense.setLocalScale(1.6f);
            defense.setName("defense");
            defense.setUserData("type","fixed");
            defense.setUserData("numDefense", 1);
            defense.addControl(defenseControl);
            defense.setLocalTranslation(coord.getIX(),coord.getIY(),coord.getIZ());
            rootNode.attachChild(defense);
            myPlanet.setUserData("defenseName", defense.getName());
        }
        else if(resOwnerId.equals("null"))  //no owner
        {
            MyPlanetControl planetControl=new MyPlanetControl(this.planetRotSpeed);
            Spatial uPlanet=assetManager.loadModel("Models/planet3-1/planet3-1.j3o");
            uPlanet.addControl(planetControl);
            uPlanet.setName("uPlanet_"+planet.getId());
            uPlanet.setUserData("type","fixed");
            uPlanet.setUserData("ownerId",planet.getOwnerID());
            uPlanet.setUserData("hasDefense", false);
            uPlanet.setLocalScale(planetScale);
            uPlanet.setLocalTranslation(coord.getIX(),coord.getIY(),coord.getIZ());
            rootNode.attachChild(uPlanet);
            uPlanet.setUserData("defenseName", "");
        }
        else    //enemy planet
        {
            MyPlanetControl planetControl=new MyPlanetControl(this.planetRotSpeed);
            Spatial ePlanet=assetManager.loadModel("Models/planet2-1/planet2-1.j3o");
            ePlanet.addControl(planetControl);
            ePlanet.setName("ePlanet_"+planet.getId());
            ePlanet.setUserData("type","fixed");
            ePlanet.setUserData("ownerId",planet.getOwnerID());
            ePlanet.setUserData("hasDefense", false);
            ePlanet.setLocalScale(planetScale);
            ePlanet.setLocalTranslation(coord.getIX(),coord.getIY(),coord.getIZ());
            rootNode.attachChild(ePlanet);
            //MultiLog.println(RTSGameGUI.class.toString(),"pianeta: "+coord.getX()+" "+coord.getY()+" "+coord.getZ());
            ePlanet.setUserData("defenseName", "");
        }
  }
  
  /**
    * Creates the enemy player's home planet, if it doesn't exist
    * @param x coordinate x
    * @param y coordinate y
    * @param planet VirtualResource associated to the planet
    */
  private void createEnemyHomePlanet(int x,int y,GamePlayerResponsible planet)
  {
    Coordinate coord=this.transformer.patrolTojMonkey(new Point(x*this.gran,y*this.gran),this.delta);
    if(rootNode.getChild("ehPlanet_"+planet.getName())!=null)
            return;
        MyPlanetControl planetControl=new MyPlanetControl(this.planetRotSpeed);
        Spatial ehPlanet=assetManager.loadModel("Models/planet2-1/planet2-1.j3o");
        ehPlanet.addControl(planetControl);
        ehPlanet.setName("ehPlanet_"+planet.getName());
        ehPlanet.setUserData("type","fixed");
        ehPlanet.setUserData("ownerId",planet.getId());
        ehPlanet.setUserData("hasDefense", false);
        ehPlanet.setLocalScale(planetScale);
        ehPlanet.setLocalTranslation(coord.getIX(),coord.getIY(),coord.getIZ());
        rootNode.attachChild(ehPlanet);
        ehPlanet.setUserData("defenseName", "");
    }
    
  /**
     * Creates the enemy starship
     * @param x coordinate x
     * @param y coordinate y
     * @param ship the ship
     */
    private void createEnemyStarship(int x,int y,GameResourceMobileResponsible ship)
    {
        String timestamp = Long.toString(System.currentTimeMillis());
        Coordinate pos=this.transformer.patrolTojMonkey(new Point(x,y),this.delta);
        MyShipControl shipControl=new MyShipControl(1.0f);
        Spatial spaceship =assetManager.loadModel("Models/cylinderSpacaship17/cylinderSpacaship17.j3o");
        spaceship.setName("spaceship_"+timestamp);
        spaceship.setUserData("id", ship.getId());
        spaceship.setUserData("owner", ship.getOwnerId());
        spaceship.setUserData("ownerID", ship.getOwnerId());
        spaceship.setUserData("visible", "true");
        spaceship.setUserData("type", "mobile");
        spaceship.setLocalScale(shipScale);
        spaceship.addControl(shipControl);
        spaceship.setLocalTranslation((int)(pos.getIX()),(int)(pos.getIY()),(int)(pos.getIZ()));
        rootNode.attachChild(spaceship);
        this.enemyShipMap.put(ship.getId(),spaceship);
    }
    
    /**
     * Updating of the view
     * @param tpf time per frame
     */
    private void updateGameView(float tpf) 
    {
        Vector3f v=cam.getLocation();
        float deltaTranslation=1.5f;
        if(left)
            cam.setLocation(new Vector3f(v.x+deltaTranslation,v.y,v.z));
        else if(right)
            cam.setLocation(new Vector3f(v.x-deltaTranslation,v.y,v.z));
        if(up)
            cam.setLocation(new Vector3f(v.x,v.y,v.z+deltaTranslation));
        else if(down)
            cam.setLocation(new Vector3f(v.x,v.y,v.z-deltaTranslation));
        this.screenController.setActualCentralPosition(v.x+" "+v.z);
    }
    
    /**
     * Updating of the logged users' list
     */
    public void UpdateLoggedUsers()
    {
        this.loggedusers=new HashMap<String,UserInfo>();
	ArrayList<String> usersList=this.request.getLoggedUsersList();//this.getMyGamePeer().getLoggedUsersList();
	if(!usersList.isEmpty())// se ci sono degli utenti nella lista invio i messaggi
        {
            for(int u=0;u<usersList.size();u++)
            {
                String str_user=usersList.get(u);
		String[] array_user=str_user.split(",");
		String userid=array_user[0]; // mi serve ???
		String userip=array_user[1];
		String userport=array_user[2];
		UserInfo info=new UserInfo(userid,userip,Integer.parseInt(userport));
		loggedusers.put(userid,info);
            }
        }
    }
    
    /**
     * Method for custom render code
     * @param rm RenderManager
     */
    @Override
    public void simpleRender(RenderManager rm) 
    {
        //TODO: add render code
    }
    
    /**
     * Sets the view on the home planet of the human player
     */
    public void setVisionToHome()
    {
        @SuppressWarnings("UnusedAssignment")
        Vector3f v=cam.getLocation();
        this.cam.setLocation(new Vector3f(this.homePlanetCoord.getIX(),20.0f+this.homePlanetCoord.getIY(),(this.homePlanetCoord.getIZ()-this.distCamera)));
        v=cam.getLocation();
        this.screenController.setActualCentralPosition(v.x+" "+v.z);
    }
    
    /**
     * Gets the type of the selected resource
     * @return the type of the selected resource
     */
    public String getSelType()
    {
        if(this.selected==null)
            return "-";
        String name=this.selected.getName();
        if(name.equals("home") || name.contains("planet") || name.contains("Planet"))
                return "Planet";
        else if(name.contains("spaceship"))
            return "Ship";
        else
            return "Unknown";
    }
    
    /**
     * Gets the id of the owner of the resource
     * @return the id of the owner
     */
    public String getResOwner()
    {
        if(this.selected==null)
            return "-";
        return this.selected.getUserData("ownerId");
    }
    
    /**
     * Gets the number of defensive resources of the selected element
     * @return the string that represent the number of defensive resources
     */
    public String getNumDefence()
    {
        if(this.selected==null)
            return "-";
        if(this.selected.getUserData("type").equals("fixed"))
        {
            //planet
            if(this.selected.getUserData("hasDefense").equals(true))
                return rootNode.getChild(this.selected.getUserData("defenseName").toString()).getUserData("numDefense").toString();
            else
                return "0";
        }
        else    //mobile resource, ship
            return "-";
    }
    
    /**
     * Gets the actual quantity of money
     * @return the string that represent the actual quantity of money
     */
    public String getActualMoney()
    {
        return Double.toString(this.request.getMyResourceFromId("moneyEvolveble").getQuantity());
    }
    
    /**
     * Properties: are divided by categories based on meaning and belonging area
     * Those that are static because they are to be used directly in the main
     */
    
    //developer's modificator
    private boolean showGUI=true;
    private static boolean showSettingsContr=false;
    private boolean displayFPS=false;
    private boolean displayStatView=false;
    
    //jmonkey's properties
    private TerrainQuad terrain;
    private Material mat_terrain;
    private Material tileMat;
    private Material fogMat;
    private static int width=800;
    private static int height=600;
    private static int colorDepth=24;
    private static boolean fullScreen=false;
    private static boolean vSynch=false;
    private static short samples=0;
    private static String title="Patrol's RTS Space Game";
    private int minX=10;
    private int minY=10;
    private int maxX=width-10;
    private int maxY=height-10;
    private boolean left=false;
    private boolean right=false;
    private boolean up=false;
    private boolean down=false;
    private boolean isRunning=true;
    private CoordinatesMapping transformer;
    private float distCamera=20.0f;   //indicates the distance from camera to the pointed object
    private float zoom=5.0f;     //zoom value
    private float minZoom=(float) (this.distCamera*2);
    private float maxZoom=(float) (this.distCamera/2);
    private Coordinate homePlanetCoord;
    private float planetScale=1.3f;
    private float shipScale=1/4f;
    private Spatial crown;
    private Spatial selected;
    private float planetRotSpeed=1/10f;
    private float defenseRotSpeed=1/15f;
    private Node gridNode;
    private boolean shiftPressed=false;
    private HashMap<String,Spatial> enemyShipMap;
    private String shipModel;
    
    //niftyGUI's properties
    private MyScreenController screenController;
    private Nifty nifty;
    
    //platform's properties
    private static final long serialVersionUID = 1L;
    private MessageSender request;
    private String playerId;
    private String playerName;
    private String ownerid;
    private boolean inClash=false;
    
    //properties related to communication between GUI and platform
    private int inPort,outPort,idLength,serverPort,gameInPort,gameOutPort,gameServerPort,stab,fix,check,pub;
    private String id,serverAddr,gameServerAddr,user,pwd;
    private ArrayList<KnowledgeSpace> knowledges;
    private ArrayList<VirtualResource> planets;
    private double gran;
    private double maxXField;
    private double maxYField;
    private double maxZField;
    private double minXField;
    private double minYField;
    private double minZField;
    private double widthField;
    private double heightField;
    private double speedPlat;
    private double vis;
    private double resMinCost;
    private int numTileX;
    private int numTileY;
    private boolean ready;
    private double delta;
    private HashMap<String, UserInfo> loggedusers;
    /**
     * Default value for the communication with the MainGamePeer 
     * (used for the 'internal' communication among GUI and the GamePeer)
     */
    private int gamePeerPort;
}