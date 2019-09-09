package fireengine.character.command.action.general.player_action.map_editor;

import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import fireengine.character.Character;
import fireengine.character.command.action.general.player_action.PlayerAction;
import fireengine.character.player.Player;
import fireengine.client_io.ClientConnectionOutput;
import fireengine.gameworld.map.Direction;
import fireengine.gameworld.map.exception.MapExceptionDirectionNotSupported;
import fireengine.gameworld.map.exception.MapExceptionExitRoomNull;
import fireengine.gameworld.map.exception.MapExceptionRoomNull;
import fireengine.main.FireEngineMain;
import fireengine.util.CheckedHibernateException;
import fireengine.util.MyLogger;

/*
 *    Copyright 2019 Ben Hook
 *    DestroyExit.java
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

public class DestroyExit extends PlayerAction {
	private static Pattern pattern = compilePattern("DESTROY EXITS? (\\w+)");

	public DestroyExit() {
		super();
	}

	@Override
	public ClientConnectionOutput doAction(Character character, Matcher matcher) {
		ClientConnectionOutput output = new ClientConnectionOutput();

		if (character instanceof Player) {
			if (Character.checkMapEditorPrivs((Player) character) == false) {
				MyLogger.log(Level.WARNING,
						"DestroyExit: Non-map editor Character tried to doAction on a map editor only action that should have already been caught in the Action callAction checks.");
				return output;
			}

		} else {
			MyLogger.log(Level.WARNING,
					"DestroyExit: Non-player Character tried to doAction on a Player-only action that should have already been caught in the Action callAction checks.");
			return output;
		}

		String directionText = matcher.group(1);
		Direction.DIRECTION direction = Direction.parseDirection(directionText);

		if (direction != null) {
			output.addOutput(doAction(character, direction));
			return output;
		} else {
			output.addPart(String.format("Could not parse '%s' into a direction.", directionText), null, null);
			return output;
		}
	}

	public ClientConnectionOutput doAction(Character character, Direction.DIRECTION direction) {
		ClientConnectionOutput output = new ClientConnectionOutput();

		try {
			character.getMap().deleteExit(character.getRoom(), direction);
			output.addPart(String.format("RoomExit destroyed %s from \"%s\".", direction.toString(),
					character.getRoom().getName()), null, null);
			return output;
		} catch (MapExceptionRoomNull e) {
			output.addPart("Can't destroy exit as player room is null.", null, null);
			return output;
		} catch (MapExceptionExitRoomNull e) {
			output.addPart("Can't destroy exit as adjacent room is null.", null, null);
			return output;
		} catch (MapExceptionDirectionNotSupported e) {
			MyLogger.log(Level.WARNING, "DestroyExit: MapExceptionDirectionNotSupported while trying to destroyExit.",
					e);
			output.addPart(String.format(
					"Can't create exit %s from \"%s\", direction not supported in exit creation code. Contact a God to fix this.",
					direction.toString(), character.getRoom().getName()), null, null);
			return output;
		} catch (CheckedHibernateException e) {
			FireEngineMain.hibernateException(e);
			return output;
		}
	}

	public Pattern getPattern() {
		return pattern;
	}
}
