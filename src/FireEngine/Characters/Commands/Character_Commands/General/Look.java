package FireEngine.Characters.Commands.Character_Commands.General;

import java.util.ArrayList;
import java.util.Iterator;

import FireEngine.Characters.Base_Character;
import FireEngine.Characters.Commands.Character_Commands.Character_Command;
import FireEngine.Characters.Player.Player_Character;
import FireEngine.client_io.ClientConnectionOutput;
import FireEngine.gameworld.maps.BaseRoom;
import FireEngine.gameworld.maps.Base_Room_Exit;
import FireEngine.gameworld.maps.Directions;
import FireEngine.gameworld.maps.Exceptions.Map_Exception_Direction_Not_Supported;
import FireEngine.gameworld.maps.Exceptions.Map_Exception_Out_Of_Bounds;
import FireEngine.utils.StringUtils;

/*
 *    Copyright 2017 Ben Hook
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

public class Look extends Character_Command {
	private String directionText;
	Directions.DIRECTION direction;

	public Look() {
		super();
	}

	public Look(String directionText) {
		this();
		this.directionText = directionText;
	}

	public Look(Directions.DIRECTION direction) {
		this();
		this.direction = direction;
	}

	@Override
	public void doAction(Base_Character character) {
		doAction(character, null);
	}

	public ClientConnectionOutput doAction(Base_Character character, ClientConnectionOutput returnOutput) {
		ClientConnectionOutput output;
		if (returnOutput == null) {
			output = new ClientConnectionOutput(4);
		} else {
			output = returnOutput;
			output.newLine();
		}

		Directions.DIRECTION direction = null;
		if (this.direction == null) {
			if (directionText != null) {
				direction = Directions.parseDirection(directionText);
			}

			if (directionText != null) {
				if (direction == null) {
					output.addPart("Could not parse '" + directionText + "' into a direction.", null, null);
					return output;
				}
			}
		} else {
			direction = this.direction;
		}

		BaseRoom lookRoom = character.getRoom();
		if (direction != null) {
			try {
				Base_Room_Exit exit = lookRoom.getExit(direction);
				if (exit != null) {
					if (exit.isOpen()) {
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
			} catch (Map_Exception_Out_Of_Bounds e) {
				output.addPart("Could not look " + StringUtils.capitalise(direction.toString())
						+ " as the doom in that direction is out of map boundary."
						+ " If you think there is a room in that direction, contact a God.", null, null);
				return output;
			} catch (Map_Exception_Direction_Not_Supported e) {
				output.addPart("Could not look " + StringUtils.capitalise(direction.toString())
						+ " as the direction is no supported for LOOK." + " Please contact a God to get this fixed.",
						null, null);
				return output;
			}
		}

		output.addPart("\"" + lookRoom.getRoomName() + "\" " + lookRoom.getCoords(), null, null);
		output.newLine();
		output.addPart(lookRoom.getRoomDesc(), null, null);
		output.newLine();
		ArrayList<Player_Character> playerList = lookRoom.getPCs();
		if (playerList.isEmpty()) {
			output.addPart("You see no one here.", null, null);
		} else {
			int seen = 0;
			output.addPart("You see ", null, null);
			for (Iterator<Player_Character> iter = playerList.iterator(); iter.hasNext();) {
				output.addPart(iter.next().getName(), null, null);
				seen++;
				if (iter.hasNext()) {
					if (seen == 1 && playerList.size() == 2) {
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

		if (returnOutput == null) {
			character.sendToListeners(output);
			return null;
		} else {
			return output;
		}
	}
}
