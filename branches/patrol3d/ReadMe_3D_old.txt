1) it.unipr.ce.dsg.patrol.gui.RTSGameBootstrapServer  per avviare il server di bootstrap. Ok
 
2) it.unipr.ce.dsg.patrol.gui3d.RTSGameMainPeer (con parametri es. "a 1167") per avviare il peer su cui posso giocare io. Dei parametri passati il primo è fake mentre il secondo è la porta del giocatore
 
3) it.unipr.ce.dsg.patrol.gui3d.RTSGameMainBot (con parametri es. "a 2267") per avviare un bot che gioca autonomamente. Dei parametri passati il primo è fake mentre il secondo è la porta del giocatore-bot
 
4) it.unipr.ce.dsg.patrol.gui3d.RTSGameGUI (con parametri "a true true true") per avviare un'interfaccia grafica che mostra la partita in corso. Dei parametri passati il primo è fake, gli altri per mostrare GUI, FPS e statistiche
 
 
 RTSGameMainBot deve essere lanciato con la stessa porta di RTSGameMainPeer. Nel caso di RTSGameGUI il primo parametro non è fake, ci siamo sbagliati nel caso dell'altra classe, determina se far apparire all'avvio la schermata di jmonkey per la scelta delle impostazioni grafiche.
 
 
1.Server
2.HumanMainGamePeer
3.BotMainGamePeer
4.GIU3D
5.MyBot1
In GUI3D ci sono 4 argomenti d passare, in modalità di sviluppo lo facciamo partire con tutti a false, nella modalità di gioco vero e proprio bisogna mettere solo il secondo a true. 


Una volta avviato, cliccando sulla sinistra si possono comprare difese e astronavi. Nel pianeta una difesa di valore minimo è già presente, è l'anello grigio che gira attorno. Puoi comprarne delle altre ma al momento il numero che viene visualizzato nella gui non è corretto perché non abbiamo ancora scritto il codice per aggiornarlo. Comunque la risorsa viene acquistata e i soldi calano. Comprando un'astronave e dopo che è uscita da sola, arrivando nella casella a fianco del pianeta bisogna selezionarla con il sinistro, verrà visualizzata un anello bianco/azzurro attorno (a volte bisogna fare attenzione nel cercare di selezionarla perché capita che non prenda bene la posizione del mouse, soprattutto a distanza elevata). Per muovere l'astronave bisogna cliccare su un punto della griglia con il destro. Tenendo premuto SHIFT e premendo con il destro si può fare un tracciato multi-point, ossia con più punti in cui l'astronave deve passare.



launch server -> it.unipr.ce.dsg.patrol.gui.RTSGameBootstrapServer

For each game node:

launch Game backend -> it.unipr.ce.dsg.patrol.gui3d.RTSHumanMainGamePeer (with parameter that specifies the port on which it is listening for GUI message) 
launch Game frontend (the 3d graphic) -> it.unipr.ce.dsg.patrol.RTSGameGUI (4 parameters related to GUI) + 1 paramter that specifies the port to be used for communicate with the GamePeer (for internal communication)