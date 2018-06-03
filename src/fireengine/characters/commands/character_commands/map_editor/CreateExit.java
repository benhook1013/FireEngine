package fireengine.characters.commands.character_commands.map_editor;

import java.util.logging.Level;

import fireengine.characters.BaseCharacter;
import fireengine.characters.commands.character_commands.CharacterCommand;
import fireengine.client_io.ClientConnectionOutput;
import fireengine.gameworld.maps.Directions;
import fireengine.gameworld.maps.Exceptions.MapExceptionDirectionNotSupported;
import fireengine.gameworld.maps.Exceptions.MapExceptionExitExists;
import fireengine.gameworld.maps.Exceptions.MapExceptionExitRoomNull;
import fireengine.gameworld.maps.Exceptions.MapExceptionOutOfBounds;
import fireengine.gameworld.maps.Exceptions.MapExceptionRoomNull;
import fireengine.main.FireEngineMain;
import fireengine.utils.CheckedHibernateException;
import fireengine.utils.MyLogger;

/*
 *    Copyright 2017 Ben Hook
 *    CreateExit.java
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

public class CreateExit extends CharacterCommand {
	private String directionText;

	public CreateExit(String directionText) {
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
				character.getMap().createExit(character.getRoom(), direction);
				character.sendToListeners(new ClientConnectionOutput(
						"Exit created " + direction.toString() + " from \"" + character.getRoom().getRoomName() + "\".",
						null, null));
				return;
			} catch (MapExceptionRoomNull e) {
				character.sendToListeners(
						new ClientConnectionOutput("Can't create exit as player room is null.", null, null));
				return;
			} catch (MapExceptionOutOfBounds e) {
				character.sendToListeners(new ClientConnectionOutput("Can't create exit " + direction.toString()
						+ " from \"" + character.getRoom().getRoomName() + "\", adjoining room is out of map boundary."
						+ " If you think room is actaully there, contact a God.", null, null));
				return;
			} catch (MapExceptionExitRoomNull e) {
				character.sendToListeners(
						new ClientConnectionOutput("Can't create exit as adjacent room is null.", null, null));
				return;
			} catch (MapExceptionExitExists e) {
				character.sendToListeners(new ClientConnectionOutput(
						"Can't create exit " + direction.toString() + " from \"" + character.getRoom().getRoomName()
								+ "\", exit already exists there." + " Try DESTROY EXIT first if exit is oneway.",
						null, null));
				return;
			} catch (MapExceptionDirectionNotSupported e) {
				MyLogger.log(Level.WARNING,
						"CreateExit: MapExceptionDirectionNotSupported while trying to createExit.", e);
				character.sendToListeners(new ClientConnectionOutput(
						"Can't create exit " + direction.toString() + " from \"" + character.getRoom().getRoomName()
								+ "\", direction not supported in exit creation code. Contact a God to fix this.",
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