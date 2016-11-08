package org.springfield.lou.websocket;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.websocket.DeploymentException;
import javax.websocket.server.ServerContainer;

public class LouWSListener implements ServletContextListener {

	public LouWSListener() {
	}

	@Override
	public void contextDestroyed(ServletContextEvent servletContextEvent) {
	}

	@Override
	public void contextInitialized(ServletContextEvent servletContextEvent) {
		
		System.out.println("----------------LouWSListener.contextInitialized() ----------------");
		ServletContext context = servletContextEvent.getServletContext();
		
		// Get a reference to the ServerContainer
		ServerContainer serverContainer = (ServerContainer) context.getAttribute("javax.websocket.server.ServerContainer");
		// Add endpoint manually to server container
		try {
			serverContainer.addEndpoint(LouWSAnnotation.class);
		} catch (DeploymentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
