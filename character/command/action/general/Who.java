package fireengine.character.command.action.general;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import fireengine.character.Character;
import fireengine.character.command.action.Action;
import fireengine.character.player.Player;
import fireengine.client_io.ClientConnectionOutput;

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

public class Who extends Action {
	private static Pattern pattern = compilePattern("WHO");

	public Who() {
		super();
	}

	// TODO Sort this by something.
	@Override
	public ClientConnectionOutput doAction(Character character, Matcher matcher) {
		return doAction(character);
	}

	public ClientConnectionOutput doAction(Character character) {
		ClientConnectionOutput output = new ClientConnectionOutput();

		output.addPart("A list of players in the realm:", null, null);

		for (Player p : Player.getPlayerList()) {
			output.newLine();
			output.addPart(p.getName(), null, null);
		}

		return output;
	}

	public Pattern getPattern() {
		return pattern;
	}
}
