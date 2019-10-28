package org.springfield.lou;

import org.springfield.lou.controllers.HtmlController;
import org.springfield.lou.model.Model;
import org.springfield.lou.model.ModelEvent;
import org.springfield.lou.model.ModelEventManager;

public class RemoteHandler extends HtmlController {

	public RemoteHandler() {
		System.out.println("REEEEEEMOTE HANDLER CREATED !!!!! SO WE CAN TALK TO JIMMY");
		
		ModelEventManager em = Model.getEventManager();
		//System.out.println("EM="+em);
		em.onNotify("/shared[app]/remote","onRemoteMessage",this);	
	}
	
	public void onRemoteMessage(ModelEvent e) {
		System.out.println("GOT A MESSAGE FOR JIMMY ! "+e.getTargetFsNode().asXML());
		
	}
	
	
}
