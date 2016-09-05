package org.springfield.lou.model;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.springfield.fs.FsNode;
import org.springfield.fs.FsPropertySet;
import org.springfield.lou.application.Html5Application;
import org.springfield.lou.application.Html5ApplicationInterface;
import org.springfield.lou.application.PathBindObject;
import org.springfield.lou.controllers.Html5Controller;
import org.springfield.lou.screen.BindEvent;
import org.springfield.lou.screen.Screen;

public class ModelEventManager {
    private Map<String, ArrayList<ModelBindObject>> propertybinds = new HashMap<String, ArrayList<ModelBindObject>>();
    private Map<String, ArrayList<ModelBindObject>> propertiesbinds = new HashMap<String, ArrayList<ModelBindObject>>();
    private Map<String, ArrayList<ModelBindObject>> pathbinds = new HashMap<String, ArrayList<ModelBindObject>>();

    
	protected Stack<ModelBindEvent> eventqueue  = new Stack<ModelBindEvent>();
	private ModelEventThread normalthread;
    
    public ModelEventManager() {
    	normalthread = new ModelEventThread("normal",this);
    }
    
    public int getTotalBindsCount() {
    	return getPropertyBindsCount()+getPropertiesBindsCount();
    }
    
    public int getPropertyBindsCount() {
    	int count = 0;
    	Iterator<String> it = propertybinds.keySet().iterator();
    	while(it.hasNext()){
    		String key = it.next();
    		List<ModelBindObject> l = (List)propertybinds.get(key);
    		count+=l.size();
    	}
    	return count;
    }
    
    public int getPropertiesBindsCount() {
    	int count = 0;
    	Iterator<String> it = propertiesbinds.keySet().iterator();
    	while(it.hasNext()){
    		String key = it.next();
    		List<ModelBindObject> l = (List)propertiesbinds.get(key);
    		count+=l.size();
    	}
    	return count;
    }
    
    public int getPathBindsCount() {
    	int count = 0;
    	Iterator<String> it = pathbinds.keySet().iterator();
    	while(it.hasNext()){
    		String key = it.next();
    		List<ModelBindObject> l = (List)pathbinds.get(key);
    		count+=l.size();
    	}
    	return count;
    }
    
    public Map<String, ArrayList<ModelBindObject>> getPropertiesBinds() {
    	return propertiesbinds;
    }
    
    public Map<String, ArrayList<ModelBindObject>> getPathBinds() {
    	return pathbinds;
    }
    
    public Map<String, ArrayList<ModelBindObject>> getPropertyBinds() {
    	return propertybinds;
    }
    
 	public void onPathUpdate(String path,String methodname,Html5Controller callbackobject) {
 		if (path.endsWith("/")) path=path.substring(0,path.length()-1);
 		try {
 			Method method = callbackobject.getClass().getMethod(methodname,ModelEvent.class);
 			String screenid = callbackobject.getScreenId();
 			String targetid = callbackobject.getSelector();
			ArrayList<ModelBindObject> list = pathbinds.get(path);
			if (list!=null) {
				for (int i=list.size()-1;i>=0;i--) {
					ModelBindObject co = list.get(i);
					if (co.screenid.equals(screenid) && co.method.equals(methodname)) {
						list.remove(i);
					}
				}
				list.add(new ModelBindObject(methodname,screenid,callbackobject.getApplicationHashCode(),targetid,callbackobject,method));
			} else {
				list = new ArrayList<ModelBindObject>();
				list.add(new ModelBindObject(methodname,screenid,callbackobject.getApplicationHashCode(),targetid,callbackobject,method));
				pathbinds.put(path, list);
			}
		} catch(Exception e) {
			e.printStackTrace();
 			return;
 		}
 	}
    
