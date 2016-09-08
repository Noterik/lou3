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

public class MemoryModel  {
	
	private FsNode root = new FsNode("root","memory");
	
	public MemoryModel() {
	}
	
	public FSList getList(String path) {
		if (path.equals("/shared")) return root.getChildList();
		String[] steps = path.substring(1).split("/");
		int stepc = 0;
		int stept = steps.length;
		FsNode current = root;
		
		FsNode snode = null;
		while ((stepc+1)<stept) {
			snode = current.getChild(steps[stepc+1]);
			if (snode==null) {
				return null;
			}

			stepc=stepc+2;
			current = snode;
		}
		if (current!=null) {
			if (stept==stepc) {
				return current.getChildList();
			} else {
				return current.getNamedChildList(steps[stepc]);
			}
		}
		return null;
	}
	

	
	public boolean setProperty(String path,String value) {
		int pos =  path.lastIndexOf("/");
		if (pos!=-1) {
			String nodepath = path.substring(0,pos);
			String name = path.substring(pos+1);
			FsNode node  = getNode(nodepath);
			if (node!=null && value!=null) {
				node.setProperty(name, value);	
				return true;
			} 
		}
		return false;
	}
	
	

	public String getProperty(String path) {
		int pos =  path.lastIndexOf("/");
		if (pos!=-1) {
			String nodepath = path.substring(0,pos);
			String name = path.substring(pos+1);
			FsNode node  = getNode(nodepath);
			if (node!=null) {
				return node.getProperty(name);	
			} 
		}
		return null;
	}
	
	
	public boolean setProperties(String path,FsPropertySet set) {
		//System.out.println("SETPROPS="+path);
		FsNode node  = getNode(path);
		if (node!=null) {
			//System.out.println("SETPROPS2="+node);
			for(Iterator<String> iter = set.getKeys() ; iter.hasNext(); ) {
				String key = (String)iter.next();
				String value = set.getProperty(key);
				node.setProperty(key, value);
			}
			return true;
		} else {
			int pos = path.lastIndexOf("/");
			if (pos==-1) return false;
			String idpart=path.substring(pos+1);
			path=path.substring(0,pos);
			pos = path.lastIndexOf("/");
			if (pos==-1) return false;
			String namepart=path.substring(pos+1);
			path=path.substring(0,pos);
			//System.out.println("PATH="+path+" NAME="+namepart+" ID="+idpart);
			
			node  = new FsNode(namepart,idpart);
			for(Iterator<String> iter = set.getKeys() ; iter.hasNext(); ) {
				String key = (String)iter.next();
				String value = set.getProperty(key);
				node.setProperty(key, value);
			}
			putNode("/shared/pointer", node);
		}
		return false;
	}
	
	
	
	public boolean putNode(String path,FsNode node) {
		//System.out.println("PUTNODE="+path+" node="+node.asXML());
		String[] steps = path.substring(1).split("/");
		int stepc = 0;
		int stept = steps.length;
		FsNode current = root;
		
		while ((stepc+1)<stept) {
			FsNode snode = current.getChild(steps[stepc+1]);
			if (snode==null) { // create the node in its path if needed ( like mkdirs() )
				snode = new FsNode(steps[stepc],steps[stepc+1]);
				current.addNode(snode);
			} 
			stepc=stepc+2;
			current = snode;
		}
		current.addNode(node);
		return true; // needs work
	}
	

	
	public FsNode getNode(String path) {
		String[] steps = path.substring(1).split("/");
		int stepc = 0;
		int stept = steps.length;
		FsNode current = root;		
		
		while ((stepc+1)<stept) {
			FsNode snode = current.getChild(steps[stepc+1]);
			if (snode==null) { // create the node in its path if needed ( like mkdirs() )
				snode = new FsNode(steps[stepc],steps[stepc+1]);
				current.addNode(snode);
			} 
			stepc=stepc+2;
			current = snode;
		}
		return current;
	}
	
	
	
}
