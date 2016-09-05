/* 
* Html5Application.java
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

package org.springfield.lou.application;

import java.io.File;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springfield.lou.controllers.Html5Controller;
import org.springfield.fs.*;
import org.springfield.lou.homer.LazyHomer;
import org.springfield.lou.model.AppInstanceModel;
import org.springfield.lou.model.AppModel;
import org.springfield.lou.model.Model;
import org.springfield.lou.screen.Capabilities;
import org.springfield.lou.screen.Html5Element;
import org.springfield.lou.screen.Screen;
import org.springfield.lou.screen.ScreenGroup;
import org.springfield.lou.screen.ScreenManager;
import org.springfield.lou.user.User;
import org.springfield.lou.user.UserManager;
import org.springfield.mojo.interfaces.ServiceInterface;
import org.springfield.mojo.interfaces.ServiceManager;

/**
 * Html5Application
 * 
 * @author Daniel Ockeloen
 * @copyright Copyright: Noterik B.V. 2012
 * @package org.springfield.lou.application
 *
 */
public class Html5Application implements Html5ApplicationInterface,Runnable {
	
	public final static int LOG_INFO = 1;
	public final static int LOG_WARNING = 2;
	public final static int LOG_ERROR = 3;
	public final static String loglevels[] = {"info","warning","error"};
	protected String id;
	protected String fullid;
	protected String htmlpath;
	protected int screencounter;
	protected Boolean timeoutcheck;
    protected ScreenManager screenmanager;
    protected UserManager usermanager;
    protected Thread t;
    protected String appname = "";
    protected boolean sessionrecovery = false;
	protected ArrayList<String> recoverylist  = new ArrayList<String>();
    protected boolean running = true;
    protected int fakeconnectionlost = 0;
    protected int fakeconnectionlostcount = 5;
//    protected String location_scope = "browserid";
    protected AppInstanceModel appinstancemodel;

    protected Map<String, String> callbackmethods = new HashMap<String, String>();
    protected Map<String, Object> callbackobjects = new HashMap<String, Object>();
    private Map<String, ArrayList<PathBindObject>> pathbindobjects = new HashMap<String, ArrayList<PathBindObject>>();
	private Map<String, Object> properties = new HashMap<String, Object>();
	//private BindManager bindmanager;
	private  AppModel appmodel; // i think this is wrong should be per url no?
    
    public Html5Application(String id, String remoteReciever) {
    	this.timeoutcheck = false;
		this.id = id;
		int pos = id.indexOf("/html5application/");
		if (pos!=-1) {
			appname = id.substring(pos+18);
			appname = appname.indexOf("?") == -1 ? appname : appname.substring(0, appname.indexOf("?"));
			pos = appname.indexOf("/");
		    if (pos!=-1) {
		    	appname = appname.substring(0,pos);
		    }
		}
		this.screencounter = 1;
		this.screenmanager = new ScreenManager();

		//this.bindmanager = new BindManager(this);
		//System.out.println("external id: " + externalInterfaceId);
		this.usermanager = new UserManager();
		t = new Thread(this);
        t.start();

       if (appmodel==null) appmodel = new AppModel(this); // create the memory for all instances of this app
       appinstancemodel = new AppInstanceModel(this); // create the memory for this app
    }
    
    public AppModel getAppModel() {
    	return appmodel;
    }
    
    public AppInstanceModel getAppInstanceModel() {
    	return appinstancemodel;
    }
    
    public void addToRecoveryList(String name) {
    	recoverylist.add(name);
    }
    
    public ArrayList<String> getRecoveryList() {
    	return recoverylist;
    }
    
    public void setSessionRecovery(boolean s) {
    	sessionrecovery = s;
    }
    
    public boolean getSessionRecovery() {
    	return sessionrecovery;
    }
    
	public Html5Application(String id) {
		this(id, "video");
	}
	
	public void setId(String i) {
		this.id = i;
	}
	
	public String getId() {
		return id;
	}
	
	public String getAppname() {
		return appname;
	}
	
	public String getDomain() {
		String result = id.substring(id.indexOf("/domain/")+8);
		result = result.substring(0,result.indexOf('/'));
		return result;
	}
	
	public void setFullId(String i) {
		this.fullid = i;
	}
	
	public String getFullId() {
		return fullid;
	}
	
	public String getHtmlPath() {
		return htmlpath;
	}
	
	public void setHtmlPath(String p) {
		htmlpath = p;
	}
	
