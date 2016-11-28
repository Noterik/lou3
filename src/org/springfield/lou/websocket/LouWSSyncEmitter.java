package org.springfield.lou.websocket;

import java.io.IOException;

import javax.websocket.RemoteEndpoint.Basic;

import org.json.simple.JSONObject;

public class LouWSSyncEmitter implements LouWSEmitter {
	
	public Basic remote;

	protected LouWSSyncEmitter() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void emit(JSONObject message) {
		try {
			remote.sendText(message.toJSONString());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void setRemote(Basic remoteEndPoint) {
		this.remote = remoteEndPoint;
	}

	@Override
	public void destroy() {
		// TODO Auto-generated method stub
		
	}

}
