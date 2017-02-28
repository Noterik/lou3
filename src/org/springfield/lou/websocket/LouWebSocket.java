/*
* LouWSAnnotation.java
*
* Copyright (c) 2012 Noterik B.V.
*
* This file is part of Lou, related to the Noterik Springfield project.
*
* Lou is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* Lou is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with Lou.  If not, see <http://www.gnu.org/licenses/>.
*/

package org.springfield.lou.websocket;

import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnOpen;
import javax.websocket.RemoteEndpoint;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import org.springfield.lou.application.ApplicationManager;
import org.springfield.lou.application.Html5ApplicationInterface;
import org.springfield.lou.screen.Screen;

//Responsible for initializing/creating LouWSConnections from Lou client. 

//Register to /lou/ws
@ServerEndpoint("/ws")
public class LouWebSocket {
	
	
	public LouWebSocket(){
		
	}

	@OnOpen
	public void open(Session session){
		RemoteEndpoint.Basic remoteEndpointBasic = session.getBasicRemote();


		// the websocket session needs to be linked to a screen object, for this we need to know
		//the session id.
		String url  = session.getQueryString();
		String screenid = url.substring(url.indexOf("screenid=")+9);
		String tappname = screenid.substring(0,screenid.indexOf("/1/screen/"));
		Html5ApplicationInterface app = ApplicationManager.instance().getApplication(tappname);
		Screen screen = app.getScreen(screenid);
		if (screen!=null) {
			LouWebSocketConnection wc = new LouWebSocketConnection(session,screen);
			session.addMessageHandler(wc);
			screen.setWebSocketConnection(wc);
		}
	}

	@OnClose
	public void close(Session session){
		//System.out.println("Websocket closed!");
	}
	
	@OnError
	public void onError(Session session, Throwable thr) {
		System.out.println("Websocket error (closed?)"+thr.getMessage());
	}
	
	

}
