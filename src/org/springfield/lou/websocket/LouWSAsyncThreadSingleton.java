package org.springfield.lou.websocket;

/**
 * Contains a reference to a thread that will be used across all application to defer emitting websocket messages
 * asynchronously. 
 * 
 * Would rather use dependecy injection, but that's something that needs to implemented into the entire app
 * at a later time.
 * 
 * @author david
 *
 */

public class LouWSAsyncThreadSingleton {

	private static LouWSAsyncThread thread = new LouWSAsyncThread();
	
	public static LouWSAsyncThread getInstance(){
		return thread;
	}
	

}
