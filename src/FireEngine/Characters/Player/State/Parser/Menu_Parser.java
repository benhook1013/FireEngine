package FireEngine.Characters.Player.State.Parser;

import FireEngine.Characters.Commands.Menu_Commands.*;

/*
 *    Copyright 2017 Ben Hook
 *    Menu_Parser.java
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

public class Menu_Parser implements Input_Parser_Interface {
	private static Menu_Parser instance = new Menu_Parser();
	private static Object instanceLock = new Object();

	private Menu_Parser() {
	}

	public Menu_Command parse(String text) {
		text = Input_Parser_Interface.clean(text);
		String compText = text.toUpperCase();

		if (compText.equals("BACK")) {
			return new Command_Back();
		} else if (compText.equals("0")) {
			return new Command_Back();
		} else if (compText.equals("1")) {
			return new Command_1();
		} else if (compText.equals("2")) {
			return new Command_2();
		} else if (compText.equals("3")) {
			return new Command_3();
		} else if (compText.equals("4")) {
			return new Command_4();
		} else if (compText.equals("5")) {
			return new Command_5();
		} else if (compText.equals("6")) {
			return new Command_6();
		} else {
			return null;
		}
	}

	public static Menu_Parser getInstance() {
		if (instance == null) {
			synchronized (instanceLock) {
				if (instance == null) {
					instance = new Menu_Parser();
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
