package fireengine.session;

import fireengine.character.command.menu_command.MenuCommand;
import fireengine.character.command.menu_command.MenuCommand1;
import fireengine.character.command.menu_command.MenuCommand2;
import fireengine.character.command.menu_command.MenuCommand3;
import fireengine.character.command.menu_command.MenuCommand4;
import fireengine.character.command.menu_command.MenuCommand5;
import fireengine.character.command.menu_command.MenuCommand6;
import fireengine.character.command.menu_command.MenuCommandBack;
import fireengine.util.StringUtils;

/*
 *    Copyright 2019 Ben Hook
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
 * @author Ben Hook
 */
public class MenuCommandParser {
	public MenuCommandParser() {
	}

	// TODO Review this for less quick and dirty implementation. Possibly use regex
	// matcher implementation.
	public MenuCommand parse(String text) {
		text = StringUtils.cleanInput(text);
		String compText = text.toUpperCase();

		if (compText.equals("BACK")) {
			return new MenuCommandBack();
		} else if (compText.equals("0")) {
			return new MenuCommandBack();
		} else if (compText.equals("1")) {
			return new MenuCommand1();
		} else if (compText.equals("2")) {
			return new MenuCommand2();
		} else if (compText.equals("3")) {
			return new MenuCommand3();
		} else if (compText.equals("4")) {
			return new MenuCommand4();
		} else if (compText.equals("5")) {
			return new MenuCommand5();
		} else if (compText.equals("6")) {
			return new MenuCommand6();
		} else {
			return null;
		}
	}

}
