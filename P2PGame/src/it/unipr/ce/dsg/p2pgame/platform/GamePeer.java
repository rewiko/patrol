package it.unipr.ce.dsg.p2pgame.platform;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import it.simplexml.message.AckMessage;
import it.simplexml.message.Message;
import it.simplexml.message.MessageReader;
import it.simplexml.sender.MessageSender;
import it.unipr.ce.dsg.p2pgame.GUI.RTSGameGUI;
import it.unipr.ce.dsg.p2pgame.network.NetPeer;
import it.unipr.ce.dsg.p2pgame.network.NetPeerInfo;
import it.unipr.ce.dsg.p2pgame.platform.message.CheckMobileResourceMessage;
import it.unipr.ce.dsg.p2pgame.platform.message.CheckPositionPlayerMessage;
import it.unipr.ce.dsg.p2pgame.platform.message.ClearAttackMatchMessage;
import it.unipr.ce.dsg.p2pgame.platform.message.DefenseMatchMessage;
import it.unipr.ce.dsg.p2pgame.platform.message.FindResourceMessage;
import it.unipr.ce.dsg.p2pgame.platform.message.GamePeerMessageListener;
import it.unipr.ce.dsg.p2pgame.platform.message.LoginPeerMessage;
import it.unipr.ce.dsg.p2pgame.platform.message.LogoutPeerMessage;
import it.unipr.ce.dsg.p2pgame.platform.message.MobileResourceMessage;
import it.unipr.ce.dsg.p2pgame.platform.message.PositionPlayerMessage;
import it.unipr.ce.dsg.p2pgame.platform.message.RegisterPeerMessage;
import it.unipr.ce.dsg.p2pgame.platform.message.StartMatchMessage;
import it.unipr.ce.dsg.p2pgame.platform.message.UserPeerMessage;
import it.unipr.ce.dsg.p2pgame.util.MultiLog;
import it.unipr.ce.dsg.p2pgame.util.SHA1;

public class GamePeer extends NetPeer {

	//identificate con l'id dell'avversario
	private HashMap<String, Clash> clashes = new HashMap<String, Clash>();
//	private int x = 0;
//	private int y = 0;
//	private int z = 0;

	private GamePlayer player = null;
	private GameWorld world = null;
	//private double playerVelocity;
	//private double visibilityScope;
	private Thread playerMovement = null;

	private ArrayList<Object> vision = null;

	//ownerId
	private ArrayList<Object> myResources = new ArrayList<Object>(); //info sulle mie risorse mobili e NON (TODO: da verificare se funziona)
	private HashMap<String, GameResourceMobileResponsible> resResources = null;//hash per posizione

	private Thread gameListener = null;

	//giocatori di cui si ï¿½ responsabili. RICERCA hashMap:posizione
	private HashMap<String, GamePlayerResponsible> resPlayers = null;

	private String myThreadId = new Long(Thread.currentThread().getId()).toString();

	private int gameInPort;
	private int gameOutPort;

	private String gameServerAddr;
	private int gameServerPort;

	private String username = null;
	private String password = null;


	private int msStabilize;
	private int msFixFinger;
	private int msCheck;
	private int msPublish;

	private Thread updateNet = null;
	private Thread gameMessageListener = null;

	//id non disponibile solo dopo la registrazione
	public GamePeer(int inPort, int outPort, int idBitLength, String id, String serverAddr, int serverPort, int gameInPort, int gameOutPort, String gameServerAddr, int gameServerPort,
			int stab, int fix, int check, int pub) {
		super(inPort, outPort, idBitLength, id, serverAddr, serverPort);

		this.gameInPort = gameInPort;
		this.gameOutPort = gameOutPort;

		this.gameServerAddr = gameServerAddr;
		this.gameServerPort = gameServerPort;


		this.msStabilize = stab;
		this.msFixFinger = fix;
		this.msCheck = check;
		this.msPublish = pub;


		this.resPlayers = new HashMap<String, GamePlayerResponsible>();
		this.resResources = new HashMap<String, GameResourceMobileResponsible>();

	}


	public void registerOnServer(String un, String pwd) {

		this.username = un;
		this.password = pwd;

		boolean resp = this.testIfPeerAlive(this.gameServerAddr, this.gameServerPort);

		if (resp){
			MultiLog.println(GamePeer.class.toString(), "Game server is online");
			//System.out.println("Game server is online");

		}

		MultiLog.println(GamePeer.class.toString(), "Sending registerMessage with:" + this.getMyPeer().getIpAddress() + ":" + this.getMyPeer().getPortNumber() + ", " + un + ", " + pwd);
		//System.out.println("Sending registerMessage with:" + this.getMyPeer().getIpAddress() + ":" + this.getMyPeer().getPortNumber() + ", " + un + ", " + pwd);
		RegisterPeerMessage registerMessage = new RegisterPeerMessage("", this.getMyPeer().getIpAddress(), this.getMyPeer().getPortNumber(), un, pwd );

		MultiLog.println(GamePeer.class.toString(), "Sending Register Peer Message");
		//System.out.println("Sending Register Peer Message");

		String responseMessage = MessageSender.sendMessage(this.gameServerAddr, this.gameServerPort, registerMessage.generateXmlMessageString());

		MultiLog.println(GamePeer.class.toString(), "Verify response...");
		//System.out.println("Verify response...");
		if (responseMessage.contains("ERROR")){

			System.err.println("Sending Register Message ERROR!");
			MultiLog.println(GamePeer.class.toString(), "Retry later...");
			//System.out.println("Retry later...");

			return;

		}
		else {
			MultiLog.println(GamePeer.class.toString(), "Reading response...");
			//System.out.println("Reading response...");
			MessageReader messageReader = new MessageReader();
			Message receivedMessage = messageReader.readMessageFromString(responseMessage.trim());

			UserPeerMessage userMessage = new UserPeerMessage(receivedMessage);
                        System.out.println("USERMSG "+userMessage.getVision() );

			MultiLog.println(GamePeer.class.toString(), "id received: " + userMessage.getSourceName());
			//System.out.println("id received: " + userMessage.getSourceName());

			if (userMessage.getSourceName().compareTo("") == 0){
				MultiLog.println(GamePeer.class.toString(), "Username already in use. Try with another");
				//System.out.println("Username already in use. Try with another");

				return;
			}
			else {
				MultiLog.println(GamePeer.class.toString(), "Registration completed succesfully");
				//System.out.println("Registration completed succesfully");

				this.setMyId(userMessage.getSourceName());

				if (this.player == null){
					this.player = new GamePlayer(this.getMyId(), this.username, userMessage.getX(), userMessage.getY(), userMessage.getZ(),
							userMessage.getVelocity(), userMessage.getVision()/*, userMessage.getGranularity()*/ );
					MultiLog.println(GamePeer.class.toString(), "Created player " + this.player.getName() + " in position ( " +  this.player.getPosX() + ", " + this.player.getPosY() + ", " + this.player.getPosZ() + " )");
					//System.out.println("Created player " + this.player.getName() + " in position ( " +  this.player.getPosX() + ", " + this.player.getPosY() + ", " + this.player.getPosZ() + " )");
                                        System.out.println( "PARAMVISION"+userMessage.getVision());
				}


				//access to Chord Network
				this.getAccessToNetwork();
				MultiLog.println(GamePeer.class.toString(), "LUNCHING UPDATE THREAD");
				//System.out.println("LUNCHING UPDATE THREAD");
				this.createUpdateNetThread();
				MultiLog.println(GamePeer.class.toString(), "LUNCHING GAME MESSAGE LISTENER THREAD");
				//System.out.println("LUNCHING GAME MESSAGE LISTENER THREAD");
				this.createGameMessageListener();
			}
		}


	}

	public void loginOnServer(String un, String pwd) {

		this.username = un;
		this.password = pwd;

		boolean resp = this.testIfPeerAlive(this.gameServerAddr, this.gameServerPort);

		if (resp){
			MultiLog.println(GamePeer.class.toString(), "Game server is online");
			//System.out.println("Game server is online");
		}
		MultiLog.println(GamePeer.class.toString(), "Sending loginMessage with:" + this.getMyPeer().getIpAddress() + ":" + this.getMyPeer().getPortNumber() + ", " + un + ", " + pwd);
		//System.out.println("Sending loginMessage with:" + this.getMyPeer().getIpAddress() + ":" + this.getMyPeer().getPortNumber() + ", " + un + ", " + pwd);

		LoginPeerMessage loginMessage = new LoginPeerMessage("", this.getMyPeer().getIpAddress(), this.getMyPeer().getPortNumber(), un, pwd );

		MultiLog.println(GamePeer.class.toString(), "Sending Login Peer Message");
		//System.out.println("Sending Login Peer Message");

		String responseMessage = MessageSender.sendMessage(this.gameServerAddr, this.gameServerPort, loginMessage.generateXmlMessageString());

		MultiLog.println(GamePeer.class.toString(), "Verify response...");
		//System.out.println("Verify response...");
		if (responseMessage.contains("ERROR")){
			MultiLog.println(GamePeer.class.toString(), "Sending Login Message ERROR!");
			//System.err.println("Sending Login Message ERROR!");
			MultiLog.println(GamePeer.class.toString(), "Retry later...");
			//System.out.println("Retry later...");

			return;

		}
		else {
			MultiLog.println(GamePeer.class.toString(), "Reading response...");
			//System.out.println("Reading response...");
			MessageReader messageReader = new MessageReader();
			Message receivedMessage = messageReader.readMessageFromString(responseMessage.trim());

			UserPeerMessage userMessage = new UserPeerMessage(receivedMessage);

			MultiLog.println(GamePeer.class.toString(), "id received: " + userMessage.getSourceName());
			//System.out.println("id received: " + userMessage.getSourceName());

			if (userMessage.getSourceName().compareTo("") == 0){
				MultiLog.println(GamePeer.class.toString(), "Username already in use. Try with another");
				//System.out.println("Username already in use. Try with another");

				return;
			}
			else {
				MultiLog.println(GamePeer.class.toString(), "Login completed succesfully");
				//System.out.println("Login completed succesfully");

				this.setMyId(userMessage.getSourceName());

				//TODO: verifica se il player ï¿½ null lo carica
				//TODO: prendere le informazioni sulla posizione date dal server alla registrazione
				if (this.player == null){
					//this.player = new GamePlayer(this.username, this.world.getRandomX(), this.world.getRandomY(), this.world.getRandomZ(), vel, vis, gran );
					this.player = new GamePlayer(this.getMyId(), this.username, userMessage.getX(), userMessage.getY(), userMessage.getZ(),
							userMessage.getVelocity(), userMessage.getVision()/*, userMessage.getGranularity()*/ );
					MultiLog.println(GamePeer.class.toString(), "Created player " + this.player.getName() + " in position ( " +  this.player.getPosX() + ", " + this.player.getPosY() + ", " + this.player.getPosZ() + " )");
					//System.out.println("Created player " + this.player.getName() + " in position ( " +  this.player.getPosX() + ", " + this.player.getPosY() + ", " + this.player.getPosZ() + " )");
				}

				//access to Chord Network
				this.getAccessToNetwork();
				this.createUpdateNetThread();
				MultiLog.println(GamePeer.class.toString(), "LUNCHING GAME MESSAGE LISTENER THREAD");
				//System.out.println("LUNCHING GAME MESSAGE LISTENER THREAD");
				this.createGameMessageListener();
			}
		}
	}


