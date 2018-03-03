package fireengine.client_io;

import fireengine.client_io.exceptions.Client_Connection_Exception;
import fireengine.session.Session;

/*
 *    Copyright 2017 Ben Hook
 *    Client_Connection_Interface.java
 *    
 *    Licensed under the Apache License, Version 2.0 (the "License"); 
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *    
 *    		http://www.apache.org/licenses/LICENSE-2.0
 *    
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

/**
 * 
 * 
 * @author Ben Hook
 */
public interface Client_Connection_Interface {
	public void setupConnection(Session sess) throws Client_Connection_Exception;

	public void writeToConnection(ClientConnectionOutput output, boolean ansi);

	public void acceptInput();

	public void refuseInput();

	public String readFromConnection();

	// Will stop reading input while allowing remaining output to be sent till
	// Session is ready to close.
	public void shutdown();

	public void close();
}
