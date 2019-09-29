package fireengine.character.command.action;

import java.util.logging.Level;
import java.util.regex.Matcher;
import fireengine.character.Character;
import fireengine.character.command.Command;
import fireengine.character.command.action.general.player_action.PlayerAction;
import fireengine.character.command.action.general.player_action.admin.AdminAction;
import fireengine.character.exception.CharacterExceptionNotInWorld;
import fireengine.character.player.Player;
import fireengine.client_io.ClientConnectionOutput;
import fireengine.util.MyLogger;

/*
 *    Copyright 2019 Ben Hook
 *    Action.java
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

/**
 * Adds ability to add functionality to a Command. A Command is simply a way to
 * know what menu option was selected etc, but an Action is done by a Character
 * and does something.
 * 
 * @author Ben Hook
 */
public abstract class Action extends Command {
	protected Action() {
		super();
	}

	/**
	 * Will do any necessary setup then call the doAction().
	 * 
	 * @return The output
	 */
	public ClientConnectionOutput callAction(Character character, Matcher matcher) {
		ClientConnectionOutput output = new ClientConnectionOutput();

		if (character == null) {
			MyLogger.log(Level.SEVERE, "Action: Null Character passed to constructor.", new Exception());
			return output;
		}
		if (matcher == null) {
			MyLogger.log(Level.SEVERE, "Action: Null Matcher passed to constructor.", new Exception());
			return output;
		}

		if (this instanceof PlayerAction) {
			if (character instanceof Player) {
				if (this instanceof AdminAction) {
					if (Character.checkAdminPrivs((Player) character)) {
						return doAction((Player) character, matcher);
					}
				} else {
					return doAction((Player) character, matcher);
				}
			} else {
				return output;
			}
		}

		if (this instanceof AdminAction) {
			if (character instanceof Player) {
				return doAction((Player) character, matcher);
			} else {
				return output;
			}
		}

		if (!character.isInWorld()) {
			MyLogger.log(Level.WARNING,
					String.format("Action: Character '%s' tried to do action while not in world.", character.getName()),
					new CharacterExceptionNotInWorld(String.format(
							"Action: Character '%s' tried to do action while not in world.", character.getName())));
			return output;
		}

		output.addOutput(doAction(character, matcher));
		return output;
	}

	/**
	 * The Player enacted way of doing the 'thing' that this Action does. Takes a
	 * matcher. An Action should implementation a same-name method taking only
	 * arguments it needs (specific not Matcher), for use to be directly called by
	 * game for NPC action etc.
	 * 
	 * @return The output
	 */
	protected abstract ClientConnectionOutput doAction(Character character, Matcher matcher);
}
