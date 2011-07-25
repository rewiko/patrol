package it.unipr.ce.dsg.p2pgame.platform.bot;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;



import it.simplexml.message.AckMessage;
import it.simplexml.message.Message;
import it.simplexml.message.MessageReader;
import it.simplexml.sender.MessageSender;
import it.unipr.ce.dsg.p2pgame.GUI.prolog.MovementEngine;
import it.unipr.ce.dsg.p2pgame.GUI.prolog.VisibilityEngine;
import it.unipr.ce.dsg.p2pgame.network.NetPeerInfo;
import it.unipr.ce.dsg.p2pgame.platform.Attack;
import it.unipr.ce.dsg.p2pgame.platform.Clash;
import it.unipr.ce.dsg.p2pgame.platform.GamePeer;
import it.unipr.ce.dsg.p2pgame.platform.GamePlayerResponsible;
import it.unipr.ce.dsg.p2pgame.platform.GameResourceMobile;
import it.unipr.ce.dsg.p2pgame.platform.GameResourceMobileResponsible;
import it.unipr.ce.dsg.p2pgame.platform.bot.message.PlanetConqueredMessage;
import it.unipr.ce.dsg.p2pgame.platform.message.StartMatchMessage;
import it.unipr.ce.dsg.p2pgame.util.MultiLog;

public class MovementThread implements Runnable{
	
