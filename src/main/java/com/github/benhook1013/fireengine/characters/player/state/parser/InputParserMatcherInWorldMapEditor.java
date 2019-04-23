package com.github.benhook1013.fireengine.characters.player.state.parser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.github.benhook1013.fireengine.characters.commands.ActionCommand;
import com.github.benhook1013.fireengine.characters.commands.character_commands.map_editor.CreateExit;
import com.github.benhook1013.fireengine.characters.commands.character_commands.map_editor.CreateRoom;
import com.github.benhook1013.fireengine.characters.commands.character_commands.map_editor.DestroyExit;
import com.github.benhook1013.fireengine.characters.commands.character_commands.map_editor.DestroyRoom;

/*
 *    Copyright 2017 Ben Hook
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

public class InputParserMatcherInWorldMapEditor implements InputParserMatcher {
	private Pattern createExitsPattern = Pattern.compile("(?i)CREATE EXITS? (\\w+)");
	private Pattern createRoomPattern = Pattern.compile("(?i)CREATE ROOM (\\w+)");
	private Pattern destroyExitsPattern = Pattern.compile("(?i)DESTROY EXITS? (\\w+)");
	private Pattern destroyRoomPattern = Pattern.compile("(?i)DESTROY ROOM (\\w+)");

	public InputParserMatcherInWorldMapEditor() {
	}

	@Override
	public ActionCommand match(String text) {
		Matcher matcher;

		if ((matcher = createExitsPattern.matcher(text)).matches()) {
			return new CreateExit(matcher.group(1));
		} else if ((matcher = createRoomPattern.matcher(text)).matches()) {
			return new CreateRoom(matcher.group(1));
		} else if ((matcher = destroyExitsPattern.matcher(text)).matches()) {
			return new DestroyExit(matcher.group(1));
		} else if ((matcher = destroyRoomPattern.matcher(text)).matches()) {
			return new DestroyRoom(matcher.group(1));
		} else {
			return null;
		}
	}
}