    public void onPropertyUpdate(String path,String methodname,Html5Controller callbackobject) {
 		try {
 			Method method = callbackobject.getClass().getMethod(methodname,ModelEvent.class);
 			String screenid = callbackobject.getScreenId();
 			String targetid = callbackobject.getSelector();
			ArrayList<ModelBindObject> list = propertybinds.get(path);
			if (list!=null) {
				// find the screen id and targetid
				//System.out.println("LIST SIZE="+list.size());
				// for it gets tricky do we allow same screen with same method to join 2 times ?
				for (int i=list.size()-1;i>=0;i--) {
					ModelBindObject co = list.get(i);
					//System.out.println("CHECK ="+co.method+" "+methodname);
					if (co.screenid.equals(screenid) && co.method.equals(methodname)) {
						//System.out.println("SHOULD REPLACE INSTEAD OF INSERT REMOVE");
						list.remove(i);
					}
				}
				list.add(new ModelBindObject(methodname,screenid,callbackobject.getApplicationHashCode(),targetid,callbackobject,method));
			} else {
				list = new ArrayList<ModelBindObject>();
				list.add(new ModelBindObject(methodname,screenid,callbackobject.getApplicationHashCode(),targetid,callbackobject,method));
				propertybinds.put(path, list);
			}
		} catch(Exception e) {
			e.printStackTrace();
 			return;
 		}

    }

    public void onPropertiesUpdate(String path,String methodname,Html5Controller callbackobject) {
		//System.out.println("WBIND = "+path);
    	try {
 			Method method = callbackobject.getClass().getMethod(methodname,ModelEvent.class);
 			String screenid = callbackobject.getScreenId();
 			String targetid = callbackobject.getSelector();
			ArrayList<ModelBindObject> list = propertiesbinds.get(path);
			if (list!=null) {
				// find the screen id and targetid
				list.add(new ModelBindObject(methodname,screenid,callbackobject.getApplicationHashCode(),targetid,callbackobject,method));
			} else {
				list = new ArrayList<ModelBindObject>();
				list.add(new ModelBindObject(methodname,screenid,callbackobject.getApplicationHashCode(),targetid,callbackobject,method));
				propertiesbinds.put(path, list);
			}
		} catch(Exception e) {
			e.printStackTrace();
 			return;
 		}
    }
    
    public void removeApplication(int applicationhashcode) {
    	removePropertyApplicationBinds(applicationhashcode);
    	removePropertiesApplicationBinds(applicationhashcode);
    	removePathApplicationBinds(applicationhashcode);
    }
    
    
    public void removeScreenBinds(String screenid) {
    	//System.out.println("SCREEN REMOVE ="+screenid);
    	removePropertyScreenBinds(screenid);
    	removePropertiesScreenBinds(screenid);
    	removePathScreenBinds(screenid);
    }
    
    public synchronized void removePropertyApplicationBinds(int applicationhashcode) {
    	Iterator<String> it = propertybinds.keySet().iterator();
    	while(it.hasNext()){
    		String key = it.next();
    		List<ModelBindObject> l = (List)propertybinds.get(key);
    		for (int i=l.size()-1;i>=0;i--) {
				ModelBindObject bind  = l.get(i);
				if (bind.applicationhashcode==applicationhashcode) {
					System.out.println("REMOVE OF OLD APPLICATION BIND !");
					l.remove(i);
				}
    		}
    		//if (l.size()==0) propertybinds.remove(key);
    	}
	}
    
    public synchronized void removePathApplicationBinds(int applicationhashcode) {
    	Iterator<String> it = pathbinds.keySet().iterator();
    	while(it.hasNext()){
    		String key = it.next();
    		List<ModelBindObject> l = (List)pathbinds.get(key);
    		for (int i=l.size()-1;i>=0;i--) {
				ModelBindObject bind  = l.get(i);
				if (bind.applicationhashcode==applicationhashcode) {
					l.remove(i);
				}
    		}

    	}
	}


    public synchronized void removePropertyScreenBinds(String screenid) {
    	Iterator<String> it = propertybinds.keySet().iterator();
    	while(it.hasNext()){
    		String key = it.next();
    		List<ModelBindObject> l = (List)propertybinds.get(key);
    		for (int i=l.size()-1;i>=0;i--) {
				ModelBindObject bind  = l.get(i);
				if (bind.screenid.equals(screenid)) {
					l.remove(i);
				}
    		}
    		//if (l.size()==0) propertybinds.remove(key);
    	}
	}
    
