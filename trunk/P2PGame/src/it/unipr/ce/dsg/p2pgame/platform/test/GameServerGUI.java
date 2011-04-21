/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * GameServerGUI.java
 *
 * Created on 8-set-2009, 11.30.49
 */

package it.unipr.ce.dsg.p2pgame.platform.test;

import java.io.IOException;

import javax.swing.JTable;
import javax.swing.table.TableColumn;

import it.unipr.ce.dsg.p2pgame.platform.GameServer;

/**
 *
 * @author stefanoSeb
 */
public class GameServerGUI extends javax.swing.JFrame {



	/**
	 *
	 */
	private static final long serialVersionUID = 1L;


	public void setRegisteredUserTable(javax.swing.JTable registeredUserTable) {
		this.registeredUserTable = registeredUserTable;
	}

	public javax.swing.JTable getRegisteredUserTable() {
		return registeredUserTable;
	}



    /** Creates new form GameServerGUI */
    public GameServerGUI() {
        initComponents();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    //@SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jTabbedPane1 = new javax.swing.JTabbedPane();
        registerUserTab = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        registeredUserTable = new javax.swing.JTable();
        jPanel1 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();



        //jTabbedPane1.setName("Game Server Test Monitor");

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);


        //TABELLA REGISTRATI
        registeredUserTable.setModel(new RegisteredUserTableModel());
        registeredUserTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        TableColumn col0 = registeredUserTable.getColumnModel().getColumn(0);
        int widthCol0 = 100;
        col0.setPreferredWidth(widthCol0);
        TableColumn col1 = registeredUserTable.getColumnModel().getColumn(1);
        int widthCol1 = 100;
        col1.setPreferredWidth(widthCol1);
        TableColumn col2 = registeredUserTable.getColumnModel().getColumn(2);
        int widthCol2 = 400;
        col2.setPreferredWidth(widthCol2);

        jScrollPane1.setViewportView(registeredUserTable);

        javax.swing.GroupLayout registerUserTabLayout = new javax.swing.GroupLayout(registerUserTab);
        registerUserTab.setLayout(registerUserTabLayout);
        registerUserTabLayout.setHorizontalGroup(
            registerUserTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(registerUserTabLayout.createSequentialGroup()
                .addGap(42, 42, 42)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 600, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(150, Short.MAX_VALUE))
        );
        registerUserTabLayout.setVerticalGroup(
            registerUserTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(registerUserTabLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 375, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(34, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab("Registered User", registerUserTab);

        jTable1.setModel(new RegisteredUserTableModel());
        jScrollPane2.setViewportView(jTable1);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(42, 42, 42)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 500, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(150, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 375, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(34, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab("tab2", jPanel1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jTabbedPane1)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jTabbedPane1)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
    * @param args the command line arguments
    */
    public static void main(String args[]) {

    	final GameServerGUI serverGUI = new GameServerGUI();
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                //gui = new GameServerGUI();
                serverGUI.setVisible(true);
            }
        });

		try {
			final GameServer gs = new GameServer(1234, 1235, 5, 160, 1111, 2222, 0,0,0, 10,10,10, 1,2,1);
			final RegisteredUserTableModel tm = new RegisteredUserTableModel();

			new Thread( new Runnable() {
				public void run() {
					try {
						while (true){

							Thread.sleep(5000);

					        serverGUI.getRegisteredUserTable().setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
					        TableColumn col0 = serverGUI.getRegisteredUserTable().getColumnModel().getColumn(0);
					        int widthCol0 = 120;
					        col0.setPreferredWidth(widthCol0);
					        TableColumn col1 = serverGUI.getRegisteredUserTable().getColumnModel().getColumn(1);
					        int widthCol1 = 120;
					        col1.setPreferredWidth(widthCol1);
					        TableColumn col2 = serverGUI.getRegisteredUserTable().getColumnModel().getColumn(2);
					        int widthCol2 = 459;
					        col2.setPreferredWidth(widthCol2);

					        serverGUI.getRegisteredUserTable().setModel(tm);
							tm.set_FileTableModel(gs.getRegisteredUser());




						}

					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

				}
			}
			).start();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTable registeredUserTable;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JTable jTable1;
    private javax.swing.JPanel registerUserTab;
    // End of variables declaration//GEN-END:variables



}