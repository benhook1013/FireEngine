package fireengine.utils;

/*
 *    Copyright 2017 Ben Hook
 *    MathUtils.java
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
 * Class containing custom/toolbox type math related functions.
 * 
 * @author Ben Hook
 */
public abstract class MathUtils {

	/**
	 * Parses a String into an Integer with a default Integer supplied in case
	 * parsing fails.
	 * 
	 * @param text
	 *            String to parse into Integer
	 * @param defaultNumber
	 *            Integer to return in case of parsing error
	 * @return number parsed from Sting or default Integer
	 */
	public static Integer parseInt(String text, Integer defaultNumber) {
		try {
			return Integer.parseInt(text);
		} catch (NumberFormatException e) {
			return defaultNumber;
		}
	}
}
