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
import org.springfield.fs.FsPropertySet;
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
	private static SharedModel sharedmodel = new SharedModel();
	private AppModel amodel;
	private ScreenModel smodel;
	private static BindManager bindmanager;
	private static ModelEventManager eventmanager;
	
	/* screen/
 	domain/
	app/
	
	model.onPropertyInsert 
	model.onPropertyUpdate
	model.onPropertyDelete
	model.onNodeInsert
	model.onNodeUpdate
	model.onNodeDelete
	*/
	
	public Model(Screen s) {
		Html5ApplicationInterface app = s.getApplication();
		smodel = new ScreenModel(app,s); // answers the /screen/ calls
		imodel = app.getAppInstanceModel(); // answers the /instance/ calls
		amodel = app.getAppModel(); // answers the /app/ calls
		if (dmodel==null) dmodel = new DomainModel(); // answers the /domain/ calls
		if (bindmanager==null) bindmanager = new BindManager();
		if (eventmanager==null) eventmanager = new ModelEventManager();
		
	}
	
	public static ModelEventManager getEventManager() {
		return eventmanager;
	}
	
 	public void onPathUpdate(String paths,String methodname,Html5Controller callbackobject) {
 		//System.out.println("onPathUpdate "+paths+" "+methodname+" "+callbackobject);
 		bindmanager.onPathUpdate(paths, methodname, callbackobject);
	}
 	
 	public void onPropertyUpdate(String path,String methodname,Html5Controller callbackobject) {
		if (path.startsWith("/app/")) {
			eventmanager.onPropertyUpdate(path,methodname,callbackobject);
		} else if (path.startsWith("/screen/")) {
			eventmanager.onPropertyUpdate(path,methodname,callbackobject);
		} else if (path.startsWith("/domain/")) {
			eventmanager.onPropertyUpdate(path,methodname,callbackobject);
		}
 	}
 	
 	public void onPropertiesUpdate(String path,String methodname,Html5Controller callbackobject) {
		if (path.startsWith("/app/")) {
			eventmanager.onPropertiesUpdate(path,methodname,callbackobject);
		} else if (path.startsWith("/shared/")) {
			eventmanager.onPropertiesUpdate(path,methodname,callbackobject);
		}
 	}
	
	public boolean setProperties(String path,FsPropertySet properties) {
		if (path.startsWith("/app/")) {
			amodel.setProperties(path.substring(5),properties);
	   	 	eventmanager.setProperties(path, properties); // signal the others new code
	   	 	return true;
		} else 	if (path.startsWith("/shared/")) {
			sharedmodel.setProperties(path.substring(8),properties);
	   	 	eventmanager.setProperties(path, properties); // signal the others new code
	   	 	return true;
		}
		return false;
	}
	
	public boolean setProperty(String path,String value) {
		if (path.startsWith("/screen/"))  {
			smodel.setProperty(path.substring(8),value);
	   	 	eventmanager.setProperty(path, value); // signal the others new code
	   	 	return true;
		} else if (path.startsWith("/shared/"))  {
				sharedmodel.setProperty(path,value);
		   	 	eventmanager.setProperty(path, value); // signal the others new code
		   	 	return true;
		} else if (path.startsWith("/app/")) {
			amodel.setProperty(path.substring(5),value);
	   	 	bindmanager.setProperty(path, value); // signal the others old code
	   	 	eventmanager.setProperty(path, value); // signal the others new code
	   	 	return true;
		} else if (path.startsWith("/domain/")) {
			dmodel.setProperty(path.substring(8),value);
	   	 	eventmanager.setProperty(path, value); // signal the others new code
	   	 	return true;
		}
		return false;
	}
	
	public String getProperty(String path) {
		System.out.println("model -> getProperty("+path+")"+this);
		if (path.startsWith("/screen/")) {
			return smodel.getProperty(path.substring(8));
		} else  if (path.startsWith("/shared/")) {
				return sharedmodel.getProperty(path);
		} else if (path.startsWith("/app/")) {
			return amodel.getProperty(path.substring(5));
		}
		return null;
	}
	
	public FSList getList(String path) {
		if (path.startsWith("/shared")) { 
			return sharedmodel.getList(path);
		} else if (path.startsWith("/domain/")) { 
		//	return dmodel.getNode(path);
		}
		return null;
	}
	
	public FsNode getNode(String path) {
		System.out.println("MODEL="+path);
		if (path.startsWith("/app/")) { 
			return amodel.getNode(path);
		} else if (path.startsWith("/shared/")) { 
			return sharedmodel.getNode(path);
		} else if (path.startsWith("/domain/")) { 
			return dmodel.getNode(path);
		}
		return null;
	}
	
	public void putNode(String uri,FsNode node) {
		if (uri.startsWith("/app/") || uri.equals("/app")) { 
			amodel.putNode(uri,node);
		} else if (uri.startsWith("/shared")) { 
				sharedmodel.putNode(uri,node);
		} else if (uri.startsWith("/domain/")) { 
			System.out.println("PUTNODE NOT DONE YET FOR DOMAIN");
		}
	}
	
	public void observeNode(MargeObserver o,String url) {
		Marge.addObserver(url, o);
	}
	
	public void observeTree(MargeObserver o,String url) {
		Marge.addObserver(url+"*", o);
	}
	
	public boolean isMainNode(String path) {
		return Fs.isMainNode(path);
	}
	
	public boolean insertNode(FsNode node,String path) {
		//System.out.println("AMODE="+amodel);
		if (path.startsWith("/app/")) { 
		} else if (path.startsWith("/shared/")) { 
			return sharedmodel.putNode(path,node);
		} else if (path.startsWith("/domain/")) { 
			return Fs.insertNode(node, path);
		}
		return false;
	}
	
	
}