	public void logoutOnServer(String threadId){

		if (this.username == null || this.password == null){
			MultiLog.println(GamePeer.class.toString(), "User not logged. There aren't username and/or password");
			//System.out.println("User not logged. There aren't username and/or password");
			return;
		}

		boolean resp = this.testIfPeerAlive(this.gameServerAddr, this.gameServerPort);

		if (resp){

			MultiLog.println(GamePeer.class.toString(), "Game server is online");
			//System.out.println("Game server is online");

		}
		MultiLog.println(GamePeer.class.toString(), "Sending logOutMessage with:" + this.getMyPeer().getIpAddress() + ":" + this.getMyPeer().getPortNumber() + ", " + this.username + ", " + this.password);
		//System.out.println("Sending logOutMessage with:" + this.getMyPeer().getIpAddress() + ":" + this.getMyPeer().getPortNumber() + ", " + this.username + ", " + this.password);

		LogoutPeerMessage logoutMessage = new LogoutPeerMessage("", this.getMyPeer().getIpAddress(), this.getMyPeer().getPortNumber(), this.username, this.password, this.getMyId() );

		MultiLog.println(GamePeer.class.toString(), "Sending LogOut Peer Message");
		//System.out.println("Sending LogOut Peer Message");

		String responseMessage = MessageSender.sendMessage(this.gameServerAddr, this.gameServerPort, logoutMessage.generateXmlMessageString());

		MultiLog.println(GamePeer.class.toString(), "Verify response...");
		//System.out.println("Verify response...");
		if (responseMessage.contains("ERROR")){

			System.err.println("Sending LogOut Message ERROR!");
			MultiLog.println(GamePeer.class.toString(), "Retry later...");
			//System.out.println("Retry later...");

			return;

		}
		else {
			MultiLog.println(GamePeer.class.toString(), "Reading response...");
			//System.out.println("Reading response...");
			MessageReader messageReader = new MessageReader();
			Message receivedMessage = messageReader.readMessageFromString(responseMessage.trim());

			AckMessage ackMessage = new AckMessage(receivedMessage);

			//If ack message status is 0
			if(ackMessage.getAckStatus() == 0) {

				MultiLog.println(GamePeer.class.toString(), "Logout on game server completed successfully ... ");
				//System.out.println("Logout on game server completed successfully ... ");

				this.updateNet.interrupt();
				if (this.updateNet.isInterrupted()){
					MultiLog.println(GamePeer.class.toString(), "Thread UpdateNet interrupted");
					//System.out.println("Thread UpdateNet interrupted");
				}

				this.gameMessageListener.interrupt();
				if (this.gameMessageListener.isInterrupted()){
					MultiLog.println(GamePeer.class.toString(), "Thread Game message listener interrupted");
					//System.out.println("Thread Game message listener interrupted");
				}
				//this.disconnectPeer();
				this.disconnectPeer(threadId);

			}
		}

	}

	private void createUpdateNetThread() {

		if (this.updateNet != null)
			return;

		MultiLog.println(GamePeer.class.toString(), "Lunching managing update overlay network thread...");
		//System.out.println("Lunching managing update overlay network thread...");

		//this.updateNet = new Thread(new ManageNetUpdate(16000, 4000, 32000, 8000, this), "Manager update thread");
		this.updateNet = new Thread(new ManageNetUpdate(this.msStabilize, this.msFixFinger, this.msCheck, this.msPublish, this), "Manager update thread");
		this.updateNet.setPriority(Thread.MAX_PRIORITY);
		this.updateNet.start();


	}

	private void createGameMessageListener() {

		if (this.gameMessageListener != null)
			return;

		MultiLog.println(GamePeer.class.toString(), "Lunching game message listener thread...");
		//System.out.println("Lunching game message listener thread...");

		this.gameMessageListener = new Thread(new GamePeerMessageListener(this.getMyId(), this.getMyPeer().getIpAddress(), this.gameInPort, this), "Game message listener thread");
		this.gameMessageListener.setPriority(Thread.MAX_PRIORITY);
		this.gameMessageListener.start();

	}

	public void startGame(double minX, double maxX, double minY, double maxY, double minZ, double maxZ, double vel, double vis, double gran){

		if (this.world == null){
			this.world = new GameWorld(minX, maxX, minY, maxY, minZ, maxZ, gran);
			MultiLog.println(GamePeer.class.toString(), "World created.");
			//System.out.println("World created.");
		}

		//TODO: controllare che possa contenere tutti gli oggetti nella vsione
		this.vision = new ArrayList<Object>();

		//TODO:creazione delle risorse personali


		//creazione thread per la ricezione della vista dell'intorno
		if (this.playerMovement == null){

			this.playerMovement = new Thread(new PlayerPositionUpdate(this, this.player, this.world), "Thread Player position");
			this.playerMovement.setPriority(Thread.MAX_PRIORITY);
			this.playerMovement.start();
			MultiLog.println(GamePeer.class.toString(), "Thread update player position created...");
			//System.out.println("Thread update player position created...");
		}

	}

	public String getGameServerAddr() {
		return gameServerAddr;
	}


	public int getGameServerPort() {
		return gameServerPort;
	}


	public GamePlayer getPlayer(){
		return this.player;
	}


