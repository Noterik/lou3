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
import javax.websocket.OnOpen;
import javax.websocket.RemoteEndpoint;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

//Responsible for initializing/creating LouWSConnections from Lou client. 

//Register to /lou/ws
@ServerEndpoint("/ws")
public class LouWSAnnotation {

	@OnOpen
	public void open(Session session){
		RemoteEndpoint.Basic remoteEndpointBasic = session.getBasicRemote();

		//Create a new LouWSConnection that handles both receiving and sending data.
		session.addMessageHandler(new LouWSConnection(remoteEndpointBasic));
	}

	@OnClose
	public void close(Session session){
		System.out.println("Websocket closed!");
	}

}
