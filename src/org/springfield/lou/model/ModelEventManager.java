package org.springfield.lou.model;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Stack;

import org.restlet.Application;
import org.springfield.fs.FsNode;
import org.springfield.fs.FsPropertySet;
import org.springfield.lou.application.Html5Application;
import org.springfield.lou.application.PathBindObject;
import org.springfield.lou.controllers.Html5Controller;
import org.springfield.lou.screen.BindEvent;
import org.springfield.lou.screen.Screen;

public class ModelEventManager {
    private Map<String, ArrayList<ModelBindObject>> propertybinds = new HashMap<String, ArrayList<ModelBindObject>>();
    private Map<String, ArrayList<ModelBindObject>> propertiesbinds = new HashMap<String, ArrayList<ModelBindObject>>();

    private Html5Application app;
	protected Stack<ModelBindEvent> eventqueue  = new Stack<ModelBindEvent>();
	private ModelEventThread normalthread;
    
    public ModelEventManager() {
    	normalthread = new ModelEventThread("normal",this);
    }
    
    public void onPropertyUpdate(String path,String methodname,Html5Controller callbackobject) {
 		try {
 			Method method = callbackobject.getClass().getMethod(methodname,ModelEvent.class);
 			String screenid = callbackobject.getScreenId();
 			String targetid = callbackobject.getSelector();
			ArrayList<ModelBindObject> list = propertybinds.get(path);
			if (list!=null) {
				// find the screen id and targetid
				list.add(new ModelBindObject(methodname,screenid,targetid,callbackobject,method));
			} else {
				list = new ArrayList<ModelBindObject>();
				list.add(new ModelBindObject(methodname,screenid,targetid,callbackobject,method));
				propertybinds.put(path, list);
			}
		} catch(Exception e) {
			e.printStackTrace();
 			return;
 		}

    }

    public void onPropertiesUpdate(String path,String methodname,Html5Controller callbackobject) {
 		try {
 			Method method = callbackobject.getClass().getMethod(methodname,ModelEvent.class);
 			String screenid = callbackobject.getScreenId();
 			String targetid = callbackobject.getSelector();
			ArrayList<ModelBindObject> list = propertiesbinds.get(path);
			if (list!=null) {
				// find the screen id and targetid
				list.add(new ModelBindObject(methodname,screenid,targetid,callbackobject,method));
			} else {
				list = new ArrayList<ModelBindObject>();
				list.add(new ModelBindObject(methodname,screenid,targetid,callbackobject,method));
				propertiesbinds.put(path, list);
			}
		} catch(Exception e) {
			e.printStackTrace();
 			return;
 		}

    }

    
    /*
 	public synchronized void onPathRemove(Screen s) {
 		if (s==null) return;
 		String screenid = s.getId();
 		//System.out.println("BIND REMOVE SCREEN S="+s+" id="+screenid);
		Iterator<String> it = pathbindobjects.keySet().iterator();
		while(it.hasNext()){
			String key = it.next();
			ArrayList<PathBindObject> binds = pathbindobjects.get(key);
			for (int i=binds.size()-1;i>=0;i--) {
				PathBindObject bind  = binds.get(i);
				if (bind.screenid.equals(screenid)) {
					binds.remove(i);
					//System.out.println("REMOVE ON BIND "+key);
				}
			}
			
		}
 	}
 	*/
 	
    public void setProperty(String path,String value) {
    	eventqueue.push(new ModelBindEvent(ModelBindEvent.PROPERTY,path,value));
    	if (eventqueue.size()>0) {
    		normalthread.check();
    	//	checkNormalQueue(); // direct delivery for testing.
    	}
    
    }
    
	public void setProperties(String path,FsPropertySet set) {
    	eventqueue.push(new ModelBindEvent(ModelBindEvent.PROPERTIES,path,set));
    	if (eventqueue.size()>0) {
    		normalthread.check();
    	}
	}
 	
    public void deliverProperty(String path,String value) {
    	String[] parts = path.split("/"); 
    	String key = parts[1]+"/"+parts[2];
    	String nodeid = parts[3];
    	String propertyname = parts[4];
   	
    	FsNode node = new FsNode(key,nodeid);
    	node.setProperty(propertyname, value);
   	
    	key = "/"+key+"/";
		ArrayList<ModelBindObject> binds = propertybinds.get(path);
		if (binds!=null) {
			for (int i=0;i<binds.size();i++) {
				ModelBindObject bind  = binds.get(i);
				try {		
					ModelEvent event = new ModelEvent();
					event.path = key;
					event.target = node;
					bind.methodcall.invoke(bind.obj,event);
				} catch(Exception e) {
					e.printStackTrace();
				}
			}
		}	 
    }
    
    public void deliverProperties(String path,FsPropertySet set) {	
		ArrayList<ModelBindObject> binds = propertiesbinds.get(path);
		if (binds!=null) {
			for (int i=0;i<binds.size();i++) {
				ModelBindObject bind  = binds.get(i);
				try {		
					ModelEvent event = new ModelEvent();
					event.path = path;
					event.target = set;
					bind.methodcall.invoke(bind.obj,event);
				} catch(Exception e) {
					e.printStackTrace();
				}
			}
		}	 
    }
    
    public void checkNormalQueue() {
    	while (eventqueue.size()>0) {
    		ModelBindEvent b = eventqueue.pop(); // should be a case statement
    		if (b.type == ModelBindEvent.PROPERTY) {
    			deliverProperty(b.path,(String)b.value);
    		} else if (b.type == ModelBindEvent.PROPERTIES) {
    			deliverProperties(b.path,(FsPropertySet)b.value);
    		}
    	}
    }

}
