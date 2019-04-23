package com.github.benhook1013.fireengine.characters.commands.character_commands.general;

import com.github.benhook1013.fireengine.characters.BaseCharacter;
import com.github.benhook1013.fireengine.characters.commands.character_commands.CharacterCommand;
import com.github.benhook1013.fireengine.client_io.ClientConnectionOutput;
import com.github.benhook1013.fireengine.gameworld.maps.BaseRoom;

/*
 *    Copyright 2017 Ben Hook
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

public class Map extends CharacterCommand {
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
	public void doAction(BaseCharacter character) {
		ClientConnectionOutput output = new ClientConnectionOutput(4);
		output.addPart("You look around and see...", null, null);

		BaseRoom room = character.getRoom();
		try {
			output = com.github.benhook1013.fireengine.gameworld.maps.GameMap.displayMap(output, room, size);
		} catch (Exception e) {
			e.printStackTrace();
		}

		character.sendToListeners(output);
	}
}
