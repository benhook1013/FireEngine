package fireengine.characters.commands.character_commands.map_editor;

import java.util.logging.Level;

import fireengine.characters.BaseCharacter;
import fireengine.characters.commands.character_commands.Character_Command;
import fireengine.client_io.ClientConnectionOutput;
import fireengine.gameworld.maps.Directions;
import fireengine.gameworld.maps.Exceptions.Map_Exception_Direction_Not_Supported;
import fireengine.gameworld.maps.Exceptions.Map_Exception_Exit_Room_Null;
import fireengine.gameworld.maps.Exceptions.Map_Exception_Out_Of_Bounds;
import fireengine.gameworld.maps.Exceptions.Map_Exception_Room_Null;
import fireengine.main.FireEngineMain;
import fireengine.utils.CheckedHibernateException;
import fireengine.utils.MyLogger;

/*
 *    Copyright 2017 Ben Hook
 *    Destroy_Exit.java
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

public class Destroy_Exit extends Character_Command {
	private String directionText;

	public Destroy_Exit(String directionText) {
		super();
		this.directionText = directionText;
	}

	@Override
	public void doAction(BaseCharacter character) {
		if (BaseCharacter.checkMapEditorPrivs(character) == false) {
			return;
		}

		Directions.DIRECTION direction = Directions.parseDirection(directionText);

		if (direction != null) {
			try {
				character.getMap().destroyExit(character.getRoom(), direction);
				character.sendToListeners(new ClientConnectionOutput("Exit destroyed " + direction.toString()
						+ " from \"" + character.getRoom().getRoomName() + "\".", null, null));
				return;
			} catch (Map_Exception_Room_Null e) {
				character.sendToListeners(
						new ClientConnectionOutput("Can't destroy exit as player room is null.", null, null));
				return;
			} catch (Map_Exception_Out_Of_Bounds e) {
				character.sendToListeners(new ClientConnectionOutput("Can't destroy exit " + direction.toString()
						+ " from \"" + character.getRoom().getRoomName() + "\", adjoining room is out of map boundary."
						+ " If you think room is actaully there, contact a God.", null, null));
				return;
			} catch (Map_Exception_Exit_Room_Null e) {
				character.sendToListeners(
						new ClientConnectionOutput("Can't destroy exit as adjacent room is null.", null, null));
				return;
			} catch (Map_Exception_Direction_Not_Supported e) {
				MyLogger.log(Level.WARNING,
						"Destroy_Exit: Map_Exception_Direction_Not_Supported while trying to destroyExit.", e);
				character.sendToListeners(new ClientConnectionOutput(
						"Can't create exit " + direction.toString() + " from \"" + character.getRoom().getRoomName()
								+ "\", direction not supported in exit creation code. Contact a God to fix this.",
						null, null));
				return;
			} catch (CheckedHibernateException e) {
				FireEngineMain.hibernateException(e);
			}
		} else {
			character.sendToListeners(new ClientConnectionOutput(
					"Could not parse '" + directionText + "' into a direction.", null, null));
			return;
		}
	}
}
