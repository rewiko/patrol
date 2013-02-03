/*
 * RTSGameGUI.java
 *
 * Created on 10-feb-2012, 11.12.34
 */

package it.unipr.ce.dsg.patrol.gui3d;

import com.jme3.app.FlyCamAppState;

//jMonkey's import
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
//import it.unipr.ce.dsg.patrol.platform.GamePeer;

/**
 * PATROL's game
 * @author Michael Benassi Giorgio Micconi
 */
public class RTSGameGUI extends SimpleApplication 
{	
    /**
     * Main method for game's GUI
     * @param argv 
     */
    @SuppressWarnings("ResultOfObjectAllocationIgnored")
    public static void main(String[] argv)
    {
        new MultiLog("ConfigFile.txt",true,true);  //initialization of MultiLog object
        //read the first argument to decide if the initial settings must be shown
        //boolean showSettings=false;
        if (argv.length < 4) {
        	System.err.println("RTSGameGUI launched without required (4) parameters");
        	System.exit(1);
        }
        
        if(argv[0].equals("true"))
            showSettingsContr=true;
        else if(argv[0].equals("false"))
            showSettingsContr=false;
        RTSGameGUI app = new RTSGameGUI(argv);
        //instantiate AppSettings doesn't show jmonkey's settings window at startup
        AppSettings settings = new AppSettings(true);
        settings.setRenderer(AppSettings.LWJGL_OPENGL2);
        settings.setWidth(width);
        settings.setHeight(height);
        settings.setFullscreen(fullScreen); //comment it to take a screenshot
        settings.setDepthBits(colorDepth);
        settings.setSamples(samples);
        settings.setVSync(vSynch);
        settings.setTitle(title);
        app.setShowSettings(showSettingsContr);
        app.setSettings(settings);
        app.start();
    }

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
        
        /*if(argv.length > 4){
        	gamePeerPort = Integer.parseInt(argv[4]);
        }*/
        
