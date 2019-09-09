package fireengine.character.command.action.general.player_action;

import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import fireengine.character.Character;
import fireengine.character.player.Player;
import fireengine.client_io.ClientConnectionOutput;
import fireengine.main.FireEngineMain;
import fireengine.util.CheckedHibernateException;
import fireengine.util.MyLogger;

/*
 *    Copyright 2019 Ben Hook
 *    QuitGame.java
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

public class QuitGame extends PlayerAction {
	private static Pattern pattern = compilePattern("QQ|QUIT(?: GAME)");

	public QuitGame() {
		super();
	}

	@Override
	public ClientConnectionOutput doAction(Character character, Matcher matcher) {
		ClientConnectionOutput output = new ClientConnectionOutput();

		if (character instanceof Player) {
			output.addOutput(doAction((Player) character));
			return output;
		} else {
			MyLogger.log(Level.WARNING,
					"QuitGame: Non-player Character tried to doAction on a Player-only action that should have already been caught in the Action callAction checks.");
			return output;
		}
	}

	public ClientConnectionOutput doAction(Player character) {
		ClientConnectionOutput output = new ClientConnectionOutput();

		output.addPart(character.getName() + " phases out of existance.", null, null);

		character.getRoom().sendToRoom(output);

		try {
			Player.saveCharacter((Player) character);
		} catch (CheckedHibernateException e) {
			FireEngineMain.hibernateException(e);
		}
		character.getSession().disconnect();

		return output;
	}

	public Pattern getPattern() {
		return pattern;
	}
}