	public void setCallback(String name,String m,Class c) {
		
	}
	
	public synchronized Screen getNewScreen(Capabilities caps,Map<String,String[]> p) {
		Long newid = new Date().getTime();
		screencounter++;
		Screen screen = new Screen(this,caps,id+"/1/screen/"+newid);
		screen.setParameters(p); // this can also be used to set location ?

		this.screenmanager.put(screen);
		this.onNewScreen(screen);
		return screen;
	}
	
	public Screen getScreen(String id) {
		return this.screenmanager.get(id);
	}
	
	public ScreenManager getScreenManager(){
		return this.screenmanager;
	}
	
	public UserManager getUserManager(){
		return this.usermanager;
	}
	
	public String getLibPaths() {
		String result = null;
		String libsdir = "";
		if (LazyHomer.isWindows()) {
			libsdir = "C:\\springfield\\tomcat\\webapps\\ROOT\\eddie\\apps\\"+id+"\\libs";
		} else {
			libsdir = "/springfield/tomcat/webapps/ROOT/eddie/apps/"+id+"/libs";
		}
		File dir = new File(libsdir);
		String[] files = dir.list();
		for (int i=0;i<files.length;i++) {
			String filename = files[i];
			if (result==null) {
				result = id+"/libs/"+filename;
			} else {
				result +=","+id+"/libs/"+filename;
			}
		}
		return result;
	}
	
	public void run() {
		while(running){
			try {
					Thread.sleep(10000);
					this.maintainanceRun();
					this.timeoutCheckup();
			} catch (Exception e) {
				System.out.println("Exception in run() application");
				e.printStackTrace();
			}
		}
		System.out.println("application run done, shutting down");
	}
	
	public void timeoutCheckup() {
		Set<String> keys = this.screenmanager.getScreens().keySet();
		ArrayList<String> wantremove = new ArrayList<String>();
		Iterator<String> it = keys.iterator();
		while(it.hasNext()){
			String next = it.next();
			Screen screen = this.screenmanager.get(next);
			if ((new Date().getTime()-screen.getLastSeen()>(60*1000))) { // moved from 12 seconds to 12 hours
				String username = screen.getUserName();
				if (username!=null) {
					this.onLogoutUser(screen,username);
				}
				wantremove.add(next+","+username);
			}
		}
		for (int i=0;i<wantremove.size();i++) {
			String[] tmp = wantremove.get(i).split(",");
			System.out.println("S="+tmp[0]+" U="+tmp[1]);
			removeScreen(tmp[0],tmp[1]);
		}
	}
	
	public void shutdown() {
		running = false;
		Set<String> keys = this.screenmanager.getScreens().keySet();
		Iterator<String> it = keys.iterator();
		while(it.hasNext()){
			String next = it.next();
			Screen screen = this.screenmanager.get(next);
			String username = screen.getUserName();
			this.onLogoutUser(screen,username);
			it.remove(); // avoids a ConcurrentModificationException
			System.out.println("SHUTDOWN");
			this.removeScreen(next,username);	
		}
		
		Model.getEventManager().removeApplication(this.hashCode());
		
		ApplicationManager.instance().removeApplication(this.id);
	}
	
	public void maintainanceRun() {
			try {
				if (!running) return;
				Set<String> keys = this.screenmanager.getScreens().keySet();
				Iterator<String> it = keys.iterator();
				while(it.hasNext()){
					String next = it.next();
					Screen s = this.screenmanager.get(next);
					s.setContent("synctime",new Date().toString());
				}
			} catch(Exception e) {
				System.out.println("Exception in maintainance run");
			}
	}
	
	public void setContent(String div,String content) {
		Set<String> keys = this.screenmanager.getScreens().keySet();
		Iterator<String> it = keys.iterator();
		while(it.hasNext()){
			String next = it.next();
			Screen s = this.screenmanager.get(next);
			s.setContent(div,content);
		}
	}
	