	//iterazione con l'utente
	public /*synchronized*/ boolean movePlayer( double relMovX, double relMovY, double relMovZ, String threadId ) {

		//TODO: vedere se la posizione nuova ï¿½ diversa dalla precedente allora informare il nuovo responsabile di cancellare la vecchia

		GamePlayer oldMovePlayer = this.getPlayer();
		GamePlayerResponsible oldRespMovePlayer = new GamePlayerResponsible(this.getMyId(), this.username, oldMovePlayer.getPosX(), oldMovePlayer.getPosY(), oldMovePlayer.getPosZ(),
				oldMovePlayer.getVelocity(), oldMovePlayer.getVisibility(), /*oldMovePlayer.getGranularityVision(),*/
				0, oldMovePlayer.getSpatialPosition(), oldMovePlayer.getSpatialPosition());
		MultiLog.println(GamePeer.class.toString(), "Old plyer: " + oldMovePlayer.getPosX() + ", " + oldMovePlayer.getPosY() + ", " + oldMovePlayer.getPosZ() +
				" spatial position " + oldRespMovePlayer.getPositionHash());
//		System.out.println("Old plyer: " + oldMovePlayer.getPosX() + ", " + oldMovePlayer.getPosY() + ", " + oldMovePlayer.getPosZ() +
//				" spatial position " + oldRespMovePlayer.getPositionHash());

		this.player.movePlayer(relMovX, relMovY, relMovZ);

		GamePlayerResponsible resPlayer = new GamePlayerResponsible(this.getMyId(), this.username, this.player.getPosX(), this.player.getPosY(), this.player.getPosZ(),
				this.player.getVelocity(), this.player.getVisibility(), /*this.player.getGranularityVision(),*/
				System.currentTimeMillis(), this.player.getSpatialPosition(), oldRespMovePlayer.getSpatialPosition());
		MultiLog.println(GamePeer.class.toString(), "New player: " + resPlayer.getPosX() + ", " + resPlayer.getPosY() + ", " + resPlayer.getPosZ() +
				" spatial position " + resPlayer.getPositionHash() + " old position " + resPlayer.getOldPos());
//		System.out.println("New player: " + resPlayer.getPosX() + ", " + resPlayer.getPosY() + ", " + resPlayer.getPosZ() +
//				" spatial position " + resPlayer.getPositionHash() + " old position " + resPlayer.getOldPos());

		//controllo di consistenza dell'input
		double scostX = Math.abs(oldMovePlayer.getPosX() - resPlayer.getPosX());
		double scostY = Math.abs(oldMovePlayer.getPosY() - resPlayer.getPosY());
		double scostZ = Math.abs(oldMovePlayer.getPosZ() - resPlayer.getPosZ());

		boolean checkResp = this.checkPosition(oldRespMovePlayer, scostX, scostY, scostZ, resPlayer.getVelocity());

		if (checkResp){
			MultiLog.println(GamePeer.class.toString(), "Move input acceptable");
			//System.out.println("Move input acceptable");
			MultiLog.println(GamePeer.class.toString(), "VERIFICA: MOSSA INTERNA ACCETTABILE");
			//System.out.println("VERIFICA: MOSSA INTERNA ACCETTABILE");
		}
		else {
			MultiLog.println(GamePeer.class.toString(), "Move input impossible");
			//System.out.println("Move input impossible");
			MultiLog.println(GamePeer.class.toString(), "VERIFICA: MOSSA INTERNA IMPOSSIBILE");
			//System.out.println("VERIFICA: MOSSA INTERNA IMPOSSIBILE");
			this.player.movePlayer(-relMovX, -relMovY, -relMovZ);
			return false;

		}


		//se il nuovo responsabile sono io controllo che sia verificato ciï¿½ che ho in cache.
		//if (this.findSuccessor(this.player.getSpatialPosition(), this.myThreadId).compareTo(getMyId()) == 0) {
		if (this.findSuccessor(this.player.getSpatialPosition(), threadId).compareTo(getMyId()) == 0) {
			MultiLog.println(GamePeer.class.toString(), "New Responsible is this");
			//System.out.println("New Responsible is this");
			MultiLog.println(GamePeer.class.toString(), "Verify who is oldResp for..." + resPlayer.getOldPos() + "!");
			//System.out.println("Verify who is oldResp for..." + resPlayer.getOldPos() + "!");
			if (this.resPlayers.containsKey(resPlayer.getOldPos())){
				MultiLog.println(GamePeer.class.toString(), "Have on cache also old position. Checking...");
				//System.out.println("Have on cache also old position. Checking...");
				GamePlayerResponsible oldPlayer = this.resPlayers.get(resPlayer.getOldPos());

				checkResp = this.checkPosition(oldPlayer, scostX, scostY, scostZ, resPlayer.getVelocity());
				if (checkResp){
					MultiLog.println(GamePeer.class.toString(), "NEW Position OK ");
					//System.out.println("NEW Position OK ");
					MultiLog.println(GamePeer.class.toString(), "SALVATAGGIO OK AVENDO ANCHE LA MOSSA PRECEDENTE");
					//System.out.println("SALVATAGGIO OK AVENDO ANCHE LA MOSSA PRECEDENTE");
					this.resPlayers.remove(resPlayer.getOldPos());
				}
				else{
					MultiLog.println(GamePeer.class.toString(), "ERROR Must be restored old position on player. UNDO");
					//System.out.println("ERROR Must be restored old posiion on player. UNDO");
					MultiLog.println(GamePeer.class.toString(), "SALVATAGGIO ERRORE DALLA MOSSA PRECEDENTE ERROR");
					//System.out.println("SALVATAGGIO ERRORE DALLA MOSSA PRECEDENTE ERROR");
					this.player.movePlayer(-relMovX, -relMovY, -relMovZ);
					return false;
				}
			}
			else {
				//Richiesta di consistenza all'altro
				MultiLog.println(GamePeer.class.toString(), "Request info to old responsible");
				//System.out.println("Request info to old responsible");
				//String responsible = this.findSuccessor(resPlayer.getOldPos(), this.myThreadId);
				String responsible = this.findSuccessor(resPlayer.getOldPos(), threadId);

				//String destAddr = this.getSharedInfos().getInfoFor(this.myThreadId).getIpAddress();
				String destAddr = this.getSharedInfos().getInfoFor(threadId).getIpAddress();
				//int destPort = this.getSharedInfos().getInfoFor(this.myThreadId).getPortNumber() + 2;
				int destPort = this.getSharedInfos().getInfoFor(threadId).getPortNumber() + 2;

				MultiLog.println(GamePeer.class.toString(), "Sending check to responsible " + responsible);
				//System.out.println("Sending check to responsible " + responsible);

				CheckPositionPlayerMessage checkPos = new CheckPositionPlayerMessage("", "", -1, resPlayer.getId(), resPlayer.getName(),
						resPlayer.getPosX(), resPlayer.getPosY(), resPlayer.getPosZ(), resPlayer.getVelocity(), resPlayer.getVisibility(), resPlayer.getOldPos());

				String responseMessage = MessageSender.sendMessage(destAddr, destPort, checkPos.generateXmlMessageString());

				if (responseMessage.contains("ERROR")){
					System.err.println("Sending check position ERROR!");
				}
				else {
					MessageReader messageReader = new MessageReader();
					Message received = messageReader.readMessageFromString(responseMessage.trim());

					AckMessage ackMessage = new AckMessage(received);

					if (ackMessage.getAckStatus() == 0){
						 MultiLog.println(GamePeer.class.toString(), "Position possible. Save on cache");
						//System.out.println("Position possible. Save on cache");
						 MultiLog.println(GamePeer.class.toString(), "IL PRECEDENTE HA DETTO OK");
						//System.out.println("IL PRECEDENTE HA DETTO OK");
					}
					else {
						MultiLog.println(GamePeer.class.toString(), "Position impossible. Received a NACK");
						//System.out.println("Position impossible. Received a NACK");
						MultiLog.println(GamePeer.class.toString(), "ERROR Must be restored old position on player. UNDO");
						//System.out.println("ERROR Must be restored old position on player. UNDO");
						MultiLog.println(GamePeer.class.toString(), "IL PRECEDENTRE HA DETTO NO");
						//System.out.println("IL PRECEDENTRE HA DETTO NO");
						this.player.movePlayer(-relMovX, -relMovY, -relMovZ);
						return false;
					}

				}

			}
		}

		this.addRespPlayerOnCache(resPlayer);
		return true;
	}

	public /*synchronized*/ void addRespPlayerOnCache(GamePlayerResponsible player){
		this.resPlayers.put(player.getSpatialPosition(), player);
	}

	public /*synchronized*/ boolean deleteResPlayer(String id){

		if (this.resPlayers.containsKey(id)){
			this.resPlayers.remove(id);
			return true;
		}
		else
			return false;

	}


	public void printRespPlayer() {

		MultiLog.println(GamePeer.class.toString(), "CACHE PLAYER:");
		//System.out.println("CACHE PLAYER:");
		if (this.resPlayers.size() == 0)
			return;

		Set<String> key_set = this.resPlayers.keySet();
		Iterator<String> iter = key_set.iterator();
		while(iter.hasNext()) {
			String key = iter.next();
			GamePlayerResponsible res = this.resPlayers.get(key);

			MultiLog.println(GamePeer.class.toString(), "Position of player: " + res.getName() + " is " + res.getPosX() + " " + res.getPosY() + " " + res.getPosZ()
					+ " ResourceKey " + key + " spatial position " + res.getSpatialPosition() + " position hash " + res.getPositionHash());
//			System.out.println("Position of player: " + res.getName() + " is " + res.getPosX() + " " + res.getPosY() + " " + res.getPosZ()
//					+ " ResourceKey " + key + " spatial position " + res.getSpatialPosition() + " position hash " + res.getPositionHash());
		}

	}

	public /*synchronized*/ void publishPosition(String threadId) {

		MultiLog.println(GamePeer.class.toString(), "Publish Position on cache...");
		//System.out.println("Publish Position on cache...");

		if (this.resPlayers.size() == 0)
			return;

		Set<String> key_set = this.resPlayers.keySet();
		Iterator<String> iter = key_set.iterator();

		while(iter.hasNext()){
			String key = iter.next();
			//String newResponsible = this.findSuccessor(key, this.myThreadId);
			String newResponsible = this.findSuccessor(key, threadId);
			MultiLog.println(GamePeer.class.toString(), "POST find successor. Responsible is " + newResponsible);
			//System.out.println("POST find successor. Responsible is " + newResponsible);
			if (newResponsible.compareTo(this.getMyId()) != 0){

				//String destAddr = this.getSharedInfos().getInfoFor(this.myThreadId).getIpAddress();
				String destAddr = this.getSharedInfos().getInfoFor(threadId).getIpAddress();
				//int destPort = this.getSharedInfos().getInfoFor(this.myThreadId).getPortNumber() + 2;
				int destPort = this.getSharedInfos().getInfoFor(threadId).getPortNumber() + 2;

				GamePlayerResponsible playerPos = this.resPlayers.get(key);
				MultiLog.println(GamePeer.class.toString(), "Sending info specific of position");
				//System.out.println("Sending info specific of position");

				PositionPlayerMessage positionMessage = new PositionPlayerMessage("", "", -1, playerPos.getId(),playerPos.getName(),
						playerPos.getPositionHash(), playerPos.getPosX(), playerPos.getPosY(), playerPos.getPosZ(),
						playerPos.getVelocity(), playerPos.getVisibility(), /*playerPos.getGranularityVision(),*/ playerPos.getOldPos());

				MultiLog.println(GamePeer.class.toString(), "Sending position publish to " + destAddr + ":" + destPort);
				//System.out.println("Sending position publish to " + destAddr + ":" + destPort);

				String responseMessage = MessageSender.sendMessage(destAddr, destPort, positionMessage.generateXmlMessageString());

				if (responseMessage.contains("ERROR")) {

					System.err.println("Sending Publish Resource Error");

				}
				else {

					MessageReader messageReader = new MessageReader();
					Message receivedMessage = messageReader.readMessageFromString(responseMessage.trim());

					AckMessage ackMessage = new AckMessage(receivedMessage);

					if (ackMessage.getAckStatus() == 0) {
						MultiLog.println(GamePeer.class.toString(), "Message sent...");
						//System.out.println("Message sent...");
						MultiLog.println(GamePeer.class.toString(), "RESPONSABILE HA DETTO OK");
						//System.out.println("RESPONSABILE HA DETTO OK");
					}
					else{
						MultiLog.println(GamePeer.class.toString(), "Position message error...");
						//System.out.println("Position message error...");
						MultiLog.println(GamePeer.class.toString(), "RESPONSABILE NON ACCETTA");
						//System.out.println("RESPONSABILE NON ACCETTA");
					}

					this.resPlayers.remove(key);
					if (this.resPlayers.size() == 0){
						return;
					}
					key_set = this.resPlayers.keySet();
					iter = key_set.iterator();
				}

			}
			else {
				MultiLog.println(GamePeer.class.toString(), "Responsible of player position is MYSELF");
				//System.out.println("Responsible of player position is MYSELF");

			}
		}

	}


