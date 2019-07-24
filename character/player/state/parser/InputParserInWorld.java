package fireengine.character.player.state.parser;

import fireengine.character.command.ActionCommand;

/*
 *    Copyright 2019 Ben Hook
 *    InputParserInWorld.java
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

public class InputParserInWorld implements InputParser {
	private static InputParserInWorld instance = new InputParserInWorld();
	private static Object instanceLock = new Object();

	private InputParserMatcherInWorldGeneral generalMatcher = new InputParserMatcherInWorldGeneral();
	private InputParserMatcherInWorldMapEditor mapEditorMatcher = new InputParserMatcherInWorldMapEditor();

	private InputParserInWorld() {
	}

	@Override
	public ActionCommand parse(String text) {
		text = InputParser.clean(text);
		ActionCommand foundCommand;

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

	public static InputParserInWorld getInstance() {
		if (instance == null) {
			synchronized (instanceLock) {
				if (instance == null) {
					instance = new InputParserInWorld();
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
