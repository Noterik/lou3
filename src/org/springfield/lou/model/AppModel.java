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

public class AppModel extends MemoryModel  {
	
	private Html5Application app;
	
	//private Map<String, String> properties = new HashMap<String,String>();
	
	public AppModel(Html5Application a) {
		super();
		app = a;
		// create our master node in mojo
		//FSListManager.put("/app"+app.getId().substring(7), new FSList());
		loadAppConfig();		
	}
	
	public void loadAppConfig() {
		// lets find out what is the active version for this app
		String basepath = "/springfield/tomcat/webapps/ROOT/eddie/";
		if (LazyHomer.isWindows()) basepath = "C:\\springfield\\tomcat\\webapps\\ROOT\\eddie\\";
		String part = app.getAppname().substring(app.getAppname().lastIndexOf("/")+1);
		String filename = basepath+File.separator+"apps"+File.separator+part+File.separator+"components"+File.separator+"app.xml";
		File file = new File(filename);
		if (file.exists()) {
			try {
				BufferedReader br = new BufferedReader(new FileReader(filename));
				StringBuffer str = new StringBuffer();
				String line = br.readLine();
				while (line != null) {
					str.append(line);
					str.append("\n");
					line = br.readLine();
				}
				br.close();
				
				String body = str.toString();
				Document result = DocumentHelper.parseText(body);
				for(Iterator<Node> iter = result.getRootElement().nodeIterator(); iter.hasNext(); ) {
					
					Node child = (Node)iter.next();
					//System.out.println("C="+child.getName());
					if (child.getName()!=null && child.getName().equals("model")) {
						Element model = (Element)child;
						String modelid = model.attributeValue("id");	
						FsNode modelnode = new FsNode("model",modelid);
						for(Iterator<Node> iter2 = model.nodeIterator(); iter2.hasNext();) {
							Node child2 = (Node)iter2.next();
							String id = child2.getName();
							if(id!=null) {
								modelnode.setProperty(id,child2.getText());
							}
						}
						putNode("/app/component",modelnode);
						System.out.println("MODELNODE="+modelnode.asXML());
					} else if (child.getName()!=null && child.getName().equals("view")) {
						Element view = (Element)child;
						String viewid = view.attributeValue("id");	
						FsNode viewnode = new FsNode("view",viewid);
						putNode("/app/component",viewnode);
						
						for(Iterator<Node> iter2 = view.nodeIterator(); iter2.hasNext();) {
							Node child2 = (Node)iter2.next();
							String id = child2.getName();
							if(id!=null) {
								if (id.equals("controller")) {
									Element controller = (Element)child2;
									String controllerid = controller.attributeValue("id");	
									FsNode controllernode = new FsNode("controller",controllerid);
									putNode("/app/component/view/"+viewid,controllernode);
									for(Iterator<Node> iter3 = controller.nodeIterator(); iter3.hasNext();) {
										Node child3 = (Node)iter3.next();
										String id2 = child3.getName();
										if (id2!=null) {
											if (id2.equals("model")) {
												
											} else {
												controllernode.setProperty(id2,child3.getText());
											}
										}
									}
								} else {
									viewnode.setProperty(id,child2.getText());
								}
							}
						}
	
					}
				}
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	/*
	public void loadAppConfig() {
		// lets find out what is the active version for this app
		String basepath = "/springfield/tomcat/webapps/ROOT/eddie/";
		if (LazyHomer.isWindows()) basepath = "C:\\springfield\\tomcat\\webapps\\ROOT\\eddie\\";
		String part = app.getAppname().substring(app.getAppname().lastIndexOf("/")+1);
		String filename = basepath+File.separator+"apps"+File.separator+part+File.separator+"components"+File.separator+"app.xml";
		File file = new File(filename);
		if (file.exists()) {
			try {
				BufferedReader br = new BufferedReader(new FileReader(filename));
				StringBuffer str = new StringBuffer();
				String line = br.readLine();
				while (line != null) {
					str.append(line);
					str.append("\n");
					line = br.readLine();
				}
				br.close();
				
				String body = str.toString();
				Document result = DocumentHelper.parseText(body);
				for(Iterator<Node> iter = result.getRootElement().nodeIterator(); iter.hasNext(); ) {
					
					Node child = (Node)iter.next();
					//System.out.println("C="+child.getName());
					if (child.getName()!=null && child.getName().equals("view")) {
						Element view = (Element)child;
						String viewid = view.attributeValue("id");	
						FsNode viewnode = new FsNode("view",viewid);
						putNode("/app/component",viewnode);
						
						for(Iterator<Node> iter2 = view.nodeIterator(); iter2.hasNext();) {
							Node child2 = (Node)iter2.next();
							String id = child2.getName();
							if(id!=null) {
								if (id.equals("controller")) {
									Element controller = (Element)child2;
									String controllerid = controller.attributeValue("id");	
									FsNode controllernode = new FsNode("controller",controllerid);
									putNode("/app/component/view/"+viewid,controllernode);
									for(Iterator<Node> iter3 = controller.nodeIterator(); iter3.hasNext();) {
										Node child3 = (Node)iter3.next();
										String id2 = child3.getName();
										if (id2!=null) {
											if (id2.equals("model")) {
												
											} else {
												controllernode.setProperty(id2,child3.getText());
											}
										}
									}
								} else {
									viewnode.setProperty(id,child2.getText());
								}
							}
						}
	
					}
				}
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
	}
	*/
	
}
