/* 
* Screen.java
* 
* Copyright (c) 2012 Noterik B.V.
* 
* This file is part of Lou, related to the Noterik Springfield project.
*
* Lou is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* Lou is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with Lou.  If not, see <http://www.gnu.org/licenses/>.
*/

package org.springfield.lou.screen;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.*;

import org.dom4j.Node;
import org.json.simple.JSONObject;
import org.springfield.fs.Fs;
import org.springfield.fs.FsNode;
import org.springfield.lou.application.*;
import org.springfield.lou.controllers.Html5Controller;
import org.springfield.lou.homer.LazyHomer;
import org.springfield.lou.model.Model;
import org.springfield.lou.performance.PerformanceManager;
import org.springfield.lou.tools.JavascriptInjector;
import org.springfield.lou.websocket.LouWebSocketConnection;
import org.springfield.mojo.interfaces.ServiceInterface;
import org.springfield.mojo.interfaces.ServiceManager;



/**
 * Screen
 * 
 * @author Daniel Ockeloen
 * @copyright Copyright: Noterik B.V. 2012
 * @package org.springfield.lou.screen
 *
 */

public class Screen {
	
	private String id;
	private String shortid;
	private String recoveryid;
	private String role = "unknown";
	private Capabilities capabilities;
	private Html5ApplicationInterface app;
	private String language = null;
	private String data = null;
	private long lastseen = -1;
	private int messagecount = 0;
	private String username = null;
	private Map<String, String[]> params;
	private Map<String, Object> properties;
	private Map<String,Html5Element> html5elements = new HashMap<String,Html5Element>();
	private ArrayList<Html5Controller> controllers = new ArrayList<Html5Controller>();
	private ArrayList<String> csscache = new ArrayList<String>();
  //  protected Map<String, String> callbackmethods = new HashMap<String, String>();
  //  protected Map<String, Object> callbackobjects = new HashMap<String, Object>();
    private Map<String, HashMap<String,PathBindObject>> pathbindobjects = new HashMap<String, HashMap<String,PathBindObject>>();
    protected Map<String, ArrayList<String>> bindoverrides = new HashMap<String, ArrayList<String>>();
    private Model model;
    private List<String> sendtemplates = new ArrayList<String>();
    private LouWebSocketConnection websocketconnection;
    
    protected Map<String, ArrayList<PropertyBindObject>> propertybindobjects = new HashMap<String, ArrayList<PropertyBindObject>>();
	
	/**
	 * Constractor for Screen class
	 * @param id the desired id for this screen
	 * @param caps the capabilities object associated with this screen
	 */
	public Screen(Html5ApplicationInterface a,Capabilities caps,String id){
		this.id = id;
		this.capabilities = caps;
		int pos = id.indexOf("/screen/")+8;
		this.shortid=id.substring(pos);
		this.app = a;
		this.properties = new HashMap<String, Object>();
	    model = new Model(this);


		
		// so some session recovery, only allow sessions per user !!!
		if (a.getSessionRecovery()) {
			String sid = caps.getCapability("smt_sessionid");

			String appuser = a.getFullId();
			pos = appuser.indexOf("/user/");
			if (pos!=-1) {
				appuser = appuser.substring(pos+6);
				appuser = appuser.substring(0, appuser.indexOf("/"));
				FsNode n = Fs.getNode("/domain/"+a.getDomain()+"/session/"+appuser);
				if (n==null) {
						FsNode sessionnode = new FsNode("session",appuser);
						Fs.insertNode(sessionnode,"/domain/"+a.getDomain());
				}
				recoveryid = "/domain/"+a.getDomain()+"/session/"+appuser+"/"+a.getAppname()+"/"+sid;
				FsNode n2 = Fs.getNode(recoveryid);
				if (n2==null) {
					n2 = new FsNode(a.getAppname(),sid);
					Fs.insertNode(n2, "/domain/"+a.getDomain()+"/session/"+appuser);
				}
				// ok lets look at the recovery list and see what to load back into the screen
				ArrayList<String> list = app.getRecoveryList();
			    model.setRecoveryKey(recoveryid);
			    model.setRecoveryList(app.getRecoveryList());			    
				for(Iterator<String> iter = list.iterator(); iter.hasNext(); ) {
					String name =  iter.next();
					String value = n2.getProperty(name.replace("/", "_"));
					if (value!=null) {
						model.setProperty("/screen/"+name, value); // put it back for now just String work !
						
					}
				}
			}
		} else {
			System.out.println("APP STARTED IN ROOT NOT AS A USER needs to be in /user/[name]/ "+a.getFullId());
		}
		//this.cm = new ComponentManager();
		setSeen();
	}
	
