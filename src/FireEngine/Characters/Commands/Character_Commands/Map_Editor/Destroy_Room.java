package FireEngine.Characters.Commands.Character_Commands.Map_Editor;

import java.util.logging.Level;

import FireEngine.Characters.Base_Character;
import FireEngine.Characters.Commands.Character_Commands.Character_Command;
import FireEngine.client_io.ClientConnectionOutput;
import FireEngine.gameworld.maps.Directions;
import FireEngine.gameworld.maps.Exceptions.Map_Exception_Direction_Not_Supported;
import FireEngine.gameworld.maps.Exceptions.Map_Exception_Out_Of_Bounds;
import FireEngine.gameworld.maps.Exceptions.Map_Exception_Room_Null;
import FireEngine.main.CyberWorldMain;
import FireEngine.utils.CheckedHibernateException;
import FireEngine.utils.MyLogger;

/*
 *    Copyright 2017 Ben Hook
 *    Destroy_Room.java
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

public class Destroy_Room extends Character_Command {
	private String directionText;

	public Destroy_Room(String directionText) {
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
				character.getMap().destroyRoom(character.getRoom(), direction);
				character.sendToListeners(new ClientConnectionOutput(
						"Room sucessfully destropyed to the " + direction.toString() + "!", null, null));
			} catch (Map_Exception_Out_Of_Bounds e) {
				MyLogger.log(Level.WARNING, "Destroy_Room: Map_Exception_Out_Of_Bounds while trying to deleteRoom.", e);
				character.sendToListeners(
						new ClientConnectionOutput("Cannot destroy room in the direction, it's outside map boundary."
								+ " If do you think there is a room there, contact a God.", null, null));
				return;
			} catch (Map_Exception_Room_Null e) {
				character.sendToListeners(new ClientConnectionOutput(
						"Cannot destroy room in that direction, there is no room there.", null, null));
				return;
			} catch (Map_Exception_Direction_Not_Supported e) {
				MyLogger.log(Level.WARNING,
						"Destroy_Room: Map_Exception_Direction_Not_Supported while trying to destroyRoom.", e);
				character.sendToListeners(new ClientConnectionOutput(
						"Can't destroy room " + direction.toString() + " from \"" + character.getRoom().getRoomName()
								+ "\", direction not supported in room destruction code. Contact a God to fix this.",
						null, null));
			} catch (CheckedHibernateException e) {
				CyberWorldMain.hibernateException(e);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			character.sendToListeners(new ClientConnectionOutput(
					"Could not parse '" + directionText + "' into a direction.", null, null));
			return;
		}
	}
}
