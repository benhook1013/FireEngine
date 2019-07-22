package fireengine.character.command.character_comand.map_editor;

import java.util.logging.Level;

import fireengine.character.BaseCharacter;
import fireengine.character.command.character_comand.CharacterCommand;
import fireengine.client_io.ClientConnectionOutput;
import fireengine.gameworld.map.Direction;
import fireengine.gameworld.map.exception.MapExceptionDirectionNotSupported;
import fireengine.gameworld.map.exception.MapExceptionOutOfBounds;
import fireengine.gameworld.map.exception.MapExceptionRoomNull;
import fireengine.main.FireEngineMain;
import fireengine.util.CheckedHibernateException;
import fireengine.util.MyLogger;

/*
 *    Copyright 2017 Ben Hook
 *    DestroyRoom.java
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

public class DestroyRoom extends CharacterCommand {
	private String directionText;

	public DestroyRoom(String directionText) {
		super();
		this.directionText = directionText;
	}

	@Override
	public void doAction(BaseCharacter character) {
		if (BaseCharacter.checkMapEditorPrivs(character) == false) {
			return;
		}

		Direction.DIRECTION direction = Direction.parseDirection(directionText);

		if (direction != null) {
			try {
				character.getMap().destroyRoom(character.getRoom(), direction);
				character.sendToListeners(new ClientConnectionOutput(
						"Room sucessfully destropyed to the " + direction.toString() + "!", null, null));
			} catch (MapExceptionOutOfBounds e) {
				MyLogger.log(Level.WARNING, "DestroyRoom: MapExceptionOutOfBounds while trying to deleteRoom.", e);
				character.sendToListeners(
						new ClientConnectionOutput("Cannot destroy room in the direction, it's outside map boundary."
								+ " If do you think there is a room there, contact a God.", null, null));
				return;
			} catch (MapExceptionRoomNull e) {
				character.sendToListeners(new ClientConnectionOutput(
						"Cannot destroy room in that direction, there is no room there.", null, null));
				return;
			} catch (MapExceptionDirectionNotSupported e) {
				MyLogger.log(Level.WARNING,
						"DestroyRoom: MapExceptionDirectionNotSupported while trying to destroyRoom.", e);
				character.sendToListeners(new ClientConnectionOutput(
						"Can't destroy room " + direction.toString() + " from \"" + character.getRoom().getRoomName()
								+ "\", direction not supported in room destruction code. Contact a God to fix this.",
						null, null));
			} catch (CheckedHibernateException e) {
				FireEngineMain.hibernateException(e);
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
