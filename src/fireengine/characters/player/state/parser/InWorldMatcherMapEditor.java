package fireengine.characters.player.state.parser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import fireengine.characters.commands.Action_Command;
import fireengine.characters.commands.character_commands.map_editor.Create_Exit;
import fireengine.characters.commands.character_commands.map_editor.Create_Room;
import fireengine.characters.commands.character_commands.map_editor.Destroy_Exit;
import fireengine.characters.commands.character_commands.map_editor.Destroy_Room;

/*
 *    Copyright 2017 Ben Hook
 *    InWorldMatcherMapEditor.java
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

public class InWorldMatcherMapEditor implements InputParserMatcherInterface {
	private Pattern createExitsPattern = Pattern.compile("(?i)CREATE EXITS? (\\w+)");
	private Pattern createRoomPattern = Pattern.compile("(?i)CREATE ROOM (\\w+)");
	private Pattern destroyExitsPattern = Pattern.compile("(?i)DESTROY EXITS? (\\w+)");
	private Pattern destroyRoomPattern = Pattern.compile("(?i)DESTROY ROOM (\\w+)");

	public InWorldMatcherMapEditor() {
	}

	@Override
	public Action_Command match(String text) {
		Matcher matcher;

		if ((matcher = createExitsPattern.matcher(text)).matches()) {
			return new Create_Exit(matcher.group(1));
		} else if ((matcher = createRoomPattern.matcher(text)).matches()) {
			return new Create_Room(matcher.group(1));
		} else if ((matcher = destroyExitsPattern.matcher(text)).matches()) {
			return new Destroy_Exit(matcher.group(1));
		} else if ((matcher = destroyRoomPattern.matcher(text)).matches()) {
			return new Destroy_Room(matcher.group(1));
		} else {
			return null;
		}
	}
}
