package org.springfield.lou.websocket;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.websocket.RemoteEndpoint.Basic;

import org.json.simple.JSONObject;

public class LouWSAsyncEmitter implements LouWSEmitter{
	
	private	List<JSONObject> scheduledMessages; 
	private int interval;
	private Basic remote;
	private Date lastEmit = null;
	private LouWSAsyncEmitterLifecycleListener listener = null;

	protected LouWSAsyncEmitter(Integer interval) {
		this.interval = interval;
		this.scheduledMessages = new ArrayList<JSONObject>();
	}
	
	protected void setLastEmit(Date date){
		this.lastEmit = date;
	}
	
	protected Date getLastEmit(){
		return lastEmit;
	}
	
	protected int getInterval(){
		return interval;
	}

	protected void clear(){
		this.scheduledMessages.clear();
	}
	
	protected List<JSONObject> getScheduledMessages(){
		return this.scheduledMessages;
	}
	
	@Override
	public void emit(JSONObject message) {
		this.scheduledMessages.add(message);
	}	

	@Override
	public void setRemote(Basic remoteEndPoint) {
		this.remote = remoteEndPoint;
	}
	
	protected Basic getRemote(){
		return this.remote;
	}
	
	public void destroy(){
		this.listener.onEmitterDestroy(this);
	}

	public void setLifecycleListener(LouWSAsyncEmitterLifecycleListener listener) {
		this.listener = listener;
	}
}
