package org.springfield.lou.websocket;

import javax.websocket.MessageHandler;
import javax.websocket.Session;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.springfield.lou.screen.Screen;

public class LouWebSocketConnection implements MessageHandler.Partial<String> {

	Session session;
	Screen screen;
	
	public LouWebSocketConnection(Session s,Screen sc) {
		session = s;
		screen = sc;
	}
	
	@Override
	public void onMessage(String data, boolean arg1) {
		screen.setSeen();
		int pos = data.indexOf("put(");
		if (pos!=-1) {
			data = data.substring(pos+4);
			int pos2 = data.indexOf(")");
			if (pos2!=-1) {
				String target = data.substring(0,pos2);
				int pos3 = target.indexOf(",");
				String from = target.substring(0,pos3);
				target = target.substring(pos3+1);
				String content = data.substring(pos2+2);
				screen.webSocketPut(from,content);
			}
		}
	}
	
	public void send(String message) {
		if (!session.isOpen()) return;
		try {
			session.getBasicRemote().sendText(message);
			screen.setSeen(); // didn't get a error so we assume its alive
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
}