	public Model getModel() {
		return model;
	}
	
	public void event(String from,String key,JSONObject data) {
		//System.out.println("EVENT="+data+" KEY="+key);
		HashMap<String,PathBindObject> binds = pathbindobjects.get(key);
		//System.out.println("BB="+binds);
		if (binds==null) return;
		
		Set<String> keys = binds.keySet();
		Iterator<String> it = keys.iterator();
		while(it.hasNext()){
			String next = it.next();
			PathBindObject bind = binds.get(next);
			String methodname = bind.method;
			// now find back the object on the screen based on its screenid and selector
			if (bind.screenid==null) { // must be a bindAndStore call
				mapAndStore(methodname,data);
			} else {
				Screen s=this.getApplication().getScreenManager().get(bind.screenid);
				if (s!=null) {
					Html5Element el = s.get(bind.selector);
					if (el!=null) {
						Object object = el.getController();
						//System.out.println("methodname="+methodname+" object="+object+" selector="+bind.selector);
						if (object!=null) {
							try {
								Method method = object.getClass().getMethod(methodname,Screen.class,JSONObject.class);
								if (method!=null) {	
									Screen fs = app.getScreen(from);
									method.invoke(object,fs,data);
								} else {
									System.out.println("MISSING METHOD IN APP ="+method);
								}
							} catch(Exception e) {
								e.printStackTrace();
							}
						}
					}
				}
			}
		}	 
	}
	
	private void mapAndStore(String path,JSONObject data) {
		//System.out.println("DATA="+data.toJSONString()+" s="+data.size());
		if (data.size()==1) {
			for (Object key : data.keySet()) {
				Object value = data.get(key);
				model.setProperty(path+"/"+key,""+value);
				break; // we only have one
			}
		} else {
			for (Object key : data.keySet()) {
				Object value = data.get(key);
				model.setProperty(path+"/"+key,""+value);
				break; // we only have one
			}	
		}
		
	}
	
	public String getRecoveryId() {
		return recoveryid;
	}

	
	public void setParameters(Map<String,String[]> p) {
		params = p;
	}
	
	public String getParameter(String name) {
		String[] values = params.get(name);
		if (values!=null) {
			return values[0];
		} else {
			return null;
		}
	}
	
	public Html5ApplicationInterface getApplication() {
		return app;
	}
	
	public Map<String, String[]> getParameters() {
		return params;
	}
	
	/*
	public void setProperty(String key, Object value){
		properties.put(key, value);
		// ok lets check if we also need to store it in the session object in smithers
		ArrayList<String> list = app.getRecoveryList();
		if (list.contains(key)) {
			// ok we need to store this for now just works for Strings
			Fs.setProperty(recoveryid, key, value.toString());
		}
		
		ArrayList<PropertyBindObject> binds = propertybindobjects.get(key);
		if (binds!=null) {
			for (int i=0;i<binds.size();i++) {
				PropertyBindObject bind  = binds.get(i);
				String methodname = bind.method;
				Object object = bind.object;
				try {
					Method method = object.getClass().getMethod(methodname,Screen.class,String.class,String.class);
					if (method!=null) {
						method.invoke(object,this,key,value);
					} else {
						System.out.println("MISSING METHOD IN APP ="+method);
					}
				} catch(Exception e) {
					e.printStackTrace();
				}
			}
		}
		
	
	}

	
	
	public Object getProperty(String key){
		return properties.get(key);
	}
	*/
	
	
	public void setSeen() {
		lastseen = new Date().getTime();	
	}
	
	public void setRole(String r) {
		this.role = r;	
	}
	
	public String getRole() {
		return role;	
	}
	
	public String getId() {
		return this.id;
	}
	
	
	public String getShortId() {
		return this.shortid;
	}
	
	public String getUserName() {
		return username;
	}	
	
	public long getLastSeen() {
		return lastseen;
	}
	
	public void put(String from,String content) {
		app.putOnScreen(this,from, content);
	}
	
	public void webSocketPut(String from,String content) {
		app.putOnScreen(this,from, content);
	}
	

	/**
	 * Assigns capabilities the screen
	 * @param caps the capabilities object to be associated with this screen
	 */
	public void setCapabilities(Capabilities caps){
		this.capabilities = caps;
	}
	
