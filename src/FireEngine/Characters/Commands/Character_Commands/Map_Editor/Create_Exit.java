package FireEngine.Characters.Commands.Character_Commands.Map_Editor;

import java.util.logging.Level;

import FireEngine.Characters.Base_Character;
import FireEngine.Characters.Commands.Character_Commands.Character_Command;
import FireEngine.client_io.ClientConnectionOutput;
import FireEngine.gameworld.maps.Directions;
import FireEngine.gameworld.maps.Exceptions.Map_Exception_Direction_Not_Supported;
import FireEngine.gameworld.maps.Exceptions.Map_Exception_Exit_Exists;
import FireEngine.gameworld.maps.Exceptions.Map_Exception_Exit_Room_Null;
import FireEngine.gameworld.maps.Exceptions.Map_Exception_Out_Of_Bounds;
import FireEngine.gameworld.maps.Exceptions.Map_Exception_Room_Null;
import FireEngine.main.CyberWorldMain;
import FireEngine.utils.CheckedHibernateException;
import FireEngine.utils.MyLogger;

/*
 *    Copyright 2017 Ben Hook
 *    Create_Exit.java
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

public class Create_Exit extends Character_Command {
	private String directionText;

	public Create_Exit(String directionText) {
		super();
		this.directionText = directionText;
	}

	@Override
	public void doAction(Base_Character character) {
		if (Base_Character.checkMapEditorPrivs(character) == false) {
			return;
		}

		Directions.DIRECTION direction = Directions.parseDirection(directionText);

		if (direction != null) {
			try {
				character.getMap().createExit(character.getRoom(), direction);
				character.sendToListeners(new ClientConnectionOutput(
						"Exit created " + direction.toString() + " from \"" + character.getRoom().getRoomName() + "\".",
						null, null));
				return;
			} catch (Map_Exception_Room_Null e) {
				character.sendToListeners(
						new ClientConnectionOutput("Can't create exit as player room is null.", null, null));
				return;
			} catch (Map_Exception_Out_Of_Bounds e) {
				character.sendToListeners(new ClientConnectionOutput("Can't create exit " + direction.toString()
						+ " from \"" + character.getRoom().getRoomName() + "\", adjoining room is out of map boundary."
						+ " If you think room is actaully there, contact a God.", null, null));
				return;
			} catch (Map_Exception_Exit_Room_Null e) {
				character.sendToListeners(
						new ClientConnectionOutput("Can't create exit as adjacent room is null.", null, null));
				return;
			} catch (Map_Exception_Exit_Exists e) {
				character.sendToListeners(new ClientConnectionOutput(
						"Can't create exit " + direction.toString() + " from \"" + character.getRoom().getRoomName()
								+ "\", exit already exists there." + " Try DESTROY EXIT first if exit is oneway.",
						null, null));
				return;
			} catch (Map_Exception_Direction_Not_Supported e) {
				MyLogger.log(Level.WARNING,
						"Create_Exit: Map_Exception_Direction_Not_Supported while trying to createExit.", e);
				character.sendToListeners(new ClientConnectionOutput(
						"Can't create exit " + direction.toString() + " from \"" + character.getRoom().getRoomName()
								+ "\", direction not supported in exit creation code. Contact a God to fix this.",
						null, null));
				return;
			} catch (CheckedHibernateException e) {
				CyberWorldMain.hibernateException(e);
			}
		} else {
			character.sendToListeners(new ClientConnectionOutput(
					"Could not parse '" + directionText + "' into a direction.", null, null));
			return;
		}
	}
}
