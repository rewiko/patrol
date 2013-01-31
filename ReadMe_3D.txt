1) it.unipr.ce.dsg.p2pgame.GUI.RTSGameBootstrapServer  per avviare il server di bootstrap. Ok
 
2) it.unipr.ce.dsg.p2pgame.GUI3D.RTSGameMainPeer (con parametri es. "a 1167") per avviare il peer su cui posso giocare io. Dei parametri passati il primo è fake mentre il secondo è la porta del giocatore
 
3) it.unipr.ce.dsg.p2pgame.GUI3D.RTSGameMainBot (con parametri es. "a 2267") per avviare un bot che gioca autonomamente. Dei parametri passati il primo è fake mentre il secondo è la porta del giocatore-bot
 
4) it.unipr.ce.dsg.p2pgame.GUI3D.RTSGameGUI (con parametri "a true true true") per avviare un'interfaccia grafica che mostra la partita in corso. Dei parametri passati il primo è fake, gli altri per mostrare GUI, FPS e statistiche
 
 
 RTSGameMainBot deve essere lanciato con la stessa porta di RTSGameMainPeer. Nel caso di RTSGameGUI il primo parametro non è fake, ci siamo sbagliati nel caso dell'altra classe, determina se far apparire all'avvio la schermata di jmonkey per la scelta delle impostazioni grafiche.