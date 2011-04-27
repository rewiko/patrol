package it.unipr.ce.dsg.p2pgame.platform.bot;

import java.util.ArrayList;

import it.unipr.ce.dsg.p2pgame.GUI.prolog.BuyResourceEngine;
import it.unipr.ce.dsg.p2pgame.GUI.prolog.ExtractionEngine;
import it.unipr.ce.dsg.p2pgame.GUI.prolog.GameEvolutionEngine;
import it.unipr.ce.dsg.p2pgame.GUI.prolog.MovementEngine;
import it.unipr.ce.dsg.p2pgame.GUI.prolog.VisibilityEngine;
import it.unipr.ce.dsg.p2pgame.platform.GamePeer;
import it.unipr.ce.dsg.p2pgame.platform.GameResource;
import it.unipr.ce.dsg.p2pgame.platform.GameResourceEvolve;
import it.unipr.ce.dsg.p2pgame.platform.GameResourceMobile;

public class Bot implements Runnable{
	
	GamePeer gp;
	BuyResourceEngine bre;
	ExtractionEngine ee;
	MovementEngine me;
	VisibilityEngine ve;
	GameEvolutionEngine gee;
	String owner="owner";
	String ownerid="ownerID";
	double L;
	int nrmobile;
	int nres;
	
	public Bot()
	{
		//this.gp=new GamePeer();
		//gp = new GamePeer(portMin+ 1 , portMin, 160, "", serverAddressTextField.getText().trim(), serverPort, portMin + 3, portMin + 2, serverAddressTextField.getText().trim(), serverPort+2, 4000,1000,64000,2000);
		gee=new GameEvolutionEngine("rules/evolutionTheory.pl");
		ee=new ExtractionEngine("rules/extractionTheory.pl");
		L=1000;
		
	}

