package org.springfield.lou.model;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Node;
import org.springfield.fs.FSList;
import org.springfield.fs.FSListManager;
import org.springfield.fs.Fs;
import org.springfield.fs.FsNode;
import org.springfield.lou.application.Html5Application;
import org.springfield.lou.application.Html5ApplicationInterface;
import org.springfield.lou.controllers.FsListController;
import org.springfield.lou.controllers.Html5Controller;
import org.springfield.lou.homer.LazyHomer;
import org.springfield.lou.screen.Screen;
import org.springfield.marge.*;

public class Model {
	
	private Html5Application app;
	
	private Map<String, String> screenproperties = new HashMap<String,String>();
	private AppInstanceModel imodel;
	private static DomainModel dmodel;
	private AppModel amodel;
	private ScreenModel smodel;
	
	public Model(Screen s) {
		Html5ApplicationInterface app = s.getApplication();
		smodel = new ScreenModel(app,s); // answers the /screen/ calls
		imodel = app.getAppInstanceModel(); // answers the /instance/ calls
		amodel = app.getAppModel(); // answers the /app/ calls
		if (dmodel==null) dmodel = new DomainModel(); // answers the /domain/ calls
	}
	
	
	public boolean setProperty(String path,String value) {
		System.out.println("model -> setProperty("+path+","+value+") "+this);
		if (path.startsWith("/screen/")) return smodel.setProperty(path.substring(8),value);
		return true;
	}
	
	public String getProperty(String path) {
		System.out.println("model -> getProperty("+path+")"+this);
		if (path.startsWith("/screen/")) return smodel.getProperty(path.substring(8));
		return null;
	}
	
	public FsNode getNode(String path) {
		System.out.println("AMODE="+amodel);
		if (path.startsWith("/app/")) { 
			return amodel.getNode(path);
		} else if (path.startsWith("/domain/")) { 
			return dmodel.getNode(path);
		}
		return null;
	}
	
	public void putNode(String uri,FsNode node) {
		
	}
	
	public void observeNode(MargeObserver o,String url) {
		Marge.addObserver(url, o);
	}
	
	public void observeTree(MargeObserver o,String url) {
		Marge.addObserver(url+"*", o);
	}
	
	
}