	private Thread runner;
	private String resid;
	private InterfaceBot mybot;
	private double targetx,targety;
	private int period;
	FileOutputStream fos;
	File file;
	PrintStream ps;
	public MovementThread(InterfaceBot mybot,String resid,double targetx,double targety,int period)
	{   //bisogna aggiungere parametro per area di gioco
		runner=new Thread(this);
		this.resid=resid;
		this.mybot=mybot;
		this.targetx=targetx;
		this.targety=targety;
		this.period=period;
		runner.start();
		file=new File("log/"+this.resid+".txt");
		try {
			fos=new FileOutputStream(file,true);
			ps=new PrintStream(fos);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
	}

	@Override
	public void run() {
		MovementEngine me=new MovementEngine("rules/movementTheory.pl");
		//VisibilityEngine ve=new VisibilityEngine("rules/visibilityTheory.pl");
		GameResourceMobile grm=this.mybot.getResourceMobilebyID(this.resid);
		
		double currentx=grm.getX();
		double currenty=grm.getY();
		
		double previousx=0;
		double previousy=0;
		
		ArrayList<Integer> vis=new ArrayList<Integer>(); // da modificare relativo ad ostacoli per ora non serve
		vis.add(new Integer(0));
		vis.add(new Integer(0));
		vis.add(new Integer(0));
		vis.add(new Integer(0));
		vis.add(new Integer(0));
		vis.add(new Integer(0));
		vis.add(new Integer(0));
		vis.add(new Integer(0));
		
		
		//sto per iniziare lo spostamento
		//System.out.println("Current position x: "+currentx+" y: "+currenty);
		//System.out.println("Target position x: "+this.targetx+" y: "+this.targety);
		
		//imposto la risorsa mobile in movimento
		
		//this.mybot.setMovStatus(resid, true); //così segnalo che è in movimento
		grm.setStatus(true); // cambia lo stato della risorsa mobile indicando che e' in movimento
			
		
		int cont=0;
		
		while((currentx!=this.targetx)||(currenty!=this.targety))
		{
			try{
				Thread.sleep(this.period);
				
				me.clearParameters("rules/movementTheory.pl");
				me.createMovementTheory((int)currentx, (int)currenty, (int)this.targetx, (int)this.targety,(int) previousx, (int)previousy, vis);
								
				
				int movx=me.longitudeMovement();
				int movy=me.latitudeMovement();
				
				if(movx==1)//verso ovest
				{
					currentx--;
					previousx=-1;
					
				}
				else if(movx==2) //verso est
				{
					currentx++;
					previousx=1;
					
				}
				else if(movx==0)//non mi muovo in orizontale
				{
					previousx=0;
				
				}
				
				
				if(movy==1)//verso nord
				{
					currenty--;
					previousy=-1;
				}
				else if(movy==2)//verso sud
				{
					currenty++;
					previousy=1;
					
				}
				else if(movy==0)//non mi muovo in verticale
				{
					previousy=0;
					
				}
					
				
				grm.setX(currentx);
				grm.setY(currenty);
				//System.out.println("\nCurrent position x: "+currentx+" y: "+currenty);
				
				
				//controllo della visibilita'
				if(cont==10)
				{
					ArrayList<Integer> posx=new ArrayList<Integer>();
		    		ArrayList<Integer> posy=new ArrayList<Integer>();
		    		ArrayList<String> owner=new ArrayList<String>();
		    		ArrayList<String> type=new ArrayList<String>();
		    		
		    		ArrayList<Integer> pattack=new ArrayList<Integer>();
		    		ArrayList<String> restypes=new ArrayList<String>();
		    		
		    		restypes.add("GameResourceMobile");
		    		restypes.add("GameResource");
		    		
		    		pattack.add(new Integer(mybot.getProbattack()));
		    		pattack.add(new Integer(mybot.getProbattack()));
					
					double xx,yy;
					xx=grm.getX();
					yy=grm.getY();
					
					int x,y;
					x=(int)xx;
					y=(int)yy;
					
					/***
					
					ArrayList<Object> vision=grm.getResourceVision();
					
					double v=grm.getVision();
					ArrayList<Integer> array_pos=new ArrayList<Integer>();
					
					for(int z=0;z<vision.size();z++)
					{
						if(vision.get(z) instanceof GamePlayerResponsible)
						{
							GamePlayerResponsible gpr=(GamePlayerResponsible)vision.get(z);
							if(((gpr.getPosX()>=xx-v)&&(gpr.getPosX()<=xx-v))&&((gpr.getPosY()>=yy-v)&&(gpr.getPosY()<=yy-v)))
							{
								int k=(int)gpr.getPosX();
								int j=(int)gpr.getPosY();
								posx.add(new Integer(k));
								posy.add(new Integer(j));
								owner.add(gpr.getId());
								type.add("GameResource");
								array_pos.add(new Integer(z));
								
							}
						}
						else if(vision.get(z) instanceof GameResourceMobileResponsible)
						{
							
							GameResourceMobileResponsible grmr=(GameResourceMobileResponsible)vision.get(z);
							if(((grmr.getX()>=xx-v)&&(grmr.getX()<=xx-v))&&((grmr.getY()>=yy-v)&&(grmr.getY()<=yy-v)))
							{
								int k=(int)grmr.getX();
								int j=(int)grmr.getY();
								posx.add(new Integer(k));
								posy.add(new Integer(j));
								owner.add(grmr.getOwnerId());
								type.add("GameResourceMobile");
								array_pos.add(new Integer(z));
							}
						}
						
					}
					
					String id=mybot.getOwnerid();
					ve.createVisibilityTheory(posx, posy, owner, type, id, pattack, restypes);
					
					//ottengo la posizione dell'arraylist che corrisponde all'elemento da attacare
					int pos=ve.attack(); // la posizione corrispo
					
					if(pos!=0) 
				       {  
						
						//recupero l'object
						int posres=array_pos.get(pos-1);
						
						Object res=vision.get(posres);
						
						if(res instanceof GamePlayerResponsible)
						{
							VIENE CAMBIATO
							
							String threadID=new Long(Thread.currentThread().getId()).toString();
							GamePlayerResponsible player=(GamePlayerResponsible)res;
						    this.mybot.getMyGamePeer().startMatch(player, player.getId(),0 , threadID); // da dove tiro fuori la quiantità?
							this.mybot.getMyGamePeer().defenseMatch(player, player.getId(), 0, threadID);//threadId deve corrispondere a quello del nemic
							//devo implementare il metodo per inviare il mio attacco in chiaro				GamePlayerResponsible non contiene l'informazione che mi serve
							//devo implementare la decisione											//	
						}
						else if(res instanceof GameResourceMobileResponsible)
						{
							String threadID=new Long(Thread.currentThread().getId()).toString();
							GameResourceMobileResponsible res_grm=(GameResourceMobileResponsible)res;
							
						    this.mybot.getMyGamePeer().startMatch(res_grm, res_grm.getId(), res_grm.getQuantity(), threadID); 
							this.mybot.getMyGamePeer().defenseMatch(res_grm, res_grm.getId(), res_grm.getQuantity(), threadID);
							//devo implementare il metodo per inviare il mio attacco in chiaro
							//devo implementare la decisione
							
							if(this.mybot.getMyGamePeer().startMatch(res_grm, res_grm.getId(), res_grm.getQuantity(), threadID))
							{
								if(this.mybot.getMyGamePeer().defenseMatch(res_grm, res_grm.getId(), res_grm.getQuantity(), threadID))
								{
									//devo implementare il metodo per inviare il mio attacco in chiaro
									Clash myclash=this.mybot.getMyGamePeer().getClashes().get(threadID);
									ArrayList<Object> mymoves =myclash.getMyMoves();
									ArrayList<Object> othermoves =myclash.getOtherPlayerMoves();
									
									
									Attack myattack=(Attack)mymoves.get(0); //inizio con il mio attacco
									
									//int so=mymoves.size();
									Attack otherattack=(Attack)othermoves.get(0); //finalizzo col mio attacco, ed è l'unica mossa che ricevo dal nemico
									
									double myq=myattack.getQuantity();
									
									double otherq=otherattack.getQuantity();
									
									//devo implementare la decisione e decidere se sono o meno il vincitore
									
									this.mybot.getMyGamePeer().getClashes().get(threadID).closeClash();									
									
									//devo implementare la decisione
									
								}
								else
								{
									
									System.out.println("ERRORE DIFESA CLASH");
								}
								
							}
							else
							{
								System.out.println("ERRORE INIZIO CLASH");
								
							}
						}
						
						
						
						
						
						
						
						
						//decision
				       }
					
				    /********/
					
					//verifica pianeti
					
					//ottengo lista di pianeti
					
					ArrayList<VirtualResource> planets=this.mybot.getPlanets();
					//System.out.println("£££planets "+planets.size());
					for(int k=0;k<planets.size();k++)
					{
						
						VirtualResource planet=planets.get(k); // per ogni pianeta, verifico se le sue coordinate sono dentro una finestra visiva della mia risorsa mobile
						//System.out.println("Coordinate: x= "+planet.getX()+ " y= "+planet.getY());
						//System.out.println("My coordinates x = "+x+ "y = "+y);
						if((planet.getX()>=(x-2)) &&(planet.getX()<=(x+2))&&(planet.getY()>=(y-2))&&(planet.getY()<(y+2)))
						{
							System.out.println("PIANETA");
							System.out.println("Coordinate: x= "+planet.getX()+ " y= "+planet.getY());
							ps.println("PIANETA");
							ps.println("Coordinate: x= "+planet.getX()+ " y= "+planet.getY());
							
							if(planet.getOwnerID().equals("null")) // se il pianeta non è stato conquistato da qualcuno
							{
								this.mybot.setPlanetOwner(planet.getId(), this.mybot.getOwnerid()); //lo conquisto
								
								
								
								//invio un messaggio in broadcast a tutti peer nel gioco
								ArrayList<String> usersList=this.mybot.getMyGamePeer().getLoggedUsersList();
								
								if(!usersList.isEmpty()) // se ci sono degli utenti nella lista invio i messaggi
								{
									System.out.println("NUMBER OF USERS: "+usersList.size());
									for(int u=0;u<usersList.size();u++)
									{
										String str_user=usersList.get(u);
										
										System.out.println("USERS: "+str_user);
										String[] array_user=str_user.split(",");
										String user_id=array_user[0]; // mi serve ???
										String userip=array_user[1];
										String userport=array_user[2];
										
										//implemento l'invio dei messaggi
										//informazioni da inviare
										//myid, planetid
										
										if(!this.mybot.getMyGamePeer().getMyId().equals(user_id))											
										{
											PlanetConqueredMessage message=new PlanetConqueredMessage(this.mybot.getMyGamePeer().getMyId(),
													this.mybot.getMyGamePeer().getMyPeer().getIpAddress(),
													this.mybot.getMyGamePeer().getMyPeer().getPortNumber(),this.mybot.getOwnerid(),planet.getOwnerID());
											
											String responseMessage=MessageSender.sendMessage(userip,Integer.parseInt(userport),message.generateXmlMessageString());
											
											MultiLog.println(GamePeer.class.toString(), "Verify response...");
											
											if(responseMessage.contains("ERROR"))
											{
												
												MultiLog.println(GamePeer.class.toString(), "Sending Message ERROR!");
												
											}
											else
											{
												//ack message
												MessageReader responseStartMessageReader = new MessageReader();
												Message receivedStartMessageReader = responseStartMessageReader.readMessageFromString(responseMessage.trim());

												AckMessage ackMessage = new AckMessage(receivedStartMessageReader);
												if (ackMessage.getAckStatus() == 0){
													MultiLog.println(GamePeer.class.toString(), "Message received");
													//System.out.println("Now Match is started");
												}
												
												
											}

											
										}
										else
										{
											System.out.println("Ho conquistato un pianeta");
										}
										
										
									}
									
								}
								else
								{
									System.out.println("ERROR USERS LIST");
									
								}
								
							}
							else if(!planet.getOwnerID().equals(this.mybot.getOwnerid()))
							{
								//gestione di clash
								
								
								
							}
							// se il pianeta è mio non faccio niente
						}
						
						
					}
					
					
					/*******/
					
											
					for(int k=x-2;k<=x+2;k++)
					{
						for(int j=y-2;j<=y+2;j++)
						{
							if((k!=x)||(j!=y))
							{
								posx.add(new Integer(k));
								posy.add(new Integer(j));
								
								//devo fare un metodo per ottenere un nemico nelle coordinate
								VirtualResource aux=mybot.getVResourcebyCoordinates(k, j);
								
								
								if(aux!=null)
								{
									owner.add(aux.getOwnerID());
									type.add(aux.getResType());
									System.out.println("NEMICO POS "+k+" , "+ j);
								}
								else
								{
									owner.add("null");
									type.add("null");
									
								}
							}
						}
						
					}
					
					//qua faccio l'interrogazione sulla visibilita'
					String id=mybot.getOwnerid();
					id="PLAYER"+id;
					//System.out.println("MOVEMENT THREAD ID "+id);
					VisibilityEngine ve=new VisibilityEngine("rules/visibilityTheory.pl");
					ve.createVisibilityTheory(posx, posy, owner, type, id, pattack, restypes);
					
					//ottengo la posizione dell'arraylist che corrisponde all'elemento da attacare
					int pos=ve.attack();
					if(pos!=0)
				       {  System.out.println("***********"+this.resid+"**************");
						  System.out.println("***********ATTACCO!!!**************");
						  System.out.println("***********Nemico Trovato!!!**************");
						  System.out.println("Pos "+pos);
				    	  int ax=ve.posX();
				          int ay=ve.posY();

				          System.out.println("attack: ("+ax+" , "+ay+")");
				          
				          
				          
				          ps.println("***********ATTACCO!!!**************");
				          ps.println("***********Nemico Trovato!!!**************");
				          ps.println("Pos "+pos);
				          ps.println("attack: ("+ax+" , "+ay+")\n");
				          
				          
				          grm.setStatus(false);
				          break;
				       }
				       else
				       {  //otherwise, verifing if i have to defend against a possible attack and the X,Y coordinates
				    	  
				          pos=ve.defense();
				          
				          if(pos!=0)
				          {
				        	  System.out.println("***********"+this.resid+"**************");
				        	  System.out.println("***********DIFFESA!!!**************"); 
					    	  System.out.println("***********Nemico Trovato!!!**************");
					    	  System.out.println("Pos "+pos);
				              int dx=ve.posDX();
				              int dy=ve.posDY();
				              System.out.println("defense: ("+dx+" , "+dy+")");
				              
				              
				              
				        	  ps.println("***********DIFFESA!!!**************"); 
				        	  ps.println("***********Nemico Trovato!!!**************");
				        	  ps.println("Pos "+pos);				              
				        	  ps.println("defense: ("+dx+" , "+dy+")\n");
				        	  
				        	  
				        	  grm.setStatus(false);
				        	  break;
				          }
				          


				       }
					
					 /***/
					
					cont=0;
				}
				cont++;

				
			}catch(InterruptedException e){
				
			}
			
		}
		
		//this.mybot.setMovStatus(resid, false); //non mi muovo più
		grm.setStatus(false);
		
		System.out.println("arrived "+resid+": x=" +targetx+" y= "+targety);
	}
	
	
	public void startMatch(GamePlayerResponsible player, String resource, double quantity, String threadId)
	{
		
		MultiLog.println(MovementThread.class.toString(), "Attacco " + player.getName());
		//creo un nuovo oggetto Attack
		NetPeerInfo oppositePeer = mybot.getMyGamePeer().getSharedInfos().getInfoFor(threadId);
		
		Attack attack = new Attack(quantity, resource);
		
		//creo il clash aggiungendo un nuovo attacco
		if(mybot.getMyGamePeer().newAttack(player.getId(), player.getName(), attack))
		{ //se creo senza problemi invio messaggio per iniziare match
			
			StartMatchMessage startMatch = new StartMatchMessage(mybot.getMyGamePeer().getMyId(),mybot.getMyGamePeer().getMyPeer().getIpAddress(), mybot.getMyGamePeer().getMyPeer().getPortNumber()+2,
					mybot.getMyGamePeer().getPlayer().getId(), mybot.getMyGamePeer().getPlayer().getName(), mybot.getMyGamePeer().getPlayer().getSpatialPosition(), mybot.getMyGamePeer().getPlayer().getPosX(), mybot.getMyGamePeer().getPlayer().getPosY(), mybot.getMyGamePeer().getPlayer().getPosZ(), attack.getHash());

			String responseStartMessage = MessageSender.sendMessage(oppositePeer.getIpAddress(), oppositePeer.getPortNumber()+2, startMatch.generateXmlMessageString());
			
			//leggo la risposta
			if (responseStartMessage.contains("ERROR")){
				MultiLog.println(MovementThread.class.toString(), "Sending START MATCH ERROR !");
				//System.out.println("Sending START MATCH ERROR !");
			}
			else {
				MessageReader responseStartMessageReader = new MessageReader();
				Message receivedStartMessageReader = responseStartMessageReader.readMessageFromString(responseStartMessage.trim());

				AckMessage ackMessage = new AckMessage(receivedStartMessageReader);
				if (ackMessage.getAckStatus() == 0){
					MultiLog.println(MovementThread.class.toString(), "Now Match is started");
					//System.out.println("Now Match is started");
				}

			}
			
			
			//scontro iniziato!!!
			//dopo che lo scontro è iniziato, il nemico deve decidere la sua mossa
			//quindi devo rimanere in ascolto del suo messaggio che contiene la sua decisione in chiaro
			//per fare questo posso implementare un ciclo che rimane in ascolto della risposta
			
			int port=0;
			ServerSocket server=null;
			Socket clientSocket = null;
			try {
				if (server == null)
		                	server = new ServerSocket(port);

		    } catch (IOException e) {
				e.printStackTrace();
		    }
		    
		    try {
				clientSocket = server.accept();
				String message = null;

				DataInputStream is = new DataInputStream(clientSocket.getInputStream());
				DataOutputStream os = new DataOutputStream(clientSocket.getOutputStream());
				
		        while(true){

                    int current = 0;
                    byte[] buf = new byte[100000];

                    while (current < 1) {

                    	int reader = is.read(buf);

                    	if (reader != -1){
                    		message = new String(buf);
                    		current++;
                    	}
                    }

                    clashMessageAction(message, os);

                    is.close();
                    os.close();
                    clientSocket.close();
                    break;
                }
		        
				
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}


			

		}
		else
		{
			MultiLog.println(MovementThread.class.toString(), "Attacco gia' in corso");
			
			
		}
		
		
			
		
	}
	
	private void clashMessageAction(String messageString, DataOutputStream os) throws IOException 
	{
		//da implementare
		
	}
	
	

}
