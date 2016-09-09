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
		//smodel = new ScreenModel(app,s); // answers the /screen/ calls
		smodel = new ScreenModel(); // answers the /screen/ calls
		imodel = app.getAppInstanceModel(); // answers the /instance/ calls
		amodel = app.getAppModel(); // answers the /app/ calls
		if (dmodel==null) dmodel = new DomainModel(); // answers the /domain/ calls
		if (eventmanager==null) eventmanager = new ModelEventManager();
		
	}
	
	public static ModelEventManager getEventManager() {
		return eventmanager;
	}
	
	
 	public void onNotify(String path,String methodname,Html5Controller callbackobject) {
		if (path.indexOf("[")!=-1) {
			path=xpathToFs(path);
		}
 		eventmanager.onNotify(path, methodname, callbackobject);
 		// signal the admin tool
 		FsNode node = new FsNode("bind","1");
 		node.setProperty("action","new onNotify");
 		node.setProperty("path",path);
 		node.setProperty("methodname",methodname);
 		notify("/shared/internal",node);
	}
 	
 	public void notify(String path,FsNode node) {
		if (path.indexOf("[")!=-1) {
			path=xpathToFs(path);
		}
 		eventmanager.notify(path,node);
	}
	
 	public void onPathUpdate(String path,String methodname,Html5Controller callbackobject) {
		if (path.indexOf("[")!=-1) {
			path=xpathToFs(path);
		}
 		eventmanager.onPathUpdate(path, methodname, callbackobject);
 		FsNode node = new FsNode("bind","1");
 		node.setProperty("action","new onPathUpdate");
 		node.setProperty("path",path);
 		node.setProperty("methodname",methodname);
 		notify("/shared/internal",node);
	}
	
 	
 	public void onPropertyUpdate(String path,String methodname,Html5Controller callbackobject) {
		if (path.indexOf("[")!=-1) {
			path=xpathToFs(path);
		}
		eventmanager.onPropertyUpdate(path,methodname,callbackobject);
 		FsNode node = new FsNode("bind","1");
 		node.setProperty("action","new onPropertyUpdate");
 		node.setProperty("path",path);
 		node.setProperty("methodname",methodname);
 		notify("/shared/internal",node);
 	}
 	
 	public void onPropertiesUpdate(String path,String methodname,Html5Controller callbackobject) {
		if (path.indexOf("[")!=-1) {
			path=xpathToFs(path);
		}
		eventmanager.onPropertiesUpdate(path,methodname,callbackobject);
 		FsNode node = new FsNode("bind","1");
 		node.setProperty("action","new onPropertiesUpdate");
 		node.setProperty("path",path);
 		node.setProperty("methodname",methodname);
 		notify("/shared/internal",node);
 	}
	
	public boolean setProperties(String path,FsPropertySet properties) {
		if (path.indexOf("[")!=-1) {
			path=xpathToFs(path);
		}
		if (path.startsWith("/app/")) {
			amodel.setProperties(path.substring(5),properties);
	   	 	eventmanager.setProperties(path, properties); // signal the others new code
	   	 	return true;
		} else if (path.startsWith("/screen/")) {
				smodel.setProperties(path.substring(5),properties);
		   	 	eventmanager.setProperties(path, properties); // signal the others new code
		   	 	return true;
		} else 	if (path.startsWith("/shared/")) {
			sharedmodel.setProperties(path,properties);
	   	 	eventmanager.setProperties(path, properties); // signal the others new code
	   	 	return true;
		}
		return false;
	}
	
	public boolean setProperty(String path,String value) {
		if (path.indexOf("[")!=-1) {
			path=xpathToFs(path);
		}
		if (path.startsWith("/screen/"))  {
			//smodel.setProperty(path.substring(8),value);
			smodel.setProperty(path,value);
	   	 	eventmanager.setProperty(path, value); // signal the others new code
	   	 	return true;
		} else if (path.startsWith("/shared/"))  {
				sharedmodel.setProperty(path,value);
		   	 	eventmanager.setProperty(path, value); // signal the others new code
		   	 	return true;
		} else if (path.startsWith("/app/")) {
			amodel.setProperty(path,value);
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
		if (path.indexOf("[")!=-1) {
			path=xpathToFs(path);
		}
		if (path.startsWith("/screen/")) {
			//return smodel.getProperty(path.substring(8));
			return smodel.getProperty(path);
		} else  if (path.startsWith("/shared/")) {
				return sharedmodel.getProperty(path);
		} else if (path.startsWith("/app/")) {
			return amodel.getProperty(path);
		}
		return null;
	}
	
	public FSList getList(String path) {
		if (path.indexOf("[")!=-1) {
			path=xpathToFs(path);
		}
		if (path.startsWith("/shared")) { 
			return sharedmodel.getList(path);
		} else if (path.startsWith("/screen")) { 
				return smodel.getList(path);
		} else if (path.startsWith("/app")) { 
			return amodel.getList(path);
		} else if (path.startsWith("/domain/")) { 
			return FSListManager.get(path,false); // need to support cache calls
		}
		return null;
	}
	
	public FsNode getNode(String path) {
		if (path.indexOf("[")!=-1) {
			path=xpathToFs(path);
		}
		if (path.startsWith("/app/")) { 
			return amodel.getNode(path);
		} else if (path.startsWith("/shared/")) { 
			return sharedmodel.getNode(path);
		} else if (path.startsWith("/screen/")) { 
			return smodel.getNode(path);
		} else if (path.startsWith("/domain/")) { 
			return dmodel.getNode(path);
		}
		return null;
	}
	
	public void putNode(String uri,FsNode node) {
		if (uri.indexOf("[")!=-1) {
			uri=xpathToFs(uri);
		}
		if (uri.startsWith("/app/") || uri.equals("/app")) { 
			amodel.putNode(uri,node);
		} else if (uri.startsWith("/shared")) { 
				sharedmodel.putNode(uri,node);
		} else if (uri.startsWith("/screen")) { 
			smodel.putNode(uri,node);
		} else if (uri.startsWith("/domain/")) { 
			System.out.println("PUTNODE NOT DONE YET FOR DOMAIN");
		}
	}
	
	
	public void observeNode(MargeObserver o,String url) { // need to be removed
		Marge.addObserver(url, o);
	}
	
	public void observeTree(MargeObserver o,String url) {
		Marge.addObserver(url+"*", o);
	}
	
	
	public boolean isMainNode(String path) {
		return Fs.isMainNode(path);
	}
	
	public boolean insertNode(FsNode node,String path) { // needs to be removed?
		if (path.indexOf("[")!=-1) {
			path=xpathToFs(path);
		}
		if (path.startsWith("/app/")) { 
		} else if (path.startsWith("/shared/")) { 
			return sharedmodel.putNode(path,node);
		} else if (path.startsWith("/domain/")) { 
			return Fs.insertNode(node, path);
		}
		return false;
	}
	
	public String FsToXpath(String input) {
		// //app[@id='remotepointer']/position
		return null;
	}
	
	public String xpathToFs(String input) {
		String result = "";
		input =  input.substring(1);
		String tags[] = input.split("/");
		for (int i=0;i<tags.length;i++) {
			String tag =  tags[i];
			int pos = tag.indexOf("['");
			if (pos!=-1){
				String type = tag.substring(0,pos);
				System.out.println("TYPE="+type);
				int pos2=tag.indexOf("']");
				String id = tag.substring(pos+2,pos2);
				if (id.equals("")) id="default";
				System.out.println("ID="+id);
				result+="/"+type+"/"+id;
			} else {
				result+="/"+tag;
			}
		}
		System.out.println("CPATH="+result);
		return result;
	}
	
	
}
