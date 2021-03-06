package org.springfield.lou.screen;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

import javax.management.modelmbean.ModelMBean;

import org.json.simple.JSONObject;
import org.springfield.fs.FSList;
import org.springfield.fs.FsNode;
import org.springfield.lou.application.Html5ApplicationInterface;
import org.springfield.lou.controllers.Html5Controller;
import org.springfield.lou.model.Model;
import org.apache.commons.codec.binary.Base64;

public class Html5Element {
	private String selector;
	private Screen screen;
	private Map<String, Object> variables;
	private String javascript=null;
	private Html5Controller controller = null;
	
	public Html5Element(Screen s,String sel) {
		selector = sel;
		screen  = s;
	}
	
	public boolean show() {
		screen.send("show("+selector+")");
		return true;
	}
	
	public boolean hide() {
		screen.send("hide("+selector+")");
		return true;
	}
	
	public boolean remove() {
		// need to remove this element and all that its tracking/doing
		screen.removeContent(selector.substring(1));
		if (controller!=null) {
			screen.getApplication().removeEvents(controller);
		} else {
			//System.out.println("remove called on null controller "+selector);
		}
		return true;
	}
	
	public boolean radarping(String pingtargets,String receiver) {
		screen.send("radarping("+selector.substring(1)+")="+receiver+","+pingtargets);
		return true;
	}
	
	public boolean externalprogram() {
		screen.send("externalprogram("+selector+")");
		return true;
	}
	
	public boolean screenshot(String name) {
		screen.send("screenshot("+selector.substring(1)+")="+name);
		return true;
	}
	
	public boolean internalprogram() {
		screen.send("internalprogram("+selector+")");
		return true;
	}
	

	public boolean draggable() {
		screen.send("draggable("+selector+")");
		return true;
	}
	
	public boolean setTrackDelay(int time) {
		screen.send("settrackdelay("+selector+")="+time);
		return true;
	}
	
	public boolean autohidecursor(String time) {
		screen.send("autohidecursor("+selector+")="+time);
		return true;
	}
	
	public boolean fullscreen() {
		screen.send("fullscreen("+selector+")");
		return true;
	}
	
	public boolean exitfullscreen() {
		screen.send("exitfullscreen("+selector+")");
		return true;
	}
	
	public boolean css(String elementname,String value) {
		screen.setDiv(selector.substring(1),"style:"+elementname+":"+value);
		return true;
	}
	
	public boolean image(byte[] blob) {
    	String encoded = new String(Base64.encodeBase64(blob));
		screen.send("image("+selector.substring(1)+")="+encoded);
		return true;
	}
	
	public boolean html(String html) {
		screen.send("html("+selector.substring(1)+")="+html);
		return true;
	}
	
	public boolean location(String url) {
		screen.send("location("+selector.substring(1)+")="+url);
		return true;
	}
	
	public boolean click() {
		screen.send("click("+selector.substring(1)+")");
		return true;
	}
	
	public boolean download(String url,String filename) {
		screen.send("download("+selector.substring(1)+")="+url+","+filename);
		return true;
	}
	
	public boolean download(byte[] blob,String filename) {
    	String encoded = new String(Base64.encodeBase64(blob));
		screen.send("downloadblob("+selector.substring(1)+")="+filename+","+encoded);
		return true;
	}
	
	public boolean watch(String url,String target) {
		screen.send("watch("+selector.substring(1)+")="+url+","+target);
		return true;
	}
	
	public boolean scrollTop(int offset) {
		screen.send("scrolltop("+selector.substring(1)+")="+offset);
		return true;
	}
	
	public boolean translate(String x,String y) {
		screen.send("translateXY("+selector.substring(1)+")="+x+","+y);
		return true;
	}
	
	public boolean play() {
		screen.send("play("+selector.substring(1)+")");
		return true;
	}
	
	public boolean focus() {
		screen.send("focus("+selector.substring(1)+")");
		return true;
	}

	public boolean pause() {
		screen.send("pause("+selector.substring(1)+")");
		return true;
	}
	
	public boolean volume(float level) {
		screen.send("volume("+selector.substring(1)+")="+level);
		return true;
	}
	
	
	public boolean autoplay(boolean state) {
		screen.send("autoplay("+selector.substring(1)+")="+state);
		return true;
	}
	
	public boolean loop(boolean state) {
		screen.send("loop("+selector.substring(1)+")="+state);
		return true;
	}
	
	public boolean append(String html) {
		screen.send("append("+selector.substring(1)+")="+html);
		return true;
	}
	
	public void syncvars(String vars) {
		// vars we want synced between client and server (one way for now)
		JSONObject data = new JSONObject();
		
		// get the 3 nodes we might want
		FsNode controllernode = controller.getControllerNode(selector);
		
		String[] items = vars.split(",");
		for (int i=0;i<items.length;i++) {
			String[] item = items[i].split("/");
			String type = item[0];
			String key = item[1];
		//	System.out.println("T="+type);
		//	System.out.println("K="+key);
			if (type.equals("controller")) {
				data.put(items[i], controllernode.getProperty(key));
			}
		}
		screen.send("syncvars("+selector.substring(1)+")="+data.toJSONString());
	}
	