	public Capabilities getCapabilities(){
		return capabilities;
	}
	
	/**
	 * Sets data to be sent to the screen
	 * @param data the data to be sent
	 */
	public void setContent(String t,String c){
		messagecount++;
		if (websocketconnection!=null) {
			websocketconnection.send("set("+t+")="+c);
		} else {
			if (data==null) {
				data = "set("+t+")="+c;
			} else {
				data += "($end$)set("+t+")="+c;
			}
			synchronized (this) {
				this.notify();
			}
		}
	}
	
	public void setDiv(String t,String p) {
		messagecount++;
		if (websocketconnection!=null) {
			websocketconnection.send("sdiv("+t+")="+p);
		} else {
			if (data==null) {
				data = "sdiv("+t+")="+p;
			} else {
				data += "($end$)sdiv("+t+")="+p;
			}
			synchronized (this) {
				this.notify();
			}
		}
	}
	
	public void setDiv(String t,String p,String m) {
		setDiv(t,p,m,app.getClass());
	}
	
	public void setDiv(String t,String p,String m,Object o) {
		int pos = p.indexOf("bind:");
		if (pos!=-1) {
			String  name = t+"/"+p.substring(pos+5);
		//	System.out.println("OSETDIV="+o);
			app.setCallback(name,m,o);
			setDiv(t,p);
		}
	}
	
	/**
	 * Sets data to be sent to the screen
	 * @param data the data to be sent
	 */
	public void addContent(String t,String c){
		messagecount++;
		if (websocketconnection!=null) {
			websocketconnection.send("add("+t+")="+c);
		} else {
			if (data==null) {
				data = "add("+t+")="+c;
			} else {
				data += "($end$)add("+t+")="+c;
			}
			synchronized (this) {
				this.notify();
			}
		}
	}
	
	public void setScript(String t,String c){
		messagecount++;
		if (websocketconnection!=null) {
			websocketconnection.send("setscript("+t+")="+c);
		} else {
			if (data==null) {
				data = "setscript("+t+")="+c;
			} else {
				data += "($end$)setscript("+t+")="+c;
			}
			synchronized (this) {
				this.notify();
			}
		}
	}
	
	/**
	 * Sets data to be sent to the screen
	 * @param data the data to be sent
	 */
	public void removeContent(String t, Html5ApplicationInterface app){
		removeContent(t, false, app);
	}
	
	public void remove(String t) {
		removeContent(t.substring(1));
	}
	
	public void removeContent(String t){
		removeContent(t, false, getApplication());
	}
	
	public void removeContent(String t, boolean leaveElement, Html5ApplicationInterface app){
		messagecount++;
		if (websocketconnection!=null) {
			websocketconnection.send("remove("+t+"," + leaveElement + ")");
		} else {
			if (data==null) {
				data = "remove("+t+"," + leaveElement + ")";
			} else {
				data += "($end$)remove("+t+"," + leaveElement + ")";
			}
			synchronized (this) {
			this.notify();
			}
		}
    	html5elements.remove("#"+t);
	}
		
	/**
	 * gets the data for this screen, emptys the buffer and notifies the servlet
	 * that there are new data to be sent
	 * @return the data to be sent
	 */
	public String getMsg(){
		String dt = this.data;
		this.data = null;
		return dt;
	}
	
	public void putMsg(String t,String f,String c) {
		messagecount++;
		if (websocketconnection!=null) {
			websocketconnection.send("put("+t+")="+c);
		} else {
		if (data==null) {
			data = "put("+t+")="+c;
		} else {
			data += "($end$)put("+t+")="+c;
		}
		synchronized (this) {
		    this.notify();	
		}
		}
	}
	
	public void dropConnection() {
		data = null;
		synchronized (this) {
		    this.notify();	
		}
	}
	
	
	public void loadStyleSheet(String style,Boolean allowcache) {
		if (allowcache && csscache.contains(style)) {
			//System.out.println("cached css="+style+" "+allowcache);
			return;
		}	
		loadStyleSheet(style);
		if (!csscache.contains(style)) csscache.add(style);
	}
	
