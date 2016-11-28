package org.springfield.lou.websocket;

public class LouWSEmitterFactory {

	private LouWSAsyncThread thread;
	private int defaultInterval = 100;
	
	public enum Type{
		SYNC,
		ASYNC
	}
	
	public LouWSEmitterFactory(LouWSAsyncThread thread) {
		this.thread = thread;
	}

	public LouWSEmitter create(LouWSEmitterFactory.Type type, int interval){
		if(type.equals(LouWSEmitterFactory.Type.SYNC)){
			return this.create(type);
		}else{
			LouWSAsyncEmitter emitter = new LouWSAsyncEmitter(interval);
			thread.addEmitter(emitter);
			return emitter;
		}
	}
	
	public LouWSEmitter create(LouWSEmitterFactory.Type type){
		if(type.equals(LouWSEmitterFactory.Type.SYNC)){
			return new LouWSSyncEmitter();
		}else{
			return this.create(type, this.defaultInterval);
		}
		
	}
}
