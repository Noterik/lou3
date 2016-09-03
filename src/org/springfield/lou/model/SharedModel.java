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
	
	private FsNode root = new FsNode("root","memory");
	
	public SharedModel() {
		FsNode testnode = new FsNode("video","1");
		putNode("/shared/testspace",testnode);
		testnode = new FsNode("soccer","1");
		putNode("/shared/games",testnode);
	}
	
	public FSList getList(String path) {
		System.out.println("SHARED GETLIST2="+path);
		if (path.equals("/shared")) return root.getChildList();
		String[] steps = path.substring(1).split("/");
		int stepc = 0;
		int stept = steps.length;
		System.out.println("STEPC="+stepc+" STEPT="+stept);
		FsNode current = root;
		
		FsNode snode = null;
		while ((stepc+1)<stept) {
			System.out.println("GET ON="+steps[stepc+1]);
			snode = current.getChild(steps[stepc+1]);
			System.out.println("GET RETURN="+snode);
			if (snode==null) {
				return null;
			} else {
				System.out.println("SNODE FOUND="+snode.asXML());
			}
			stepc=stepc+2;
			current = snode;
			System.out.println("STEPC="+stepc+" STEPT="+stept);
			System.out.println("SNODE="+current.asXML());
		//	return current.getChildList();
		}
		if (current!=null) {
			if (stept==stepc) {
				System.out.println("RESULT LIST="+current.getChildList());
				return current.getChildList();
			} else {
				System.out.println("RETURN NAMES LIST="+steps[stepc]);
				return current.getNamedChildList(steps[stepc]);
			}
		}
		return null;
	}
	

	
	public boolean setProperty(String path,String value) {
		System.out.println("shared model -> setProperty("+path+","+value+") "+this);
		int pos =  path.lastIndexOf("/");
		if (pos!=-1) {
			String nodepath = path.substring(0,pos);
			String name = path.substring(pos+1);
			System.out.println("shared model -> nodepath="+nodepath+" name="+name+") "+this);
			FsNode node  = getNode(nodepath);
			if (node!=null) {
				node.setProperty(name, value);	
				return true;
			} 
		}
		return false;
	}
	
	

	public String getProperty(String path) {
		System.out.println("shared model -> getProperty("+path+") "+this);
		int pos =  path.lastIndexOf("/");
		if (pos!=-1) {
			String nodepath = path.substring(0,pos);
			String name = path.substring(pos+1);
			System.out.println("shared model -> nodepath="+nodepath+" name="+name+") "+this);
			FsNode node  = getNode(nodepath);
			if (node!=null) {
				return node.getProperty(name);	

			} 
		}
		return null;
	}
	
	
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
	
	
	
	public boolean putNode(String path,FsNode node) {
		System.out.println("PUT NODE PATH="+path+" node="+node.asXML());
		String[] steps = path.substring(1).split("/");
		int stepc = 0;
		int stept = steps.length;
		FsNode current = root;
		
		while ((stepc+1)<stept) {
		//	System.out.println("SNODE="+current.asXML()+" ID="+steps[stepc+1]);
			FsNode snode = current.getChild(steps[stepc+1]);
			if (snode==null) { // create the node in its path if needed ( like mkdirs() )
				snode = new FsNode(steps[stepc],steps[stepc+1]);
			//	System.out.println("MKDIRNODE="+snode.asXML());
				current.addNode(snode);
			} 
			stepc=stepc+2;
			current = snode;
			//System.out.println("STEPC="+stepc+" STEPT="+stept);
			//System.out.println("SNODE="+current.asXML());
		}
		System.out.println("ADDNODE="+node.asXML()+" TO NODE="+current.asXML());
		current.addNode(node);
		return true; // needs work
	}
	

	
	public FsNode getNode(String path) {
		System.out.println("SHARED GETNODE="+path);
		String[] steps = path.substring(1).split("/");
		int stepc = 0;
		int stept = steps.length;
		System.out.println("STEPC="+stepc+" STEPT="+stept);
		FsNode current = root;
		
		FsNode snode = null;
		while ((stepc+1)<stept) {
			System.out.println("GET ON="+steps[stepc+1]);
			snode = current.getChild(steps[stepc+1]);
			System.out.println("GET RETURN="+snode);
			if (snode==null) { // create the node in its path if needed ( like mkdirs() )
				return null;
			} else {
				System.out.println("SNODE FOUND="+snode.asXML());
			}
			stepc=stepc+2;
			current = snode;
			System.out.println("STEPC="+stepc+" STEPT="+stept);
			System.out.println("SNODE="+current.asXML());
		//	return current.getChildList();
		}
		System.out.println("RESULT NODE="+current.asXML());
		return current;
	}
	
	
	
}
