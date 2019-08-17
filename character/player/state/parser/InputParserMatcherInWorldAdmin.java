package fireengine.character.player.state.parser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import fireengine.character.command.CommandAction;
import fireengine.character.command.character_comand.admin.Shutdown;
import fireengine.character.command.character_comand.map_editor.CreateExit;
import fireengine.character.command.character_comand.map_editor.CreateRoom;
import fireengine.character.command.character_comand.map_editor.DestroyExit;
import fireengine.character.command.character_comand.map_editor.DestroyRoom;

/*
 *    Copyright 2019 Ben Hook
 *    InputParserMatcherInWorldMapEditor.java
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

public class InputParserMatcherInWorldAdmin implements InputParserMatcher {
	private Pattern shutdown = Pattern.compile("(?i)SHUT ?DOWN");

	public InputParserMatcherInWorldAdmin() {
	}

	@Override
	public CommandAction match(String text) {
		@SuppressWarnings("unused")
		Matcher matcher; // This is used when need to retrieve capture group.

		if ((matcher = shutdown.matcher(text)).matches()) {
			return new Shutdown();
		} else {
			return null;
		}
	}
}