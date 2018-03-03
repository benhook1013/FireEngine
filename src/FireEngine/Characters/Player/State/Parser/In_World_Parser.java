package FireEngine.Characters.Player.State.Parser;

import FireEngine.Characters.Commands.Action_Command;

/*
 *    Copyright 2017 Ben Hook
 *    In_World_Parser.java
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

public class In_World_Parser implements Input_Parser_Interface {
	private static In_World_Parser instance = new In_World_Parser();
	private static Object instanceLock = new Object();

	private In_World_Matcher_General generalMatcher = new In_World_Matcher_General();
	private In_World_Matcher_Map_Editor mapEditorMatcher = new In_World_Matcher_Map_Editor();

	private In_World_Parser() {
	}

	public Action_Command parse(String text) {
		text = Input_Parser_Interface.clean(text);
		Action_Command foundCommand;

		foundCommand = generalMatcher.match(text);
		if (foundCommand != null) {
			return foundCommand;
		}
		foundCommand = mapEditorMatcher.match(text);
		if (foundCommand != null) {
			return foundCommand;
		}

		return null;
	}

	public static String getUnkownCommandText() {
		return "I don't know what you mean.";
	}

	public static In_World_Parser getInstance() {
		if (instance == null) {
			synchronized (instanceLock) {
				if (instance == null) {
					instance = new In_World_Parser();
					return instance;
				} else {
					return instance;
				}
			}
		} else {
			return instance;
		}
	}

}
