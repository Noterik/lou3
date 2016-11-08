package org.springfield.lou.application;

import org.json.simple.JSONObject;
import org.springfield.lou.screen.Screen;

public interface Html5ApplicationFunction {
	public void call(Screen s, JSONObject arguments);
	public void call(Screen s);
}
