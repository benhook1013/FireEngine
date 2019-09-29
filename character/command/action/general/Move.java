package fireengine.character.command.action.general;

import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import fireengine.character.Character;
import fireengine.character.command.action.Action;
import fireengine.client_io.ClientConnectionOutput;
import fireengine.gameworld.map.Direction;
import fireengine.gameworld.map.Direction.DIRECTION;
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

public class Move extends Action {
	private static Pattern pattern = compilePattern(
			"(?:(?:MOVE|GO) )?((?:U(?:P)?)|(?:D(?:OWN)?)|(?:N(?:ORTH)?(?:(?:W(?:EST)?)?|E(?:AST)?))|E(?:AST)?|(?:S(?:OUTH)?(?:(?:W(?:EST)?)?|E(?:AST)?))|W(?:EST)?)");

	public Move() {
		super();
	}

	@Override
	public ClientConnectionOutput doAction(Character character, Matcher matcher) {
		ClientConnectionOutput output = new ClientConnectionOutput();

		String directionText = matcher.group(1);
		Direction.DIRECTION direction = Direction.parseDirection(directionText);

		if (direction != null) {
			output.addOutput(doAction(character, direction));
			return output;
		} else {
			output.addPart("Could not parse '" + directionText + "' into a direction.", null, null);
			return output;
		}
	}

	public ClientConnectionOutput doAction(Character character, Direction.DIRECTION direction) {
		ClientConnectionOutput output = new ClientConnectionOutput();

		try {
			Room currentRoom = character.getRoom();
			if (currentRoom.getExit(direction) == null) {
				throw new MapExceptionExitNull("Character: Cannot move in specified direction, exit is null.");
			}
			Room toRoom = character.getMap().getRoom(character.getRoom(), direction);
			if (toRoom == null) {
				throw new MapExceptionExitRoomNull(String.format(
						"Character: Somehow room to the %s of %s has exit leading to it but room is null.", direction,
						character.getRoom().getName()));
			}

			// Anything blocking movement goes here //

			// Actual moving //
			character.setRoom(toRoom);

			// Post move output //
			currentRoom.sendToRoomExcluding(new ClientConnectionOutput(
					character.getName() + " exits to the " + direction.toString() + ".", null, null), character);

			output.addPart("You move " + StringUtils.capitalise(direction.toString()) + ".", null, null);
			output.addOutput(new Look().doAction(character, (DIRECTION) null));
			output.addOutput(GameMap.displayMap(character.getRoom(), 3));

			toRoom.sendToRoomExcluding(new ClientConnectionOutput(
					character.getName() + " enters from the " + StringUtils.capitalise(direction.toString()) + ".",
					null, null), character);

		} catch (MapExceptionExitNull e) {
			output.addPart("You see no way to move in that direction.", null, null);
			return output;
		} catch (MapExceptionDirectionNotSupported e) {
			MyLogger.log(Level.WARNING, "Move: MapExceptionDirectionNotSupported while trying to move.", e);
			output.addPart("You can't move in that direction.", null, null);
			return output;
		} catch (MapExceptionExitRoomNull e) {
			MyLogger.log(Level.WARNING, "Move: MapExceptionExitRoomNull after check for exit.", e);
			output.addPart("You see no room to move into that a-way.", null, null);
			return output;
		}

		return output;
	}

	public Pattern getPattern() {
		return pattern;
	}
}
