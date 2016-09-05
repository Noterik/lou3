package org.springfield.lou.model;

public class ModelBindEvent {

	public final static int PROPERTY = 1;
	public final static int PROPERTIES = 2;
	public final static int PATH = 3;
	
	public String path;
	public int type;
	public Object value;
	
	public ModelBindEvent(int t,String p,Object v) {
		type = t;
		path = p;
		value = v;
	}
}
