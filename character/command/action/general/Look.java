package fireengine.character.command.action.general;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import fireengine.character.Character;
import fireengine.character.command.action.Action;
import fireengine.character.player.Player;
import fireengine.client_io.ClientConnectionOutput;
import fireengine.gameworld.map.Direction;
import fireengine.gameworld.map.exception.MapExceptionDirectionNotSupported;
import fireengine.gameworld.map.exit.RoomExit;
import fireengine.gameworld.map.room.Room;
import fireengine.util.StringUtils;

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

public class Look extends Action {
	private static Pattern pattern = compilePattern("L(?:OOK)?(?: (\\w+))?");

	public Look() {
		super();
	}

	@Override
	public ClientConnectionOutput doAction(Character character, Matcher matcher) {
		ClientConnectionOutput output = new ClientConnectionOutput();

		String directionText = matcher.group(1);
		Direction.DIRECTION direction = null;

		if (directionText != null) {
			direction = Direction.parseDirection(directionText);

			if (direction == null) {
				output.addPart(String.format("Could not parse '%s' into a direction.", directionText), null, null);
				return output;
			}
		}

		output.addOutput(doAction(character, direction));
		return output;
	}

	public ClientConnectionOutput doAction(Character character, Direction.DIRECTION direction) {
		ClientConnectionOutput output = new ClientConnectionOutput();

		Room lookRoom = character.getRoom();
		if (direction != null) {
			try {
				RoomExit roomExit = lookRoom.getExit(direction);
				if (roomExit != null) {
					if (roomExit.isOpen()) {
						lookRoom = character.getMap().getRoom(lookRoom, direction);
						output.addPart("You look " + StringUtils.capitalise(direction.toString()) + ".", null, null);
						output.newLine();
					} else {
						output.addPart("Could not look " + StringUtils.capitalise(direction.toString())
								+ " as the exit that-a-way is closed.", null, null);
						return output;
					}
				} else {
					output.addPart("Could not look " + StringUtils.capitalise(direction.toString())
							+ " as there is no exit that-a-way.", null, null);
					return output;
				}
			} catch (MapExceptionDirectionNotSupported e) {
				output.addPart("Could not look " + StringUtils.capitalise(direction.toString())
						+ " as the direction is no supported for LOOK." + " Please contact a God to get this fixed.",
						null, null);
				return output;
			}
		}

		output.addPart("\"" + lookRoom.getName() + "\" " + lookRoom.getCoord().toString(), null, null);
		output.newLine();
		output.addPart(lookRoom.getDescription(), null, null);
		output.newLine();
		ArrayList<Player> playerList = lookRoom.getPlayers();
		if (playerList.isEmpty()) {
			output.addPart("You see no one here.", null, null);
		} else {
			int seen = 0;
			output.addPart("You see ", null, null);
			for (Iterator<Player> iter = playerList.iterator(); iter.hasNext();) {
				output.addPart(iter.next().getName(), null, null);
				seen++;
				if (iter.hasNext()) {
					if ((seen == 1) && (playerList.size() == 2)) {
						output.addPart(" and ", null, null);
					} else if ((playerList.size() - seen) == 1) {
						output.addPart(", and ", null, null);
					} else {
						output.addPart(", ", null, null);
					}
				}
			}
			output.addPart(" here.", null, null);
		}
		return output;
	}

	public Pattern getPattern() {
		return pattern;
	}
}
