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
    private Map<String, ArrayList<ModelBindObject>> notifybinds = new HashMap<String, ArrayList<ModelBindObject>>();
    private Map<String, ArrayList<ModelBindObject>> timelinenotifybinds = new HashMap<String, ArrayList<ModelBindObject>>();
    private Map<String,TimeLineWatcher> timelinewatchers = new HashMap<String,TimeLineWatcher>();

    
	protected Stack<ModelBindEvent> eventqueue  = new Stack<ModelBindEvent>();
	private ModelEventThread normalthread;
    
    public ModelEventManager() {
    	normalthread = new ModelEventThread("normal",this);
    }
    
    public int getTotalBindsCount() {
    	return getPropertyBindsCount()+getPropertiesBindsCount()+getPathBindsCount()+getNotifyBindsCount()+getTimeLineNotifyBindsCount();
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
    
    public int getNotifyBindsCount() {
    	int count = 0;
    	Iterator<String> it = notifybinds.keySet().iterator();
    	while(it.hasNext()){
    		String key = it.next();
    		List<ModelBindObject> l = (List)notifybinds.get(key);
    		count+=l.size();
    	}
    	return count;
    }
    
    public int getTimeLineNotifyBindsCount() {
    	int count = 0;
    	Iterator<String> it = timelinenotifybinds.keySet().iterator();
    	while(it.hasNext()){
    		String key = it.next();
    		List<ModelBindObject> l = (List)timelinenotifybinds.get(key);
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
    
    public Map<String, ArrayList<ModelBindObject>> getNotifyBinds() {
    	return notifybinds;
    }
    
    public Map<String, ArrayList<ModelBindObject>> getTimeLineNotifyBinds() {
    	return timelinenotifybinds;
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
					if (co.screenid.equals(screenid) && co.method.equals(methodname) && co.methodcall.getDeclaringClass().getName().equals(method.getDeclaringClass().getName())) {
						list.remove(i); // dub kill
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
				for (int i=list.size()-1;i>=0;i--) {
					ModelBindObject co = list.get(i);
					if (co.screenid.equals(screenid) && co.method.equals(methodname) && co.methodcall.getDeclaringClass().getName().equals(method.getDeclaringClass().getName())) {
						list.remove(i); // dub kill
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
    	try {
 			Method method = callbackobject.getClass().getMethod(methodname,ModelEvent.class);
 			String screenid = callbackobject.getScreenId();
 			String targetid = callbackobject.getSelector();
			ArrayList<ModelBindObject> list = propertiesbinds.get(path);
			if (list!=null) {
				for (int i=list.size()-1;i>=0;i--) {
					ModelBindObject co = list.get(i);
					if (co.screenid.equals(screenid) && co.method.equals(methodname) && co.methodcall.getDeclaringClass().getName().equals(method.getDeclaringClass().getName())) {
						list.remove(i); // dub kill
					}
				}
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
    
    public void onNotify(String path,String methodname,Html5Controller callbackobject) {
    	try {
 			Method method = callbackobject.getClass().getMethod(methodname,ModelEvent.class);
 			String screenid = callbackobject.getScreenId();
 			String targetid = callbackobject.getSelector();
			ArrayList<ModelBindObject> list = notifybinds.get(path);
			if (list!=null) {
				for (int i=list.size()-1;i>=0;i--) {
					ModelBindObject co = list.get(i);
					if (co.screenid.equals(screenid) && co.method.equals(methodname) && co.methodcall.getDeclaringClass().getName().equals(method.getDeclaringClass().getName())) {
						list.remove(i); // dub kill
					}
				}
				list.add(new ModelBindObject(methodname,screenid,callbackobject.getApplicationHashCode(),targetid,callbackobject,method));
			} else {
				list = new ArrayList<ModelBindObject>();
				list.add(new ModelBindObject(methodname,screenid,callbackobject.getApplicationHashCode(),targetid,callbackobject,method));
				notifybinds.put(path, list);
			}
		} catch(Exception e) {
			e.printStackTrace();
 			return;
 		}
    }

    public void removeController(Object controller) {
    	System.out.println("REMOVE CONTROLLER EVENTS="+controller);
    	removeControllerBinds(notifybinds,controller);
    	removeControllerBinds(propertybinds,controller);
    	removeControllerBinds(propertiesbinds,controller);
    	removeControllerBinds(pathbinds,controller);
    	removeControllerBinds(timelinenotifybinds,controller);
 		FsNode node = new FsNode("bind","1");
 		node.setProperty("action","remove controller");
 		node.setProperty("controller",""+controller.hashCode());
 		notify("/shared/internal",node);
    }
    
    public void removeApplication(int applicationhashcode) {
    	removeApplicationBinds(propertybinds,applicationhashcode);
    	removeApplicationBinds(propertiesbinds,applicationhashcode);
    	removeApplicationBinds(notifybinds,applicationhashcode);
    	removeApplicationBinds(pathbinds,applicationhashcode);
    	removeApplicationBinds(timelinenotifybinds,applicationhashcode);
 		FsNode node = new FsNode("bind","1");
 		node.setProperty("action","remove application");
 		node.setProperty("application",""+applicationhashcode);
 		notify("/shared/internal",node);
    }
    
    
    public void removeScreenBinds(String screenid) {
    	removeScreenBinds(propertybinds,screenid);
    	removeScreenBinds(propertiesbinds,screenid);
    	removeScreenBinds(notifybinds,screenid);
    	removeScreenBinds(pathbinds,screenid);
    	removeScreenBinds(timelinenotifybinds,screenid);
 		FsNode node = new FsNode("bind","1");
 		node.setProperty("action","remove screen");
 		node.setProperty("screen",screenid);
 		notify("/shared/internal",node);
    }
    
    public synchronized void removeApplicationBinds(Map<String, ArrayList<ModelBindObject>> binds,int applicationhashcode) {
    	Iterator<String> it = binds.keySet().iterator();
    	while(it.hasNext()){
    		String key = it.next();
    		List<ModelBindObject> l = (List)binds.get(key);
    		for (int i=l.size()-1;i>=0;i--) {
				ModelBindObject bind  = l.get(i);
				if (bind.applicationhashcode==applicationhashcode) {
					l.remove(i);
				}
    		}
    	}
	}
    
    
    
    public synchronized void removeControllerBinds(Map<String, ArrayList<ModelBindObject>> binds,Object controller) {
    	Iterator<String> it = binds.keySet().iterator();
    	while(it.hasNext()){
    		String key = it.next();
    		List<ModelBindObject> l = (List)binds.get(key);
    		for (int i=l.size()-1;i>=0;i--) {
				ModelBindObject bind  = l.get(i);
				if (bind.obj==controller) {
					l.remove(i);
				}
    		}
    	}
	}
    
 


    public synchronized void removeScreenBinds(Map<String, ArrayList<ModelBindObject>> binds,String screenid) {
    	Iterator<String> it = binds.keySet().iterator();
    	while(it.hasNext()){
    		String key = it.next();
    		List<ModelBindObject> l = (List)binds.get(key);
    		for (int i=l.size()-1;i>=0;i--) {
				ModelBindObject bind  = l.get(i);
				if (bind.screenid.equals(screenid)) {
					l.remove(i);
				}
    		}
    	}
	}
    
     
    
    public void setProperty(String path,String value) {
    	eventqueue.push(new ModelBindEvent(ModelBindEvent.PROPERTY,path,value));
    	if (eventqueue.size()>0) {
    		normalthread.check();
    	//	checkNormalQueue(); // direct delivery for testing.
    	}
    
    }
    
    public void notify(String path,FsNode value) {
    	eventqueue.push(new ModelBindEvent(ModelBindEvent.NOTIFY,path,value));
    	if (eventqueue.size()>0) {
    		normalthread.check();
    	}
    
    }
    
	public synchronized void onTimeLineNotify(String path,String timer,String starttime,String duration,String methodname,Html5Controller callbackobject) {
		System.out.println("timeline notify request="+path+" "+timer+" "+starttime+" "+duration+" callb="+callbackobject);
		
		TimeLineWatcher tw = timelinewatchers.get(path+":"+timer);
		if (tw==null) {
			tw = new TimeLineWatcher(path,timer,starttime,duration,this);
			timelinewatchers.put(path+":"+timer, tw);
		}
    	try {
 			Method method = callbackobject.getClass().getMethod(methodname,ModelEvent.class);
 			String screenid = callbackobject.getScreenId();
 			String targetid = callbackobject.getSelector();
			ArrayList<ModelBindObject> list = timelinenotifybinds.get(path+":"+timer);
			if (list!=null) {
				for (int i=list.size()-1;i>=0;i--) {
					ModelBindObject co = list.get(i);
					if (co.screenid.equals(screenid) && co.method.equals(methodname) && co.methodcall.getDeclaringClass().getName().equals(method.getDeclaringClass().getName())) {
						System.out.println("DUB KILL");
						list.remove(i); // dub kill
					}
				}
				System.out.println("ADD TO LIST 2");
				list.add(new ModelBindObject(methodname,screenid,callbackobject.getApplicationHashCode(),targetid,callbackobject,method));
			} else {
				System.out.println("ADD TO LIST 1");
				list = new ArrayList<ModelBindObject>();
				list.add(new ModelBindObject(methodname,screenid,callbackobject.getApplicationHashCode(),targetid,callbackobject,method));
				timelinenotifybinds.put(path+":"+timer, list);
			}
		} catch(Exception e) {
			e.printStackTrace();
 			return;
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
    	String nodeid = parts[parts.length-2];
    	String propertyname = parts[parts.length-1];
   	
    	FsNode node = new FsNode(key,nodeid);
    	//System.out.println("p="+propertyname+" v="+value);
    	node.setProperty(propertyname, value);
   	
    	key = "/"+key+"/";
		ArrayList<ModelBindObject> binds = propertybinds.get(path);
		if (binds!=null) {
			long starttime = new Date().getTime();
			ModelEvent event = new ModelEvent();
			event.path = key;
			event.target = node;
			event.eventtype = ModelBindEvent.PROPERTY;
    		for (int i=binds.size()-1;i>-1;i--) {
				ModelBindObject bind  = binds.get(i);
				try {		
					bind.methodcall.invoke(bind.obj,event);
				} catch(Exception e) {
					e.printStackTrace();
				}
			}
			long endtime = new Date().getTime();
			System.out.println("property delivertime="+path+" "+(endtime-starttime));
		}
		deliverPath(path,node,ModelBindEvent.PROPERTY);
    }
    
    public void deliverPath(String path,Object target,int eventtype) {
    	String pathwalker = path;
    	int pos = pathwalker.lastIndexOf("/");
    	while (pos!=-1) {
    		ArrayList<ModelBindObject> binds = pathbinds.get(pathwalker);
    		if (binds!=null) {
				ModelEvent event = new ModelEvent();
				event.path = path;
				event.eventtype = eventtype;
				event.target = target;
    			for (int i=binds.size()-1;i>-1;i--) {
    				ModelBindObject bind  = binds.get(i);
    				try {		
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
			ModelEvent event = new ModelEvent();
			event.path = path;
			event.target = set;
			event.eventtype = ModelBindEvent.PROPERTIES;
			for (int i=binds.size()-1;i>-1;i--) {
				ModelBindObject bind  = binds.get(i);
				try {		
					bind.methodcall.invoke(bind.obj,event);
				} catch(Exception e) {
					e.printStackTrace();
				}
			}
		}
		deliverPath(path,set,ModelBindEvent.PROPERTIES);
    }
    
    public void deliverTimeLineNotify(String path,int eventtype,FsNode node) {	
 //   	System.out.println("PATH="+path+" N="+node.asXML());
		ArrayList<ModelBindObject> binds = timelinenotifybinds.get(path); // direct hit
		if (binds!=null) {
			ModelEvent event = new ModelEvent();
			event.path = path;
			event.target = node;
			event.eventtype = eventtype;
			for (int i=binds.size()-1;i>-1;i--) {
				ModelBindObject bind  = binds.get(i);
				try {		
					bind.methodcall.invoke(bind.obj,event);
				} catch(Exception e) {
					System.out.println("Error during Time nofity delivery : "+bind.selector+" "+bind.method+" "+bind.obj);
					e.printStackTrace();
				}
			}
		}
    }
    
    
    public void deliverNotify(String path,FsNode node) {	
 //   	System.out.println("PATH="+path+" N="+node.asXML());
		long starttime = new Date().getTime();
		ArrayList<ModelBindObject> binds = notifybinds.get(path); // direct hit
		if (binds!=null) {
			ModelEvent event = new ModelEvent();
			event.path = path;
			event.target = node;
			event.eventtype = ModelBindEvent.NOTIFY;
			for (int i=binds.size()-1;i>-1;i--) {
				ModelBindObject bind  = binds.get(i);
				try {							
					bind.methodcall.invoke(bind.obj,event);
				} catch(Exception e) {
					System.out.println("Error during nofity delivery : "+bind.selector+" "+bind.method+" "+bind.obj);

					e.printStackTrace();
				}
			}
		}
		long time = new Date().getTime()-starttime;
		if (time>0) System.out.println("notify delivertime="+path+" "+time);
    }
    
    
    public void checkNormalQueue() {
    	while (eventqueue.size()>0) {
    		ModelBindEvent b = eventqueue.pop(); // should be a case statement
    		if (b.type == ModelBindEvent.PROPERTY) {
    			deliverProperty(b.path,(String)b.value);
    		} else if (b.type == ModelBindEvent.PROPERTIES) {
        			deliverProperties(b.path,(FsPropertySet)b.value);
    		} else if (b.type == ModelBindEvent.NOTIFY) {
    			deliverNotify(b.path,(FsNode)b.value);
    		}
    	}
    }

}
