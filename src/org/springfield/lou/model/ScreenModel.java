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
	
	String recoverykey;
	ArrayList<String> recoverylist;
	
	public ScreenModel() {
	}
	public void setRecoveryList(ArrayList<String> l) {
		recoverylist = l;
	}
	
	public void setRecoveryKey(String r) {
		recoverykey = r;
	}
	
	public boolean setProperty(String path,String value) {
		if (recoverylist.contains(path.substring(8))) {
			String spath = path.replace("/","_");
			// ok we need to store this for now just works for Strings
			Fs.setProperty(recoverykey,path.substring(8), value.toString());
		}
		return super.setProperty(path, value);
	}
}
