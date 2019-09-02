package org.springfield.lou;

import org.springfield.lou.application.ApplicationManager;
import org.springfield.lou.model.*;
import org.springfield.lou.homer.LazyHomer;
import org.springfield.lou.model.ModelEventManager;
import org.springfield.mojo.interfaces.ServiceInterface;
import org.springfield.mojo.interfaces.ServiceManager;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springfield.fs.FsNode;

public class ServiceHandler implements ServiceInterface{

	private static ServiceHandler instance;
	private static RemoteHandler rh;

	public static ServiceHandler instance() {
		if (instance==null) {
			instance = new ServiceHandler();
			ServiceManager.setService(instance);
		}
		
		// also start a remote hander to talk to Jimmy
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
		
		//if (rh==null) rh = new RemoteHandler();
		
		int pos = uri.indexOf("(");
		if (pos!=-1) {
			String command = uri.substring(0,pos);
			String path = uri.substring(pos+1);
			path = path.substring(0,path.length()-1);
			if (command.equals("notify")) {
				ModelEventManager em = Model.getEventManager();
				FsNode msg = json2msg(value);
				System.out.println("MSG2="+value);
				/*
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
				*/
				System.out.println("EM="+em+" P="+path);
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
	
	private FsNode json2msg(String value) {
		FsNode msg = new FsNode("msg","1");
		
		JSONParser parser = new JSONParser();
		try {
			JSONObject jo = (JSONObject) parser.parse(value);
			for (Object key : jo.keySet()) {
				String keyname = (String)key;
				System.out.println("KEYNAME="+keyname);
				
				Object o = jo.get(keyname); 
				if (o instanceof String) {
					//String value = (String)o;
					System.out.println("KEYNAME="+keyname+" VALUE="+value);
				} else {
					System.out.println("KEYNAME="+keyname+" OBJECT?");
				}
		
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
		return msg;
	}
}