	@Override
	public void run() {
		
		//inizialmente imposto i parametri delle teorie
		
		//imposto altri parametri e imposto il profilo del bot
		/*
		 * required_exploration_money: 10000
		   required_exploration_resources: [GameResource,GameResourceMobile]
		   required_exploration_qresources: [2,5]

		   required_conquest_money: 20000
		   required_conquest_resources: [GameResource,GameResourceMobile]
		   required_conquest_qresources: [3,10]

		 * */
		double currentmoney=10000;//money.getQuantity();
		ee.createExtractTheory(10,1000,true,(int)currentmoney);
		
		double req_exp_money=10000;		
		ArrayList<String> req_exp_res=new ArrayList<String>();
		req_exp_res.add("GameResource");
		req_exp_res.add("GameResourceMobile");		
		ArrayList<Integer> req_exp_qres=new ArrayList<Integer>();
		req_exp_qres.add(new Integer(3));
		req_exp_qres.add(new Integer(5));
		
		double req_conq_money=10000;		
		ArrayList<String> req_conq_res=new ArrayList<String>();
		req_conq_res.add("GameResource");
		req_conq_res.add("GameResourceMobile");		
		ArrayList<Integer> req_conq_qres=new ArrayList<Integer>();
		req_conq_qres.add(new Integer(5));
		req_conq_qres.add(new Integer(7));
		
		
		
		//ottengo da gp informazioni sulle risorse e i soldi a disposizione
		//ArrayList<Object> res=gp.getMyResources(); // poi verifico le risorse sulla lista tranne moneyEvolve
		
		ArrayList<Object> res=new ArrayList<Object>();
		
		res.add(new GameResource("id1","defense",1.0));
		res.add(new GameResource("id2","defense",1.0));
		//res.add(new GameResource("id3","defense",1.0));
		//res.add(new GameResource("id4","defense",1.0));
		//res.add(new GameResource("id5","defense",1.0));
		this.nres=2;
		
		//String id, String description, String owner, String ownerId, double quantity, double x, double y, double z, double vel, double vis
		res.add(new GameResourceMobile("m1","attack",owner,ownerid,1.0,0,0,0,0,0));
		res.add(new GameResourceMobile("m2","attack",owner,ownerid,1.0,0,0,0,0,0));
	    //res.add(new GameResourceMobile("m3","attack",owner,ownerid,1.0,0,0,0,0,0));
		//res.add(new GameResourceMobile("m4","attack",owner,ownerid,1.0,0,0,0,0,0));
		//res.add(new GameResourceMobile("m5","attack",owner,ownerid,1.0,0,0,0,0,0));
		this.nrmobile=2;
		ArrayList<String> currentres=new ArrayList<String>();
		
		currentres.add("GameResource");
		currentres.add("GameResourceMobile");
		
		
		
		
		
		
		//poi entro nel ciclo infinito
		int c=0;
		while(true)
		{
			try {
				Thread.sleep(2000);
				c++;
				System.out.println("@@@@@@@@@@@@@@@@@@@ ciclo "+c+"@@@@@@@@@@@@@@@@ò");
				
				ArrayList<Integer> currentqres=new ArrayList<Integer>();
				currentqres.add(new Integer(nres));
				currentqres.add(new Integer(nrmobile));
				
				if(ee.isInfinite())
					currentmoney+=1000;
				//GameResourceEvolve money=(GameResourceEvolve) gp.getMyResourceFromId("moneyEvolveble");
				
				
				gee=new GameEvolutionEngine("rules/evolutionTheory.pl");
				
				gee.createGameEvolutionTheory(currentmoney, currentres, currentqres, req_exp_money, req_exp_res, req_exp_qres, req_conq_money, req_conq_res, req_conq_qres);
				
								
				// con queste informazioni interrogo al engine in quale fase del gioco mi trovo, e ricavo le prob di attacco e il raggio
				//di spostamento
				int f=gee.getFase();
				
				System.out.println("&&&&&&&&&& FASE "+f+" @@@@@@@@@@@");
				
				System.out.println("Soldi: "+currentmoney);
				
				double rad=gee.getRadius();
				
				rad*=L;
				System.out.println("rad: "+rad);
				
				int probattack=gee.getProbAttack();
				
				System.out.println("prob attack: "+probattack);
				
				int probdefense=gee.getProbDefense();
				
				System.out.println("prob defense: "+probdefense);
				
				//con una probilita' x decido se acquistare o meno una nuova risorsa
				//Questo controllo lo aggiungo in una nuova teoria, associata al profilo di giocatore (Fatto)
				
				int probbuy=gee.getProbBuy();
				
				System.out.println("prob buy: "+probbuy);
				
				//con una probabilita' y decido se spostare o no una risorsa mobile. Questa
				//operazione la effettuo per ogni risorsa mobile che possiedo
				
				int probmove=gee.getProbMovement();
				System.out.println("prob move: "+probmove);
				
				
				//decido se comprare una nuova risorsa
				int randombuy=(int)(Math.random()*100);
				
				System.out.println("probabilità di acquistare una nuova risorsa: "+randombuy + "%");
				if(randombuy<probbuy)
				{
					
					int aux=(int)Math.random()*10;
					
					if((aux%2)==0)
					{
						//compro risorsa mobile
						
						this.nrmobile++;
						res.add(new GameResourceMobile("m"+this.nrmobile,"attack",owner,ownerid,1.0,0,0,0,0,0));
						currentmoney-=1000;
						System.out.println("###################nuova risorsa mobile##################");
					}
					else
					{
						//compro risorsa di difesa
						this.nres++;
						res.add(new GameResource("id"+this.nres,"defense",1.0));
						currentmoney-=1000;
						System.out.println("###########nuova risorsa di difesa####################");
					}
					
					
				}
				
				
				//decido se spostare ogni risorsa mobile e determino la destinazione
				int sr=res.size();
				
				for(int i=0;i<sr;i++)
				{
					
					if( res.get(i) instanceof GameResourceMobile){
						
						
						int aux=(int)(Math.random()*100);
						GameResourceMobile grm=(GameResourceMobile)res.get(i);
						String id=grm.getId();
						
						System.out.println("pribabilita' di spostare risorsa "+id+" : "+aux+" %");
						if(aux<probmove)
						{
							//se decido di spostare una risorsa, devo scegliere in modo aleatorio il punto d'arrivo
							// il punto d'arrivo dipende del raggio massimo di spostamento scelto dalla fase del gioco
							
							
							
							//scelgo tra 5 possibili angoli rispetto all'ascisa
							int angle=(int)(Math.random()*4+ 1);
							
							//scelgo le coordinate per un vettore di lunghezza 1
							double x,y;
							
							if(angle==1){//0°
								x=1;
								y=0;
								
							}else if(angle==2){ //30°
								x=0.8;
								y=0.5;
								
							}else if(angle==3){//45°
								x=y=0.5;
								
							}else if(angle==4){//60°
								
								x=0.5;
								y=0.8;
								
							}else{//90°
								x=0;
								y=1;
							}
							
							//qudrante
							//scelgo aleatoriamente qudrante e angolo 
							int quad=(int)(Math.random()*3+ 1);
							
							// se e' il primo quadrante non faccio niente
							if(quad==2){
								x*=-1;
								//y rimane uguale
							}else if(quad==3){
								x*=-1;
								y*=-1;
								
							}else{
								y*=-1;
								//x rimane uguale
							}
							
							//lunghezza della traiettoria
								double l=(double)(Math.random()*rad);					
							
							x*=l; //coordinate di destinazione
							y*=l;
								
							
							int cx=(int)x;
							int cy=(int)y;
							
							System.out.println("move "+id+": x=" +cx+" y= "+cy);
							
						}
						
						
					}
						
					
					
					
				}
				
				
				
				
				
				
				
				
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		
		
	}

}
