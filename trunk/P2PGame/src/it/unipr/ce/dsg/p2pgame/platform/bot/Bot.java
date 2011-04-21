package it.unipr.ce.dsg.p2pgame.platform.bot;

import java.util.ArrayList;

import it.unipr.ce.dsg.p2pgame.GUI.prolog.BuyResourceEngine;
import it.unipr.ce.dsg.p2pgame.GUI.prolog.ExtractionEngine;
import it.unipr.ce.dsg.p2pgame.GUI.prolog.MovementEngine;
import it.unipr.ce.dsg.p2pgame.GUI.prolog.VisibilityEngine;
import it.unipr.ce.dsg.p2pgame.platform.GamePeer;
import it.unipr.ce.dsg.p2pgame.platform.GameResourceEvolve;
import it.unipr.ce.dsg.p2pgame.platform.GameResourceMobile;

public class Bot implements Runnable{
	
	GamePeer gp;
	BuyResourceEngine bre;
	ExtractionEngine ee;
	MovementEngine me;
	VisibilityEngine ve;
	
	
	public Bot()
	{
		//this.gp=new GamePeer();
		//gp = new GamePeer(portMin+ 1 , portMin, 160, "", serverAddressTextField.getText().trim(), serverPort, portMin + 3, portMin + 2, serverAddressTextField.getText().trim(), serverPort+2, 4000,1000,64000,2000);
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
		
		double required_exploration_money=10000;		
		ArrayList<String> req_exp_res=new ArrayList<String>();
		req_exp_res.add("GameResource");
		req_exp_res.add("GameResourceMobile");		
		ArrayList<Integer> req_exp_qres=new ArrayList<Integer>();
		req_exp_qres.add(new Integer(2));
		req_exp_qres.add(new Integer(5));
		
		double required_conquest_money=20000;
		String required_conquest_resources= "[GameResource,GameResourceMobile]";
		ArrayList<String> req_conq_res=new ArrayList<String>();
		req_conq_res.add("GameResource");
		req_conq_res.add("GameResourceMobile");
		String required_conquest_qresources= "[3,10]";
		ArrayList<Integer> req_conq_qres=new ArrayList<Integer>();
		req_conq_qres.add(new Integer(3));
		req_conq_qres.add(new Integer(10));
		
		
		//poi entro nel ciclo infinito
		
		while(true)
		{
			try {
				Thread.sleep(1000);
				
				//ottengo da gp informazioni sulle risorse e i soldi a disposizione
				ArrayList<Object> res=gp.getMyResources(); // poi verifico le risorse sulla lista tranne moneyEvolve
				
				int s=res.size();
				int nrmobile=0;
				int nres=0;
				
				for(int i=0;i<s;i++)
				{
					
					Object obj=res.get(i);
					
					if(obj instanceof GameResourceMobile)
					{
						nrmobile++;
						
					}
					else if(!(obj instanceof GameResourceEvolve))
					{
						nres++;
						
					}					
					
				}
				
				
				//								
				GameResourceEvolve money=(GameResourceEvolve) gp.getMyResourceFromId("moneyEvolveble");
				double moneyvalue=money.getQuantity();
				
				// con queste informazioni interrogo al engine in quale fase del gioco mi trovo, e ricavo le prob di attacco e il raggio
				//di spostamento			
				
				//con una probilita' x decido se acquistare o meno una nuova risorsa
				//Questo controllo lo aggiungo in una nuova teoria, associata al profilo di giocatore (Fatto)
				
				//con una probabilita' y decido se spostare o no una risorsa mobile. Questa
				//operazione la effettuo per ogni risorsa mobile che possiedo
				
				//se decido di spostare una risorsa, devo scelgo in modo aleatorio il punto d'arrivo
				// il punto d'arrivo dipende del raggio massimo di spostamento scelto dalla fase del gioco
				
				
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		
		
	}

}
