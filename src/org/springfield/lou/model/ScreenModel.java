package org.springfield.lou.model;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
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

public class ScreenModel extends MemoryModel {
	
	/*
	private Html5ApplicationInterface app;
	
	private Map<String, String> screenproperties = new HashMap<String,String>();
	private Screen screen;
	
	public ScreenModel(Html5ApplicationInterface a,Screen s) {
		screen = s;
		app = a;
	}
	
	
	public boolean setProperty(String path,String value) {
		//System.out.println("screen model -> setProperty("+path+","+value+") "+this);
		screenproperties.put(path, value);
		// ok lets check if we also need to store it in the session object in smithers
		ArrayList<String> list = app.getRecoveryList();
		if (list.contains(path)) {
			// ok we need to store this for now just works for Strings
			Fs.setProperty(screen.getRecoveryId(), path, value.toString());
		}
		return true;
	}
	
	public String getProperty(String path) {
		//System.out.println("screen model -> getProperty("+path+")"+this);
		return screenproperties.get(path);
	}
	*/
	
}
