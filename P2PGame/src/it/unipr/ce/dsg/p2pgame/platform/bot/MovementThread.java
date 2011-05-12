package it.unipr.ce.dsg.p2pgame.platform.bot;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;



import it.unipr.ce.dsg.p2pgame.GUI.prolog.MovementEngine;
import it.unipr.ce.dsg.p2pgame.GUI.prolog.VisibilityEngine;
import it.unipr.ce.dsg.p2pgame.platform.GameResourceMobile;

public class MovementThread implements Runnable{
	
	private Thread runner;
	private String resid;
	private Bot mybot;
	private double targetx,targety;
	FileOutputStream fos;
	File file;
	PrintStream ps;
	public MovementThread(Bot mybot,String resid,double targetx,double targety)
	{   //bisogna aggiungere parametro per area di gioco
		runner=new Thread(this);
		this.resid=resid;
		this.mybot=mybot;
		this.targetx=targetx;
		this.targety=targety;
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
		VisibilityEngine ve=new VisibilityEngine("rules/visibilityTheory.pl");
		GameResourceMobile grm=this.mybot.getResourceMobilebyID(this.resid);
		
		double currentx=grm.getX();
		double currenty=grm.getY();
		
		double previousx=0;
		double previousy=0;
		
		ArrayList<Integer> vis=new ArrayList<Integer>(); // da modificare
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
		
		this.mybot.setMovStatus(resid, true); //così segnalo che è in movimento
		
			
		
		int cont=0;
		
		while((currentx!=this.targetx)||(currenty!=this.targety))
		{
			try{
				Thread.sleep(100);
				
				me.clearParameters("rules/movementTheory.pl");
				me.createMovementTheory((int)currentx, (int)currenty, (int)this.targetx, (int)this.targety,(int) previousx, (int)previousy, vis);
				
				int m=me.nextMovement();
				
				if(m==1)//nord
				{
					currentx--;
					previousx=-1;
					previousy=0;
				}
				else if(m==2)
				{
					currentx++;
					previousx=1;
					previousy=0;
					
				}
				else if(m==3)
				{
					currenty++;
					previousy=1;
					previousx=0;
					
				}
				else if(m==4)
				{
					currenty--;
					previousy=-1;
					previousx=0;
					
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
					
					cont=0;
				}
				cont++;

				
			}catch(InterruptedException e){
				
			}
			
		}
		
		this.mybot.setMovStatus(resid, false); //non mi muovo più
		
		System.out.println("arrived "+resid+": x=" +targetx+" y= "+targety);
	}
	

}
