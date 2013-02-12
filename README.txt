####################
README PATROL GUI 3D
####################

The principal class of the project is it.unipr.ce.dsg.patrol.gui3d.RTSGameGUI. It contains all the core code of the client. It's one of the last class that must be launched 
to start the application. It takes 4 arguments, as shown below in run configuration description:
1) show graphics settings window, it allow to choose resolution, AA sample, VSynch, Fullscreen, colours depth (if it's true)
2) show login GUI, it allow the user to choose name, password, My First Port (port used for various communication, every players, humans or bots, must have a different value), 
	IP address of server (for now always 127.0.0.1), and Message Port (it's for MessageSender class, it must be equal to the one used in the MainGamePeer of every peer and it 
	must be different for every player)(if it's true)
3) show fps, it allow to show frame per second number, useful for performance optimization (if it's true)
4) show graphics stats, it allow to show other graphics information and statistics, useful for performance optimization (if it's true)
In jMonkeyEngine SDK there are just several run configurations, 2 for this class:
- GUI3D: development mode, without login GUI, use of default values. It can be used easily with HumanMainGamePeer run configuration of MainGamePeer;
- GUI3D_2: game mode, with login GUI, use of user's values. It can be used easily with HumanMainGamePeer2 run configuration of MainGamePeer using MessagePort=63000. In case
	of playing with also GUI3D configuration, the First Port must be different from the one show in the login GUI (40000).

-------------USE CASES------------------------------------------------------------------------
LAUNCH APPLICATIONS 1 HUMAN PLAYER
[run configuration of jMonkeyEngine SDK] class to launch (arguments e details)
1)[Server] it.unipr.ce.dsg.patrol.gui.RTSGameBootstrapServer
2)
	2.1)[HumanMainGamePeer] it.unipr.ce.dsg.patrol.gui3d.RTSHumanMainGamePeer
	or
	2.2)[HumanMainGamePeer2] it.unipr.ce.dsg.patrol.gui3d.RTSHumanMainGamePeer (message port is 63000 in run configuration HumanMainGamePeer2, it can also be omitted in this case and will be used the default one, 9998)
3)
	3.1)[GUI3D] it.unipr.ce.dsg.patrol.gui3d.RTSGameGUI (show graphics settings window, show login GUI, show fps, show graphics stats; in run configuration is false false false false; development mode)
	or
	3.2)[GUI3D_2] it.unipr.ce.dsg.patrol.gui3d.RTSGameGUI (in run configuration is false true false false; game mode)

LAUNCH APPLICATIONS 2 HUMAN PLAYERS
[run configuration of jMonkeyEngine SDK] class to launch (arguments e details)
1)[Server] it.unipr.ce.dsg.patrol.gui.RTSGameBootstrapServer
2)
	2.1)-[HumanMainGamePeer] it.unipr.ce.dsg.patrol.gui3d.RTSHumanMainGamePeer
		and
		-[HumanMainGamePeer2] it.unipr.ce.dsg.patrol.gui3d.RTSHumanMainGamePeer (message port is 63000 in run configuration HumanMainGamePeer2)
	or
	2.2) 2 times it.unipr.ce.dsg.patrol.gui3d.RTSHumanMainGamePeer but with 2 different port as argument
		
3)
	3.1)-[GUI3D] it.unipr.ce.dsg.patrol.gui3d.RTSGameGUI (show graphics setting window, show login GUI, show fps, show graphics stats; in run configuration is false false false false; development mode)
		and
		-[GUI3D_2] it.unipr.ce.dsg.patrol.gui3d.RTSGameGUI (in run configuration is false true false false; game mode)
	or
	3.2) 2 times GUI3D_2 but in login screen insert 2 different First Port and the 2 Message Ports must be equal to the 2 used in 2) respectively

LAUNCH APPLICATIONS 1 HUMAN PLAYER 1 BOT
Equal to above but in 2) one of the GamePeer must be:
1) [BotMainGamePeer] it.unipr.ce.dsg.patrol.platform.bot.RTSGameMainPeer
or
2) [BotMainGamePeer2] it.unipr.ce.dsg.patrol.platform.bot.RTSGameMainPeer2
and in 3) one of the 2 GUI must be:
1) [MyBot1] it.unipr.ce.dsg.patrol.gui3d.RTSGameBotLauncher (can be passed the communication port, if there isn't it will calculate one random )
or
2) [MyBot2] it.unipr.ce.dsg.patrol.gui3d.RTSGameBotLauncher (communicatio port=7700)

-------------TUTORIAL------------------------------------------------------------------------
In development mode (second argument of class RTSGameGUI equal to false) the first screen displayed is constituted 
by a single button. Clicking on it the game starts, the GUI appears. The planet at the center of the view is your 
home planet and it has already a defence of minimum value (the grey ring around the planet). You can buy ship clicking 
on the button on the left, same thing for the defence. In both cases a new window will be open. Here, there is a slider 
that allow you to choose the value (and the cost) of the resource. If you have enough money to buy the resource, when 
you click on the button, you will return at the GUI screen. If you have buy a ship, it will come out from the planet. 
Otherwise the defence won't be shown because there is already one. The button Go To Home centers the view on the home planet. 
Hide/Show causes that the GUI will be hidden or shown. Above the button there is a indication of the quantity of money 
actually accumulated. This value is updated every 3 seconds (this temporization is customable, change the code in 
MyScreenController). At top of the screen there is the NotifyArea. When you conquer a planet, a message will be shown. 
It can be very useful for debug purposes instead of using console output or MultiLog files. To move a ship you have to 
selected it with the left button of the mouse, clicking on the model (from far distance the selection doesn't work very 
well, it needs a lot of click). After that you must click on a point of the grid with the right button (automatically will 
be selected the center of the cell) and ship will start to go in that direction. If you want you can create a multi-point 
path. To do this, also while the ship is already moving, you have to push and hold the shift button and click with the 
right on the grid. All points will be enqueued and the ship progressively will reach everyone of these. To see if a 
planet was conquered by someone you can select it and look at the long central value, in the GUI at bottom. There is 
the ID of the owner. The value on the same row is the type of the resource (planet, ship, ecc.). The value below this is 
the name of the resource. At right, on the first row, there is the number of the defences of a planet (if the selection is 
a planet will be wrote -). The value below are the coordinates of the center of the view.

-------------TODO AND PROBLEMS--------------------------------------------------------------
See the file Problems.txt

-------------INFO ABOUT jMonkey and NiftyGUI------------------------------------------------
Go to the site http://jmonkeyengine.org/, there is a forum and several tutorials which can be useful, both for 
jMonkey and NiftyGUI. For NiftyGUI, there is the manual on http://nifty-gui.lessvoid.com/ . The manual 
can be useful to start but after a certain point it's pretty useless. Look the xml/java code that already 
exist in the project and takes a look at the examples integrated in the SDK (New File/GUI/ there are several 
files).