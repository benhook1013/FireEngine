package fireengine.character.command.action.general;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import fireengine.character.Character;
import fireengine.character.command.action.Action;
import fireengine.client_io.ClientConnectionOutput;
import fireengine.gameworld.map.room.Room;

/*
 *    Copyright 2019 Ben Hook
 *    Map.java
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

public class Map extends Action {
	private static Pattern pattern = compilePattern("MAP(?: (\\w+))?");

	public Map() {
		super();
	}

	@Override
	public ClientConnectionOutput doAction(Character character, Matcher matcher) {
		// TODO Some map size limit
		String sizeString = matcher.group(1);
		int size;
		if (sizeString == null) {
			size = 3;
		} else {
			try {
				size = Integer.parseInt(sizeString);
			} catch (NumberFormatException e) {
				size = 3;
			}
		}
		return doAction(character, size);
	}

	public ClientConnectionOutput doAction(Character character, int size) {
		ClientConnectionOutput output = new ClientConnectionOutput();
		output.addPart("You look around and see...", null, null);

		Room room = character.getRoom();

		output.addOutput(fireengine.gameworld.map.GameMap.displayMap(room, size));
		return output;
	}

	public Pattern getPattern() {
		return pattern;
	}
}