        //initialize screenController class
        this.screenController=new MyScreenController(this);
        this.screenController.setUser("user");
        this.screenController.setPwd("pwd");
        this.screenController.setServerAddress("127.0.0.1");
        this.screenController.setServerPort(1235);
        this.screenController.setOutPort(40000/*7000*//*6891*/);
        this.screenController.setMessagePort(9998);
        this.screenController.setActualCentralPosition("0 0");
        this.screenController.setActualCursorPosition("0 0");
        this.screenController.setSelection("Niente");
        this.printer=new StatusPrinter(RTSGameGUI.class.toString());
    }
    
    /**
     * jMonkey's fundamental method override, it creates the scene at startup.
     */
    @Override
    public void simpleInitApp() 
    {
        ScreenshotAppState state = new ScreenshotAppState();  //ScreenshotAppState class allows to take a screenshot
        this.stateManager.attach(state);
        stateManager.detach( stateManager.getState( FlyCamAppState.class ) );
        this.setDisplayFps(displayFPS);
        this.setDisplayStatView(displayStatView);
        flyCam.setMoveSpeed(/*50*/150); //set cam speed
        this.transformer=new CoordinatesMapping();
        this.rootNode.setCullHint(Spatial.CullHint.Dynamic);
        this.rootNode.setShadowMode(ShadowMode.Off);
        this.enemyShipMap=new HashMap<String,Spatial>();
        this.ready=false;
        if(this.showGUI)
            {
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
            this.nifty.addXml("Interface/endGame.xml");
            this.stateManager.attach(this.screenController);
            guiViewPort.addProcessor(niftyDisplay);
            flyCam.setDragToRotate(true); //disable mouse interaction in graphic scene
            }
        else
            {
            NiftyJmeDisplay niftyDisplay=new NiftyJmeDisplay(assetManager, inputManager, audioRenderer, guiViewPort);
            this.nifty=niftyDisplay.getNifty();
            this.nifty.fromXml("Interface/HUD_start.xml","start",this.screenController);
            this.nifty.addXml("Interface/HUD.xml");
            this.nifty.addXml("Interface/endGame.xml");
            this.stateManager.attach(this.screenController);
            guiViewPort.addProcessor(niftyDisplay);
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
     * Provides connection between this class and server/game peer
     * Provides connection among GUI and game peer (Stefano) 
     */
    public void connect()
    {
        if(this.showGUI)
            {
            this.outPort=Integer.parseInt(this.screenController.getOutPort());//6891;
            this.serverAddr=this.screenController.getServerAddress();//"127.0.0.1";
            this.serverPort=Integer.parseInt(this.screenController.getServerPort());//1235;
            this.user=this.screenController.getUser();
            this.pwd=this.screenController.getPwd();
            this.gamePeerPort=Integer.parseInt(this.screenController.getMessagePort());
            }
        else
            {
            this.outPort=40000;
            this.serverAddr="127.0.0.1";
            this.serverPort=1235;
            this.user="giorgio";
            this.pwd="ggg";
            this.gamePeerPort=9998;
            }
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
        this.request=new MessageSender(this.gamePeerPort);
        //this.request=new MessageSender(9998);
        //start of communication between bootstrap server and GUI
        MultiLog.println(RTSGameGUI.class.toString(), "Try to connect and register...");
        this.request.CreateGamePeer(inPort, outPort, idLength, id, serverAddr, serverPort, gameInPort, gameOutPort, gameServerAddr, gameServerPort, stab, fix, check, pub);
        MultiLog.println(RTSGameGUI.class.toString(), "Peer is created");
        this.request.registerOnServer(this.user,this.pwd);
        MultiLog.println(RTSGameGUI.class.toString(), "Peer is registrated into the network");
        this.confRead();
        MultiLog.println(RTSGameGUI.class.toString(), "Speed: "+this.speedPlat+" Visibility: "+this.vis+" Granularity: "+this.gran);
        this.request.startGame(this.minXField, this.maxXField, this.minYField, this.maxYField, this.minZField, this.maxZField, this.speedPlat, this.vis, this.gran);
        this.playerId=this.request.getGamePeerId();
        //TODO delete playerID and/or owner and ownerId
        this.owner=this.playerId;
        this.ownerid=this.playerId;
        MultiLog.println(RTSGameGUI.class.toString(),"Player ID: "+this.playerId);
        //this.initializeKnowledges();
        //this.initTerrain();
        //this.initGroundGrid();
        //this.addLightToScene();
        this.initMoneyResource();
        //this.getHomePlanetAndDefense();
        //this.buyAndCreateMobileResource();
        //initKeys(); // load my custom keybinding
        //this.connected=true;
    }
    
    public void updatePositionGUI()
    {
        Vector3f v=this.cam.getLocation();//rootNode.getWorldTranslation();
        /*Vector2f v2=this.inputManager.getCursorPosition();
        this.textElement=this.nifty.getScreen("hud").findElementByName("actualCenterPosition");
        this.textRenderer=textElement.getRenderer(TextRenderer.class);
        this.textRenderer.setText(v.x+" "+v.z);*/
        this.screenController.setActualCentralPosition(v.x+" "+v.z);
        /*this.textElement=this.nifty.getScreen("hud").findElementByName("actualCursorPosition");
        this.textRenderer=textElement.getRenderer(TextRenderer.class);
        this.textRenderer.setText(v2.x+" "+v2.y);*/
    }
    
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
	    //minXField, maxXField, minYField, maxYField, minZField, maxZField, vel, vis, gran
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
    
    /**Terrain's initialization function*/
    public void initTerrain()
    {
        /* 1. Create terrain material and load four textures into it. */
        mat_terrain = new Material(assetManager, "Common/MatDefs/Terrain/Terrain.j3md");
        /* 1.1) Add ALPHA map (for red-blue-green coded splat textures) */
        mat_terrain.setTexture("Alpha", assetManager.loadTexture("Textures/Terrain/splat/groundmap.png"));
        /* 1.2) Add sky texture into the red layer (Tex1). */
        Texture sky = assetManager.loadTexture("Textures/Terrain/splat/sky3.jpg");
        sky.setWrap(WrapMode.Repeat);
        mat_terrain.setTexture("Tex1", sky);
        mat_terrain.setFloat("Tex1Scale", /*64f*/1f);
        @SuppressWarnings("UnusedAssignment")
        AbstractHeightMap heightmap = null;
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
        int patchSize = /*65*/1025; //risolution of the background's image + 1
        terrain = new TerrainQuad("my terrain", patchSize, /*513*/1025, heightmap.getHeightMap());
        /* 4. We give the terrain its material, position & scale it, and attach it. */
        terrain.setMaterial(mat_terrain);
        terrain.setLocalTranslation(0.0f, -5.0f, 0.0f);
        //terrain.setLocalScale(/*2*/1f, 1f, /*2*/1f);
        terrain.setLocalScale((float)(this.widthField)/1025,1.0f,(float)(this.heightField)/1025);//scalato per avere le dimensioni
        terrain.setName("terrain");
        rootNode.attachChild(terrain);
        /* 5. The LOD (level of detail) depends on were the camera is: */
        TerrainLodControl control = new TerrainLodControl(terrain, getCamera());
        terrain.addControl(control);
    }
    
    public void initGroundGrid()
    {
        tileMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        tileMat.setTexture("ColorMap",assetManager.loadTexture("Textures/Grid/grid.png"));
        //tileMat.setTexture("ColorMap",assetManager.loadTexture("Textures/FogOfWar/fog4.png"));
        tileMat.getAdditionalRenderState().setBlendMode(BlendMode.Alpha); // activate transparency
        tileMat.setReceivesShadows(false);
        tileMat.preload(renderManager);
        tileMat.getAdditionalRenderState().setFaceCullMode(FaceCullMode.Back);
        fogMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        fogMat.setTexture("ColorMap",assetManager.loadTexture("Textures/FogOfWar/fog4.png"));
        //tileMat.setTexture("ColorMap",assetManager.loadTexture("Textures/FogOfWar/fog4.png"));
        //tileMat.getAdditionalRenderState().setBlendMode(BlendMode.Alpha); // activate transparency
        fogMat.setReceivesShadows(false);
        fogMat.preload(renderManager);
        fogMat.getAdditionalRenderState().setFaceCullMode(FaceCullMode.Back);
        Box tile = new Box(Vector3f.ZERO, (float)this.gran/2, 0.01f, (float)this.gran/2);
        Geometry tileGeom = new Geometry("tile", tile);
        tileGeom.setMaterial(fogMat);
        //tileGeom.setQueueBucket(Bucket.Transparent);
        /*MultiLog.println(RTSGameGUI.class.toString(),"widthField: "+this.widthField+" heightField: "+this.heightField);
        MultiLog.println(RTSGameGUI.class.toString(),"gran: "+this.gran);*/
        MultiLog.println(RTSGameGUI.class.toString(),"Initialize grid of the ground, tile dimension is "+this.gran+", field dimensions are "+this.widthField+"x"+this.heightField);
        gridNode=new Node("gridNode");
        for(int i=0;i<(this.widthField/this.gran);i++)
            for(int j=0;j<(this.heightField/this.gran);j++)
                {
                //Spatial other=tileGeom.deepClone();
                Geometry other=tileGeom.clone();
                other.setLocalTranslation(new Vector3f((float)((-(this.widthField)/2+this.gran/2)+this.gran*i),0.0f,(float)((-(this.heightField)/2+this.gran/2)+this.gran*j)));
                //MultiLog.println(RTSGameGUI.class.toString(),"Grid's tile: "+other.getLocalTranslation().getX()+" "+other.getLocalTranslation().getY()+" "+other.getLocalTranslation().getZ());
                other.setUserData("index",(j*this.numTileX + i));
                other.setName("tile"+(j*this.numTileX + i));
                //MultiLog.println(RTSGameGUI.class.toString(),"i: "+i+" j: "+j+" this.numTileX: "+this.numTileX+" tile"+(j*this.numTileX + i));
                //rootNode.attachChild(other);
                gridNode.attachChild(other);
                }
        gridNode.setLocalTranslation(gridNode.getLocalTranslation().getX()-(float)(this.gran/2),gridNode.getLocalTranslation().getY(),gridNode.getLocalTranslation().getZ()-(float)(this.gran/2));
        rootNode.attachChild(gridNode);
        MultiLog.println(RTSGameGUI.class.toString(),"Created "+(int)((this.widthField/this.gran)*(this.heightField/this.gran))+" tile");
    }
    
    private void initMoneyResource()
    {
        this.request.addResourceEvolve("moneyEvolveble","Money",0,1000,1);
        //this.request.addResourceEvolve("moneyEvolveble","Money",8000.0,1000,1);
    }
    
    public void getHomePlanetAndDefense()
    {
        System.out.println("Home planet");
        Point punto=this.request.getGamePlayerPosition();
        MultiLog.println(RTSGameGUI.class.toString(),"Home planet position: "+punto.getX()+" "+punto.getY()+" delta:"+(this.maxXField-this.minXField)/2);
        Coordinate pos=this.transformer.patrolTojMonkey(punto,(this.maxXField-this.minXField)/2);
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
        this.crown=assetManager.loadModel("Models/crown3/crown3.j3o");
        this.crown.setName("SelectionCrown");
        this.crown.setLocalScale(2.5f, 1.0f, 2.5f);
        this.crown.setLocalTranslation(homePlanet.getLocalTranslation());
        rootNode.attachChild(crown);
        this.cam.setLocation(new Vector3f(pos.getIX(),20.0f+pos.getIY(),(pos.getIZ()-this.distCamera)));
        this.cam.lookAt(new Vector3f(pos.getIX(),pos.getIY(),pos.getIZ()), Vector3f.UNIT_Y);
        MultiLog.println(RTSGameGUI.class.toString(),"pianeta: "+pos.getX()+" "+pos.getY()+" "+pos.getZ()+" io "+pos.getX()+" "+(20.0f+(float) pos.getY())+" "+(pos.getZ()-this.distCamera));
        System.out.println("COMPRATA DIFESA");
        String timestamp = Long.toString(System.currentTimeMillis());
        this.request.addResource("strdef" + timestamp, "Defense" + timestamp, 1.0/*Double.parseDouble(defenseQt.getText())*/);
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
    
    public void setReadyState(boolean state)
    {
        this.ready=state;
    }
    
    /**Function that buy and create a mobile resource*/
    public boolean buyAndCreateMobileResource()
    {
        MultiLog.println(RTSGameGUI.class.toString(),"Creating a ship spatial");
        Double money=this.request.getMyResourceFromId("moneyEvolveble").getQuantity();
        if(this.resMinCost>money)
            return false;
        String timestamp = Long.toString(System.currentTimeMillis());
        this.request.createMobileResource("Attack"+timestamp, 1.0);
        this.request.UpdateResourceEvolve(this.request.getMyResourceFromId("moneyEvolveble").getQuantity()-this.resMinCost); 
        //System.out.println("COMPRATA ASTRONAVE");
        ArrayList<Object> myResources=this.request.getResources();
        //TODO check if last resource is a mobile resource
        GameResourceMobile grm=(GameResourceMobile) myResources.get(myResources.size()-1);
        MultiLog.println(RTSGameGUI.class.toString(),"Comprata astronave: x:"+grm.getX()+" z:"+grm.getY());
        //this.request.addResource("def" + timestamp, "Defense" + timestamp, 1.0/*Double.parseDouble(defenseQt.getText())*/); 
        //Spatial spaceship = assetManager.loadModel("Models/spaceship8-2/spaceship8-2.j3o");
        Coordinate pos=this.transformer.patrolTojMonkey(this.request.getGamePlayerPosition(),this.delta);
        MyShipControl shipControl=new MyShipControl(1.0f);
        Spatial spaceship =assetManager.loadModel("Models/spaceship-51-prova_per_jmonkey/spaceship-5.1-prova_per_jmonkey.j3o");
        spaceship.setName("spaceship_"+timestamp);
        spaceship.setUserData("id", grm.getId());
        spaceship.setUserData("owner", this.playerId);
        spaceship.setUserData("ownerID", this.playerId);
        spaceship.setUserData("type", "mobile");
        spaceship.setLocalScale(shipScale);
        spaceship.addControl(shipControl);
        spaceship.setLocalTranslation((int)(pos.getX()),(int)(pos.getY()),(int)(pos.getZ()));
        rootNode.attachChild(spaceship);
        //spaceship.setLocalTranslation((int)(this.homePlanetCoord.getX()+5.0f),(int)(this.homePlanetCoord.getY()),(int)(this.homePlanetCoord.getZ()+5.0f));
        MultiLog.println(RTSGameGUI.class.toString(),"Moving ship out of planet");
        if(pos.getIZ()>(-this.delta))
            //spaceship.setLocalTranslation((int)(pos.getX()),(int)(pos.getY()),(int)(pos.getZ()+this.gran));
            this.moveOnCreation(spaceship,new Vector3f((int)(pos.getX()),(int)(pos.getY()),(int)(pos.getZ()-this.gran)));
        else
            //spaceship.setLocalTranslation((int)(pos.getX()),(int)(pos.getY()),(int)(pos.getZ()-this.gran));
            this.moveOnCreation(spaceship,new Vector3f((int)(pos.getX()),(int)(pos.getY()),(int)(pos.getZ()+this.gran)));
        MultiLog.println(RTSGameGUI.class.toString(),"Ship is created and moved");
        /*for(int i=0;i<rootNode.getChildren().size();i++)
            MultiLog.println(RTSGameGUI.class.toString(),"Child: "+rootNode.getChild(i));*/
        return true;
    }
    
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
                    //MultiLog.println(RTSGameGUI.class.toString(),"Enemy ship found");
                    if(!this.enemyShipMap.containsKey(resource.getId()))
                        {
                        //MultiLog.println(RTSGameGUI.class.toString(),"\t it's new");
                        this.createEnemyStarship((int) resource.getX(), (int) resource.getY(), resource);
                        }
                    else
                        {
                        //MultiLog.println(RTSGameGUI.class.toString(),"\t it isn't new");
                        this.enemyShipMap.get(resource.getId()).setUserData("visible", "true");
                        }
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
    
    private void getStarShipVision(GameResourceMobile mob, double gran, GamePlayer player)
    {
    //MultiLog.println(RTSGameGUI.class.toString(),"Start getStarShipVisionVision");
    //vision parameter must be taken by net
    for (int i_x = (int) (mob.getX() - this.vis); i_x <= mob.getX() + this.vis; i_x += gran)
	for (int i_y = (int) (mob.getY() - this.vis); i_y <= mob.getY() + this.vis; i_y += gran )
            if(i_x>=this.minXField && i_y>=this.minYField && i_x<this.maxXField && i_y<this.maxYField)
                {
                //System.out.println("pos " + i_x + " ,  " + i_y);
                if ( !(i_x == player.getPosX() && i_y == player.getPosY()) && !(i_x == mob.getX() && i_y == mob.getY()))
                    this.addNewInfo(i_x, i_y, null);
                }
    //MultiLog.println(RTSGameGUI.class.toString(),"End tile vision");
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
                    //MultiLog.println(RTSGameGUI.class.toString(),"Enemy ship found");
                    if(!this.enemyShipMap.containsKey(resource.getId()))
                        {
                        //MultiLog.println(RTSGameGUI.class.toString(),"\t it's new");
                        this.createEnemyStarship((int) resource.getX(), (int) resource.getY(), resource);
                        }
                    else
                        {
                        //MultiLog.println(RTSGameGUI.class.toString(),"\t it isn't new");
                        this.enemyShipMap.get(resource.getId()).setUserData("visible", "true");
                        }
                    }
            }
    Vector3f center=new Vector3f((float)mob.getX(),(float)mob.getY(),(float)mob.getZ());
    for(int i=0;i<this.planets.size();i++)
        {
        //MultiLog.println(RTSGameGUI.class.toString(),i+"-th planet");
        VirtualResource planet=this.planets.get(i);
        Vector3f planetPos=new Vector3f((float)planet.getX(),(float)planet.getY(),(float)planet.getZ());
        //MultiLog.println(RTSGameGUI.class.toString(),"x="+planetPos.getX()+" y="+planetPos.getY());
        if(this.isInRange(planetPos,center,this.vis))
            this.addNewInfo((int)planetPos.getX(),(int)planetPos.getY(),planet);
        }
    GameResourceMobileResponsible ship = new GameResourceMobileResponsible(mob.getId(), mob.getDescription(), mob.getOwner(), mob.getOwnerId(),mob.getQuantity(), mob.getX(), mob.getY(), 0, mob.getVelocity(), /*mob.getVision()*/this.vis, 0, "", "");
    if ( !(mob.getX() == player.getPosX() && mob.getY() == player.getPosY()) )
        this.addNewInfo((int) mob.getX(), (int) mob.getY(), ship);
    }
    
    public void addMyPlanetPosition(int x, int y, GamePlayerResponsible myPlanet)
    {
	int x_pos = (int) Math.round( x / this.gran - 0.5);
	int y_pos = (int) Math.round( y / this.gran - 0.5);
	KnowledgeSpace planet = new KnowledgeSpace();
	planet.setToPlanet(myPlanet);
	this.knowledges.set((int)Math.round(y_pos*this.numTileX + x_pos), planet);
    }
    
    public void addNewInfo(int x, int y,Object obj)
    {
        /*MultiLog.println(RTSGameGUI.class.toString(),"Add New Info");
        MultiLog.println(RTSGameGUI.class.toString(),"\tParameter: x:"+x+" y:"+y);
        MultiLog.println(RTSGameGUI.class.toString(),"\t x_pos(double)="+((double) x/(double) this.gran - 0.5));
        MultiLog.println(RTSGameGUI.class.toString(),"\t y_pos(double)="+((double) y/(double) this.gran - 0.5));*/
        int x_pos = (int) Math.round( (double) x/(double) this.gran - 0.5);
	int y_pos = (int) Math.round( (double) y/(double) this.gran - 0.5);
        /*MultiLog.println(RTSGameGUI.class.toString(),"\t x_pos(int)="+x_pos);
        MultiLog.println(RTSGameGUI.class.toString(),"\t y_pos(int)="+y_pos);
        MultiLog.println(RTSGameGUI.class.toString(),"\t index of ArrayList:"+((int)Math.round(y_pos*this.numTileX + x_pos)));*/
	//vedere di che tipo è l'istanza ricevuta e creare la relativa Knowledge
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
            //MultiLog.println(RTSGameGUI.class.toString(),"It's a planet");
            KnowledgeSpace planetKs = new KnowledgeSpace();
            VirtualResource planet=(VirtualResource) obj;
            if(planet.getOwnerID().equals("null"))
                planetKs.setToUnconqueredPlanet(planet);
            else if(planet.getOwnerID().equals(this.ownerid))
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
    
    private boolean isInRange(Vector3f pos,Vector3f center,double vis)
    {
        if((pos.getX()<=(center.getX()+vis) && pos.getX()>=(center.getX()-vis)) && (pos.getY()<=(center.getY()+vis) && pos.getY()>=(center.getY()-vis)))
            return true;
        else
            return false;
    }
      
    /** Custom Keybinding: Map named actions to inputs. */
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
        //this.inputManager.addMapping("AddPlanet",new KeyTrigger(KeyInput.KEY_SPACE));
        // Add the names to the action listener.
        inputManager.addListener(actionListener, new String[]{"Pause","Toggle_HUD","Mouse_Left_Button_click","Mouse_Right_Button_click","Shift_r_l"});
        inputManager.addListener(analogListener, new String[]{"Exit","Mouse_movement_X_l","Mouse_movement_X_r", "Mouse_movement_Y_d","Mouse_movement_Y_u","Mouse_wheel_u","Mouse_wheel_d", "Left_arrow", "Right_arrow", "Up_arrow", "Down_arrow","+","-"});
    }
 
  private ActionListener actionListener = new ActionListener()
  {
    public void onAction(String name, boolean keyPressed, float tpf) 
        {
        if (name.equals("Pause") && !keyPressed)
            isRunning = !isRunning;
        else if(name.equals("Toggle_HUD") && !keyPressed)
            screenController.toggleHUD();
        else if(name.equals("Shift_r_l") && keyPressed)
            shiftPressed=true;
        else if(name.equals("Shift_r_l") && !keyPressed)
            shiftPressed=false;
        if (name.equals("Mouse_Left_Button_click") && !keyPressed) 
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
                // (For each , we know distance, impact point, geometry.)
                float dist = results.getCollision(i).getDistance();
                Vector3f pt = results.getCollision(i).getContactPoint();
                String target = results.getCollision(i).getGeometry().getName();
                //System.out.println("Selection #" + i + ": " + target + " at " + pt + ", " + dist + " WU away.");
                }
            // Use the results -- we rotate the selected geometry.
            if (results.size() > 0)
                {
                // The closest result is the target that the player picked:
                Geometry target = results.getClosestCollision().getGeometry();
                if(target.getName().contains("terrain") || target.getName().contains("Torus") || target.getName().contains("tile"))
                    {
                    //System.out.println("Terrain");
                    selected=null;
                    screenController.setSelection("Terrain");
                    if(rootNode.hasChild(crown))
                        rootNode.detachChild(crown);
                    }
                else
                    {
                    int i=rootNode.getChildIndex(target.getParent().getParent());
                    //System.out.println("I picked "+rootNode.getChild(i).getName());
                    //MultiLog.println(RTSGameGUI.class.toString(),"I picked "+rootNode.getChild(i).getName());
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
                // (For each , we know distance, impact point, geometry.)
                float dist = results.getCollision(i).getDistance();
                Vector3f pt = results.getCollision(i).getContactPoint();
                String target = results.getCollision(i).getGeometry().getName();
                //System.out.println("Selection #" + i + ": " + target + " at " + pt + ", " + dist + " WU away.");
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
  
  private void move(Spatial object,Vector3f contactPointPreCalc,boolean multiPath)
  {
    if(object!=null)
    if(object.getUserData("type")!=null)
    {
        if(object.getUserData("type").equals("mobile"))
        {
        Vector3f contactPoint=this.findTile(contactPointPreCalc);
        //System.out.println("Mobile resource");
        //MultiLog.println(RTSGameGUI.class.toString(), "Selection: "+contactPoint.getX()+" "+contactPoint.getZ());
        //MultiLog.println(RTSGameGUI.class.toString(), "Selection(int): "+(int)contactPoint.getX()+" "+(int)contactPoint.getZ());
        this.crown.setLocalTranslation(contactPoint.getX(), 0.0f, contactPoint.getZ());
        //MultiLog.println(RTSGameGUI.class.toString(),"Crown: "+this.crown.getLocalTranslation().getX()+" "+this.crown.getLocalTranslation().getY()+" "+this.crown.getLocalTranslation().getZ());
        int period_movement=/*250*/500;//millisecond
        String resId=object.getUserData("id");
        //MultiLog.println(RTSGameGUI.class.toString(),"Resource ID: "+resId);
        Coordinate start=new Coordinate(object.getLocalTranslation().getX(),0,object.getLocalTranslation().getZ());
        Coordinate arrival=new Coordinate(contactPoint.getX(),0,contactPoint.getZ());
        if(multiPath==false || object.getUserData("movement")==null)
            object.setUserData("movement",new HandlingMovement(this,resId,start,arrival,period_movement,object,this.request,this.delta,this.gran));
        else
            ((HandlingMovement)object.getUserData("movement")).addMovement(start, arrival);
        }
      else
          System.out.println("Fixed resource");
    }
  }
  
  private void moveOnCreation(Spatial object,Vector3f contactPointPreCalc)
  {
      //MultiLog.println(RTSGameGUI.class.toString(),"move on creation");
      //MultiLog.println(RTSGameGUI.class.toString(),"Spatial name:"+object.getName());
      if(object.getUserData("type")!=null && object.getUserData("type").equals("mobile"))
        {
        Vector3f contactPoint=this.findTile(contactPointPreCalc);
        System.out.println("Mobile resource");
        //MultiLog.println(RTSGameGUI.class.toString(),"Mobile resource");
        //MultiLog.println(RTSGameGUI.class.toString(), "Selection: "+contactPoint.getX()+" "+contactPoint.getZ());
        //MultiLog.println(RTSGameGUI.class.toString(), "Selection(int): "+(int)contactPoint.getX()+" "+(int)contactPoint.getZ());
        int period_movement=/*250*/500;//millisecond
        String resId=object.getUserData("id");
        //MultiLog.println(RTSGameGUI.class.toString(),"Resource ID: "+resId);
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
    
  private Vector3f findTile(Vector3f contactPoint)
  {
      //MultiLog.println(RTSGameGUI.class.toString(), "contactX: "+contactPoint.getX()+" contactZ: "+contactPoint.getZ());
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
      //MultiLog.println(RTSGameGUI.class.toString(), "tileX: "+tileX+" tileZ: "+tileZ);
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
 
  private AnalogListener analogListener = new AnalogListener() 
  {
    public void onAnalog(String name, float value, float tpf)
        {
        if(!isRunning)
            return;
        if(name.equals("Exit"))
            {
            /*TODO aggiungere controlli per chiusura socket ecc*/
            stop();
            }
        Vector3f v=cam.getLocation();
        float deltaTranslation=1.5f;
        float camLimitXp=(float) (maxXField/2);
        float camLimitXn=(float) -(maxXField/2);
        float camLimitZp=(float) ((maxYField/2)-distCamera);
        float camLimitZn=(float) (-(maxYField/2)-distCamera);
        if (name.equals("Left_arrow") && v.x<camLimitXp)
            {
            //MultiLog.println(RTSGameGUI.class.toString(),"left");
            cam.setLocation(new Vector3f(v.x+deltaTranslation,v.y,v.z));
            }
        else if (name.equals("Right_arrow") && v.x>camLimitXn) 
            {
            //MultiLog.println(RTSGameGUI.class.toString(),"right");
            cam.setLocation(new Vector3f(v.x-deltaTranslation,v.y,v.z));
            }
        else if (name.equals("Up_arrow") && v.z<camLimitZp)//forward  sistemare Y
            {
            //MultiLog.println(RTSGameGUI.class.toString(),"up");
            cam.setLocation(new Vector3f(v.x,v.y,v.z+deltaTranslation));
            }
        else if (name.equals("Down_arrow") && v.z>camLimitZn)//backwards
            {
            //MultiLog.println(RTSGameGUI.class.toString(),"down");
            cam.setLocation(new Vector3f(v.x,v.y,v.z-deltaTranslation));
            }
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
            {
                //MultiLog.println(RTSGameGUI.class.toString(),"Zoom in");
                cam.setLocation(new Vector3f(v.x,v.y-zoom,v.z+zoom));
            }
        else if(v.y<minZoom && (name.equals("Mouse_wheel_d") || name.equals("-")))
            {
                //MultiLog.println(RTSGameGUI.class.toString(),"Zoom out");
                cam.setLocation(new Vector3f(v.x,v.y+zoom,v.z-zoom));
            }
        }
  };
  
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
            String playerid=players.get(i);//players.get(i).substring(6);
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
    
    private void updatePosition(float tpf)
    {
        //MultiLog.println(RTSGameGUI.class.toString(),"Update position");
        HashMap positions=new HashMap();
        for(int i=0;i<this.rootNode.getChildren().size();i++)
            {
            //MultiLog.println(RTSGameGUI.class.toString(),i+"-th child name: "+this.rootNode.getChild(i).getName());
            HandlingMovement handler=(HandlingMovement)this.rootNode.getChild(i).getUserData("movement");
            if(handler!=null)
                {
                //MultiLog.println(RTSGameGUI.class.toString(),"\thas handler");
                Coordinate pos=handler.calculateNextWayPoint();
                if(pos!=null)
                    {
                    //MultiLog.println(RTSGameGUI.class.toString(),"Next hop");
                    positions.put(this.rootNode.getChild(i).getName(),pos);
                    }
                }
            }
        for(int i=0;i<positions.size();i++)
            {
            String index=(String) positions.keySet().iterator().next();
            Coordinate coord=(Coordinate) positions.get(index);
            //this.rootNode.getChild(index).setLocalTranslation((float)coord.getX(),(float)coord.getY(),(float)coord.getZ());
            Spatial ship=this.rootNode.getChild(index);
            MyShipControl shipControl=(MyShipControl) ship.getControl(0);
            if(shipControl.setMovement(coord))
                {
                System.out.println("Ready to go");
                //MultiLog.println(RTSGameGUI.class.toString(),"Ready to go");
                }
            }
    }
    
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
        //MultiLog.println(RTSGameGUI.class.toString(),"Numero mie risorse: "+myResources.size());
        for (int k = 0; k < myResources.size(); k++)
            {
            //MultiLog.println(RTSGameGUI.class.toString(),"Classe della risorsa: "+myResources.get(k).getClass().toString());
            if (myResources.get(k) instanceof GameResourceMobile)
                {
                //MultiLog.println(RTSGameGUI.class.toString(),"Risorsa mobile");
                this.getStarShipVision((GameResourceMobile) myResources.get(k), gran, gp);
                }
            }
        for(int i=0;i<this.widthField/this.gran;i++)
            for(int j=0;j<this.heightField/this.gran;j++)
                {
                SpaceType type=this.knowledges.get((int)Math.round(j*this.numTileX + i)).getType();
                //if(type.equals(SpaceType.SPACE) || type.equals(SpaceType.PLANET) || type.equals(SpaceType.STARSHIP))
                if(!type.equals(SpaceType.UNKNOW))
                    {
                    Spatial cell=gridNode.getChild("tile"+((int)Math.round(j*this.numTileX + i)));
                    if(cell==null)
                        System.exit(20);
                    cell.setMaterial(this.tileMat);
                    cell.setQueueBucket(Bucket.Transparent);
                    if(this.knowledges.get((int)Math.round(j*this.numTileX + i)).getElement()==null)
                        continue;
                    //MultiLog.println(RTSGameGUI.class.toString(),"Element: "+this.knowledges.get((int)Math.round(j*this.numTileX + i)).getElement().getClass().toString()+" x:"+i+" z:"+j);
                    if(type.equals(SpaceType.PLANET) && !matchCoordinates(i,j,this.homePlanetCoord) && (this.knowledges.get((int)Math.round(j*this.numTileX + i)).getElement() instanceof GamePlayerResponsible))
                        this.createEnemyHomePlanet(j, j, (GamePlayerResponsible)this.knowledges.get((int)Math.round(j*this.numTileX + i)).getElement());
                    else if((type.equals(SpaceType.UNCONQUEREDPLANET) || type.equals(SpaceType.ENEMYCONQUEREDPLANET) || type.equals(SpaceType.MYCONQUEREDPLANET)) && !matchCoordinates(i,j,this.homePlanetCoord) && (this.knowledges.get((int)Math.round(j*this.numTileX + i)).getElement() instanceof VirtualResource))
                        this.createOtherPlanets(j, j, (VirtualResource)this.knowledges.get((int)Math.round(j*this.numTileX + i)).getElement());
                    else if(type.equals(SpaceType.STARSHIP) && (this.knowledges.get((int)Math.round(j*this.numTileX + i)).getElement() instanceof GameResourceMobileResponsible))
                            {
                            //MultiLog.println(RTSGameGUI.class.toString(),"Ship rendering");
                            GameResourceMobileResponsible ship=(GameResourceMobileResponsible) this.knowledges.get((int)Math.round(j*this.numTileX + i)).getElement();
                            if(!ship.getOwnerId().equals(this.playerId))
                                //this.createEnemyStarship(j, j, ship);
                                {
                                //MultiLog.println(RTSGameGUI.class.toString(),"\tEnemy Ship rendering");
                                Spatial enemyShip=this.enemyShipMap.get(ship.getId());
                                if(enemyShip.getUserData("visible").equals("true") && rootNode.hasChild(enemyShip))
                                    {
                                    //MultiLog.println(RTSGameGUI.class.toString(),"\t\tOld");
                                    Coordinate coord=this.transformer.patrolTojMonkey(new Point(i,j), this.delta);
                                    //MultiLog.println(RTSGameGUI.class.toString(),"\t\t xP:"+i+" yP:"+j);
                                    //MultiLog.println(RTSGameGUI.class.toString(),"\t\t x:"+coord.getIX()+" z:"+coord.getIZ());
                                    //enemyShip.setLocalTranslation(coord.getIX(),coord.getIY(),coord.getIZ());
                                    enemyShip.setLocalTranslation(this.findTile(new Vector3f(coord.getIX(),coord.getIY(),coord.getIZ())));
                                    }
                                else if(enemyShip.getUserData("visible").equals("true") && !rootNode.hasChild(enemyShip))
                                    {
                                    //MultiLog.println(RTSGameGUI.class.toString(),"\t\tNew");
                                    Coordinate coord=this.transformer.patrolTojMonkey(new Point(i,j), this.delta);
                                    //MultiLog.println(RTSGameGUI.class.toString(),"\t\t xP:"+i+" yP:"+j);
                                    //MultiLog.println(RTSGameGUI.class.toString(),"\t\t x:"+coord.getIX()+" z:"+coord.getIZ());
                                    //enemyShip.setLocalTranslation(coord.getIX(),coord.getIY(),coord.getIZ());
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
    
    private boolean matchCoordinates(int i,int j,Coordinate coord)
    {
        Point point=this.transformer.jMonkeyToPatrol(coord, this.delta);
        Point tile=new Point(point.getX()/this.gran,point.getY()/this.gran);
        if((int)Math.round(j*this.numTileX + i)==(tile.getY()*this.numTileX+tile.getX()))
            return true;
        return false;
    }
    
    public void verifyPlanets(GameResourceMobile grm)
    {
        //*PIANETI: RICERCA DI PIANETI**//
	//verifica pianeti
	//ottengo lista di pianeti
	double x=grm.getX();
	double y=grm.getY();
	//ArrayList<VirtualResource> planets=this.planets;
	for(int k=0;k<planets.size();k++)
            {
            VirtualResource planet=planets.get(k); // per ogni pianeta, verifico se le sue coordinate sono dentro una finestra visiva della mia risorsa mobile
            //System.out.println("Coordinate: x= "+planet.getX()+ " y= "+planet.getY());
            //System.out.println("My coordinates x = "+x+ "y = "+y);
            if((x>=(planet.getX()-this.vis)) && (x<=(planet.getX()+this.vis)) && (y>=(planet.getY()-this.vis)) && (y<planet.getY()+this.vis))
                {
		System.out.println("PIANETA");
		System.out.println("Coordinate: x= "+planet.getX()+ " y= "+planet.getY());
		if(planet.getOwnerID().equals("null")) // se il pianeta non è stato conquistato da qualcuno
                    {
                    if(this.createResource(planet.getId())) //se ho abbastanza soldi per creare una diffesa
                        { //conquisto il pianeta
			System.out.println("CONQUISTO IL PIANETA "+planet.getId());
                        this.screenController.setTextNotification("Planet "+planet.getId()+" conquered");
			this.setPlanetOwner(planet.getId(),this.ownerid,this.owner); //lo conquisto 
                        this.UpdateLoggedUsers();
			HashMap<String,UserInfo> userslist=this.getLoggedUsers();
			Set<String> key_set=userslist.keySet();
			Iterator<String> iterator=key_set.iterator();
			System.out.println("USERS "+userslist.size());
			while(iterator.hasNext())
                        {		
                            String iduser=iterator.next();
                            UserInfo info=userslist.get(iduser);	
                            String user_id=info.getId();
                            String userip=info.getIp();
                            int userport=info.getPort();
                            if(!this.ownerid.equals(user_id))											
                                {
				System.out.println("INVIO MESSAGGIO A "+user_id);	
				PlanetConqueredMessage message=new PlanetConqueredMessage(this.ownerid,this.request.getIpAddress(),(/*this.portMin*/this.outPort+7),this.ownerid,this.owner,planet.getId());	
				System.out.println("Invio messaggio a "+userip+" , "+((userport+1)+7));
				String responseMessage=it.simplexml.sender.MessageSender.sendMessage(userip,((userport)+7),message.generateXmlMessageString());
				if(responseMessage.contains("ERROR"))
                                    {
                                    System.out.println(" Sending Message ERROR!");
                                    }
				else
                                    {
                                    //ack message
                                    MessageReader responseStartMessageReader = new MessageReader();
                                    Message receivedStartMessageReader = responseStartMessageReader.readMessageFromString(responseMessage.trim());
                                    AckMessage ackMessage = new AckMessage(receivedStartMessageReader);
                                    if (ackMessage.getAckStatus() == 0)
                                        {
					System.out.println(" Message received");
					}
                                    }
                                }				  				
                        }
                    }
                    else
                    {
                       this.screenController.setTextNotification("Not enough money to conquer the planet"); 
                    }
		}
            else if(!planet.getOwnerID().equals(this.ownerid))
                {
		//gestione di clash
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
                    String resId,peerId;
                    resId=planet.getId();
                    peerId=planet.getOwnerID();
                    //invio messaggio
                    long currentTime=System.currentTimeMillis();
                    //long timeStamp=currentTime;
                    System.out.println("#######################RTSGameGUI--->richiesta di scontro#############################");
                    Message message=new BotStartMatchRequestMessage(this.ownerid,grm.getId(),planet.getId(),Long.toString(currentTime));
                    String responseMessage=it.simplexml.sender.MessageSender.sendMessage(info.getIp(),(info.getPort()+7),message.generateXmlMessageString());	
                    MessageReader messageReader=new MessageReader();
                    Message receivedMessage = messageReader.readMessageFromString(responseMessage.trim());
                    if(receivedMessage.getMessageType().equals("BOTSTARTMATCH"))
                        {
			System.out.println("##########RTSGameGUI-->Risposta############ ");
			BotStartMatchResponseMessage response=new BotStartMatchResponseMessage(receivedMessage);
			boolean isInMatch=response.getInMatch();
			boolean decision=response.getDecision();
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
                                    this.setPlanetOwner(planet.getId(), this.ownerid,this.owner); //lo conquisto
                                    //long timestamp=System.currentTimeMillis();
                                    //this.writeLog(timestamp);
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
                                        if(!this.ownerid.equals(user_id))											
                                            {
                                            System.out.println("Invio messaggio a "+user_id);
                                            PlanetConqueredMessage msg=new PlanetConqueredMessage(this.ownerid,this.request.getIpAddress(),(this.outPort+7),this.ownerid,this.owner,planet.getId());
                                            String responseMess=it.simplexml.sender.MessageSender.sendMessage(userip,(userport+7),msg.generateXmlMessageString());
                                            if(responseMess.contains("ERROR"))
                                                {
						System.out.println(RTSGameGUI.class.toString()+ "Sending Message ERROR!");
						}	
                                            else
						{
						//	ack message
						MessageReader responseStartMessageReader = new MessageReader();
						Message receivedStartMessageReader = responseStartMessageReader.readMessageFromString(responseMess.trim());
						AckMessage ackMessage = new AckMessage(receivedStartMessageReader);
						if (ackMessage.getAckStatus() == 0)
                                                    {
                                                    System.out.println("Messaggio ricevuto da "+user_id);
                                                    //	System.out.println("Now Match is started");
                                                    }
						}
                                            }
					}
                                    }					
				}
                            else
                            {//destroy ship after losing
                                for(Iterator<Spatial> iter=rootNode.getChildren().iterator();iter.hasNext();)
                                {
                                    Spatial ship=iter.next();
                                    if(ship.getUserData("id").equals(grm.getId()))
                                        rootNode.detachChild(ship);
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
		// se il pianeta è mio non faccio niente
            }
        }	
    }
    
    public void verifyEnemies(GameResourceMobile grm)
    {		
        String resID=grm.getId();
	//controllo visibilità del grm
	ArrayList<Object> vision=grm.getResourceVision();	
	//creo un arraylist dove salvo la posizione dentro l'array della visibilita' della risorsa mobile
	ArrayList<Integer> array_pos=new ArrayList<Integer>();	
	for(int z=0;z<vision.size();z++)
	{
            if(vision.get(z) instanceof GamePlayerResponsible)
            {
                GamePlayerResponsible gpr=(GamePlayerResponsible)vision.get(z);
		if(!this.ownerid.equals(gpr.getId()))
                {
                    System.out.println("@@@@@@@@@@@@@@@@@@@@base nemica@@@@@@@@@@@@@@@@@@@");			
                    array_pos.add(new Integer(z));
                }
            }
            else if(vision.get(z) instanceof GameResourceMobileResponsible)
            {			
		GameResourceMobileResponsible grmr=(GameResourceMobileResponsible)vision.get(z);
		if(!this.ownerid.equals(grmr.getOwnerId()))
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
                    Message message=new BotStartMatchRequestMessage(this.ownerid,grm.getId(),player.getId(),Long.toString(currentTime));				
                    String responseMessage=it.simplexml.sender.MessageSender.sendMessage(info.getIp(),(info.getPort()+7),message.generateXmlMessageString());			
                    MessageReader messageReader=new MessageReader();
                    Message receivedMessage = messageReader.readMessageFromString(responseMessage.trim());			    
                    if(receivedMessage.getMessageType().equals("BOTSTARTMATCH"))
                    {
			System.out.println("##########RTSGameBot-->Risposta############ ");
			BotStartMatchResponseMessage response=new BotStartMatchResponseMessage(receivedMessage);
			boolean isInMatch=response.getInMatch();
			boolean decision=response.getDecision();
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
                                    Spatial ship=iter.next();
                                    if(ship.getUserData("id").equals(resID))
                                        rootNode.detachChild(ship);
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
                    Message message=new BotStartMatchRequestMessage(this.ownerid,grm.getId(),res_grm.getId(),Long.toString(currentTime));
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
                                    Spatial ship=iter.next();
                                    if(ship.getUserData("id")==null)
                                        System.exit(-5);
                                    else
                                        System.out.println("MORTA id="+ship.getUserData("id"));
                                    if(ship.getUserData("id").equals(resID))
                                        rootNode.detachChild(ship);
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
    
    public boolean createResource(String idRes)
    {
        //compro risorsa di difesa
        double qt=this.request.getMyResourceFromId("moneyEvolveble").getQuantity();
        int multiplicity=(int) (qt/this.resMinCost);
        if(multiplicity>0)
            {
            qt=multiplicity*this.resMinCost; //uso la stessa variabile
            String timestamp = Long.toString(System.currentTimeMillis());
            //this.nres++;
            //res.add(new GameResource("id"+this.nres,"defense",1.0));
            //GameResource dif = new GameResource(idRes, "Defense" + timestamp, resmincost);
            //this.gp.addToMyResource(dif);
            this.request.addResource(idRes, "Defense" + timestamp, this.resMinCost);
            //currentmoney-=1000;
            this.request.UpdateResourceEvolve(this.request.getMyResourceFromId("moneyEvolveble").getQuantity()-qt);
            //System.out.println("###########nuova risorsa di difesa####################");
            return true;	
            }
        else
            {
            //System.out.println("NON HO SOLDI PER COMPRARE DIFFESE");
            return false;
            }	
    }
    
    public HashMap<String,UserInfo> getLoggedUsers()
    {
        return this.loggedusers;
    }
    
    public void setPlanetOwner(String idPlanet,String idOwner,String nameOwner)
    {
	this.getPlanetbyID(idPlanet).setOwnerID(idOwner);
	this.getPlanetbyID(idPlanet).setOwnerName(nameOwner);	
        rootNode.getChild("uPlanet_"+idPlanet).setUserData("ownerId",idOwner);
    }
    
    public UserInfo getLoggedUserInfo(String id)
    {
        return this.loggedusers.get(id);	
    }
    
    public String getOwnerid()
    {
        return ownerid;
    }
    
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
    
    private void createOtherPlanets(int x,int y,VirtualResource planet)
    {
        //MultiLog.println(RTSGameGUI.class.toString(),"Create new planet x="+x+" y="+y);
        Coordinate coord=this.transformer.patrolTojMonkey(new Point(x*this.gran,y*this.gran),this.delta);
        //MultiLog.println(RTSGameGUI.class.toString(),"\tjmonkey x="+coord.getIX()+" z="+coord.getIZ());
        String resOwnerId=planet.getOwnerID();
        if(rootNode.getChild("myPlanet_"+planet.getId())!=null || rootNode.getChild("uPlanet_"+planet.getId())!=null || rootNode.getChild("ePlanet_"+planet.getId())!=null)
            return;
        if(resOwnerId.equals(this.playerId))
            {//TODO check the code
            //System.out.println("My planet");
            //MultiLog.println(RTSGameGUI.class.toString(),"My planet position: "+(x*this.gran)+" "+(y*this.gran));
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
            //MultiLog.println(RTSGameGUI.class.toString(),"pianeta: "+coord.getX()+" "+coord.getY()+" "+coord.getZ());
            //System.out.println("COMPRATA DIFESA");
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
            //System.out.println("Unknown planet");
            //MultiLog.println(RTSGameGUI.class.toString(),"Unknown planet position: "+(x*this.gran)+" "+(y*this.gran));
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
            //MultiLog.println(RTSGameGUI.class.toString(),"pianeta: "+coord.getX()+" "+coord.getY()+" "+coord.getZ());
            uPlanet.setUserData("defenseName", "");
            }
        else    //enemy planet
            {
            //System.out.println("Enemy planet");
            //MultiLog.println(RTSGameGUI.class.toString(),"Enemy planet position: "+(x*this.gran)+" "+(y*this.gran));
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
    
    private void createEnemyHomePlanet(int x,int y,GamePlayerResponsible planet)
    {
        Coordinate coord=this.transformer.patrolTojMonkey(new Point(x*this.gran,y*this.gran),this.delta);
        String resId=planet.getId();
        if(rootNode.getChild("ehPlanet_"+planet.getName())!=null)
            return;
        //System.out.println("Enemy Home planet");
        //MultiLog.println(RTSGameGUI.class.toString(),"Enemy Home planet position: "+(x*this.gran)+" "+(y*this.gran));
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
        //MultiLog.println(RTSGameGUI.class.toString(),"pianeta: "+coord.getX()+" "+coord.getY()+" "+coord.getZ());
        ehPlanet.setUserData("defenseName", "");
    }
    
    private void createEnemyStarship(int x,int y,GameResourceMobileResponsible ship)
    {
        //MultiLog.println(RTSGameGUI.class.toString(),"Creating an enemy ship spatial");
        String timestamp = Long.toString(System.currentTimeMillis());
        Coordinate pos=this.transformer.patrolTojMonkey(new Point(x,y),this.delta);
        //MultiLog.println(RTSGameGUI.class.toString(),"\t\t at xP:"+x+" yP:"+y);
        //MultiLog.println(RTSGameGUI.class.toString(),"\t\t at x:"+pos.getIX()+" y:"+pos.getIZ());
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
        //MultiLog.println(RTSGameGUI.class.toString(),"Enemy Ship is created");
    }
    
    private void updateGameView(float tpf) 
    {
        Vector3f v=cam.getLocation();
        float deltaTranslation=1.5f;
        if(left)
            {
            //MultiLog.println(RTSGameGUI.class.toString(),"left");
            cam.setLocation(new Vector3f(v.x+deltaTranslation,v.y,v.z));
            }
        else if(right)
            {
            //MultiLog.println(RTSGameGUI.class.toString(),"right");
            cam.setLocation(new Vector3f(v.x-deltaTranslation,v.y,v.z));
            }
        if(up)
            {
            //MultiLog.println(RTSGameGUI.class.toString(),"up");
            cam.setLocation(new Vector3f(v.x,v.y,v.z+deltaTranslation));
            }
        else if(down)
            {
            //MultiLog.println(RTSGameGUI.class.toString(),"down");
            cam.setLocation(new Vector3f(v.x,v.y,v.z-deltaTranslation));
            }
        this.screenController.setActualCentralPosition(v.x+" "+v.z);
    }
    
    public void UpdateLoggedUsers()
    {
        this.loggedusers=new HashMap<String,UserInfo>();
	ArrayList<String> usersList=this.request.getLoggedUsersList();//this.getMyGamePeer().getLoggedUsersList();
	if(!usersList.isEmpty())// se ci sono degli utenti nella lista invio i messaggi
            {
            //System.out.println("NUMBER OF USERS: "+usersList.size());
            for(int u=0;u<usersList.size();u++)
                {
		String str_user=usersList.get(u);
		//System.out.println("USERS: "+str_user);
		String[] array_user=str_user.split(",");
		String userid=array_user[0]; // mi serve ???
		String userip=array_user[1];
		String userport=array_user[2];
		UserInfo info=new UserInfo(userid,userip,Integer.parseInt(userport));
		loggedusers.put(userid,info);
		}	
            }
    }

    @Override
    public void simpleRender(RenderManager rm) 
    {
        //TODO: add render code
    }
    
    public void setVisionToHome()
    {
        @SuppressWarnings("UnusedAssignment")
        Vector3f v=cam.getLocation();
        this.cam.setLocation(new Vector3f(this.homePlanetCoord.getIX(),20.0f+this.homePlanetCoord.getIY(),(this.homePlanetCoord.getIZ()-this.distCamera)));
        v=cam.getLocation();
        this.screenController.setActualCentralPosition(v.x+" "+v.z);
    }
    
    public String getSelType()
    {
        if(this.selected==null)
            return "-";
        String name=this.selected.getName();
        if(name.equals("home") || name.contains("planet"))
                return "Planet";
        else if(name.contains("spaceship"))
            return "Ship";
        else
            return "Unknown";
    }
    
    public String getResOwner()
    {
        if(this.selected==null)
            return "-";
        return this.selected.getUserData("ownerId");
    }
    
    public String getNumDefence()
    {
        if(this.selected==null)
            return "-";
        if(this.selected.getUserData("type").equals("fixed"))
            {
            if(this.selected.getUserData("hasDefense").equals(true))
                return rootNode.getChild(this.selected.getUserData("defenseName").toString()).getUserData("numDefense").toString();
            else
                return "0";
            }
        else
        //if(this.selected.getUserData("type").equals("mobile"))
            return "-";
    }
    
    public String getActualMoney()
    {
        return "Money: "+Double.toString(this.request.getMyResourceFromId("moneyEvolveble").getQuantity());
    }

    public void buyHomeDefence()
    {
        if(this.request.getMyResourceFromId("moneyEvolveble").getQuantity()-this.resMinCost<0)
            return;
        String timestamp = Long.toString(System.currentTimeMillis());
        this.request.addResource("strdef" + timestamp, "Defense" + timestamp, 1.0);
        this.request.UpdateResourceEvolve(this.request.getMyResourceFromId("moneyEvolveble").getQuantity()-this.resMinCost); 
    }
      
    //developer's modificator
    private boolean showGUI=true;
    static private boolean showSettingsContr=false;
    private boolean displayFPS=false;
    private boolean displayStatView=false;
    private StatusPrinter printer;
    
    //jmonkey's properties
    private TerrainQuad terrain;
    private Material mat_terrain;
    private Material tileMat;
    private Material fogMat;
    static protected int width=800/*1600*/;
    static protected int height=600/*900*/;
    static protected int colorDepth=24;
    static protected boolean fullScreen=false;
    static protected boolean vSynch=/*true*/false;
    static protected short samples=/*4*/0;
    static protected String title="Patrol's RTS Space Game";
    protected int minX=10;
    protected int minY=10;
    protected int maxX=width-10;
    protected int maxY=height-10;
    protected int minZ=0;
    protected int maxZ=5;
    protected float visAngle=-45.0f;
    protected boolean left=false;
    protected boolean right=false;
    protected boolean up=false;
    protected boolean down=false;
    protected boolean isRunning=true;
    private CoordinatesMapping transformer;
    private Point actualCenterPosition;
    private Point actualCursorPosition;
    private Vector3f upCamera;    //this vector3f define the up direction for the camera
    private Quaternion rotCamera;   //quaternion used to rotate camera at start
    private float distCamera=20.0f;   //indicates the distance from camera to the pointed object
    private float zoom=5.0f;     //zoom value
    private Vector3f pointed;   //point pointed
    private float minZoom=(float) (this.distCamera*2);
    private float maxZoom=(float) (this.distCamera/2);
    public Coordinate homePlanetCoord;//TODO public only for stamp the status
    private float planetScale=1.3f;
    private float shipScale=1/4f;
    private Spatial crown;
    private Spatial selected;
    private float planetRotSpeed=1/10f;
    private float defenseRotSpeed=1/15f;
    private Node gridNode;
    private boolean shiftPressed=false;
    public HashMap<String,Spatial> enemyShipMap;    //TODO public only for stamp the status
    
    //niftyGUI's properties
    private MyScreenController screenController;
    private Nifty nifty;
    private Element textElement;
    private TextRenderer textRenderer;
    //public HUDScreenController HUDScreen;
    
    //platform's properties
    private static final long serialVersionUID = 1L;
    private MessageSender request;
    private String playerId;
    private String owner;
    private String ownerid;
    private boolean inClash=false;
    
    //properties related to communication between GUI and platform
    private int inPort,outPort,idLength,serverPort,gameInPort,gameOutPort,gameServerPort,stab,fix,check,pub;
    private String id,serverAddr,gameServerAddr,user,pwd;
    public ArrayList<KnowledgeSpace> knowledges;    //TODO public only for stamp the status
    public ArrayList<VirtualResource> planets;    //TODO public only for stamp the status
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
    public HashMap<String, UserInfo> loggedusers;    //TODO public only for stamp the status
    /**
     * Default value for the communication with the MainGamePeer 
     * (used for the 'internal' communication among GUI and the GamePeer)
     */
    private int gamePeerPort;
}