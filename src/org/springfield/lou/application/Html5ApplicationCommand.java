/*
* Html5ApplicationCommand.java
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

import org.json.simple.JSONObject;
import org.springfield.lou.screen.Screen;

//These are commands that are registered to an HTML5Application. 

public interface Html5ApplicationCommand {
	public void call(Screen s, JSONObject arguments);
	public void call(Screen s);
}
