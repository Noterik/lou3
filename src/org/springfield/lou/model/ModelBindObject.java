package org.springfield.lou.model;

import java.lang.reflect.Method;

public class ModelBindObject {
	public String method;
	public String screenid;
	public int applicationhashcode;
	public String selector;
	public Object obj;
	public Method methodcall;
	
	public ModelBindObject(String m,String s,int a,String t,Object o,Method mc) {
		this.method = m;
		this.screenid = s;
		this.applicationhashcode = a;
		this.selector= t;
		this.methodcall = mc;
		this.obj = o;
	}
}
