package fireengine.characters.commands.character_commands.general.player_only;

import fireengine.characters.Base_Character;
import fireengine.characters.commands.character_commands.Character_Command;
import fireengine.characters.player.Player_Character;
import fireengine.client_io.ClientConnectionOutput;
import fireengine.main.FireEngineMain;
import fireengine.utils.CheckedHibernateException;

/*
 *    Copyright 2017 Ben Hook
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

public class Quit_Game extends Character_Command {

	@Override
	public void doAction(Base_Character character) {
		ClientConnectionOutput output = new ClientConnectionOutput(2);

		output.addPart(character.getName() + " phases out of existance.", null, null);

		character.getRoom().sendToRoom(output);

		try {
			Player_Character.saveCharacter((Player_Character) character);
		} catch (CheckedHibernateException e) {
			FireEngineMain.hibernateException(e);
		}
		((Player_Character) character).getSession().disconnect();
	}
}
