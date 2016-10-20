package org.springfield.lou.model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.springfield.fs.FSList;
import org.springfield.fs.FSListManager;
import org.springfield.fs.FsNode;
import org.springfield.lou.controllers.Html5Controller;
import org.springfield.lou.screen.Screen;

public class TimeLineWatcher extends Html5Controller {

	ModelEventManager eh;
	String timer;
	String starttime;
	String duration;
	String path;
	FSList list;
	FsNode oldnode;
	
	public TimeLineWatcher(String p,String t,String s,String d,ModelEventManager h) {
		eh=h;
		path = p;
		timer = t;
		starttime = s;
		duration = d;
		eh.onPropertyUpdate(timer,"onTimerEvent", this);
		// get the nodes we are working with
		list = FSListManager.get(p,false);
		//System.out.println("LIST COUNT="+list.size());
	}
	
	public void onTimerEvent(ModelEvent e) {
		FsNode n = e.getTargetFsNode();
		try {
			Double currenttime = Double.parseDouble(n.getProperty("currenttime"));
			//System.out.println("CURTIME="+currenttime);
			
			FsNode newnode = getCurrentFsNode(currenttime);
			if (newnode==null && oldnode!=null) {
				// ok so we lost a current block make a event for it
				//System.out.println("LOST BLOCK="+oldnode.asXML());
				eh.deliverTimeLineNotify(path+":"+timer,ModelBindEvent.TIMELINENOTIFY_LEAVE ,oldnode);
				oldnode = null;
			} else if (newnode!=oldnode) {
				// ok we are  in a different block make a event
				eh.deliverTimeLineNotify(path+":"+timer,ModelBindEvent.TIMELINENOTIFY_ENTER,newnode);
				//System.out.println("NEW BLOCK="+newnode.asXML());
				oldnode = newnode;
			}
			
		} catch(Exception error) {
			
		}
	}
	
	public FsNode getCurrentFsNode(double time) {
		FsNode match = null;
		List<FsNode> nodes = list.getNodes();
		if (nodes != null) {
			for (Iterator<FsNode> iter = nodes.iterator(); iter.hasNext();) {
				FsNode node = (FsNode) iter.next();
				if (time>=node.getStarttime() && time<=(node.getDuration()+node.getStarttime())) {
					match = node;
				}
			}
		}
		return match;
	}
	
	
	public String getScreenId() {
		return this.toString();
	}
	
	public String getSelector() {
		return this.toString();
	}
	
	public int getApplicationHashCode() {
		return this.hashCode();

	}


	
	
}
