package org.springfield.lou.websocket;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class LouWSAsyncThread extends Thread implements LouWSAsyncEmitterLifecycleListener{
	
	private boolean running = true;
	private List<LouWSAsyncEmitter> emitters;
	
	public LouWSAsyncThread() {
		System.out.println("starting new LouWSAsyncThread");
		emitters = new ArrayList<LouWSAsyncEmitter>();
		running = true;
	}
	
	public void addEmitter(LouWSAsyncEmitter emitter){
		emitter.setLifecycleListener(this);
		emitters.add(emitter);
	}
	
	public void stopExecution(){
		this.running = false;
	}

	public void run(){
		
		while(running){
			
			for(LouWSAsyncEmitter emitter : emitters){
				
				Date lastEmit = emitter.getLastEmit();
				Date now = new Date();
				
				if(lastEmit == null || now.getTime() - lastEmit.getTime() >= emitter.getInterval()){
				
					JSONObject message = new JSONObject();
					JSONArray messages = new JSONArray();
					
					for(JSONObject messageObj : emitter.getScheduledMessages()){
						messages.add(messageObj);
					}
					
					message.put("messages", messages);
					
					try {
						emitter.getRemote().sendText(message.toJSONString());
						emitter.clear();
						emitter.setLastEmit(now);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		
		System.out.println("Stopped LouWSAsyncThread execution!");
		
	}

	@Override
	public void onEmitterDestroy(LouWSAsyncEmitter emitter) {
		System.out.println("LouWSAsyncThread.onEmitterDestroy(" + emitter + ")");
		this.emitters.remove(emitter);
	}

}
