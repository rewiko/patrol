/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.unipr.ce.dsg.patrol.GUI3D;

import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.controls.TextField;
import de.lessvoid.nifty.elements.Element;
import de.lessvoid.nifty.elements.render.TextRenderer;
import de.lessvoid.nifty.screen.Screen;
import de.lessvoid.nifty.screen.ScreenController;
import it.unipr.ce.dsg.patrol.util.MultiLog;

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;

/**
 *
 * @author Benassi Michael Micconi Giorgio
 */
public class MyScreenController extends AbstractAppState implements ScreenController
{
    public MyScreenController(RTSGameGUI gui)
    {
        this.gui=gui;
        this.i=-1;
        this.time=0;
        this.screensMap=new HashMap<Integer,String>();
        this.screensMap.put(0, "loadingNetwork");
        this.screensMap.put(1, "loadingKnowledges");
        this.screensMap.put(2, "loadingTerrain");
        this.screensMap.put(3, "loadingGrid");
        this.screensMap.put(4, "loadingLights");
        this.screensMap.put(5, "loadingHome");
        this.screensMap.put(6, "loadingKeys");
        this.screensMap.put(7, "hud");
    }
    
    public void bind(Nifty nifty, Screen screen) 
    {
        this.nifty=nifty;
        this.screen=screen;
    }

    public void onStartScreen() 
    {
        //throw new UnsupportedOperationException("Not supported yet.");
    }

