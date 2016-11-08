package org.springfield.lou.websocket;

public interface LouWSConnectionListener {

	void onTextMessage(LouWSConnection louWSConnection, String string);
	void onOpen(LouWSConnection louWSConnection);
	void onClose(LouWSConnection louWSConnection);

}
