package it.unipr.ce.dsg.p2pgame.platform.bot;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;



import it.unipr.ce.dsg.p2pgame.GUI.prolog.MovementEngine;
import it.unipr.ce.dsg.p2pgame.GUI.prolog.VisibilityEngine;
import it.unipr.ce.dsg.p2pgame.platform.Attack;
import it.unipr.ce.dsg.p2pgame.platform.Clash;
import it.unipr.ce.dsg.p2pgame.platform.GamePlayerResponsible;
import it.unipr.ce.dsg.p2pgame.platform.GameResourceMobile;
import it.unipr.ce.dsg.p2pgame.platform.GameResourceMobileResponsible;

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
		grm.setStatus(true);
			
		
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
						
						//qua implemento il protocollo di clash
						
						
						
						
						//startmatch
						
						//defensematch
						
						//decision
				       }
					
				    /***/
						
						
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
	
	
	

}