	public boolean checkPosition(GamePlayerResponsible oldPlayer, double x, double y, double z, double velocity){

//		double scostX = Math.abs(oldPlayer.getPosX() - x);
//		double scostY = Math.abs(oldPlayer.getPosY() - y);
//		double scostZ = Math.abs(oldPlayer.getPosZ() - z);
//
		//TODO: aggiungere il caso per non far sovrapporre due giocatori

		//TODO: completare il secondo membro mettendo dei riferimenti temporali
		//if (x + y + z <= velocity){
		if(true){
			return true;

		}
		else
			return false;
	}

	//TODO: chiamarla alla disconnessione
	//TODO: aggiustarla secondo la publish normale
	//TODO: estenderla per pubblicare anche le risorse mobili o crearne un'altra
	public /*synchronized*/ void publishPositionTo(){

		///this.publishResourceTo();

		MultiLog.println(GamePeer.class.toString(), "Publish Resource To...");
		//System.out.println("Publish Resource To...");

		if (this.resPlayers.size() == 0)
			return;

		Set<String> key_set = this.resPlayers.keySet();
		Iterator<String> iter = key_set.iterator();

		while(iter.hasNext()) {
			String key = iter.next();

			String responsible = "";
			String destAddr = "";
			int destPort = -1;

			if (this.getSuccessorId() != null && this.getSuccessorId().compareTo("") != 0){

				responsible = this.getSuccessorId();
				destAddr = this.getSuccessor().getIpAddress();
				destPort = this.getSuccessor().getPortNumber();

			} else {
				boolean finded = false;

				for (int k = this.getFingerEntry().size(); k>=1 && !finded; k--){

					responsible = this.getFingerEntry().get(k);
					destAddr = this.getFingerTable().get(responsible).getIpAddress();
					destPort = this.getFingerTable().get(responsible).getPortNumber() + 2;
					finded = true;

				}

			}

			GamePlayerResponsible playerPos = this.resPlayers.get(key);

			MultiLog.println(GamePeer.class.toString(), "Sending info specific of position");
			//System.out.println("Sending info specific of position");

			PositionPlayerMessage positionMessage = new PositionPlayerMessage("", "", -1, playerPos.getId(),playerPos.getName(),
					playerPos.getPositionHash(), playerPos.getPosX(), playerPos.getPosY(), playerPos.getPosZ(),
					playerPos.getVelocity(), playerPos.getVisibility(), /*playerPos.getGranularityVision(),*/ playerPos.getOldPos());

			MultiLog.println(GamePeer.class.toString(), "Sending position publish to " + destAddr + ":" + destPort);
			//System.out.println("Sending position publish to " + destAddr + ":" + destPort);

			String responseMessage = MessageSender.sendMessage(destAddr, destPort, positionMessage.generateXmlMessageString());

			if (responseMessage.contains("ERROR")) {

				System.err.println("Sending Publish Resource Error");

			}
			else {

				MessageReader messageReader = new MessageReader();
				Message receivedMessage = messageReader.readMessageFromString(responseMessage.trim());

				AckMessage ackMessage = new AckMessage(receivedMessage);

				if (ackMessage.getAckStatus() == 0) {
					MultiLog.println(GamePeer.class.toString(), "Message sent...");
					//System.out.println("Message sent...");
					MultiLog.println(GamePeer.class.toString(), "RESPONSABILE HA DETTO OK");
					//System.out.println("RESPONSABILE HA DETTO OK");
				}
				else{
					MultiLog.println(GamePeer.class.toString(), "Position message error...");
					//System.out.println("Position message error...");
					MultiLog.println(GamePeer.class.toString(), "RESPONSABILE NON ACCETTA");
					//System.out.println("RESPONSABILE NON ACCETTA");
				}

				this.resPlayers.remove(key);
				if (this.resPlayers.size() == 0){
					return;
				}
				key_set = this.resPlayers.keySet();
				iter = key_set.iterator();
			}

		}

	}



	public HashMap<String, GamePlayerResponsible> getResPlayers() {
		return resPlayers;
	}


	public /*synchronized*/ void addToVision(Object obj, int pos) {
                System.out.println("ADDVISION "+ pos);
		if (this.vision.size() > pos)
			this.vision.set(pos, obj);
		else
			this.vision.add(pos, obj);

	}

	public void printVision(){
                System.out.println("PRINTVISION");
		for (int i=0; i< this.vision.size(); i++){
			if (this.vision.get(i) instanceof GamePlayerResponsible){
				GamePlayerResponsible player = (GamePlayerResponsible) this.vision.get(i);
				MultiLog.println(GamePeer.class.toString(), "Vis " + i + " (GIOCATORE): " + player.getName() + ":)" + player.getPosX() + "," + player.getPosY() + "," + player.getPosZ() + " posHash " + player.getPositionHash());
				System.out.println("Vis " + i + " (GIOCATORE): " + player.getName() + ":)" + player.getPosX() + "," + player.getPosY() + "," + player.getPosZ() + " posHash " + player.getPositionHash());
			}
			else if (this.vision.get(i) instanceof GameResourceMobileResponsible){
				GameResourceMobileResponsible resource = (GameResourceMobileResponsible) this.vision.get(i);
				MultiLog.println(GamePeer.class.toString(), "Vis "+ i + " (RISORSA): " + resource.getDescription() + ":)" + resource.getX() + "," + resource.getY() + "," + resource.getZ());
				System.out.println("Vis "+ i + " (RISORSA): " + resource.getDescription() + ":)" + resource.getX() + "," + resource.getY() + "," + resource.getZ());
			}
			else{
			MultiLog.println(GamePeer.class.toString(), "Vis " + i + ":" + this.vision.get(i) + "!");
				System.out.println("Vis " + i + ":" + this.vision.get(i) + "!");

			}
		}
	}



	public ArrayList<Object> getVision() {
		return vision;
	}


	//TODO: non void. Richiesta della risorsa
	public Object requestResource(String id, double x, double y, double z, String threadReq){

		//non si puï¿½ usare la ricerca di Chord non usando la sua cache
		//NetResourceInfo resInfo = this.searchResource(id);
		//Trova chi dovrebbe essere il responsabile della risorsa
		String peerResp = this.findSuccessor(id, threadReq);
		if (peerResp != null){
			//chiedi al responsabile della risorsa id di ottenerla
			//String addr = resInfo.getOwner().getIpAddress();

			//TODO: aggiunto il 091015 per evitare un null pointer senza origine da singolo thread
			// dovrebbe essere dovuto ad un problema di concorrenza sulla scirttura alla risorsa condivisa
			// Si potrebbe risolvere con una synchronize sulla scrittura su sharedInfos (saveOnCache)
			if (this.getSharedInfos() == null || this.getSharedInfos().getIdFor(threadReq) == null){

				System.err.println("Impossible to Find my infos");
				MultiLog.println(GamePeer.class.toString(), "My infos on shared resource is null");
				//System.out.println("My infos on shared resource is null");
				MultiLog.println(GamePeer.class.toString(), "shared Infos " + this.getSharedInfos());
				MultiLog.println(GamePeer.class.toString(), "my infos on shared resource " + this.getSharedInfos().getIdFor(threadReq));
				System.exit(1);
				return null;
			}

			String addr = this.getSharedInfos().getInfoFor(threadReq).getIpAddress();
			int port = this.getSharedInfos().getInfoFor(threadReq).getPortNumber() + 2;

			FindResourceMessage findResourceMessage = new FindResourceMessage(this.getMyId(),this.getMyPeer().getIpAddress(),this.gameOutPort, this.username, id,x,y,z, this.player.getSpatialPosition());

			String responseMessage = MessageSender.sendMessage(addr, port, findResourceMessage.generateXmlMessageString());

			if (responseMessage.contains("ERROR")) {
				System.err.println("Sending Find Resource request ERROR!");
				MultiLog.println(GamePeer.class.toString(), "Not enough privileges for obtain the resource");
				//System.out.println("Not enough privileges for obtain the resource");
				return null;
			}
			else {
				MessageReader messageReader = new MessageReader();
				Message receivedMessage = messageReader.readMessageFromString(responseMessage.trim());

				//TODO:vedere il tipo di messaggio da ricevere 3 tipi: Permesso non dispo, player, risorsa
				if (receivedMessage.getMessageType().equals("ACK")){
					MultiLog.println(GamePeer.class.toString(), "Impossible to obtain resource");
					//System.out.println("Impossible to obtain resource");
					MultiLog.println(GamePeer.class.toString(), "Risorsa INESISTENTE o non autorizzato");
					//System.out.println("Risorsa INESISTENTE o non autorizzato");
					return null;
				}
				else if (receivedMessage.getMessageType().equals("POSITION")){
					MultiLog.println(GamePeer.class.toString(), "Esiste un giocatore... ");
					//System.out.println("Esiste un giocatore... ");

					PositionPlayerMessage posMessage = new PositionPlayerMessage(receivedMessage);

					GamePlayerResponsible resp = new GamePlayerResponsible(posMessage.getId(),posMessage.getUserName(), posMessage.getPosX(), posMessage.getPosY(), posMessage.getPosZ(),
							posMessage.getVel(), posMessage.getVis(), System.currentTimeMillis(), posMessage.getPositionHash(), posMessage.getOldPos());
					MultiLog.println(GamePeer.class.toString(), "RICEVUTO GIOCATORE " + resp.getName() + " pos " + resp.getPosX() + ", " + resp.getPosY() + ", " + resp.getPosZ());
					//System.out.println("RICEVUTO GIOCATORE " + resp.getName() + " pos " + resp.getPosX() + ", " + resp.getPosY() + ", " + resp.getPosZ());
					return resp;
				}
				//TODO: aggiungere il caso delle risorse mobili
				else if(receivedMessage.getMessageType().equals("MOBILERESOURCE")){

					MultiLog.println(GamePeer.class.toString(), "Esiste una risorsa...");
					//System.out.println("Esiste una risorsa...");

					MobileResourceMessage resMessage = new MobileResourceMessage(receivedMessage);
//					String id, String description,
//					String owner, String ownerId, double quantity, double x, double y,
//					double z, double vel, double vis,
//					long time, String pos, String oldPos
					GameResourceMobileResponsible resp = new GameResourceMobileResponsible(resMessage.getId(),resMessage.getUserName(), resMessage.getOwner(),
							resMessage.getOwnerId(), resMessage.getQuantity(),resMessage.getPosX(),resMessage.getPosY(), resMessage.getPosZ(),
							resMessage.getVel(),resMessage.getVis(), System.currentTimeMillis(), resMessage.getPositionHash(), resMessage.getOldPos());
					return resp;
				}
			}

		}


		return null;

	}