	public void putData(String data) {
		int pos = data.indexOf("put(");
		if (pos!=-1) {
			data = data.substring(pos+4);
			int pos2 = data.indexOf(")");
			if (pos2!=-1) {
				String target = data.substring(0,pos2);
				int pos3 = target.indexOf(",");
				String from = target.substring(0,pos3);
				target = target.substring(pos3+1);
				String content = data.substring(pos2+2);
				Screen ts = this.screenmanager.get(from);
				if (ts!=null) {
					ts.setSeen();
				} else {
				//	System.out.println("EMPTY FROM SCREEN = "+from);
				}
				
				//System.out.println("FROM="+from+" TARGET="+target+" CONTENT="+content);
				if (target.equals("")) {
					// get the correct screen in this case the sender
					ts.put(from,content);
				} else {
				    if (target.indexOf("../")==0){
						// ok so not all screens but a screen !
						String ns = target.substring(3);
						String cs = from.substring(0,from.lastIndexOf("/"));
						//System.out.println("NS="+ns+" CS="+cs);
						Screen ts2 = this.screenmanager.get(cs+"/"+ns);
						ts2.put(from,content);
					}//else System.out.println("nothing to do");
				}
			}
		}
 	}
	
	
	public void loadStyleSheet(Screen s,String sname) {
		s.loadStyleSheet(getApplicationCSS(sname) , this);
	}
	
	public void loadStyleSheet(Screen s,String dstyle,String sname) {
		String fs = getDeviceCSS(dstyle);
		if (LazyHomer.isWindows()) {
			fs = "C:\\springfield\\tomcat\\webapps\\ROOT\\eddie\\apps\\"+appname+"\\css\\"+dstyle+".css";
		} else {
			fs = "/springfield/tomcat/webapps/ROOT/eddie/apps/"+appname+"/css/"+dstyle+".css";
		}
		File f = new File(fs);
		if (f.exists()) {
			s.loadStyleSheet(getDeviceCSS(dstyle) , this);
		} else {
			s.loadStyleSheet(sname , this);
		}
	}
	
	
	public void put(String from,String content) {
		System.out.println("Application put should be overridden");
	}
	
	public void putOnScreen(Screen s,String from,String content) {
		String component = content.substring(content.indexOf("(")+1, content.indexOf(")"));
		if(content.indexOf("load(")==0)	{
			System.out.println("LOAD CALLED!!");
		} else if(content.indexOf("add(")==0) {
			System.out.println("ADD PUT CALLED");
		} else if(content.indexOf("remove(")==0) {
			removeContent(s, component);
		} else if(content.indexOf("event(")==0) {
    		try {
    			int pos = component.indexOf(",");
    			String lookup = component.substring(0,pos);
    			component = component.substring(pos+1);
    			JSONObject data = (JSONObject)new JSONParser().parse(component);
    			s.event(from,lookup,data);
    		} catch(Exception e) {
    			e.printStackTrace();
    		}
			return;
		} else if(content.indexOf("log(")==0) {
			String[] parts = component.split(",");
			eddieLog(s,component);
		}
	}
	
	public void removeContent(Screen s, String comp){
		s.removeContent(comp, this);	
	}
	
	
	public void removeScreen(String id,String username){
		Screen screen = this.screenmanager.get(id);
		if (screen!=null) {
			//System.out.println("REQUEST SCREEN REMOVE ID="+id+"");
			screen.getModel().getEventManager().removeScreenBinds(id);
			username = screen.getUserName();
		}
		ScreenManager.globalremove(id);
	//	bindmanager.onPathRemove(screen); problem for lou3 need fix

		
		onScreenTimeout(screen);

		if (username!=null) {
			User u = usermanager.getUser(username);
				if(u!=null) {
					u.removeScreen(id);
					if (u.getScreens().size()==0) {
						// user not on any screen remove it from the app
						usermanager.removeUser(u);	
					}
			}
		}
	}
	
	
	public void onScreenTimeout(Screen s) {
	}
	
	public void onNewScreen(Screen s) {
	}
	
	public void onLogoutUser(Screen s,String name) {
		User u = usermanager.getUser(name);
		if (u!=null) { // should check if still on other screen !!!
			usermanager.removeUser(u);
		}
	}
	
	public void onNewUser(Screen s,String id) {
		User u = usermanager.getUser(id);
		if (u==null) {
			u = new User(id);
			u.addScreen(s);
			usermanager.addUser(u);
		} else {
			u.addScreen(s);
		}
	}
	
	
	
	public void onLoginFail(Screen s,String id) {
	}
	
    public String getApplicationCSS(String name) {
    	String path = "apps/"+appname+"/css/"+name+".css";
    	return path;
     }
    
    public String getApplicationCSSRefer(String refcss,String refappname) {
    	// weird for now.
    	//System.out.println("CSS NAME="+refcss);
		String path = "apps/"+refappname+"/css/"+refcss+".css";
		//System.out.println("REFERID CSS !!!!="+path);
		return path;
    }

    
    
