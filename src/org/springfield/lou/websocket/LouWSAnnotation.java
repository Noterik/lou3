package org.springfield.lou.websocket;

import javax.websocket.OnClose;
import javax.websocket.OnOpen;
import javax.websocket.RemoteEndpoint;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

@ServerEndpoint("/ws")
public class LouWSAnnotation {
	
	@OnOpen
	public void open(Session session){
		RemoteEndpoint.Basic remoteEndpointBasic = session.getBasicRemote();
		session.addMessageHandler(new LouWSConnection(remoteEndpointBasic));
	}
	
	@OnClose
	public void close(Session session){
		System.out.println("Websocket closed!");
	}

}
