package org.springfield.lou.model;

import java.util.*;

public class ModelPoolNotify implements Runnable {

	ModelEvent event;
	ModelBindObject bind;
	int groupsize;
	int iamnumber;
	
	public ModelPoolNotify(ModelEvent e,ModelBindObject b,int g,int i) {
		event = e;
		bind = b;
		groupsize = g;
		iamnumber = i;
	}
	
	public void run() {
		long starttime = new Date().getTime();
		try {	
			bind.methodcall.invoke(bind.obj,event);
			//Thread.sleep(2000); JUST FOR TESTING
		} catch(Exception e) {
			System.out.println("Error during nofity delivery : "+bind.selector+" "+bind.method+" "+bind.obj);
			e.printStackTrace();
		}
		long time = new Date().getTime()-starttime;
		if (time>100) {
			System.out.println("SLOW notify delivertime("+groupsize+"/"+iamnumber+")="+time+" "+bind.selector+" "+bind.method+" "+bind.obj);
		}

	}

}