    public String getDeviceCSS(String dstyle) {
    	// weird for now.
    	String path = "apps/"+appname+"/css/"+dstyle+".css";
    	return path;
    }
    
    
    public int getScreenCount() {
    	return screenmanager.getScreens().size();
    }
    
    public int getUserCount() {
    	return usermanager.size();
    }
    
    public int getScreenIdCounter() {
    	return screencounter-1;
    }
    
    
    private void eddieLog(Screen s,String content) {
    	String[] parts = content.split(",");
    	String l = parts[1];
    	int level = LOG_INFO; // default to info
    	if (l.equals("warning")) { level = LOG_WARNING; }
    	else if (l.equals("error")) { level = LOG_ERROR; }
  		SimpleDateFormat f = new SimpleDateFormat("HH:mm:ss");
    	FsNode n = new FsNode("log",f.format(new Date()));	
  		n.setProperty("level", loglevels[level-1]);
  		n.setProperty("source", "js");
  		n.setProperty("msg", parts[0]);
  		if (s!=null) {
  			n.setProperty("screen", s.getShortId());
  			if (s.getUserName()!=null) {
  				n.setProperty("user", s.getUserName());
  			} else {
  				n.setProperty("user", "unknown");
  			}
  		}
  		ApplicationManager.log(this, n);
    }
    
    public void log(String msg) {
    		log(null,msg,LOG_INFO);
    }
    
    public void log(String msg,int level) {
		log(null,msg,level);
    }
    
    public void log(Screen s,String msg) {
		log(s,msg,LOG_INFO);
    }
    
    public boolean inDevelopemntMode() {
    	return LazyHomer.inDeveloperMode();
    }
    
    /**
     * 
     * adds application id, checks with barney and talks to mojo if allowed
     * 
     * @param path
     * @return
     */
    public final FsNode getNode(String path) {
    	String asker = this.getClass().getName(); // gets the name from the classloader
    	int pos = asker.indexOf("org.springfield.lou.application.types.");
    	if (pos==0) { // make sure we are in the right package
    		asker = asker.substring(pos+38);
    		//System.out.println("getNode "+asker);
    		ServiceInterface barney = ServiceManager.getService("barney");
    		if (barney!=null) {
    			String allowed = barney.get("applicationallowed(read,"+path+",0,"+asker+")",null,null);
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
    	String asker = this.getClass().getName(); // gets the name from the classloader
    	int pos = asker.indexOf("org.springfield.lou.application.types.");
    	if (pos==0) { // make sure we are in the right package
    		asker = asker.substring(pos+38);
    		return node.checkActions(asker,"application",depth,actions); 
    	}
    	return false;
	}
	
	public String getFavicon() {
		return null;
	}
	
	public String getMetaHeaders(HttpServletRequest request) {
		return ""; // default is empty;
	}
    
    public void log(Screen s,String msg,int level) {
    		SimpleDateFormat f = new SimpleDateFormat("HH:mm:ss");
    		FsNode n = new FsNode("log",f.format(new Date()));
    		n.setProperty("level", loglevels[level-1]);
    		n.setProperty("source", "java");
    		n.setProperty("msg", msg);
    		if (s!=null) {
    			n.setProperty("screen", s.getShortId());
    			if (s.getUserName()!=null) {
    					n.setProperty("user", s.getUserName());
    			} else {
					n.setProperty("user","unknown");	
    			}
    		} else {
    			n.setProperty("screen", "application");
    			n.setProperty("user","unknown");    			
    		}
    		ApplicationManager.log(this, n);
    }
    
    public void setCallback(String name,String m,Object o) {
    	callbackmethods.put(name, m); 
    	callbackobjects.put(name, o); 
    }
    
    private String dirToName(String dir) {
    	// converts things like divione/mouseup to 'actionDivoneMouseup'
    	String name = "action";
    	String[] parts = dir.split("/");
    	for (int i=0;i<parts.length;i++) {
    		String tmp = parts[i].substring(0,1);
    		String tmp2 = parts[i].substring(1);
    		name += tmp.toUpperCase()+tmp2.toLowerCase();
    	}
    	return name;
    }
    
    
    
    public Html5Controller createController(String name) {
    	System.out.println("PLACE createController node in your app");
    	return null;
    }
    

    
    public void removeEvents(Object obj) {
    	// remove all event callbacks this objects has
    	
    	// remove from path binds
    }

}
