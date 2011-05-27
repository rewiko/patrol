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

public class RTSGameBot implements Runnable,InterfaceBot{
	
	
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
	//aggiungere oggetto della classe MessageSender
	MessageSender request;
	
	public ArrayList<VirtualResource> getEnemies() {
		return enemies;
	}

	public void setEnemies(ArrayList<VirtualResource> enemies) {
		this.enemies = enemies;
	}

	int probattack;
	int probdefense;
	
	
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

	public RTSGameBot(String profile)
	{
		
		gee=new GameEvolutionEngine("rules/evolutionTheory.pl");
		ee=new ExtractionEngine("rules/extractionTheory.pl");
		ve=new VisibilityEngine("rules/visibilityTheory.pl");
		res=new ArrayList<Object>();
		status=new HashMap<String, Boolean>();
		L=1000;
		enemies=new ArrayList<VirtualResource>();
		this.profile=profile;
		this.request=new MessageSender();
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
		request.CreateGamePeer(inPort, outPort, idBitLength, id, serverAddr, serverPort, gameInPort, gameOutPort, gameServerAddr, gameServerPort, stab, fix, check, pub);
		request.registerOnServer(username, password);
		request.startGame(minX, maxX, minY, maxY, minZ, maxZ, vel, vis, gran);
		
		
		
		
		
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
		this.request.addResourceEvolve("moneyEvolveble", "Money", 0, 1000, 1);
		
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
				
				
				
				gee=new GameEvolutionEngine("rules/evolutionTheory.pl");
				
				gee.createGameEvolutionTheory(currentmoney, currentres, currentqres, req_exp_money, req_exp_res, req_exp_qres, req_conq_money, req_conq_res, req_conq_qres);
				
								
				// con queste informazioni interrogo al engine in quale fase del gioco mi trovo, e ricavo le prob di attacco e il raggio
				//di spostamento
				int fase=gee.getFase();
				
				System.out.println("&&&&&&&&&& FASE "+fase+" @@@@@@@@@@@");
				
				System.out.println("Soldi: "+currentmoney);
				
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
					
					int aux=(int)Math.random()*10;
					
					if((aux%2)==0)
					{
						//compro risorsa mobile
						String timestamp = Long.toString(System.currentTimeMillis());
						this.nrmobile++;						
						
						this.request.createMobileResource("Attack" + timestamp, this.resmobcost);
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
						this.request.addResource("def" + timestamp, "Defense" + timestamp, resmobcost);
						//currentmoney-=1000;
						this.decrementMoney(resmobcost);
						System.out.println("###########nuova risorsa di difesa####################");
					}
					
					
				}
				
				//stampo elenco di risorse
				this.printResources();
				
				
				//decido se spostare ogni risorsa mobile e determino la destinazione
				ArrayList<Object> res=request.getResources();
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
									
								
								int tx=(int)x;
								int ty=(int)y;
								
								System.out.println("move "+id+": x=" +tx+" y= "+ty);
								
								MovementThread move=new MovementThread(this,id,tx,ty);
								
								
								
								
								
							}
							
							
							
						}
						else
						{
							
							System.out.println("Resource "+id+" in movimento");
						}
						
						//controllo della visibilità della navicella. Poi estenderò il controllo della visibilità 
						// a quella associata al gamepeer
												

						
						
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
		GameResourceMobile grm=request.getMobileResource(id);
		
		return grm;
	}
	
	
	public void printResources()
	{
		
		ArrayList<Object> res=request.getResources();
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
		GameResourceEvolve evolve=(GameResourceEvolve)request.getMyResourceFromId("moneyEvolveble");
		double val=evolve.getQuantity();
		request.UpdateResourceEvolve(val+inc);	
		
	}
	
	private void decrementMoney(double dec)
	{
		GameResourceEvolve evolve=(GameResourceEvolve)request.getMyResourceFromId("moneyEvolveble");
		double val=evolve.getQuantity();
		request.UpdateResourceEvolve(val-dec);
		
	}
	

}
