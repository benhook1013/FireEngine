package fireengine.characters.commands.character_commands.general;

import java.util.logging.Level;

import fireengine.characters.Base_Character;
import fireengine.characters.commands.character_commands.Character_Command;
import fireengine.characters.player.exceptions.PC_Exception_Null_Room;
import fireengine.client_io.ClientConnectionOutput;
import fireengine.gameworld.maps.BaseRoom;
import fireengine.gameworld.maps.Directions;
import fireengine.gameworld.maps.GameMap;
import fireengine.gameworld.maps.Exceptions.Map_Exception_Direction_Not_Supported;
import fireengine.gameworld.maps.Exceptions.Map_Exception_Exit_Null;
import fireengine.gameworld.maps.Exceptions.Map_Exception_Exit_Room_Null;
import fireengine.gameworld.maps.Exceptions.Map_Exception_Out_Of_Bounds;
import fireengine.utils.MyLogger;
import fireengine.utils.StringUtils;

/*
 *    Copyright 2017 Ben Hook
 *    Move.java
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

public class Move extends Character_Command {
	private String directionText;

	public Move(String directionText) {
		super();
		this.directionText = directionText;
	}

	@Override
	public void doAction(Base_Character character) {
		Directions.DIRECTION direction = Directions.parseDirection(directionText);

		if (direction != null) {
			try {
				BaseRoom oldRoom = character.getRoom();
				if (oldRoom.getExit(direction) == null) {
					throw new Map_Exception_Exit_Null(
							"Base_Character: Cannot move in specified direction, exit is null.");
				}
				BaseRoom newRoom = character.getMap().getRoom(character.getRoom(), direction);
				if (newRoom == null) {
					throw new Map_Exception_Exit_Room_Null("Base_Character: Somehow room to the " + direction + " of "
							+ character.getRoom().getRoomName() + " had exit leading to it but room is null.");
				}

				// Anything blocking movement goes here //

				// Actual moving //
				try {
					character.setRoom(newRoom);
				} catch (PC_Exception_Null_Room e) {
					MyLogger.log(Level.WARNING,
							"Base_Character: PC_Exception_Null_Room during move AFTER null check happened.");
					return;
				}
				oldRoom.removeCharacter(character);
				newRoom.addCharacter(character);

				// Post move output //
				oldRoom.sendToRoom(new ClientConnectionOutput(
						character.getName() + " exits to the " + direction.toString() + ".", null, null), character);

				ClientConnectionOutput output = new ClientConnectionOutput();
				output.addPart("You move " + StringUtils.capitalise(direction.toString()) + ".", null, null);
				output = new Look().doAction(character, output);
				output = GameMap.displayMap(output, character.getRoom(), 3);
				character.sendToListeners(output);

				newRoom.sendToRoom(new ClientConnectionOutput(
						character.getName() + " enters from the " + StringUtils.capitalise(direction.toString()) + ".",
						null, null), character);

			} catch (Map_Exception_Exit_Null e) {
				character.sendToListeners(
						new ClientConnectionOutput("You see no way to move in that direction.", null, null));
				return;
			} catch (Map_Exception_Out_Of_Bounds e) {
				MyLogger.log(Level.WARNING, "Move: Map_Exception_Out_Of_Bounds after check for exit.", e);
				character.sendToListeners(
						new ClientConnectionOutput("You see no room to move into that a-way.", null, null));
				return;
			} catch (Map_Exception_Direction_Not_Supported e) {
				MyLogger.log(Level.WARNING, "Move: Map_Exception_Direction_Not_Supported while trying to move.", e);
				character.sendToListeners(new ClientConnectionOutput("You can't move in that direction.", null, null));
				return;
			} catch (Map_Exception_Exit_Room_Null e) {
				MyLogger.log(Level.WARNING, "Move: Map_Exception_Exit_Room_Null after check for exit.", e);
				character.sendToListeners(
						new ClientConnectionOutput("You see no room to move into that a-way.", null, null));
				return;
			}
		} else {
			character.sendToListeners(new ClientConnectionOutput(
					"Could not parse '" + directionText + "' into a direction.", null, null));
			return;
		}
	}
}
