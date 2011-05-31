package it.unipr.ce.dsg.p2pgame.platform.bot;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;

import it.unipr.ce.dsg.p2pgame.GUI.MessageSender;
import it.unipr.ce.dsg.p2pgame.GUI.prolog.BuyResourceEngine;
import it.unipr.ce.dsg.p2pgame.GUI.prolog.ExtractionEngine;
import it.unipr.ce.dsg.p2pgame.GUI.prolog.GameEvolutionEngine;
import it.unipr.ce.dsg.p2pgame.GUI.prolog.MovementEngine;
import it.unipr.ce.dsg.p2pgame.GUI.prolog.VisibilityEngine;
import it.unipr.ce.dsg.p2pgame.platform.GamePeer;
import it.unipr.ce.dsg.p2pgame.platform.GameResource;
import it.unipr.ce.dsg.p2pgame.platform.GameResourceEvolve;
import it.unipr.ce.dsg.p2pgame.platform.GameResourceMobile;

public class RTSGameBot2 implements Runnable,InterfaceBot{
	
	
	private BuyResourceEngine bre;
	private ExtractionEngine ee;
	private MovementEngine me;
	private VisibilityEngine ve;
	private GameEvolutionEngine gee;
	private String owner="owner";
	private String ownerid="ownerID";
	private double L;
	private int nrmobile;
	private int nres;
	private ArrayList<Object> res;
	private HashMap<String, Boolean> status;
	private ArrayList<VirtualResource> enemies;
	private String profile;
	private double resmobcost;
	private int period_movement;
	private int probattack;
	private int probdefense;
	//aggiungere oggetto della classe MessageSender
	//MessageSender request;
	GamePeer gp;
	


