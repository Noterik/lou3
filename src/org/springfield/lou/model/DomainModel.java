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

public class DomainModel  {
	
	private Html5Application app;
	
	private Map<String, String> screenproperties = new HashMap<String,String>();
	
	public DomainModel() {
	}
	
	
	public boolean setProperty(String path,String value) {
		int pos=path.lastIndexOf("/");
    	String propertyname = path.substring(pos+1);
    	path = path.substring(0,pos);
		Fs.setProperty("/domain/"+path,propertyname,value);

		return true;
	}
	
	public String getProperty(String path) {
		int pos=path.lastIndexOf("/");
    	String propertyname = path.substring(pos+1);
    	path = path.substring(0,pos);
    	FsNode node = Fs.getNode("/domain/"+path);
    	if  (node!=null) {
    		return node.getProperty(propertyname);
    	}
    	return null;
	}
	
	public FsNode getNode(String uri) {
			return Fs.getNode(uri);
	}
	
}
