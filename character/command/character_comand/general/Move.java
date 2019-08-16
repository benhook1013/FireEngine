package fireengine.character.command.character_comand.general;

import java.util.logging.Level;

import fireengine.character.Character;
import fireengine.character.command.character_comand.CommandCharacter;
import fireengine.character.player.exception.PlayerExceptionNullRoom;
import fireengine.client_io.ClientConnectionOutput;
import fireengine.gameworld.map.Direction;
import fireengine.gameworld.map.GameMap;
import fireengine.gameworld.map.exception.MapExceptionDirectionNotSupported;
import fireengine.gameworld.map.exception.MapExceptionExitNull;
import fireengine.gameworld.map.exception.MapExceptionExitRoomNull;
import fireengine.gameworld.map.room.Room;
import fireengine.util.MyLogger;
import fireengine.util.StringUtils;

/*
 *    Copyright 2019 Ben Hook
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

public class Move extends CommandCharacter {
	private String directionText;

	public Move(String directionText) {
		super();
		this.directionText = directionText;
	}

	@Override
	public void doAction(Character character) {
		Direction.DIRECTION direction = Direction.parseDirection(directionText);

		if (direction != null) {
			try {
				Room currentRoom = character.getRoom();
				if (currentRoom.getExit(direction) == null) {
					throw new MapExceptionExitNull("Character: Cannot move in specified direction, exit is null.");
				}
				Room toRoom = character.getMap().getRoom(character.getRoom(), direction);
				if (toRoom == null) {
					throw new MapExceptionExitRoomNull(String.format(
							"Character: Somehow room to the %s of %s has exit leading to it but room is null.",
							direction, character.getRoom().getName()));
				}

				// Anything blocking movement goes here //

				// Actual moving //
				try {
					character.setRoom(toRoom);
				} catch (PlayerExceptionNullRoom e) {
					MyLogger.log(Level.WARNING,
							"Character: PlayerExceptionNullRoom during move AFTER null check happened.");
					return;
				}
				currentRoom.removeCharacter(character);
				toRoom.addCharacter(character);

				// Post move output //
				currentRoom.sendToRoomExcluding(new ClientConnectionOutput(
						character.getName() + " exits to the " + direction.toString() + ".", null, null), character);

				ClientConnectionOutput output = new ClientConnectionOutput();
				output.addPart("You move " + StringUtils.capitalise(direction.toString()) + ".", null, null);
				output = new Look().doAction(character, output);
				output = GameMap.displayMap(output, character.getRoom(), 3);
				character.sendToListeners(output);

				toRoom.sendToRoomExcluding(new ClientConnectionOutput(
						character.getName() + " enters from the " + StringUtils.capitalise(direction.toString()) + ".",
						null, null), character);

			} catch (MapExceptionExitNull e) {
				character.sendToListeners(
						new ClientConnectionOutput("You see no way to move in that direction.", null, null));
				return;
			} catch (MapExceptionDirectionNotSupported e) {
				MyLogger.log(Level.WARNING, "Move: MapExceptionDirectionNotSupported while trying to move.", e);
				character.sendToListeners(new ClientConnectionOutput("You can't move in that direction.", null, null));
				return;
			} catch (MapExceptionExitRoomNull e) {
				MyLogger.log(Level.WARNING, "Move: MapExceptionExitRoomNull after check for exit.", e);
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
