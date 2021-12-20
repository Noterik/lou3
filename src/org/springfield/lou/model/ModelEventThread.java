package org.springfield.lou.model;


public class ModelEventThread extends Thread {
	private boolean running = false;
	private String type;
	private ModelEventManager parent;
	
	public ModelEventThread(String t,ModelEventManager b) {
		super("bind thread "+t);
		type = t;
		parent = b;
		running = true;
		start();
	}
	
	public void run() {
		while (running) {
			try {	
				doWork();
				synchronized (this) {
					this.wait(10*1000);
				}
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	private void doWork() {
	//	parent.checkNormalQueue();
	}
	
	public void check() {
		synchronized (this) {
			this.notify();
		}
	}
}
