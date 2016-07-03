/* 
* Html5ApplicationInterface.java
* 
* Copyright (c) 2012 Noterik B.V.
* 
* This file is part of Lou, related to the Noterik Springfield project.
*
* Lou is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* Lou is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with Lou.  If not, see <http://www.gnu.org/licenses/>.
*/

package org.springfield.lou.application;

import java.util.ArrayList;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springfield.lou.controllers.Html5Controller;
import org.springfield.lou.model.AppInstanceModel;
import org.springfield.lou.model.AppModel;
import org.springfield.lou.model.Model;
import org.springfield.lou.screen.*;
import org.springfield.fs.*;


/**
 * Html5Application interface
 * 
 * @author Daniel Ockeloen
 * @copyright Copyright: Noterik B.V. 2012
 * @package org.springfield.lou.application
 *
 */
public interface Html5ApplicationInterface {
	public void setId(String id);
	public String getId();
	public String getAppname();
	public void setFullId(String id);
	public String getFullId();
	public String getDomain();
	public String getHtmlPath(); 
	public void shutdown();
	public void log(String msg);
	public void log(Screen s,String msg);
	public void log(Screen s,String msg,int level);	
	//public String getReferid(String ctype);
	public void setContent(String div,String content);
	public void setHtmlPath(String p);
	public void loadStyleSheet(Screen s,String sname);
	public Screen getScreen(String id);
	public Screen getNewScreen(Capabilities caps,Map<String,String[]> params);
	public void putData(String msg);
	public void put(String from,String content);
	public void putOnScreen(Screen s,String from, String content);
	public void removeScreen(String id,String username);
	public ScreenManager getScreenManager();
	public String getLibPaths();
	public void onNewUser(Screen s,String name);
	public void onLoginFail(Screen s,String name);
	public void onLogoutUser(Screen s,String name);
	public int getScreenCount();
	public int getUserCount();
	public int getScreenIdCounter();
	public AppModel getAppModel();
	public AppInstanceModel getAppInstanceModel();
//	public int getExternalInterfaceId();
	public FsNode getNode(String path);
	public String getFavicon();
//	public Model getModel();
	public String getMetaHeaders(HttpServletRequest request);
	public void setSessionRecovery(boolean s);
	public boolean getSessionRecovery();
	public ArrayList<String> getRecoveryList(); 
	public void addToRecoveryList(String name);
	public void setCallback(String name,String k,Object o);
	public Html5Controller createController(String name);
 	public void onPathUpdate(String paths,String methodname,Html5Controller callbackobject);
 	public void removeEvents(Object obj);
 	public void setProperty(String path,String value);
}
