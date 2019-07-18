package com.github.benhook1013.fireengine.character.command.character_comand.map_editor;

import java.util.logging.Level;

import com.github.benhook1013.fireengine.character.BaseCharacter;
import com.github.benhook1013.fireengine.character.command.character_comand.CharacterCommand;
import com.github.benhook1013.fireengine.client_io.ClientConnectionOutput;
import com.github.benhook1013.fireengine.gameworld.map.Directions;
import com.github.benhook1013.fireengine.gameworld.map.exception.MapExceptionDirectionNotSupported;
import com.github.benhook1013.fireengine.gameworld.map.exception.MapExceptionOutOfBounds;
import com.github.benhook1013.fireengine.gameworld.map.exception.MapExceptionRoomExists;
import com.github.benhook1013.fireengine.main.FireEngineMain;
import com.github.benhook1013.fireengine.util.CheckedHibernateException;
import com.github.benhook1013.fireengine.util.MyLogger;

/*
 *    Copyright 2017 Ben Hook
 *    CreateRoom.java
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

public class CreateRoom extends CharacterCommand {
	private String directionText;

	public CreateRoom(String directionText) {
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
				character.getMap().createRoom(character.getRoom(), direction);
				character.sendToListeners(new ClientConnectionOutput(
						"New room sucessfully created to the " + direction.toString() + "!", null, null));
			} catch (MapExceptionOutOfBounds e) {
				character.sendToListeners(new ClientConnectionOutput(
						"Cannot create room in the direction, it would run over map boundary.", null, null));
				return;
			} catch (MapExceptionRoomExists e) {
				character.sendToListeners(new ClientConnectionOutput(
						"Cannot create room in the direction, room already exists there.", null, null));
				return;
			} catch (MapExceptionDirectionNotSupported e) {
				MyLogger.log(Level.WARNING, "CreateRoom: MapExceptionDirectionNotSupported while trying to createRoom.",
						e);
				character.sendToListeners(new ClientConnectionOutput(
						"Can't create room " + direction.toString() + " from \"" + character.getRoom().getRoomName()
								+ "\", direction not supported in room creation code. Contact a God to fix this.",
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
