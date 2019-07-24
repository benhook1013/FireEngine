package fireengine.character.command.character_comand.general;

import fireengine.character.BaseCharacter;
import fireengine.character.command.character_comand.CharacterCommand;
import fireengine.character.player.PlayerCharacter;
import fireengine.client_io.ClientConnectionOutput;

/*
 *    Copyright 2019 Ben Hook
 *    Look.java
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

public class Who extends CharacterCommand {

	public Who() {
		super();
	}

	// TODO Sort this by something.
	@Override
	public void doAction(BaseCharacter character) {
		ClientConnectionOutput output = new ClientConnectionOutput(2);

		output.addPart("A list of players in the realm:", null, null);

		for (PlayerCharacter pc : PlayerCharacter.getPlayerList()) {
			output.newLine();
			output.addPart(pc.getName(), null, null);
		}

		character.getRoom().sendToRoom(output);
	}
}
