package fireengine.character.command.action.general.player_action.admin;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import fireengine.character.Character;
import fireengine.client_io.ClientConnectionOutput;
import fireengine.client_io.ClientIOColour.COLOURS;
import fireengine.main.FireEngineMain;

/*
 *    Copyright 2019 Ben Hook
 *    Shutdown.java
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

// TODO Ensure ordering a player to do a command cannot cause Admin commands.
public class Shutdown extends AdminAction {
	private static Pattern pattern = compilePattern("ADMIN SHUT ?DOWN");

	public Shutdown() {
		super();
	}

	@Override
	public ClientConnectionOutput doAction(Character character, Matcher matcher) {
		return doAction(character);
	}

	public ClientConnectionOutput doAction(Character character) {
		ClientConnectionOutput output = new ClientConnectionOutput();

		FireEngineMain.stop();
		output.addPart("You have started a server shutdown.", COLOURS.RED, null);

		return output;
	}

	public Pattern getPattern() {
		return pattern;
	}
}