	public boolean template(String template) {
			// extend for the real path
		    if (template==null || template.equals("")) {
		    	if (controller!=null) {
		    		template = controller.getDefaultTemplate();
		    	}
		    } else {
		    	String part = screen.getApplication().getAppname().substring(screen.getApplication().getAppname().lastIndexOf("/")+1);
		    	template = "/springfield/tomcat/webapps/ROOT/eddie/apps/"+part+"/components/"+template;	
		    }
			StringBuffer str = null;
				try {
					str = new StringBuffer();
					BufferedReader br;
					br = new BufferedReader(new FileReader(template));
					String line = br.readLine();
					while (line != null) {
						str.append(line);
						str.append("\n");
						line = br.readLine();
					 }
					br.close();
				} catch (FileNotFoundException e) {
					System.out.println("COULD NOT FIND TEMPLATE ("+selector+") : "+template);
				} catch (IOException e) {
					e.printStackTrace();
				}
				JSONObject data = new JSONObject();
				data.put("template", str.toString());
				screen.send("template("+selector.substring(1)+")="+data);
				return true;
	}
	
	public boolean update(JSONObject data) {
		if (javascript!=null) {
			screen.send("update("+selector.substring(1)+")="+data);
		} else {
			if (controller!=null) controller.update(data);
		}
	
		return true;
	}
	
	public boolean render() {
		return parsehtml(new JSONObject(),"");
	}
	
	public boolean render(JSONObject json) {
		return parsehtml(json,"");
	}
	
	public boolean render(JSONObject json,String tagname) {
		return parsehtml(json,tagname);
	}
	
	public boolean parsehtml(JSONObject json) {
		return parsehtml(json,"");
	}
	
	public boolean parsehtml(JSONObject json,String tagname) {
		FsNode node = screen.getModel().getNode("/app/component/view/"+selector);
		if (node!=null) {
			String template = node.getProperty("template");
			// extend for the real path
			String part = screen.getApplication().getAppname().substring(screen.getApplication().getAppname().lastIndexOf("/")+1);
			
		    if (template==null || template.equals("")) {
	    		template = controller.getDefaultTemplate();
		    } else {
				template = "/springfield/tomcat/webapps/ROOT/eddie/apps/"+part+"/components/"+template;	
		    }
			if (template!=null && !template.equals("")) {
				// did we already send this one before ?
				if(screen.alreadySendTemplate(template)) {
					json.put("tmpcrc", template.hashCode());
					json.put("tagname", tagname);
					screen.send("parsehtml("+selector.substring(1)+")="+json);
				} else {
					StringBuffer str = null;
					try {
						str = new StringBuffer();
						BufferedReader br;
						br = new BufferedReader(new FileReader(template));
						String line = br.readLine();
						while (line != null) {
							str.append(line);
							str.append("\n");
							line = br.readLine();
						}
						br.close();
					} catch (FileNotFoundException e) {
						System.out.println("COULD NOT FIND TEMPLATE ("+selector+") : "+template);
					} catch (IOException e) {
						e.printStackTrace();
					}
					json.put("template", str.toString());
					json.put("newcrc",template.hashCode());
					json.put("tagname", tagname);
					screen.send("parsehtml("+selector.substring(1)+")="+json);
					screen.setSendTemplate(template);
				}
				return true;
			} else {
				html("NO TEMPLATE IN VIEW NODE "+selector+" DEFINED");
				return false;
			}
		} else {
			html("NO VIEW NODE "+selector+" DEFINED");
			return false;
		}
	}
	
	public boolean loadScript(Html5Controller c) {
		controller = c;
		//Model model = screen.getApplication().getModel();
		//System.out.println("SELECTOR="+selector);
		FsNode node = screen.getModel().getNode("/app/component/view/"+selector+"/controller/"+controller.getControllerName());
		if (node!=null) {
			String scriptname = node.getProperty("javascript");
			if (scriptname==null || scriptname.equals("")) {
				scriptname = controller.getDefaultScript();
			} else {
				// extend for the real path
				String part = screen.getApplication().getAppname().substring(screen.getApplication().getAppname().lastIndexOf("/")+1);
				scriptname = "/springfield/tomcat/webapps/ROOT/eddie/apps/"+part+"/components/"+scriptname;	
			}
			if (scriptname!=null && !scriptname.equals("")) {
				
				StringBuffer str = null;
				try {
					str = new StringBuffer();
					BufferedReader br;
					br = new BufferedReader(new FileReader(scriptname));
					String line = br.readLine();
					while (line != null) {
						str.append(line);
						str.append("\n");
						line = br.readLine();
					 }
					br.close();
				} catch (FileNotFoundException e) {
					System.out.println("COULD NOT FIND JS : "+scriptname);
					return false;
				} catch (IOException e) {
					e.printStackTrace();
					return false;
				}

				screen.setScript(selector, str.toString());
				javascript = scriptname;
				//System.out.println("SET JAVACRIPT ON "+selector+" "+javascript+" THIS="+this);
				return true;
			} else {
				html("NO SCRIPT IN VIEW NODE "+selector+" DEFINED");
				return false;
			}
		} else {
			//html("NO VIEW NODE "+selector+" DEFINED");
			return false;
		}

	}
	
