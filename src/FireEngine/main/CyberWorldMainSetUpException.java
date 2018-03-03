package FireEngine.main;

/*
 *    Copyright 2017 Ben Hook
 *    CyberWorldMainSetUpException.java
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
 * An exception thrown representing various problems during setup.
 * 
 * @author Ben Hook
 */
public class CyberWorldMainSetUpException extends Exception {
	private static final long serialVersionUID = 1L;

	public CyberWorldMainSetUpException(String message) {
		super(message);
	}

	public CyberWorldMainSetUpException(Throwable throwable) {
		super(throwable);
	}

	public CyberWorldMainSetUpException(String message, Throwable throwable) {
		super(message, throwable);
	}
}