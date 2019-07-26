package fireengine.character.command.character_comand.general;

import fireengine.character.Character;
import fireengine.character.command.character_comand.CommandCharacter;
import fireengine.client_io.ClientConnectionOutput;
import fireengine.gameworld.map.room.Room;

/*
 *    Copyright 2019 Ben Hook
 *    GameMap.java
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

public class Map extends CommandCharacter {
	private int size;

	public Map() {
		super();
		this.size = 3;
	}

	public Map(int size) {
		super();
		this.size = size;
	}

	@Override
	public void doAction(Character character) {
		ClientConnectionOutput output = new ClientConnectionOutput(4);
		output.addPart("You look around and see...", null, null);

		Room room = character.getRoom();
		try {
			output = fireengine.gameworld.map.GameMap.displayMap(output, room, size);
		} catch (Exception e) {
			e.printStackTrace();
		}

		character.sendToListeners(output);
	}
}
