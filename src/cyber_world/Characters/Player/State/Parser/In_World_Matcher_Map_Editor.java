package cyber_world.Characters.Player.State.Parser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cyber_world.Characters.Commands.Action_Command;
import cyber_world.Characters.Commands.Character_Commands.Map_Editor.Create_Exit;
import cyber_world.Characters.Commands.Character_Commands.Map_Editor.Create_Room;
import cyber_world.Characters.Commands.Character_Commands.Map_Editor.Destroy_Exit;
import cyber_world.Characters.Commands.Character_Commands.Map_Editor.Destroy_Room;

/*
 *    Copyright 2017 Ben Hook
 *    In_World_Matcher_Map_Editor.java
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

public class In_World_Matcher_Map_Editor implements Input_Parser_Matcher_Interface {
	private Pattern createExitsPattern = Pattern.compile("(?i)CREATE EXITS? (\\w+)");
	private Pattern createRoomPattern = Pattern.compile("(?i)CREATE ROOM (\\w+)");
	private Pattern destroyExitsPattern = Pattern.compile("(?i)DESTROY EXITS? (\\w+)");
	private Pattern destroyRoomPattern = Pattern.compile("(?i)DESTROY ROOM (\\w+)");

	public In_World_Matcher_Map_Editor() {
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
