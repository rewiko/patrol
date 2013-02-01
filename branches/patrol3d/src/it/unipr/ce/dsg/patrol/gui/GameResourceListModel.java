package it.unipr.ce.dsg.patrol.gui;

import it.unipr.ce.dsg.patrol.gui.test.*;
import it.unipr.ce.dsg.patrol.platform.GameResource;
import it.unipr.ce.dsg.patrol.rtsdemo.*;

import java.util.ArrayList;

import javax.swing.AbstractListModel;

public class GameResourceListModel extends AbstractListModel {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	//private Object list[];
	private ArrayList<Object> list = new ArrayList<Object>();
	//private ArrayList<String> listNames = new ArrayList<String>();

	public GameResourceListModel(ArrayList<Object> l)
	{
		super();
		this.list = l;
	}

	public int getSize() {
		//return list.length;
		return list.size();
	}


	public /*Object*/ String getElementAt(int i) {
		//return list[i];
		//return list.get(i);
		return ((GameResource) list.get(i)).getId();
	}

	public Object getObjectAt(int i) {
		return list.get(i);
	}

	public void addElement(Object obj){
		//list[list.length] = obj;
		list.add(obj);
	}

	public void setList(ArrayList<Object> l){
		this.list = l;
	}
}