	public void loadStyleSheet(String style) {
		String stylepath = app.getHtmlPath()+"components/"+style;

		StringBuffer str = null;
		try {
			str = new StringBuffer();
			BufferedReader br;
			br = new BufferedReader(new FileReader(stylepath));
			String line = br.readLine();
			while (line != null) {
				str.append(line);
				str.append("\n");
				line = br.readLine();
			 }
			br.close();
		} catch (FileNotFoundException e) {
			System.out.println("COULD NOT FIND : "+stylepath);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		String body = ""+ str.toString();
		String stylename = stylepath.substring(stylepath.lastIndexOf("/")+1, stylepath.indexOf(".css"));
		if(stylename.contains("_")) stylename = stylename.substring(0, stylename.indexOf("_"));
		messagecount++;
		if (websocketconnection!=null) {
			websocketconnection.send("setstyle(head)=" + stylename +"style,"+body);
		} else {
			if (data==null) {
				data = "setstyle(head)=" + stylename +"style,"+body;
			} else {
				data += "($end$)setstyle(head)="+ stylename +"style,"+body;
			}
			synchronized (this) {
				this.notify();
			}
		}
	}
	
	public boolean alreadySendTemplate(String template) {
		if (sendtemplates.contains(template)) return true;
		return false;
	}
	
	public void setSendTemplate(String template) {
		sendtemplates.add(template);
	}
	
	
	public void loadMstStyleSheet(String style, Html5ApplicationInterface app) {
		//TODO: make this at least windows compatible or configurable
		String stylepath ="/springfield/tomcat/webapps/ROOT/eddie/"+style;
		// ugly but works

		
		String packagepath = app.getHtmlPath();
		if (packagepath!=null) {
			int pos = style.indexOf("/css/");
			if (pos!=-1) {
				stylepath = packagepath + style.substring(pos+1);
			}
		}
		

		int pos = stylepath.indexOf(".css");
		if (pos!=-1) {
			stylepath = stylepath.substring(0,pos)+".mst";
		}
		StringBuffer str = null;
		try {
			str = new StringBuffer();
			BufferedReader br;
			br = new BufferedReader(new FileReader(stylepath));
			String line = br.readLine();
			while (line != null) {
				str.append(line);
				str.append("\n");
				line = br.readLine();
			 }
			br.close();
			
			
			String body = ""+ str.toString();
			
			
			String stylename = stylepath.substring(stylepath.lastIndexOf("/")+1, stylepath.indexOf(".mst"));
			if(stylename.contains("_")) stylename = stylename.substring(0, stylename.indexOf("_"));
			stylename+="-mst";
			
			body = doMstReplace(body);
			messagecount++;
			if (websocketconnection!=null) {
				websocketconnection.send("setstyle(head)=" + stylename +"style,"+body);
			} else {
			if (data==null) {
				data = "setstyle(head)=" + stylename +"style,"+body;
			} else {
				data += "($end$)setstyle(head)="+ stylename +"style,"+body;
			}
			synchronized (this) {
			    this.notify();
			}
			}
			
		} catch (FileNotFoundException e) {
		} catch (IOException e) {
			e.printStackTrace();
		}	
	}
	
	private String  doMstReplace(String body) {
		String newbody = "";
		int pos = body.indexOf("{{");
		if (pos!=-1) {
			while (pos!=-1) {
				newbody += body.substring(0,pos);
				body = body.substring(pos+2);
				pos = body.indexOf("}}");
				if (pos!=-1) {
					String token = body.substring(0,pos);
					//System.out.println("TOKEN="+token);
					String part = getModel().getProperty(token);
					//System.out.println("PART="+part);
					newbody += part;	
					body = body.substring(pos+2);
				}
				pos = body.indexOf("{{");
			}
			return newbody;
		} else {
			return body;
		}
	}
	
	public void loadStyleSheet(String style, Html5ApplicationInterface app) {

		//TODO: make this at least windows compatible or configurable
		String stylepath ="/springfield/tomcat/webapps/ROOT/eddie/"+style;
		// ugly but works
		
		String packagepath = app.getHtmlPath();
		if (packagepath!=null) {
			int pos = style.indexOf("/css/");
			if (pos!=-1) {
				stylepath = packagepath + style.substring(pos+1);
			}
		}
		
		
		//System.out.println("LOADING STYLE="+stylepath);
		if (style.equals("apps/dashboard/css/dashboardapp.css")) {
			stylepath="/springfield/tomcat/webapps/ROOT/eddie/apps/dashboard/css/generic.css";
		}
		Boolean failed = false;
		StringBuffer str = null;
		try {
			str = new StringBuffer();
			BufferedReader br;
			br = new BufferedReader(new FileReader(stylepath));
			String line = br.readLine();
			while (line != null) {
				str.append(line);
				str.append("\n");
				line = br.readLine();
			 }
			br.close();
		} catch (FileNotFoundException e) {
			failed=true;
			System.out.println("COULD NOT FIND : "+stylepath);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		if (failed) {
			stylepath ="/springfield/tomcat/webapps/ROOT/eddie/generic.css";
			
			packagepath = app.getHtmlPath();
			if (packagepath!=null) {
				int pos = style.indexOf("/css/");
				if (pos!=-1) {
					stylepath = packagepath + "css/generic.css";
				}
			}
		//	System.out.println("LOADING STYLE="+stylepath);
//			stylepath ="C:\\\\springfield\\tomcat\\webapps\\ROOT\\eddie\\"+stylepath;
			 str = null;
			try {
				str = new StringBuffer();
				BufferedReader br;
				br = new BufferedReader(new FileReader(stylepath));
				String line = br.readLine();
				while (line != null) {
					str.append(line);
					str.append("\n");
					line = br.readLine();
				 }
				br.close();
			} catch (FileNotFoundException e) {
				failed=true;
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		}
			
		String body = ""+ str.toString();
		String stylename = stylepath.substring(stylepath.lastIndexOf("/")+1, stylepath.indexOf(".css"));
		if(stylename.contains("_")) stylename = stylename.substring(0, stylename.indexOf("_"));
		
		messagecount++;
		if (websocketconnection!=null) {
			websocketconnection.send("setstyle(head)=" + stylename +"style,"+body);
		} else {
		if (data==null) {
			data = "setstyle(head)=" + stylename +"style,"+body;
		} else {
			data += "($end$)setstyle(head)="+ stylename +"style,"+body;
		}
		synchronized (this) {
		    this.notify();
		}
		}
	}
	
	public void loadStyleSheetRefer(String style,String refappname) {
		//TODO: make this at least windows compatible or configurable
		//System.out.println("Screen.loadStyleSheet(" + style + ", " + app + ")");
		String stylepath ="/springfield/tomcat/webapps/ROOT/eddie/"+style;
		// ugly but works
		
		/*
		String packagepath = app.getHtmlPath();
		if (packagepath!=null) {
			int pos = style.indexOf("/css/");
			if (pos!=-1) {
				stylepath = packagepath + style.substring(pos+1);
			}
		}
		*/
		
		
		//System.out.println("LOADING STYLE="+stylepath);

//		stylepath ="C:\\\\springfield\\tomcat\\webapps\\ROOT\\eddie\\"+stylepath;
		StringBuffer str = null;
		try {
			str = new StringBuffer();
			BufferedReader br;
			br = new BufferedReader(new FileReader(stylepath));
			String line = br.readLine();
			while (line != null) {
				str.append(line);
				str.append("\n");
				line = br.readLine();
			 }
			br.close();
		} catch (FileNotFoundException e) {
			System.out.println("COULD NOT FIND : "+stylepath);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
			
		String body = ""+ str.toString();
		String stylename = stylepath.substring(stylepath.lastIndexOf("/")+1, stylepath.indexOf(".css"));
		if(stylename.contains("_")) stylename = stylename.substring(0, stylename.indexOf("_"));
		messagecount++;
		if (websocketconnection!=null) {
			websocketconnection.send("setstyle(head)=" + stylename +"style,"+body);
		} else {
		if (data==null) {
			data = "setstyle(head)=" + stylename +"style,"+body;
		} else {
			data += "($end$)setstyle(head)="+ stylename +"style,"+body;
		}
		synchronized (this) {
		    this.notify();
		}
		}
	}


	

	public void removeStyle(String style){
		messagecount++;
		if (websocketconnection!=null) {
			websocketconnection.send("removestyle("+style+"style)");
		} else {
		if (data==null) {
			data = "removestyle("+style+"style)";
		} else {
			data += "($end$)removestyle("+style+"style)";
		}
		synchronized (this) {
		    this.notify();
		}
		}
	}
	
	public void loadScript(String target,String scriptpath, Html5ApplicationInterface app) {
		// lets find out what is the active version for this app
		String basepath = "/springfield/tomcat/webapps/ROOT/eddie/";
		if (LazyHomer.isWindows()) basepath = "C:\\springfield\\tomcat\\webapps\\ROOT\\eddie\\";

		
		String filename = basepath+"domain"+File.separator+app.getDomain()+File.separator+"apps"+File.separator+app.getAppname()+File.separator+"components"+File.separator+scriptpath;
		File file = new File(filename);
		if (!file.exists()) {
			// ok so not in the domain/app/component (step 1)
						
			filename = basepath+"domain"+File.separator+app.getDomain()+File.separator+"components"+File.separator+scriptpath;
			file = new File(filename);
			if (!file.exists()) {
				// ok also not in domain/component

				filename = basepath+"apps"+File.separator+app.getAppname()+File.separator+"components"+File.separator+scriptpath;
				file = new File(filename);
				if (!file.exists()) {
					// ok also not in app/component

					// so its in component
					filename = basepath+"components"+File.separator+scriptpath;
				}
			}
		}
		
		if(new File(filename).exists()){
			String touchBindingsXml = filename.substring(0, filename.lastIndexOf("\\")+1) + "bindings.xml";
//			System.out.println("checking for file: " + filename);
//			System.out.println("checking for bindings: " + touchBindingsXml);
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
				body = JavascriptInjector.injectTryCatch(body, scriptpath);
				//if there is an bindings.xml file in the component directory
				//inject the Javascript with hammer.js events
				if(new File(touchBindingsXml).exists()){
					body = JavascriptInjector.injectTouchBindings(body, touchBindingsXml);
				}
				this.setScript(target, body);
			} catch (Exception e){
				e.printStackTrace();
			}
		}else {
			//System.out.println("File " +filename+ " does not exist");
		}
	}
	
	public void loadComponentScript(String target,String scriptpath, Html5ApplicationInterface app, String comp) {
		// lets find out what is the active version for this app
		String basepath = "/springfield/tomcat/webapps/ROOT/eddie/";
		if (LazyHomer.isWindows()) basepath = "C:\\springfield\\tomcat\\webapps\\ROOT\\eddie\\";

		String packagepath = app.getHtmlPath();
		String filename = null;
		if (packagepath!=null) {
			filename = packagepath + "components"+File.separator+scriptpath;
		} else {	
			filename = basepath+"domain"+File.separator+app.getDomain()+File.separator+"apps"+File.separator+app.getAppname()+File.separator+"components"+File.separator+scriptpath;
			File file = new File(filename);
			if (!file.exists()) {
				// ok so not in the domain/app/component (step 1)
							
				filename = basepath+"domain"+File.separator+app.getDomain()+File.separator+"components"+File.separator+scriptpath;
				file = new File(filename);
				if (!file.exists()) {
					// ok also not in domain/component
	
					filename = basepath+"apps"+File.separator+app.getAppname()+File.separator+"components"+File.separator+scriptpath;
					file = new File(filename);
					if (!file.exists()) {
						// ok also not in app/component
	
						// so its in component
						filename = basepath+"components"+File.separator+scriptpath;
					}
				}
			}
		}
		
		// so lets see if we have a referid on this that overrides it ?
		/*
		String referid = app.getReferid(target);
		if (referid!=null) {
			// so create new filename based on it ? (example : /websiteserviceone/defaultoutput);
			if (referid.startsWith("/")) {
				String refappname = referid.substring(1);
				int pos = refappname.indexOf("/");
				String refcname = refappname.substring(pos+1);
				refappname = refappname.substring(0,pos);
				Html5AvailableApplication refapp = ApplicationManager.instance().getAvailableApplication(refappname);
				if (refapp!=null) {
					if (LazyHomer.inDeveloperMode()) {
						filename = "/springfield/lou/apps/"+refappname+File.separator+refapp.getDevelopmentVersion()+File.separator+"components"+File.separator+refcname+File.separator+refcname+".js";
					} else {
						filename = "/springfield/lou/apps/"+refappname+File.separator+refapp.getProductionVersion()+File.separator+"components"+File.separator+refcname+File.separator+refcname+".js";						
					}
				}
			}
			
		}
		*/

		
		
		if(new File(filename).exists()){
			String touchBindingsXml = filename.substring(0, filename.lastIndexOf(LazyHomer.isWindows() ? "\\" : "/")+1) + "bindings.xml";
//			System.out.println("checking for file: " + filename);
//			System.out.println("checking for bindings: " + touchBindingsXml);
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
				
			
				body = body.replace("$cname",target.substring(0,1).toUpperCase()+target.substring(1));
				
				body = JavascriptInjector.injectTryCatch(body, scriptpath);
				//if there is an bindings.xml file in the component directory
				//inject the Javascript with hammer.js events
				if(new File(touchBindingsXml).exists()){
					body = JavascriptInjector.injectTouchBindings(body, touchBindingsXml);
				}
				body = JavascriptInjector.injectComponentGlobalDefinitions(body, comp, target);
				this.setScript(target, body);
			} catch (Exception e){
				e.printStackTrace();
			}
		}else {
			//System.out.println("File " +filename+ " does not exist");
		}
	}
	
	public void onNewUser(String name) {
		username = name;
		//System.out.println("onNewUser="+name);
		app.onNewUser(this, name);
	}
	
	public void onLoginFail(String name) {
		app.onLoginFail(this, name);
	}
	
	public void onLogoutUser(String name) {
		username = null;
		//System.out.println("USERLOGOUT="+name);
		app.onLogoutUser(this, name);
	}
	
    /**
     * 
     * adds application id, checks with barney and talks to mojo if allowed
     * 
     * @param path
     * @return
     */
    public final FsNode getNode(String path) {
    	String asker = this.getUserName(); // gets the use name
    	if (asker!=null && !asker.equals("")) {
    		System.out.println("screen getNode "+asker);
    		ServiceInterface barney = ServiceManager.getService("barney");
    		if (barney!=null) {
    			String allowed = barney.get("userallowed(read,"+path+",0,"+asker+")",null,null);
    			if (allowed!=null && allowed.equals("true")) {
    				return Fs.getNode(path); // so its allowed ask it
    			}
    		}
    	}
    	return null;
    }
    
	public boolean checkNodeActions(FsNode node,String actions) {
		return checkNodeActions(node,0,actions);
	}
    
	public boolean checkNodeActions(FsNode node,int depth,String actions) {
		if (this.getUserName()==null) return false; // no user always wromg
		return node.checkActions(getUserName(),"user",depth,actions); 
	}
	
	public void setLanguageCode(String isocode) {
		if (language==null || !language.equals(isocode)) {
		language  = isocode;
		// tell all the controllers incase they want to change
		for(Html5Controller controller: controllers){
				controller.languageChanged();
			}
		}
	}
	
	public void destroyed() {
		for(Html5Controller controller: controllers){
			controller.destroyed();
		}
	}
	
	public String getLanguageCode() {
		return language;
	}
    
	
	public void log(String msg) {
		app.log(this,msg);
	}
	
	public void log(String msg,int level) {
		app.log(this,msg,level);
	}
	
	/*
	public void setProperties(String content) {
		String[] cmd=content.split(",");
		for (int i=0;i<cmd.length;i++) {
			String[] param = cmd[i].split("=");
			setProperty(param[0],param[1]);
		}
	}
	*/
	
	public void bindOverride(String selector,ArrayList<String> overrides) {
		bindoverrides.put(selector, overrides);
	}
	
	public void bind(String selector,String eventtype,String methodname,Object callbackobject) {
		bind(selector,eventtype,"",methodname,callbackobject);
	}
	
	public void bind(String selector,String eventtype,String eventpadding,String methodname,Object callbackobject) {
		// is it overriden eventtype 
		boolean override = false;
		ArrayList o = bindoverrides.get(selector);
		if (o!=null) {
			if (o.contains(eventtype)) {
				override = true;
			}
		}
		
		if (!eventtype.equals("client") && selector.indexOf("/controller/")==-1 && !override) {
			messagecount++;
			if (websocketconnection!=null) {
				if (eventpadding.equals("")) {
					websocketconnection.send("bind("+selector.substring(1)+")="+eventtype);
				} else {
					websocketconnection.send("bind("+selector.substring(1)+")="+eventtype+","+eventpadding);
				}
			} else {
			if (data==null) {
				if (eventpadding.equals("")) {
					data = "bind("+selector.substring(1)+")="+eventtype;
				} else {
					data = "bind("+selector.substring(1)+")="+eventtype+","+eventpadding;
				}
			} else {
				if (eventpadding.equals("")) {
					data += "($end$)bind("+selector.substring(1)+")="+eventtype;
				} else {
					data += "($end$)bind("+selector.substring(1)+")="+eventtype+","+eventpadding;
				}
			}
			synchronized (this) {
				this.notify();
			}
			}
		}
		
		if (eventtype.startsWith("track/")) eventtype = "client";

		String mid = methodname;
		String screenid = null;
		String targetid = null;
		if (callbackobject!=null) {
			screenid = ((Html5Controller)callbackobject).getScreenId();
			targetid = ((Html5Controller)callbackobject).getSelector();
	   // System.out.println("BIND = "+screenid+" "+targetid+" "+methodname);
			mid = screenid+"/"+targetid+"/"+methodname;
		}
		
		
		HashMap<String,PathBindObject> list = pathbindobjects.get(selector.substring(1)+"/"+eventtype);
		if (list!=null) {
			// find the screen id and targetid
			
			// im i already watching ?, should the id include the methodname ?
			list.put(mid,new PathBindObject(methodname,screenid,targetid,null,null));
		} else {
			list = new HashMap<String,PathBindObject>();
			list.put(mid,new PathBindObject(methodname,screenid,targetid,null,null));
			pathbindobjects.put(selector.substring(1)+"/"+eventtype, list);
		}
	}
	
	
	public void setGroup(String name) {
		ScreenGroup sg = app.getScreenManager().getScreenGroup(name);
		if (sg!=null) {
			sg.add(this); // ok add this screen
		} else {
			sg =  new ScreenGroup();
			sg.add(this);
			app.getScreenManager().setScreenGroup(name, sg);
		}
	}
	
	public String[] getGroups() {
		return app.getScreenManager().getGroups(this);
	}
	
	public boolean isMember(String name) {
		ScreenGroup sg = app.getScreenManager().getScreenGroup(name);
		//System.out.println("SCR="+sg+" SG="+name);
		if (sg!=null) {
			if (sg.isMember(this)) return true;
		}
		return false;
	}
	
	public String getBrowserId() {
		return capabilities.getCapability("smt_sessionid");
	}
	
    public Html5Element get(String s) {
    	Html5Element e = html5elements.get(s);
    	if (e!=null) return e;
    	e  = new Html5Element(this,s);
    	html5elements.put(s,e);
    	return e;
    }
    
    public boolean exists(String s) {
    	Html5Element e = html5elements.get(s);
    	if (e!=null) {
    		return true;
    	} else {
    		return false;
    	}
    }
    
    public void snapshot(String s,String moment) {
    	// send snapshot commando to client
    }
    
    public void recoverSnapshot(String s,String moment) {
    	// send client command to recover snapshot
    }
    
    public String getConnectionType() {
		if (websocketconnection!=null) {
			return("websocket");
		} else {
			return("http");
		}
    }
    
    public int getMessageCount() {
    		return messagecount;
    }
	
	public boolean send(String msg) {
		messagecount++;
		if (websocketconnection!=null) {
			websocketconnection.send(msg);
		} else {
			if (data==null) {
				data = msg;
			} else {
				data += "($end$)"+msg;
			}
			synchronized (this) {
				this.notify();
			}
		}
		return true;
	}
	

	
    
	public void append(String selector,String elementtype,String attributes,String content) {
		messagecount++;
		if (websocketconnection!=null) {
			websocketconnection.send("append("+selector+" "+elementtype+" "+attributes+")="+content);
		} else {
		
		// for now hardcoded/fake
		if (data==null) {
			data = "append("+selector+" "+elementtype+" "+attributes+")="+content;
		} else {
			data += "($end$)append("+selector+" "+elementtype+" "+attributes+")="+content;
		}
		synchronized (this) {
		    this.notify();
		}
		}
	}
	
	public void observerController(Html5Controller c) {
		controllers.add(c);
	}
	
	
	public void onPropertyUpdate(String properties,String methodname,Object callbackobject) {
		String[] vars=properties.split(",");
		for (int i=0;i<vars.length;i++) {
			ArrayList<PropertyBindObject> list = propertybindobjects.get(vars[i]);
			if (list!=null) {
				list.add(new PropertyBindObject(methodname,callbackobject));
			} else {
				list = new ArrayList<PropertyBindObject>();
				list.add(new PropertyBindObject(methodname,callbackobject));
				propertybindobjects.put(vars[i], list);
			}
		}
	}
	
	public LouWebSocketConnection getWebSocketConnection() {
		return websocketconnection;
	}
	
	public void setWebSocketConnection(LouWebSocketConnection wc) {
		websocketconnection = wc;
		messagecount++;
		// send a message over http to close the http connection and flush all messages still underway
		if (data==null) {
			data = "terminate(httpworker)";
		} else {
			data += "($end$)terminate(httpworker)";
		}
		synchronized (this) {
			this.notify();
		}
	}
	

	


}