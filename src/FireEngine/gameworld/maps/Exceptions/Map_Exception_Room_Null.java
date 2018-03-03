package FireEngine.gameworld.maps.Exceptions;

/*
 *    Copyright 2017 Ben Hook
 *    Map_Exception_Room_Null.java
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

public class Map_Exception_Room_Null extends Map_Exception {
	private static final long serialVersionUID = 1L;

	public Map_Exception_Room_Null(String message) {
		super(message);
	}

	public Map_Exception_Room_Null(Throwable throwable) {
		super(throwable);
	}

	public Map_Exception_Room_Null(String message, Throwable throwable) {
		super(message, throwable);
	}
}
