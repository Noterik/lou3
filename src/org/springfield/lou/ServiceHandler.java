package org.springfield.lou;

import org.springfield.lou.application.ApplicationManager;
import org.springfield.lou.model.*;
import org.springfield.lou.homer.LazyHomer;
import org.springfield.lou.model.ModelEventManager;
import org.springfield.mojo.interfaces.ServiceInterface;
import org.springfield.mojo.interfaces.ServiceManager;
import org.springfield.fs.FsNode;

public class ServiceHandler implements ServiceInterface{

	private static ServiceHandler instance;

	public static ServiceHandler instance() {
		if (instance==null) {
			instance = new ServiceHandler();
			ServiceManager.setService(instance);
		}
		return instance;
	}

	public String getName() {
		return "lou";
	}

	public String get(String uri,String fsxml,String mimetype) {
		int pos = uri.indexOf("(");
		if (pos!=-1) {
			String command = uri.substring(0,pos);
			String values = uri.substring(pos+1);
			values = values.substring(0,values.length());
			String[] params = values.split(",");
			return handleGetCommand(command,params);
		}
		return null;
	}

	public String put(String uri,String value,String mimetype) {
		int pos = uri.indexOf("(");
		if (pos!=-1) {
			String command = uri.substring(0,pos);
			String path = uri.substring(pos+1);
			path = path.substring(0,path.length()-1);
			if (command.equals("notify")) {
				ModelEventManager em = Model.getEventManager();
				FsNode msg = new FsNode("msg","1");
				String[] list = value.split(",");
				if (list.length>0) {
					for (int i=0;i<list.length;i++) {
						pos = list[i].indexOf("=");
						if (pos!=-1) {
							msg.setProperty(list[i].substring(0,pos),list[i].substring(pos+1));
						}
					}
				}			
				em.notify(path,msg);
			}
		}
		return null;
	}

	public String post(String path,String fsxml,String mimetype) {
		return null;
	}

	public String delete(String path,String value,String mimetype) {
		return null;
	}

	private String handleGetCommand(String command,String[] params) {
		//System.out.println("HG="+command);
		if (command.equals("getAppWAR")) return getAppWar(params[0],params[1]);
		return null;
	}

	private String getAppWar(String appname,String version) {
		String result = ApplicationManager.getApplicationWarAsString(appname,version);
		if (result!=null) {
			System.out.println("RETURNING STRING SIZE="+result.length());
			return result;
		} else { 
			System.out.println("CAN'T find APP");
			return null;
		}
	}
}