    public void onEndScreen() 
    {
        //throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public void quit()
    {
        this.app.stop();
    }
    
    public void toLogin()
    {
        this.nifty.gotoScreen("loginScreen");
        this.nifty.getScreen("loginScreen").findNiftyControl("pwd",TextField.class).enablePasswordChar('*');
    }
    
    public void startGame()
    {
        //Screen screen=this.nifty.getScreen("loginScreen");
        TextField control=screen.findNiftyControl("user",TextField.class);
        if(control.getText().isEmpty())
            return;
        control=screen.findNiftyControl("pwd",TextField.class);
        if(control.getText().isEmpty())
            return;
        control=screen.findNiftyControl("serverIP",TextField.class);
        if(control.getText().isEmpty())
            return;
        control=screen.findNiftyControl("serverPort",TextField.class);
        if(control.getText().isEmpty())
            return;
        control=screen.findNiftyControl("myFirstPort",TextField.class);
        if(control.getText().isEmpty())
            return;
        //this.nifty.gotoScreen("loading");
        this.i=0;
        this.oldDate=new Date();
    }
    
    public void startGameWithoutGUI()
    {
        this.i=-2;
        this.oldDate=new Date();
        //this.nifty.gotoScreen("hud");
    }
    
    public void goToHudStart()
    {
        this.nifty.gotoScreen("hud");
    }
    
    public void goToEndGame(boolean victory)
    {
        if(victory)
            this.nifty.gotoScreen("endGameWin");
        else
            this.nifty.gotoScreen("endGameLose");
    }
    
    public void endGame()
    {
        this.gui.stop();
    }
    
    /*public void setEndMessage(String message)
    {
        textElement=nifty.getScreen("endGame").findElementByName("endGameButton");
        textRenderer=textElement.getRenderer(TextRenderer.class);
        textRenderer.setText(message.concat(" Click To Esc"));
    }*/
    
    public void back()
    {
        this.nifty.gotoScreen("start");
    }
    
    public String getUser()
    {
        return this.user;
    }
    
    public String getPwd()
    {
        return this.pwd;
    }
    
    public String getServerAddress()
    {
        return this.serverAddr;
    }
    
    public String getServerPort()
    {
        return String.valueOf(this.serverPort);
    }
    
    public String getOutPort()
    {
        return String.valueOf(this.outPort);
    }
    
    public void setUser(String user)
    {
        this.user=user;
    }
    
    public void setPwd(String pwd)
    {
        this.pwd=pwd;
    }
    
    public void setServerAddress(String serverAddr)
    {
        this.serverAddr=serverAddr;
    }
    
    public void setServerPort(int serverPort)
    {
        this.serverPort=serverPort;
    }
    
    public void setOutPort(int outPort)
    {
        this.outPort=outPort;
    }
    
    public String getActualCentralPosition()
    {
        return this.actualCentralPosition;
    }
    
    public void setActualCentralPosition(String position)
    {
        this.actualCentralPosition=position;
        if(this.i==-2)
            {
            textElement=nifty.getScreen("hud").findElementByName("coordText");
            textRenderer=textElement.getRenderer(TextRenderer.class);
            textRenderer.setText(this.actualCentralPosition);
            }
    }
    
    public String getActualCursorPosition()
    {
        return this.actualCentralPosition;
    }
    
    public void setActualCursorPosition(String position)
    {
        this.actualCentralPosition=position;
    }
    
    public String getSelection()
    {
        return this.selection;
    }
    
    public void setSelection(String sel)
    {
        this.selection=sel;
        if(this.i==-2)
            {
            textElement=nifty.getScreen("hud").findElementByName("selText");
            textRenderer=textElement.getRenderer(TextRenderer.class);
            textRenderer.setText(this.selection);
            this.setDefence();
            this.setOwner();
            this.setType();
            }
    }
    
    public String getMoney()
    {
        return Float.toString(0.0f);
    }
    
    public void setTextMoney()
    {
        if(this.i==-2)
            {
            textElement=nifty.getScreen("hud").findElementByName("moneytext");
            textRenderer=textElement.getRenderer(TextRenderer.class);
            textRenderer.setText(this.gui.getActualMoney());
            }
    }
    
    public void setTextNotification(String notification)
    {
        if(this.i==-2)
        {
            textElement=nifty.getScreen("hud").findElementByName("notifyText");
            textRenderer=textElement.getRenderer(TextRenderer.class);
            textRenderer.setText(notification);
        }
    }
    
    public String getType()
    {
        return "type";
    }
    
    public void setType()
    {
        if(this.i==-2)
            {
            textElement=nifty.getScreen("hud").findElementByName("typeText");
            textRenderer=textElement.getRenderer(TextRenderer.class);
            textRenderer.setText(this.gui.getSelType());
            }
    }
    
    public String getOwner()
    {
        return "owner";
    }
    
    public void setOwner()
    {
        if(this.i==-2)
            {
            textElement=nifty.getScreen("hud").findElementByName("ownerText");
            textRenderer=textElement.getRenderer(TextRenderer.class);
            textRenderer.setText(this.gui.getResOwner());
            }
    }
    
    public String getDefence()
    {
        return "50";
    }
    
    public void setDefence()
    {
        if(this.i==-2)
            {
            textElement=nifty.getScreen("hud").findElementByName("defenceText");
            textRenderer=textElement.getRenderer(TextRenderer.class);
            textRenderer.setText(this.gui.getNumDefence());
            }
    }
    
    public void buyHomeDefenceClicked()
    {
        this.gui.buyHomeDefence();
    }
    
    public void toggleHUD()
    {
        this.hudVisible=!this.hudVisible;
        for(Iterator<Element> iter=this.nifty.getScreen("hud").getLayerElements().iterator(); iter.hasNext();)
            {
                Element elem=iter.next();
                if(!elem.getId().equals("angleLayer"))
                    elem.setVisible(this.hudVisible);
               /* else
                    {
                    CustomButtonController controller=nifty.getScreen("hud").findControl(elem.getId(),CustomButtonController.class);
                    //textRenderer=textElement.getRenderer(TextRenderer.class);
                    if(controller.getText().equals("Hide"))
                        controller.setText("Show");
                    else
                        controller.setText("Hide");
                    }*/
            }
    }
    
    public void homeVisionClicked()
    {
        this.gui.setVisionToHome();
    }
    
    public void buyShipClicked()
    {
        MultiLog.println(RTSGameGUI.class.toString(),"Buy ship button clicked");
        if(this.gui.buyAndCreateMobileResource())
            MultiLog.println(RTSGameGUI.class.toString(),"The ship was bought");
        else
            MultiLog.println(RTSGameGUI.class.toString(),"Couldn't buy the ship");
    }
    
    @Override
    public void initialize(AppStateManager stateManager, Application app) 
    {
        super.initialize(stateManager, app);
        this.app=(SimpleApplication)app;
    }
 
    @Override
    public void update(float tpf) 
    { 
    Date newDate=new Date();
        if(this.i>=0 && ((newDate.getTime()-this.oldDate.getTime()>=3000) || this.i==0))
            {
            this.oldDate=newDate;
            this.nifty.gotoScreen(this.screensMap.get(this.i));
            if(this.i==0)
                {
                this.gui.connect();
                this.i++;
                }
            else if(this.i==1)
                {
                this.gui.initializeKnowledges();
                this.i++;
                }
            else if(this.i==2)
                {
                this.gui.initTerrain();
                this.i++;
                }
            else if(this.i==3)
                {
                this.gui.initGroundGrid();
                this.i++;
                }
            else if(this.i==4)
                {
                this.gui.addLightToScene();
                this.i++;
                }
            else if(this.i==5)
                {
                this.gui.getHomePlanetAndDefense();
                this.i++;
                }
            else if(this.i==6)
                {
                this.gui.initKeys();
                this.i++;
                }
            else if(this.i==7)
                {
                this.gui.updatePositionGUI();
                this.app.getFlyByCamera().setDragToRotate(false);  //to allow the user to interact using mouse
                this.i=-2;
                this.gui.setReadyState(true);
                }
            }
        if(this.i==-2 && (newDate.getTime()-this.oldDate.getTime()>=3000))
            {
            this.oldDate=newDate;
            this.setTextMoney();
            }
    }
    
    private Nifty nifty;
    private Screen screen;
    private SimpleApplication app;
    private int i;
    private float time;
    private Date oldDate;
    private HashMap<Integer,String> screensMap;
    private Element textElement;
    private TextRenderer textRenderer;
    
    private RTSGameGUI gui;
    
    private boolean hudVisible=true;
    
    private String user;
    private String pwd;
    private String serverAddr;
    private int serverPort;
    private int outPort;
    private String actualCentralPosition;
    private String actualCursorPosition;
    private String selection;
}