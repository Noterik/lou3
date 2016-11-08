package org.springfield.lou.websocket;

import java.io.IOException;

import javax.websocket.MessageHandler;
import javax.websocket.RemoteEndpoint;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.springfield.lou.application.ApplicationManager;
import org.springfield.lou.application.Html5Application;
import org.springfield.lou.application.Html5ApplicationInterface;
import org.springfield.lou.screen.Screen;

public class LouWSConnection implements MessageHandler.Partial<String>{
	
	private final RemoteEndpoint.Basic remoteEndPoint;
	private Screen s = null;
	private boolean registered = false;
	private Html5ApplicationInterface app = null;
	
	public LouWSConnection(RemoteEndpoint.Basic remoteEndpoint){
		this.remoteEndPoint = remoteEndpoint;
	}
	
	public void setScreen(Screen s){
		this.s = s;
	}
		
	public Screen getScreen(){
		return s;
	}
		
	public void setApp(Html5ApplicationInterface app){
		this.registered = true;
		this.app = app;
	}
		
	public boolean isRegistered(){
		return registered;
	}
		
	public Html5ApplicationInterface getApp(){
		return this.app;
	}
		
	public void emit(String message){
		try {
			remoteEndPoint.sendText(message);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void onMessage(String message, boolean arg1) {
		JSONObject messageObj = (JSONObject) JSONValue.parse(message);
		
		String command = (String) messageObj.get("command");
		
		//We first need to bind the socket to a screen!
		if(command.equals("register")){
			String application = (String) messageObj.get("app");
			Html5Application app = (Html5Application) ApplicationManager.instance().getApplication(application);
			String screenId = (String) messageObj.get("screenId");
			Screen s = app.getScreen(screenId);
			
			if(s.getSocket() == null){
				s.setSocket(this);
				this.setScreen(s);
			}
			
			this.setApp(app);
		}else if(this.isRegistered()){
			JSONObject arguments = (JSONObject) messageObj.get("args");
				
			Html5ApplicationInterface app = this.getApp();
			Screen s = this.getScreen();
			
			app.call(s, command, arguments);
		}
		
	}
	
}

/*
public class LouWSConnection extends MessageInbound {
	
	private final String id;
	private boolean open = false;
	private boolean registered = false;
	private List<LouWSConnectionListener> listeners;
	private Html5ApplicationInterface app;
	private Screen s;
	
	public LouWSConnection(int id, LouWSConnectionListener listener) {
		this.id = "" + id;
		listeners = new ArrayList<LouWSConnectionListener>();
		listeners.add(listener);
	}
	
	public void listen(LouWSConnectionListener listener){
		listeners.add(listener);
	}
	
	public boolean isOpen(){
		return open;
	}
	
	public String getId(){
		return id;
	}
	
	public void setScreen(Screen s){
		this.s = s;
	}
	
	public Screen getScreen(){
		return s;
	}
	
	public void setApp(Html5ApplicationInterface app){
		this.registered = true;
		this.app = app;
	}
	
	public boolean isRegistered(){
		return registered;
	}
	
	public Html5ApplicationInterface getApp(){
		return this.app;
	}
	
	public void emit(String message){
		CharBuffer buffer = CharBuffer.wrap(message);
		try {
			super.getWsOutbound().writeTextMessage(buffer);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Override
    protected void onOpen(WsOutbound outbound) {
		open = true;
		for(LouWSConnectionListener listener : listeners){
			listener.onOpen(this);
		}
    }

	@Override
	protected void onClose(int status) {
		// TODO Auto-generated method stub
		super.onClose(status);
		open = false;
		for(LouWSConnectionListener listener : listeners){
			listener.onClose(this);
		}
	}

	@Override
	protected void onBinaryMessage(ByteBuffer arg0) throws IOException {
		// TODO Auto-generated method stub

	}

	@Override
	protected void onTextMessage(CharBuffer message) throws IOException {
		for(LouWSConnectionListener listener : listeners){
			listener.onTextMessage(this, message.toString());
		}
	}

}
*/