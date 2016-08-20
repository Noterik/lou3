/* 
* UserManager.java
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

package org.springfield.lou.performance;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * UserManager
 * 
 * @author Daniel Ockeloen
 * @copyright Copyright: Noterik B.V. 2012
 * @package org.springfield.lou.user
 */
public class PerformanceManager {

	static long totalnetworktime = 0; 
	static long totalnetworkcalls = 0; 
	
	public static void addNetworkCallTime(long time) {
		totalnetworktime +=time;
		totalnetworkcalls++;
	}
	
	public static long getTotalNetworkTime() {
		return totalnetworktime;
	}
	
	public static long getTotalNetworkCalls() {
		return totalnetworkcalls;
	}
	
	public static long getAvgNetworkCall() {
		if (totalnetworktime==0 || totalnetworkcalls==0) return 0;
		return (totalnetworktime*1000)/totalnetworkcalls;
	}
	
	
	
}
