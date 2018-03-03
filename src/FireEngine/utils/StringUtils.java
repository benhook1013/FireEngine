package FireEngine.utils;

/*
 *    Copyright 2017 Ben Hook
 *    StringUtils.java
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
 * Class containing custom/toolbox type String related functions.
 * 
 * @author Ben Hook
 */
public abstract class StringUtils {
	/**
	 * Ensures first character is upper case and rest of String is lower case.
	 * 
	 * @param string
	 *            string to capitalise
	 * @return string that has been capitalised.
	 */
	public static String capitalise(String string) {
		return string = string.substring(0, 1).toUpperCase() + string.substring(1).toLowerCase();
	}

	// TODO Recursive sentence parsing for multi-sentence strings
	/**
	 * Ensures first letter of the {@link String} is capitalised and that end of the
	 * string has a period.
	 * 
	 * @param string
	 *            String to format
	 * @return formatted String
	 */
	public static String sentance(String string) {
		string = string.substring(0, 1).toUpperCase() + string.substring(1);
		String periodTest = string.substring(string.length() - 1, string.length());
		if (!periodTest.equals(".")) {
			string = string + ".";
		}
		return string;
	}
}