	//TODO: in fase di costruzione
	public void createMobileResource(String type, double qt){
		String id = "";
		try {
			id = SHA1.convertToHex(SHA1.calculateSHA1(type));
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//TODO: aggiustare alcuni parametri fissati come prova
		//String id, String description, String owner, String ownerId, double quantity, double x, double y, double z, double vel, double vis
		GameResourceMobile resource = new GameResourceMobile(id, type, this.username, this.player.getId(), qt,
				this.player.getPosX(), this.player.getPosY(), this.player.getPosZ(), this.player.getVelocity()+2, this.player.getVisibility());


		//TODO:C'ï¿½ il parametro del periodo come costante. Per le prove occorre aver creato prima il mondo
		resource.initializeSearch(5000, this.world, this);

		this.myResources.add(resource);

		MultiLog.println(GamePeer.class.toString(), "Non ancora pubblicata");
		//System.out.println("Non ancora pubblicata");
	}

	public void printMyResource(){
		MultiLog.println(GamePeer.class.toString(), "My resource...");
		//System.out.println("My resource...");
		for (int i=0; i < this.myResources.size(); i++) {
			if (this.myResources.get(i) instanceof GameResourceMobile){
				GameResourceMobile res = (GameResourceMobile) this.myResources.get(i);
				MultiLog.println(GamePeer.class.toString(), "MOBILE " + res.getDescription() + " id " + res.getId() + " at : " + res.getX() + ", " + res.getY() + ", " + res.getZ()
						+ " current position " + res.getSpatialPosition());
//				System.out.println("MOBILE " + res.getDescription() + " id " + res.getId() + " at : " + res.getX() + ", " + res.getY() + ", " + res.getZ()
//						+ " current position " + res.getSpatialPosition());
			}
			else if (this.myResources.get(i) instanceof GameResourceEvolve){
				GameResourceEvolve resEvo = (GameResourceEvolve) this.myResources.get(i);
				MultiLog.println(GamePeer.class.toString(), "EVOLVE " + resEvo.getDescription() + " id " + resEvo.getId() + " with : " + resEvo.getOffset() + ", " + resEvo.getPeriod() + ",Quant " + resEvo.getQuantity());
				//System.out.println("EVOLVE " + resEvo.getDescription() + " id " + resEvo.getId() + " with : " + resEvo.getOffset() + ", " + resEvo.getPeriod() + ",Quant " + resEvo.getQuantity());

			}
			else if (this.myResources.get(i) instanceof GameResource){
				GameResource res = (GameResource) this.myResources.get(i);
				MultiLog.println(GamePeer.class.toString(), "RESOURCE " + res.getDescription() + " id " + res.getId() + " quant " + res.getQuantity());
				//System.out.println("RESOURCE " + res.getDescription() + " id " + res.getId() + " quant " + res.getQuantity());
			}
		}
	}

	public void printRespResources(){
		MultiLog.println(GamePeer.class.toString(), "CACHE RESOURCE:");
		//System.out.println("CACHE RESOURCE:");
		if (this.resResources.size() == 0)
			return;
		Set<String> key_set = this.resResources.keySet();
		Iterator<String> iter =key_set.iterator();
		while(iter.hasNext()) {
			String key = iter.next();
			GameResourceMobileResponsible res = this.resResources.get(key);

			MultiLog.println(GamePeer.class.toString(), "Resource: " + res.getDescription() + " id " + res.getId() + " owned by " +
					res.getOwner() + " with id " + res.getOwnerId() + " at position " + res.getX() + ", " + res.getY() + ", " + res.getZ());
//			System.out.println("Resource: " + res.getDescription() + " id " + res.getId() + " owned by " +
//					res.getOwner() + " with id " + res.getOwnerId() + " at position " + res.getX() + ", " + res.getY() + ", " + res.getZ());

		}

	}

	//TODO: da testare restituisce solo la prima ma l'id dovrebbe essere univoco
	public GameResourceMobile getMyMobileResourceFromId(String id) {
		//GameResourceMobile resource = null;
		for (int i=0; i < this.myResources.size(); i++){
			if (this.myResources.get(i) instanceof GameResourceMobile){
				GameResourceMobile resource = (GameResourceMobile) this.myResources.get(i);
				if (resource.getId().compareTo(id) == 0)
				//break;
					return resource;
			}
		}

		//return resource;
		return null;
	}

	//TODO: provare, scritto 091001
	public GameResource getMyResourceFromId(String id){
		//System.out.println("GET MY RESOURCE");
		for (int i=0; i < this.myResources.size(); i++){

			if (((GameResource) this.myResources.get(i)).getId().compareTo(id) == 0){
				//System.out.println("ESISTE RISORSA CON l'ID cercato");
				//Trovata una risorsa personale con l'id specificato
				if ( this.myResources.get(i) instanceof GameResourceMobile){
					//System.out.println("c'e' una risorsa Mobile");
					return (GameResourceMobile) this.myResources.get(i);

				}
				else if (this.myResources.get(i) instanceof GameResourceEvolve){
					//System.out.println("c'e' una risorsa Evolve");
					return (GameResourceEvolve) this.myResources.get(i);
				}
				else{
					//System.out.println("c'e' una risorsa Normale");
					return (GameResource) this.myResources.get(i);
				}
			}

		}
		MultiLog.println(GamePeer.class.toString(), "Dimensioni delle risorsa personali " + this.myResources.size());
		//System.out.println("Dimensioni delle risorsa personali " + this.myResources.size());
		return null;
	}


	public ArrayList<Object> getMyResources() {
		return myResources;
	}

//TODO: da provare - mod 091002
	public /*synchronized*/ void addToMyResource(GameResource res){
		//System.out.println("addToMyResource CALLED###");
		//if (this.getMyMobileResourceFromId(res.getId()) == null){
		System.out.println();
		MultiLog.println(GamePeer.class.toString(), "RISORSA AGGIUNTA "+ res.getId() );
		if (this.getMyResourceFromId(res.getId()) == null){

			this.myResources.add(res);

		}
		else {
			MultiLog.println(GamePeer.class.toString(), "devono essere eliminate le vecchie informazioni sulla risorsa");
			//System.out.println("devono essere eliminate le vecchie informazioni sulla risorsa");
			//GameResource  oldRes = this.getMyMobileResourceFromId(res.getId());
			GameResource  oldRes = this.getMyResourceFromId(res.getId());
			this.myResources.remove(oldRes);
			this.myResources.add(res);

		}
		

	}
        
        //added 16/07/2010
        
        public void removeToMyResources(GameResource res)
        {
            if (this.getMyResourceFromId(res.getId()) != null){

			this.myResources.remove(res);

		}
        }



	public /*synchronized*/ boolean deleteResResource(String id){

		if (this.resResources.containsKey(id)){
			this.resResources.remove(id);
			return true;
		}
		else
			return false;

	}

	public HashMap<String, GameResourceMobileResponsible> getResResources() {
		return resResources;
	}


	public boolean checkResourceMobile(GameResourceMobileResponsible oldResource, double x, double y, double z, double velocity){

		//TODO: fare un test significativo
		if(true){
			return true;
		}
		else
			return false;

	}

	public /*synchronized*/ void addRespResourceOnCache(GameResourceMobileResponsible resource){
		this.resResources.put(resource.getSpatialPosition(), resource);
	}

	//identificativo univoco della risorsa mobile
	public /*synchronized*/ boolean moveResourceMobile(String id, double relMovX, double relMovY, double relMovZ, String threadId ){


		GameResourceMobile resource = this.getMyMobileResourceFromId(id);
		MultiLog.println(GamePeer.class.toString(), "oldResource position " + resource.getSpatialPosition());
		//System.out.println("oldResource position " + resource.getSpatialPosition());
		//GameResourceMobile newResource = oldResource;
		//GameResourceMobile newResource = (GameResourceMobile) oldResource.clone();


		//String id, String description,String owner, String ownerId, double quantity, double x, double y,
		//double z, double vel, double vis, long time, String pos, String oldPos
		GameResourceMobileResponsible oldRespMoveResource = new GameResourceMobileResponsible(resource.getId(), resource.getDescription(),
				resource.getOwner(), resource.getOwnerId(), resource.getQuantity(),
				resource.getX(), resource.getY(), resource.getZ(), resource.getVelocity(), resource.getVision(),
				0, resource.getSpatialPosition(), resource.getSpatialPosition());

		//newResource.moveResource(relMovX, relMovY, relMovZ);
		resource.moveResource(relMovX, relMovY, relMovZ);
		//this.addToMyMobileResource(newResource); //salva
		//TODO: solo una prova per vedere se cambia old resource nella cache senza salvare
		//this.addToMyMobileResource(oldResource);
		MultiLog.println(GamePeer.class.toString(), "oldResource position DOPO  " + resource.getSpatialPosition());
		//System.out.println("oldResource position DOPO  " + resource.getSpatialPosition());



		GameResourceMobileResponsible resResource = new GameResourceMobileResponsible(resource.getId(), resource.getDescription(),
				resource.getOwner(), resource.getOwnerId(), resource.getQuantity(),
				resource.getX(), resource.getY(), resource.getZ(), resource.getVelocity(), resource.getVision(),
				System.currentTimeMillis(), resource.getSpatialPosition(), oldRespMoveResource.getSpatialPosition());

		MultiLog.println(GamePeer.class.toString(), "RESOURCE new position " + resResource.getSpatialPosition() + " old " + resResource.getOldPos());
		//System.out.println("RESOURCE new position " + resResource.getSpatialPosition() + " old " + resResource.getOldPos());
		//System.out.println("sull'resResource: " + resResource.getSpatialPosition() + ", old " + resResource.getOldPos());
		double scostX = Math.abs(oldRespMoveResource.getX() - resResource.getX());
		double scostY = Math.abs(oldRespMoveResource.getY() - resResource.getY());
		double scostZ = Math.abs(oldRespMoveResource.getZ() - resResource.getZ());

		boolean checkResp = this.checkResourceMobile(oldRespMoveResource, scostX, scostY, scostZ, resResource.getVelocity());

		if (checkResp){
			MultiLog.println(GamePeer.class.toString(), "Mossa accettabile VERIFICA INTERNA");
			//System.out.println("Mossa accettabile VERIFICA INTERNA");
		}
		else {
			MultiLog.println(GamePeer.class.toString(), "Mossa impossibile VERIFICA INTERNA IMPOSSIBILE");
			//System.out.println("Mossa impossibile VERIFICA INTERNA IMPOSSIBILE");
			resource.moveResource(-relMovZ, -relMovY, -relMovZ);
			//this.addToMyMobileResource(newResource); //salva


			return false;
		}


		//if (this.findSuccessor(resource.getSpatialPosition(), this.myThreadId).compareTo(getMyId()) == 0){
		if (this.findSuccessor(resource.getSpatialPosition(), threadId).compareTo(getMyId()) == 0){

			MultiLog.println(GamePeer.class.toString(), "New responsible is this");
			//System.out.println("New responsible is this");
			MultiLog.println(GamePeer.class.toString(), "Verifica di chi e' il vecchio respons per ... " + resResource.getOldPos());
			//System.out.println("Verifica di chi e' il vecchio respons per ... " + resResource.getOldPos());

			if (this.resResources.containsKey(resResource.getOldPos())){
				MultiLog.println(GamePeer.class.toString(), "ho in cache anche la vecchia posizione della risorsa");
				//System.out.println("ho in cache anche la vecchia posizione della risorsa");

				GameResourceMobileResponsible oldResourceOnCache = this.resResources.get(resResource.getOldPos());

				checkResp = this.checkResourceMobile(oldResourceOnCache, scostX, scostY, scostZ, resResource.getVelocity());

				if (checkResp){
					MultiLog.println(GamePeer.class.toString(), "NEW Position for Resource OK");
					//System.out.println("NEW Position for Resource OK");

					MultiLog.println(GamePeer.class.toString(), "Salvataggio OK AVENDO ANCHE LA POSIZIONE PRECEDENTE DELLA RISORSA");
					//System.out.println("Salvataggio OK AVENDO ANCHE LA POSIZIONE PRECEDENTE DELLA RISORSA");

					this.resResources.remove(resResource.getOldPos());

				}
				else {
					MultiLog.println(GamePeer.class.toString(), "ERROR ripristino poszione");
					//System.out.println("ERROR ripristino poszione");
					resource.moveResource(-relMovZ, -relMovY, -relMovZ);
					//this.addToMyMobileResource(newResource); //salva



					return false;
				}
			}
			else if (this.resPlayers.containsKey(resResource.getOldPos()) &&
					resResource.getOwnerId().compareTo(this.resPlayers.get(resResource.getOldPos()).getId()) == 0) { //se ï¿½ la prima posizione mossa dalla risorsa

				MultiLog.println(GamePeer.class.toString(), "PRMIA MOSSA DELLA RISORSA");
				//System.out.println("PRMIA MOSSA DELLA RISORSA");
				GamePlayerResponsible player = this.resPlayers.get(resResource.getOldPos());
				checkResp = this.checkPosition(player, scostX, scostY, scostZ, resResource.getVelocity());

				if (checkResp){
					MultiLog.println(GamePeer.class.toString(), "NEW Position for Resource OK AFTER PLAYER");
					//System.out.println("NEW Position for Resource OK AFTER PLAYER");

					MultiLog.println(GamePeer.class.toString(), "Salvataggio OK AVENDO IL PLAYER DELLA RISORSA");
					//System.out.println("Salvataggio OK AVENDO IL PLAYER DELLA RISORSA");

					//non tolta non avendo la vecchia posizione
					//this.resResources.remove(resResource.getOldPos());

				}
				else {
					MultiLog.println(GamePeer.class.toString(), "ERROR ripristino poszione RISORSA");
					//System.out.println("ERROR ripristino poszione RISORSA");
					resource.moveResource(-relMovZ, -relMovY, -relMovZ);
					//this.addToMyMobileResource(newResource); //salva


					return false;
				}

			}
			else {
				MultiLog.println(GamePeer.class.toString(), "Request info to old responsible for RESOURCE");
				//System.out.println("Request info to old responsible for RESOURCE");

				//String responsible = this.findSuccessor(resResource.getOldPos(), this.myThreadId);
				String responsible = this.findSuccessor(resResource.getOldPos(), threadId);
				//String destAddr = this.getSharedInfos().getInfoFor(this.myThreadId).getIpAddress();
				String destAddr = this.getSharedInfos().getInfoFor(threadId).getIpAddress();
				//int destPort = this.getSharedInfos().getInfoFor(this.myThreadId).getPortNumber() +2;
				int destPort = this.getSharedInfos().getInfoFor(threadId).getPortNumber() +2;

				MultiLog.println(GamePeer.class.toString(), "Ask check to " + responsible + " the old RESP RESOURCE");
				//System.out.println("Ask check to " + responsible + " the old RESP RESOURCE");

				//String sourceName, String sourceSocketAddr, int sourcePort, String id, String username,
				// double x, double y, double z, double vel, double vis, String oldPos, String owner, String ownerId, double quantity
				CheckMobileResourceMessage checkResPos = new CheckMobileResourceMessage("","",-1, resResource.getId(),resResource.getDescription(),
						resResource.getX(),resResource.getY(),resResource.getZ(), resResource.getVelocity(), resResource.getVision(),
						resResource.getOldPos(), resResource.getOwner(), resResource.getOwnerId(), resResource.getQuantity());

				String responseMessage = MessageSender.sendMessage(destAddr, destPort, checkResPos.generateXmlMessageString());

				if (responseMessage.contains("ERROR")){
					System.err.println("Sending check RESOURCE POSITION ERROR!");

				}
				else {
					MessageReader messageReader = new MessageReader();
					Message received = messageReader.readMessageFromString(responseMessage.trim());
					AckMessage ackMessage = new AckMessage(received);
					if (ackMessage.getAckStatus() == 0){
						MultiLog.println(GamePeer.class.toString(), "IL PRECEDENTE DELLA RISORSA HA DETTO OK");
						//System.out.println("IL PRECEDENTE DELLA RISORSA HA DETTO OK");

					}
					else {
						MultiLog.println(GamePeer.class.toString(), "POSIZIONE RISORSA ERRATA");
						//System.out.println("POSIZIONE RISORSA ERRATA");
						resource.moveResource(-relMovZ, -relMovY, -relMovZ);
						//this.addToMyMobileResource(newResource); //salva



						return false;
					}
				}
			}

		}

		this.addRespResourceOnCache(resResource);




		return true;
	}

	public /*synchronized*/ void publishResourceMobile(String threadId){
		MultiLog.println(GamePeer.class.toString(), "Publish RESOURCE MOBILE on cache...");
		//System.out.println("Publish RESOURCE MOBILE on cache...");

		if (this.resResources.size() == 0)
			return;

		Set<String> key_set = this.resResources.keySet();
		Iterator<String> iter = key_set.iterator();

		while(iter.hasNext()){
			String key = iter.next();
			//String newResponsible = this.findSuccessor(key, this.myThreadId);
			String newResponsible = this.findSuccessor(key, threadId);

			if (newResponsible.compareTo(this.getMyId()) != 0){

				//String destAddr = this.getSharedInfos().getInfoFor(this.myThreadId).getIpAddress();
				String destAddr = this.getSharedInfos().getInfoFor(threadId).getIpAddress();
				//int destPort = this.getSharedInfos().getInfoFor(this.myThreadId).getPortNumber()+2;
				int destPort = this.getSharedInfos().getInfoFor(threadId).getPortNumber()+2;

				GameResourceMobileResponsible resourcePos = this.resResources.get(key);
				MultiLog.println(GamePeer.class.toString(), "Sending info for mobile position");
				//System.out.println("Sending info for mobile position");

				//String sourceName, String sourceSocketAddr, int sourcePort, String id, String username,
				//String position, double x, double y, double z, double vel, double vis, String oldPos, String owner, String ownerId, double quantity
				MobileResourceMessage mobileResourceMessage = new MobileResourceMessage("","",-1, resourcePos.getId(), resourcePos.getDescription(), resourcePos.getSpatialPosition(),
						resourcePos.getX(), resourcePos.getY(), resourcePos.getZ(), resourcePos.getVelocity(), resourcePos.getVision(),
						resourcePos.getOldPos(), resourcePos.getOwner(), resourcePos.getOwnerId(), resourcePos.getQuantity());

				MultiLog.println(GamePeer.class.toString(), "sending RESOURCE MOBILE pub to " + destAddr + " : " + destPort);
				//System.out.println("sending RESOURCE MOBILE pub to " + destAddr + " : " + destPort);

				String responseMessage = MessageSender.sendMessage(destAddr, destPort, mobileResourceMessage.generateXmlMessageString());

				if (responseMessage.contains("ERROR")){
					System.err.println("Sending Publish MOBILE RESOURCE Error");
				}
				else {
					MessageReader messageReader = new MessageReader();
					Message receivedMessage = messageReader.readMessageFromString(responseMessage.trim());

					AckMessage ackMessage = new AckMessage(receivedMessage);

					if (ackMessage.getAckStatus() == 0){
						MultiLog.println(GamePeer.class.toString(), "RESPONSIBILE RISORSA DETTO OK");
						//System.out.println("RESPONSIBILE RISORSA DETTO OK");
					}
					else {
						MultiLog.println(GamePeer.class.toString(), "RESPONSABILE NON ACCETTA LA RISORSA");
						//System.out.println("RESPONSABILE NON ACCETTA LA RISORSA");
					}
					this.resResources.remove(key);
					if (this.resResources.size() == 0){
						return;
					}
					key_set = this.resResources.keySet();
					iter = key_set.iterator();


				}
			}
			else{
				MultiLog.println(GamePeer.class.toString(), "Responsible of MOBILE RESOURCE is MYSELF");
				//System.out.println("Responsible of MOBILE RESOURCE is MYSELF");
			}
		}

	}


	public /*synchronized*/ void publishResourceMobileTo(){
		MultiLog.println(GamePeer.class.toString(), "Publish RESOURCE MOBILE To...");
		//System.out.println("Publish RESOURCE MOBILE To...");

		if (this.resResources.size() == 0)
			return;

		Set<String> key_set = this.resResources.keySet();
		Iterator<String> iter = key_set.iterator();

		while(iter.hasNext()){
			String key = iter.next();

			String responsible = "";
			String destAddr = "";
			int destPort = -1;

			if (this.getSuccessorId() != null && this.getSuccessorId().compareTo("") != 0){

				responsible = this.getSuccessorId();
				destAddr = this.getSuccessor().getIpAddress();
				destPort = this.getSuccessor().getPortNumber();

			} else {
				boolean finded = false;

				for (int k = this.getFingerEntry().size(); k>=1 && !finded; k--){

					responsible = this.getFingerEntry().get(k);
					destAddr = this.getFingerTable().get(responsible).getIpAddress();
					destPort = this.getFingerTable().get(responsible).getPortNumber() + 2;
					finded = true;

				}

			}

				GameResourceMobileResponsible resourcePos = this.resResources.get(key);
				MultiLog.println(GamePeer.class.toString(), "Sending info for mobile position");
				//System.out.println("Sending info for mobile position");

				//String sourceName, String sourceSocketAddr, int sourcePort, String id, String username,
				//String position, double x, double y, double z, double vel, double vis, String oldPos, String owner, String ownerId, double quantity
				MobileResourceMessage mobileResourceMessage = new MobileResourceMessage("","",-1, resourcePos.getId(), resourcePos.getDescription(), resourcePos.getSpatialPosition(),
						resourcePos.getX(), resourcePos.getY(), resourcePos.getZ(), resourcePos.getVelocity(), resourcePos.getVision(),
						resourcePos.getOldPos(), resourcePos.getOwner(), resourcePos.getOwnerId(), resourcePos.getQuantity());

				MultiLog.println(GamePeer.class.toString(), "sending RESOURCE MOBILE pub to " + destAddr + " : " + destPort);
				//System.out.println("sending RESOURCE MOBILE pub to " + destAddr + " : " + destPort);

				String responseMessage = MessageSender.sendMessage(destAddr, destPort, mobileResourceMessage.generateXmlMessageString());

				if (responseMessage.contains("ERROR")){
					System.err.println("Sending Publish MOBILE RESOURCE Error");
				}
				else {
					MessageReader messageReader = new MessageReader();
					Message receivedMessage = messageReader.readMessageFromString(responseMessage.trim());

					AckMessage ackMessage = new AckMessage(receivedMessage);

					if (ackMessage.getAckStatus() == 0){
						MultiLog.println(GamePeer.class.toString(), "RESPONSIBILE RISORSA DETTO OK");
						//System.out.println("RESPONSIBILE RISORSA DETTO OK");
					}
					else {
						MultiLog.println(GamePeer.class.toString(), "RESPONSABILE NON ACCETTA LA RISORSA");
						//System.out.println("RESPONSABILE NON ACCETTA LA RISORSA");
					}
					this.resResources.remove(key);
					if (this.resResources.size() == 0){
						return;
					}
					key_set = this.resResources.keySet();
					iter = key_set.iterator();


				}
//			}
//			else{
//				System.out.println("Responsible of MOBILE RESOURCE is MYSELF");
//			}
		}

	}

	public String getUsername() {
		return username;
	}


	public String getPassword() {
		return password;
	}

//########################
	//Manage clashes

	public void printLastClashes(){
		MultiLog.println(GamePeer.class.toString(), "Print all last Clashes...");
		//System.out.println("Print all last Clashes...");

		if (this.clashes.size() == 0)
			return;

		Set<String> key_set = this.clashes.keySet();
		Iterator<String> iter = key_set.iterator();

		while(iter.hasNext()) {
			String key = iter.next();
			Clash clash = this.clashes.get(key);
			MultiLog.println(GamePeer.class.toString(), "Attack " + clash.getOtherPlayer() + " status " + clash.getStatusLast());
			//System.out.println("Attack " + clash.getOtherPlayer() + " status " + clash.getStatusLast());
		}

	}

	public /*synchronized*/ boolean newAttach(String oppositeId, String opposite, Attack myMove){

		//se si ha giï¿½ avuto contatto con l'altro giocatore ed il precedente scontro non ï¿½ finito . ERRORE non si puï¿½ iniziare nuovamente
		if ( this.clashes.containsKey(oppositeId) && this.clashes.get(oppositeId).getStatusLast() != Clash.Phase.END){
			return false;
		}
		else{

			if (this.clashes.containsKey(oppositeId)){

				this.clashes.get(oppositeId).addMyMove(myMove);
				this.clashes.get(oppositeId).addHash(myMove.getHash());
				return true;
			}
			else { //occorre creare un nuovo campo

				//this.clashes.put(oppositeId, value);
				Clash newClash = new Clash(opposite, oppositeId);
				newClash.setStatusLast(Clash.Phase.HASH);
				newClash.addMyMove(myMove);
				newClash.addHash(myMove.getHash());
				this.clashes.put(oppositeId, newClash);

				return true;

			}
		}

	}


	public /*synchronized*/ boolean addAttackReceived(String oppositeId, String opposite, String hash){

		//se si ha giï¿½ avuto contatto con l'altro giocatore ed il precedente scontro non ï¿½ finito . ERRORE non si puï¿½ iniziare nuovamente
		if ( this.clashes.containsKey(oppositeId) && this.clashes.get(oppositeId).getStatusLast() != Clash.Phase.END){
			return false;
		}

		else{

			if (this.clashes.containsKey(oppositeId)){

				//this.clashes.get(oppositeId).addMyMove(myMove);
				this.clashes.get(oppositeId).addHash(hash);
				return true;
			}
			else { //occorre creare un nuovo campo

				//this.clashes.put(oppositeId, value);
				Clash newClash = new Clash(opposite, oppositeId);
				newClash.setStatusLast(Clash.Phase.HASH);
				//newClash.addMyMove(myMove);
				newClash.addHash(hash);
				this.clashes.put(oppositeId, newClash);

				return true;


			}
		}

	}

	public /*synchronized*/ boolean addClearAttackReceived(String oppositeId, String opposite, Attack otherAttack){
		if (this.clashes.containsKey(oppositeId) && this.clashes.get(oppositeId).getStatusLast() != Clash.Phase.DEFENSE){

			return false;
		}

		if (this.clashes.containsKey(oppositeId)){
			this.clashes.get(oppositeId).addOtherPlayerMove(otherAttack);
			this.clashes.get(oppositeId).setStatusLast(Clash.Phase.END);
			return true;
		}
		else
			return false;
	}

//attacca //TODO: utile o da eliminare
	public void startMatch(GamePlayerResponsible player, String resource, double quantity, String threadId){
		MultiLog.println(GamePeer.class.toString(), "Attacco " + player.getName());
		//System.out.println("Attacco " + player.getName());
		//String oppositeId = this.findSuccessor(player.getId(), this.myThreadId);
		String oppositeId = this.findSuccessor(player.getId(), threadId);
		//NetPeerInfo oppositePeer = this.getSharedInfos().getInfoFor(this.myThreadId);
		NetPeerInfo oppositePeer = this.getSharedInfos().getInfoFor(threadId);

		Attack attack = new Attack(quantity, resource);

		if (this.newAttach(player.getId(), player.getName(), attack)) {


			StartMatchMessage startMatch = new StartMatchMessage(this.getMyId(),this.getMyPeer().getIpAddress(), this.getMyPeer().getPortNumber()+2,
					this.player.getId(), this.player.getName(), this.player.getSpatialPosition(), this.player.getPosX(), this.player.getPosY(), this.player.getPosZ(), attack.getHash());

			String responseStartMessage = MessageSender.sendMessage(oppositePeer.getIpAddress(), oppositePeer.getPortNumber()+2, startMatch.generateXmlMessageString());

			if (responseStartMessage.contains("ERROR")){
				MultiLog.println(GamePeer.class.toString(), "Sending START MATCH ERROR !");
				//System.out.println("Sending START MATCH ERROR !");
			}
			else {
				MessageReader responseStartMessageReader = new MessageReader();
				Message receivedStartMessageReader = responseStartMessageReader.readMessageFromString(responseStartMessage.trim());

				AckMessage ackMessage = new AckMessage(receivedStartMessageReader);
				if (ackMessage.getAckStatus() == 0){
					MultiLog.println(GamePeer.class.toString(), "Now Match is started");
					//System.out.println("Now Match is started");
				}

			}
		}
		else{
			MultiLog.println(GamePeer.class.toString(), "Attacco gia' in corso");
			//System.out.println("Attacco giï¿½ in corso");
		}
	}



	public /*synchronized*/ boolean newDefense(String oppositeId, String opposite, Defense myMove) {

		//se si ha giï¿½ avuto contatto con l'altro giocatore ed il precedente scontro non ï¿½ finito . ERRORE non si puï¿½ iniziare nuovamente
		if ( this.clashes.containsKey(oppositeId) && this.clashes.get(oppositeId).getStatusLast() != Clash.Phase.HASH ){
			return false;
		}


			if (this.clashes.containsKey(oppositeId)){

				this.clashes.get(oppositeId).addMyMove(myMove);
				this.clashes.get(oppositeId).setStatusLast(Clash.Phase.DEFENSE);
				//this.clashes.get(oppositeId).addHash(myMove.getHash());
				return true;
			}
			else
				return false;
//			else { //occorre creare un nuovo campo
//
//				//this.clashes.put(oppositeId, value);
//				Clash newClash = new Clash(opposite, oppositeId);
//				newClash.setStatusLast(Clash.DEFENSE);
//				newClash.addMyMove(myMove);
//				//newClash.addHash(myMove.getHash());
//				this.clashes.put(oppositeId, newClash);
//
//				return true;
//
//			}
		//}
	}


	public /*synchronized*/ boolean addDefenseReceived(String oppositeId, String opposite, Defense otherMove){

		//se si ha giï¿½ avuto contatto con l'altro giocatore ed il precedente scontro non ï¿½ finito . ERRORE non si puï¿½ iniziare nuovamente
		if ( this.clashes.containsKey(oppositeId) && this.clashes.get(oppositeId).getStatusLast() != Clash.Phase.HASH){
			return false;
		}

	//	else{

			if (this.clashes.containsKey(oppositeId)){

				//this.clashes.get(oppositeId).addMyMove(myMove);
				//this.clashes.get(oppositeId).addHash(hash);
				this.clashes.get(oppositeId).addOtherPlayerMove(otherMove);
				this.clashes.get(oppositeId).setStatusLast(Clash.Phase.DEFENSE);
				return true;
			}

			else
				return false;
//			else { //occorre creare un nuovo campo
//
//				Clash newClash = new Clash(opposite, oppositeId);
//				newClash.setStatusLast(Clash.DEFENSE);
//				newClash.addOtherPlayerMove(otherMove);
//				this.clashes.put(oppositeId, newClash);
//
//				return true;
//
//
//			}
		//}

	}

	public Object getAttackClear(String oppositeId) {

		if(this.clashes.containsKey(oppositeId)){
			return this.clashes.get(oppositeId).getMyMoves().get(this.clashes.size());
		}
		else
			return null;
	}

	public /*synchronized*/ boolean closeMatch(String oppositeId){

		if ( this.clashes.containsKey(oppositeId) && this.clashes.get(oppositeId).getStatusLast() != Clash.Phase.DEFENSE){
			return false;
		}

		if (this.clashes.containsKey(oppositeId)){

			//this.clashes.get(oppositeId).addOtherPlayerMove(otherMove);
			this.clashes.get(oppositeId).setStatusLast(Clash.Phase.END);
			this.clashes.get(oppositeId).closeClash();
			return true;

		}
		else
			return false;

	}

	//difendi //TODO: salvare la risposta della difesa
	public void defenseMatch(GamePlayerResponsible player, String resource, double quantity, String threadId){
		MultiLog.println(GamePeer.class.toString(), "Difesa " + player.getName());
		//System.out.println("Difesa " + player.getName());
		//String oppositeId = this.findSuccessor(player.getId(), this.myThreadId);
		String oppositeId = this.findSuccessor(player.getId(), threadId);
		//NetPeerInfo oppositePeer = this.getSharedInfos().getInfoFor(this.myThreadId);
		NetPeerInfo oppositePeer = this.getSharedInfos().getInfoFor(threadId);

		Defense defense = new Defense(quantity, resource);

		if (this.newDefense(player.getId(), player.getName(), defense)) {


			DefenseMatchMessage startMatch = new DefenseMatchMessage(this.getMyId(),this.getMyPeer().getIpAddress(), this.getMyPeer().getPortNumber()+2,
					this.player.getId(), this.player.getName(), this.player.getSpatialPosition(), this.player.getPosX(), this.player.getPosY(), this.player.getPosZ(), defense.getType(), defense.getQuantity());

			String responseDefenseMessage = MessageSender.sendMessage(oppositePeer.getIpAddress(), oppositePeer.getPortNumber()+2, startMatch.generateXmlMessageString());

			if (responseDefenseMessage.contains("ERROR")){
				MultiLog.println(GamePeer.class.toString(), "Sending DEFENSE MATCH ERROR !");
				//System.out.println("Sending DEFENSE MATCH ERROR !");
			}
			else {
				MessageReader responseDefendMessageReader = new MessageReader();
				Message receivedDefendMessageReader = responseDefendMessageReader.readMessageFromString(responseDefenseMessage.trim());



				//TODO: non ci si aspetta un attacco ma la risposta con il messaggio in chiaro
				if (receivedDefendMessageReader.getMessageType().equals("ACK")){
					MultiLog.println(GamePeer.class.toString(), "ERRORE INVIO DIFESA");
					//System.out.println("ERRORE INVIO DIFESA");
				}
				//AckMessage ackMessage = new AckMessage(receivedDefendMessageReader);
//				if (ackMessage.getAckStatus() == 0){
//					System.out.println("Now REPLYED Match with defense");
//				}
				else if (receivedDefendMessageReader.getMessageType().equals("CLEARATTACK")){
					MultiLog.println(GamePeer.class.toString(), "Riceveuto l'attacco in chiaro");
					//System.out.println("Riceveuto l'attacco in chiaro");

					ClearAttackMatchMessage clearAttack = new ClearAttackMatchMessage(receivedDefendMessageReader);


					Attack attack = new Attack(clearAttack.getQuantity(), clearAttack.getType(), clearAttack.getNonce());
					this.addClearAttackReceived(clearAttack.getId(), clearAttack.getUserName(), attack);

					//TODO: vedere se l'hash corrisponde ed in caso contrario inviare un NACK
					if (this.clashes.get(clearAttack.getId()).verifyAttack()){

						MultiLog.println(GamePeer.class.toString(), "Confronto CHIUSO CORRETTAMENTE");
						//System.out.println("Confronto CHIUSO CORRETTAMENTE");
						this.closeMatch(clearAttack.getId());

						//(new AckMessage(this.listenerId, this.listenerAddr, this.listenerPort, 1, "")).generateXmlMessageString().getBytes()
						//MessageSender.sendMessage(oppositePeer.getIpAddress(), oppositePeer.getPortNumber()+2, (new AckMessage("","",-1,0,"")).generateXmlMessageString());

					}
					else {
						MultiLog.println(GamePeer.class.toString(), "CHIUSURA MATCH ERRORE RILEVATO - Send Nack");
						//System.out.println("CHIUSURA MATCH ERRORE RILEVATO - Send Nack");

					//	MessageSender.sendMessage(oppositePeer.getIpAddress(), oppositePeer.getPortNumber()+2, (new AckMessage("","",-1,1,"")).generateXmlMessageString());
					}

				}
			}
		}
		else{
			MultiLog.println(GamePeer.class.toString(), "Attacco gia' in corso");
			//System.out.println("Attacco giï¿½ in corso");
		}
	}


	public GameWorld getWorld() {
		return world;
	}


	public HashMap<String, Clash> getClashes() {
		return clashes;
	}


}