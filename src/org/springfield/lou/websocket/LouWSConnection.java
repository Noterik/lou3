/*
* LouWSConnection.java
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

import java.io.IOException;

import javax.websocket.MessageHandler;
import javax.websocket.RemoteEndpoint;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.springfield.lou.application.ApplicationManager;
import org.springfield.lou.application.Html5Application;
import org.springfield.lou.application.Html5ApplicationInterface;
import org.springfield.lou.screen.Screen;

/**
 * Responsible for handling both sending and receiving data
 * from/to the client. Forwards commands to the specific Html5Application.
 */

public class LouWSConnection implements MessageHandler.Partial<String>{

	private final RemoteEndpoint.Basic remoteEndPoint;

	//The screen associated with this WebSocket connection.
	private Screen s = null;

	//Indicates if the socket is ready to be used.
	private boolean registered = false;

	//Reference to the app associated with this Websocket Connection.
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

	//Emits a String message to the client.
	public void emit(String message){
		try {
			remoteEndPoint.sendText(message);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	//Called whenever a message is received from the client.
	@Override
	public void onMessage(String message, boolean arg1) {
		JSONObject messageObj = (JSONObject) JSONValue.parse(message);

		//Extract the command from the data.
		String command = (String) messageObj.get("command");

		//Check if this is the "register" command, if so, we don't forward it. But
		//we cache some stuff.
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
		//Otherwise we attempt to call the command on the app.
		//TODO: Still need proper error handling.
		}else if(this.isRegistered()){
			JSONObject arguments = (JSONObject) messageObj.get("args");

			Html5ApplicationInterface app = this.getApp();
			Screen s = this.getScreen();

			app.call(s, command, arguments);
		}

	}

}
