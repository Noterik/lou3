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

public class SharedModel  {
	
	private Map<String, HashMap<String,String>> sharedspaces = new HashMap<String,HashMap<String,String>>();
	
	public SharedModel() {
	}
	

	/*
	public boolean setProperty(String path,String value) {
		//System.out.println("app model -> setProperty("+path+","+value+") "+this);
		properties.put(path, value);
		return true;
	}
	*/
	
	/*
	public String getProperty(String path) {
		//System.out.println("app model -> getProperty("+path+")"+this);
		return properties.get(path);
	}
	*/
	
	public boolean setProperties(String sharedpath,FsPropertySet set) {
		int pos = sharedpath.indexOf("/");
		String spacename = sharedpath.substring(0,pos);
		String path = sharedpath.substring(pos+1);
		//System.out.println("SPACENAME="+spacename);
		//System.out.println("PATH="+path);
		/*
		for(Iterator<String> iter = set.getKeys() ; iter.hasNext(); ) {
			String key = (String)iter.next();
			String value = set.getProperty(key);
			properties.put(path+"/"+key, value);
		}
		*/
		return true;
	}
	
	
	/*
	public void putNode(String uri,FsNode node) {
		if (uri.equals("/app")) {
			String listurl = "/app"+app.getId().substring(7);

			FSList list = FSListManager.get(listurl);
			if (list==null) {
				
				list = new FSList(listurl);
				FSListManager.put(listurl, list);
			}
			list.addNode(node);
		} else if (uri.startsWith("/app/")) {
			String listurl = "/app"+app.getId().substring(7)+uri.substring(4);
			FSList list = FSListManager.get(listurl);
			if (list==null) {
				list = new FSList(listurl);
				FSListManager.put(listurl, list);
			}
			list.addNode(node);
			
		}
	}
	*/

	/*
	public FsNode getNode(String uri) {
			// memory app
		//System.out.println("AMODEL GET="+uri);
			String listurl = "/app"+app.getId().substring(7)+uri.substring(4);
			int pos = listurl.lastIndexOf("/");
			String id = listurl.substring(pos+1);
			listurl = listurl.substring(0, pos);
			pos = listurl.lastIndexOf("/");
			String name = listurl.substring(pos+1);
			listurl = listurl.substring(0, pos);
			FSList list = FSListManager.get(listurl);
			if (list!=null) {
				for(Iterator<FsNode> iter = list.getNodes().iterator() ; iter.hasNext(); ) {
					FsNode n = (FsNode)iter.next();	
					if (n.getId().equals(id) && n.getName().equals(name)) {
						return n;
					}
				}
			}
		return null;
	}
	*/
	
	
}