	public boolean css(String[] list) {
		for (int i=0;i<list.length;i++) {
			screen.setDiv(selector.substring(1),"style:"+list[i]);
		}
		return true;
	}
	
	public boolean on(String eventtype,String callbackmethod) { // should this be removed and make the main app also a controller? (Daniel)
		screen.bind(selector,eventtype,callbackmethod,screen.getApplication());
		return true;
	}
	
	public boolean on(String eventtype,String callbackmethod,Object callbackobject) {
		return on(eventtype,"",callbackmethod,callbackobject);
	}
	
	public boolean on(String eventtype,String eventpadding,String callbackmethod,Object callbackobject) {
		screen.bind(selector,eventtype,eventpadding,callbackmethod,callbackobject);
		return true;
	}
	
	public boolean val(String value) {
		screen.send("val("+selector.substring(1)+")="+value);
		return true;
	}
	
	public void track(String vars,String callbackmethod,Object callbackobject) {
		screen.bind(selector,"track/"+vars,callbackmethod,callbackobject);
	}
	
	public void trackAndStore(String vars,String path) {
		screen.bind(selector,"track/"+vars,path,null);
	}
	
	public boolean on(String eventtype,Html5Controller c) {
		controller = c;
		controller.setScreen(screen);
		controller.setModel(screen.getModel());
		controller.attach(selector,eventtype);
		return true;
	}
	
	public boolean html(Html5Controller c) {
		controller = c;
		controller.setScreen(screen);
		controller.setModel(screen.getModel());
		controller.attach(selector);
		return true;
	}
	
	public boolean attach(Html5Controller c) {
		controller = c;
		controller.setScreen(screen);
		controller.setModel(screen.getModel());
		FsNode node = screen.getModel().getNode("/app/component/view/"+selector);
		if (node!=null) {
			String style = node.getProperty("style");
			if (style!=null) {
				screen.loadStyleSheet(style);
			}
		}
		controller.attach(selector);
		if (javascript==null) {
			node = screen.getModel().getNode("/app/component/view/"+selector+"/controller/"+controller.getControllerName());
			if (node!=null) {
				String scriptname = node.getProperty("javascript");
				if (scriptname!=null && !scriptname.equals("")) {
					loadScript(c); 
				}
			}
		}
		return true;
	}
	
	public boolean append(String elementname,String elementid,Html5Controller c) {
		//html("<"+elementname+" id=\""+elementid+"\"></"+elementname+">"); should be append !
		append("<"+elementname+" id=\""+elementid+"\"></"+elementname+">");
		screen.get("#"+elementid).attach(c);
		return true;
	}
	
	public boolean append(String elementname, String elementid, String classes, Html5Controller c) {
		append("<"+elementname+" id=\""+elementid+"\" class=\""+classes+"\"></"+elementname+">");
		screen.get("#"+elementid).attach(c);
		return true;
	}
	
	public boolean append(String elementname, String parentid, String childid, String classes, Html5Controller c) {
		append("<"+elementname+" id=\""+childid+"\" class=\""+classes+"\"></"+elementname+">");
		screen.get("#"+parentid).attach(c);
		return true;
	}
	
	public Object getVariable(String name) {
		return variables.get(name);
	}
	
	public void setVariable(String name,Object value) {
		variables.put(name,value);
	}

	public void setViewProperty(String name,String value) {
		FsNode view  = screen.getModel().getNode("/app/component/view/"+selector);
		if (view==null) {
			view = new FsNode("view",selector);
			screen.getModel().putNode("/app",view);
		}
		view.setProperty(name,value);
	}
	
	public void setControllerProperty(String controllername,String name,String value) {
		FsNode controller  = screen.getModel().getNode("/app/component/view/"+selector+"/controller/"+controllername);
		
		if (controller==null) {
			controller = new FsNode("controller",controllername);
			screen.getModel().putNode("/app/component/view/"+selector,controller);
		}
		controller.setProperty(name,value);
	}
	
	public Html5Controller getController() {
		return controller;
	}
	
	public boolean gethtml() {
		screen.send("gethtml("+selector.substring(1)+")");
		return true;
	}
	
	public boolean requesthtml(String method) {
		screen.send("requesthtml("+selector.substring(1)+")="+method);
		return true;
	}
	
	
}
