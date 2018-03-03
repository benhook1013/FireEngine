package FireEngine.Characters.Condition.Exceptions;

/*
 *    Copyright 2017 Ben Hook
 *    Health_Exception_0_Health.java
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

public class Health_Exception_0_Health extends Exception {
	private static final long serialVersionUID = 1L;

	public Health_Exception_0_Health(String message) {
		super(message);
	}

	public Health_Exception_0_Health(Throwable throwable) {
		super(throwable);
	}

	public Health_Exception_0_Health(String message, Throwable throwable) {
		super(message, throwable);
	}
}