    public synchronized void removePropertiesScreenBinds(String screenid) {
    	Iterator<String> it = propertiesbinds.keySet().iterator();
    	while(it.hasNext()){
    		String key = it.next();
    		List<ModelBindObject> l = (List)propertiesbinds.get(key);
    		for (int i=l.size()-1;i>=0;i--) {
				ModelBindObject bind  = l.get(i);
				if (bind.screenid.equals(screenid)) {
					l.remove(i);
				}
    		}
    		//if (l.size()==0) propertiesbinds.remove(key);
    	}
	}
    
    public synchronized void removePathScreenBinds(String screenid) {
    	Iterator<String> it = pathbinds.keySet().iterator();
    	while(it.hasNext()){
    		String key = it.next();
    		List<ModelBindObject> l = (List)pathbinds.get(key);
    		for (int i=l.size()-1;i>=0;i--) {
				ModelBindObject bind  = l.get(i);
				if (bind.screenid.equals(screenid)) {
					l.remove(i);
				}
    		}
    		//if (l.size()==0) propertiesbinds.remove(key);
    	}
	}
    
    
    public synchronized void removePropertiesApplicationBinds(int applicationhashcode) {
    	Iterator<String> it = propertiesbinds.keySet().iterator();
    	while(it.hasNext()){
    		String key = it.next();
    		List<ModelBindObject> l = (List)propertiesbinds.get(key);
    		for (int i=l.size()-1;i>=0;i--) {
				ModelBindObject bind  = l.get(i);
				if (bind.applicationhashcode==applicationhashcode) {
					l.remove(i);
				}
    		}
    		//if (l.size()==0) propertiesbinds.remove(key);
    	}
	}
 	
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
	
	
	/*
    public void deliverPath(String path,String value) {
    	String[] parts = path.split("/"); 
    	String key = parts[1]+"/"+parts[2];
    	String nodeid = parts[parts.length-2];
    	String propertyname = parts[parts.length-1];
   	
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
    */
 	
    public void deliverProperty(String path,String value) {
    	String[] parts = path.split("/"); 
    	String key = parts[1]+"/"+parts[2];
    	String nodeid = parts[parts.length-2];
    	String propertyname = parts[parts.length-1];
   	
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
					event.eventtype = ModelBindEvent.PROPERTY;
					bind.methodcall.invoke(bind.obj,event);
				} catch(Exception e) {
					e.printStackTrace();
				}
			}
		}
		deliverPath(path,node,ModelBindEvent.PROPERTY);
    }
    
    public void deliverPath(String path,Object target,int eventtype) {
    	String pathwalker = path;
    	int pos = pathwalker.lastIndexOf("/");
    	while (pos!=-1) {
    		ArrayList<ModelBindObject> binds = pathbinds.get(pathwalker);
    		if (binds!=null) {
    			for (int i=0;i<binds.size();i++) {
    				ModelBindObject bind  = binds.get(i);
    				try {		
    					ModelEvent event = new ModelEvent();
    					event.path = path;
    					event.eventtype = eventtype;
    					event.target = target;
    					bind.methodcall.invoke(bind.obj,event);
    				} catch(Exception e) {
    					e.printStackTrace();
    				}
    			}	
    		}
    		pathwalker = pathwalker.substring(0,pos);
    		pos = pathwalker.lastIndexOf("/"); 
    	}
    }
    
    public void deliverProperties(String path,FsPropertySet set) {	
		ArrayList<ModelBindObject> binds = propertiesbinds.get(path); // direct hit
		if (binds!=null) {
			for (int i=0;i<binds.size();i++) {
				ModelBindObject bind  = binds.get(i);
				try {		
					ModelEvent event = new ModelEvent();
					event.path = path;
					event.target = set;
					event.eventtype = ModelBindEvent.PROPERTIES;
					bind.methodcall.invoke(bind.obj,event);
				} catch(Exception e) {
					e.printStackTrace();
				}
			}
		}
		deliverPath(path,set,ModelBindEvent.PROPERTIES);
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
