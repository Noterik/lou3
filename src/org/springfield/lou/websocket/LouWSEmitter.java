package org.springfield.lou.websocket;

import javax.websocket.RemoteEndpoint.Basic;

import org.json.simple.JSONObject;

public interface LouWSEmitter {
	public void emit(JSONObject message);
	public void setRemote(Basic remoteEndPoint);
	public void destroy();
}
