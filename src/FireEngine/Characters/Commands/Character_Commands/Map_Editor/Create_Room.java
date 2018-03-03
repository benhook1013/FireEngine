package FireEngine.Characters.Commands.Character_Commands.Map_Editor;

import java.util.logging.Level;

import FireEngine.Characters.Base_Character;
import FireEngine.Characters.Commands.Character_Commands.Character_Command;
import FireEngine.client_io.ClientConnectionOutput;
import FireEngine.gameworld.maps.Directions;
import FireEngine.gameworld.maps.Exceptions.Map_Exception_Direction_Not_Supported;
import FireEngine.gameworld.maps.Exceptions.Map_Exception_Out_Of_Bounds;
import FireEngine.gameworld.maps.Exceptions.Map_Exception_Room_Exists;
import FireEngine.gameworld.maps.Exceptions.Map_Exception_Room_Null;
import FireEngine.main.CyberWorldMain;
import FireEngine.utils.CheckedHibernateException;
import FireEngine.utils.MyLogger;

/*
 *    Copyright 2017 Ben Hook
 *    Create_Room.java
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

public class Create_Room extends Character_Command {
	private String directionText;

	public Create_Room(String directionText) {
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
				character.getMap().createRoom(character.getRoom(), direction);
				character.sendToListeners(new ClientConnectionOutput(
						"New room sucessfully created to the " + direction.toString() + "!", null, null));
			} catch (Map_Exception_Out_Of_Bounds e) {
				character.sendToListeners(new ClientConnectionOutput(
						"Cannot create room in the direction, it would run over map boundary.", null, null));
				return;
			} catch (Map_Exception_Room_Exists e) {
				character.sendToListeners(new ClientConnectionOutput(
						"Cannot create room in the direction, room already exists there.", null, null));
				return;
			} catch (Map_Exception_Room_Null e) {
				MyLogger.log(Level.WARNING, "Create_Room: Map_Exception_Room_Null while trying to createRoom.", e);
				character.sendToListeners(new ClientConnectionOutput(
						"Unusual error (Map_Exception_Room_Null) while trying to createRoom, report to a God.", null,
						null));
				return;
			} catch (Map_Exception_Direction_Not_Supported e) {
				MyLogger.log(Level.WARNING,
						"Create_Room: Map_Exception_Direction_Not_Supported while trying to createRoom.", e);
				character.sendToListeners(new ClientConnectionOutput(
						"Can't create room " + direction.toString() + " from \"" + character.getRoom().getRoomName()
								+ "\", direction not supported in room creation code. Contact a God to fix this.",
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
