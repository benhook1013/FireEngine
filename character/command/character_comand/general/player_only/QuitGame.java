package fireengine.character.command.character_comand.general.player_only;

import fireengine.character.Character;
import fireengine.character.command.character_comand.CommandCharacter;
import fireengine.character.player.Player;
import fireengine.client_io.ClientConnectionOutput;
import fireengine.main.FireEngineMain;
import fireengine.util.CheckedHibernateException;

/*
 *    Copyright 2019 Ben Hook
 *    Say.java
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

public class QuitGame extends CommandCharacter {

	@Override
	public void doAction(Character character) {
		ClientConnectionOutput output = new ClientConnectionOutput(2);

		output.addPart(character.getName() + " phases out of existance.", null, null);

		character.getRoom().sendToRoom(output);

		try {
			Player.saveCharacter((Player) character);
		} catch (CheckedHibernateException e) {
			FireEngineMain.hibernateException(e);
		}
		((Player) character).getSession().disconnect();
	}
}
