package org.springfield.lou.websocket;

import java.util.Date;

import javax.websocket.MessageHandler;
import javax.websocket.Session;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.springfield.lou.screen.Screen;

public class LouWebSocketConnection implements MessageHandler.Partial<String> {

	Session session;
	Screen screen;
	private int sendcount=0;
	private int senddata=0;
	private int sendtime=0;
	
	private int receivecount=0;
	private int receivedata=0;
	private int receivetime=0;
	
	private boolean debug=false;
	
	public LouWebSocketConnection(Session s,Screen sc) {
		session = s;
		screen = sc;
	}
	
	
	public int getPacketReceiveCount() {
		return receivecount;
	}
	
	public int getPacketReceiveTime() {
		return receivetime;
	}
	
	public int getBytesReceiveSize() {
		return receivedata;
	}
	
	public int getBytesSendSize() {
		return senddata;
	}
	
	public int getPacketSendTime() {
		return sendtime;
	}
	
	public int getPacketSendCount() {
		return sendcount;
	}
	
	@Override
	public void onMessage(String data, boolean arg1) {
		long st = new Date().getTime();
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
				receivedata+=content.length();
				receivetime+=new Date().getTime()-st;
				receivecount++;
				//if (debug) System.out.println("ws receive ("+this.hashCode()+")"+(receivecount++)+" s="+receivedata);
			}
		}
	}
	
	public void send(String message) {
		if (!session.isOpen()) return;
		long st = new Date().getTime();
		try {
			session.getBasicRemote().sendText(message);
			screen.setSeen(); // didn't get a error so we assume its alive
			senddata+=message.length();
			sendtime+=new Date().getTime()-st;
			sendcount++;
			//System.out.println("ws send ("+this.hashCode()+")"+(sendcount++)+" s="+message.length());
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
}