	public RTSGameBot2(String profile)
	{
		
		gee=new GameEvolutionEngine("rules/evolutionTheory.pl");
		ee=new ExtractionEngine("rules/extractionTheory.pl");
		ve=new VisibilityEngine("rules/visibilityTheory.pl");
		res=new ArrayList<Object>();
		status=new HashMap<String, Boolean>();
		L=1000;
		enemies=new ArrayList<VirtualResource>();
		this.profile=profile;
		
		this.resmobcost=10;
       
		
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
		
		//GamePeer
		int portMin = 6891;
    	int serverPort = 1235;
    	String serverAdd="127.0.0.1";
    	String username="username";
    	String password="password";
		
		
		this.gp = new GamePeer(portMin+ 1 , portMin, 160, "", serverAdd, serverPort, portMin + 3, portMin + 2, serverAdd, serverPort+2, 4000,1000,64000,2000);
		
		this.gp.registerOnServer(username, password);
		//0,575,0,575,0,0, 1,10, 5
		//minX, maxX, minY, maxY, minZ, maxZ, vel, vis, gran
		//startgame
		this.gp.startGame(0,575,0,575,0,0, 1,10, 5);
		
		
		this.owner=gp.getMyId();
		this.ownerid=gp.getMyId();
		System.out.println("STARTGAME POSX "+this.gp.getPlayer().getPosX()+" POSY "+this.gp.getPlayer().getPosY());
		
		//apro il file di profilo
		//carico profilo del giocatore
		
		File f=new File(profile);
       	FileInputStream fis;
       	ArrayList<String> req_exp_res=new ArrayList<String>();
       	ArrayList<Integer> req_exp_qres=new ArrayList<Integer>();
       	ArrayList<String> req_conq_res=new ArrayList<String>();
       	ArrayList<Integer> req_conq_qres=new ArrayList<Integer>();
       	double req_conq_money=0;
       	double req_exp_money=0;
       	
       	
		try {
			fis = new FileInputStream(f);
			InputStreamReader isr=new InputStreamReader(fis);
	       	BufferedReader br=new BufferedReader(isr);
	    	
	       	//ExtractionTheory
			String str=br.readLine();
			int per=Integer.parseInt(str);
			str=br.readLine();
			int val=Integer.parseInt(str);
			str=br.readLine();
			boolean isinf=Boolean.parseBoolean(str);
			str=br.readLine();
			int current=Integer.parseInt(str);
			
			//create the extraction theory
			ee.createExtractTheory(per,val,isinf,current);
			
			//EvolutionTheory
			str=br.readLine();			
			req_exp_money=Double.parseDouble(str);
			str=br.readLine();
			String [] array_str=str.split(",");
			
			
			for(int i=0;i<array_str.length;i++)
			{
				req_exp_res.add(array_str[i]);
				
			}					
			
			str=br.readLine();
			array_str=str.split(",");
			
			for(int i=0;i<array_str.length;i++)
			{
				int int_aux=Integer.parseInt(array_str[i]);
				req_exp_qres.add(new Integer(int_aux));
			}
			
			
			str=br.readLine();
			req_conq_money=Double.parseDouble(str);
			
			str=br.readLine();
			array_str=str.split(",");			
			
			for(int i=0;i<array_str.length;i++)
			{
				req_conq_res.add(array_str[i]);
				
			}
			
			str=br.readLine();
			array_str=str.split(",");		
			
			for(int i=0;i<array_str.length;i++)
			{
				int int_aux=Integer.parseInt(array_str[i]);
				req_conq_qres.add(new Integer(int_aux));
			}
			
			//period movement
			str=br.readLine();
			 this.period_movement=Integer.parseInt(str);
						
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		double currentmoney=0;//money.getQuantity();
		this.nres=0;
		this.nrmobile=0;
		//creo l'oggetto ResourceEvolve
		double offset=(double)this.ee.getAccValue();
		final long period=(long)this.ee.getPeriod();
		double quantity=(double)this.ee.getCurrentResource();
		GameResourceEvolve revolve=new GameResourceEvolve("moneyEvolveble", "Money", quantity, period, offset);
		this.gp.addToMyResource(revolve);
		//ottengo da gp informazioni sulle risorse e i soldi a disposizione
		//ArrayList<Object> res=gp.getMyResources(); // poi verifico le risorse sulla lista tranne moneyEvolve
		
		//aggiungere un solo oggetto GameResource usando messagesender
		
		
		ArrayList<String> currentres=new ArrayList<String>();
		
		currentres.add("GameResource");
		currentres.add("GameResourceMobile");
		
	
		
		//poi entro nel ciclo infinito
		int c=0;
		while(true)
		{
			try {
				Thread.sleep(3000);
				c++;
				System.out.println("@@@@@@@@@@@@@@@@@@@ ciclo "+c+"@@@@@@@@@@@@@@@@ò");
				
				ArrayList<Integer> currentqres=new ArrayList<Integer>(); 
				currentqres.add(new Integer(nres));
				currentqres.add(new Integer(nrmobile));
				
				if(ee.isInfinite())
				{
					
					this.incrementMoney(resmobcost);
					
				}
				
				
				currentmoney=this.gp.getMyResourceFromId("moneyEvolveble").getQuantity();
				
				
				gee=new GameEvolutionEngine("rules/evolutionTheory.pl");
				
				gee.createGameEvolutionTheory(currentmoney, currentres, currentqres, req_exp_money, req_exp_res, req_exp_qres, req_conq_money, req_conq_res, req_conq_qres);
				
								
				// con queste informazioni interrogo al engine in quale fase del gioco mi trovo, e ricavo le prob di attacco e il raggio
				//di spostamento
				int fase=gee.getFase();
				
				System.out.println("&&&&&&&&&& FASE "+fase+" @@@@@@@@@@@");
				
				GameResourceEvolve evolve=(GameResourceEvolve)gp.getMyResourceFromId("moneyEvolveble");
				double val=evolve.getQuantity();
				
				System.out.println("Soldi: "+val);
				
				double rad=gee.getRadius();
				
				rad*=L;
				System.out.println("rad: "+rad);
				
				 probattack=gee.getProbAttack();
				
				System.out.println("prob attack: "+probattack);
				
				probdefense=gee.getProbDefense();
				
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
					
					int xx=(int)(Math.random()*10);
					System.out.println("random "+xx);
					if((xx%2)==0)
					{
						//compro risorsa mobile
						String timestamp = Long.toString(System.currentTimeMillis());
						this.nrmobile++;						
						
						
						this.gp.createMobileResource("Attack" + timestamp, this.resmobcost);
						//lo status per default è false
						
						
						//status.put("m"+timestamp, new Boolean(false)); // devo salvare anche l'ID
						//currentmoney-=resmobcost;
						this.decrementMoney(resmobcost);
						System.out.println("###################nuova risorsa mobile##################");
					}
					else
					{
						//compro risorsa di difesa
						
						String timestamp = Long.toString(System.currentTimeMillis());
						this.nres++;
						//res.add(new GameResource("id"+this.nres,"defense",1.0));
						
						GameResource dif = new GameResource("def" + timestamp, "Defense" + timestamp, resmobcost);
		                this.gp.addToMyResource(dif);
						//currentmoney-=1000;
						this.decrementMoney(resmobcost);
						System.out.println("###########nuova risorsa di difesa####################");
					}
					
					
				}
				
				//stampo elenco di risorse
				this.printResources();
				
				
				//decido se spostare ogni risorsa mobile e determino la destinazione
				
				ArrayList<Object> res=this.gp.getMyResources(); 
				int sr=res.size();
				
				for(int i=0;i<sr;i++)
				{
					
					if( res.get(i) instanceof GameResourceMobile){
						
						
						
						GameResourceMobile grm=(GameResourceMobile)res.get(i);
						String id=grm.getId();
						boolean st=grm.getStatus();
						//String id=grm.getDescription();
						
						if(!st) // se non è in movimento
						{
														
							// qua devo mettere il tread di spostamento e indicare che la risorsa e' in movimento
							//prima verifico se la risors è attualmente in movimento
							int aux=(int)(Math.random()*100);
							System.out.println("pribabilita' di spostare risorsa "+id+" : "+aux+" %");
						    if(aux<probmove)
							{
								//se la risorsa non è attualmente in movimento
								//se decido di spostare una risorsa, devo scegliere in modo aleatorio il punto d'arrivo
								// il punto d'arrivo dipende del raggio massimo di spostamento scelto dalla fase del gioco
								
										
								boolean mband=false;
								double mx=0;
								double my=0;
								while(!mband) //cicla fino a trovare delle coordinate dentro lo scenario del gioco
								{
									mx=(double)(Math.random()*rad);	
									my=(double)(Math.random()*rad);
									
									int mquad=(int)(Math.random()*3+ 1);
									
									// se e' il primo quadrante non faccio niente
									if(mquad==2){
										mx*=-1;
										//y rimane uguale
									}else if(mquad==3){
										mx*=-1;
										my*=-1;
										
									}else{
										my*=-1;
										//x rimane uguale
									}
									
									mx=mx+this.gp.getPlayer().getPosX();
									my=my+this.gp.getPlayer().getPosY();
									
									if((mx>=0&&mx<=575)&&(my>=0&&my<=575))
									{
										mband=true;
									}
							
								}
								
								int tx=(int)mx;
								int ty=(int)my;
								
								System.out.println("move "+id+": x=" +tx+" y= "+ty);
								
								MovementThread move=new MovementThread(this,id,tx,ty,period_movement);
								
							
								
							}
							
							
							
						}
						else
						{
							
							System.out.println("Resource "+id+" in movimento");
						}
						
						
												

						
						
					}
						
					
					
					
				}
				
		
				
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		
		
	}
	
	public void setMovStatus(String id,boolean newstatus)
	{
		this.status.put(id,new Boolean(newstatus));
		
	}
	
	public boolean getMovStatus(String id)
	{
		Boolean st=(Boolean)this.status.get(id);
		return st.booleanValue();
	}
	
	public GameResourceMobile getResourceMobilebyID(String id)
	{
		
		GameResourceMobile grm=this.gp.getMyMobileResourceFromId(id);
		return grm;
	}
	
	
	public void printResources()
	{
		
		
		ArrayList<Object> res=this.gp.getMyResources();
		System.out.println("Resource:");
		for(int i=0;i<res.size();i++)
		{
			Object aux=res.get(i);
			
			if((aux instanceof GameResource)&&!(aux instanceof GameResourceMobile))
			{
				if(!(aux instanceof GameResourceEvolve))
				{
					GameResource gr=(GameResource)aux;
					System.out.println("Resource: "+gr.getId());
										
				}
				
			}
			
		}
		
		System.out.println("\n\nResourceMobile:");
		for(int i=0;i<res.size();i++)
		{
			Object aux=res.get(i);
			
			if(aux instanceof GameResourceMobile)
			{
				GameResource gr=(GameResourceMobile)aux;
				System.out.println("Resource: "+gr.getId());
				
			}
			
		}
		
		
	}
	
	public VirtualResource getVResourcebyCoordinates(int x,int y)
	{
		int l=this.enemies.size();
		VirtualResource res=null;
		VirtualResource aux=null;
		for(int i=0;i<l;i++)
		{
			aux=this.enemies.get(i);
			
			if((aux.getX()==x) &&(aux.getY()==y))
			{
				res=aux;
				System.out.println("NEMICO TROVATO POS "+x +" , "+ y );
				break;
			}
			
		}
		
		return res;
		
		
	}
	
	private void incrementMoney(double inc)
	{
		
		GameResourceEvolve evolve=(GameResourceEvolve)gp.getMyResourceFromId("moneyEvolveble");
		double val=evolve.getQuantity();
			
		double qt=val+inc;
		//evolve.setQuantity(qt);
		gp.getMyResourceFromId("moneyEvolveble").setQuantity(qt);
		
	}
	
	private void decrementMoney(double dec)
	{
		
		GameResourceEvolve evolve=(GameResourceEvolve)gp.getMyResourceFromId("moneyEvolveble");
		double val=evolve.getQuantity();
		
		double qt=val-dec;
		//evolve.setQuantity(qt);
		gp.getMyResourceFromId("moneyEvolveble").setQuantity(qt);
		
	}
	


	@Override
	public GamePeer getMyGamePeer() {
		return this.gp;
	}
	
	public ArrayList<VirtualResource> getEnemies() {
		return enemies;
	}

	public void setEnemies(ArrayList<VirtualResource> enemies) {
		this.enemies = enemies;
	}

	
	
	public String getOwner() {
		return owner;
	}

	public void setOwner(String owner) {
		this.owner = owner;
	}

	public String getOwnerid() {
		return ownerid;
	}

	public void setOwnerid(String ownerid) {
		this.ownerid = ownerid;
	}

	public int getProbattack() {
		return probattack;
	}

	public void setProbattack(int probattack) {
		this.probattack = probattack;
	}

	public int getProbdefense() {
		return probdefense;
	}

	public void setProbdefense(int probdefense) {
		this.probdefense = probdefense;
	}
	

}
