package fireengine.character.command.action.general.player_action.admin;

import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import fireengine.character.Character;
import fireengine.character.player.Player;
import fireengine.client_io.ClientConnectionOutput;
import fireengine.util.MyLogger;

/*
 *    Copyright 2019 Ben Hook
 *    Watch.java
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
// TODO Extend to watching Characters not just Players
public class Watch extends AdminAction {
	private static Pattern pattern = compilePattern("ADMIN WATCH (\\w+)");

	public Watch() {
		super();
	}

	@Override
	public ClientConnectionOutput doAction(Character character, Matcher matcher) {
		String watchedName = matcher.group(1);
		Player watched = Player.findCharacter(watchedName);

		if (watched == null) {
			return new ClientConnectionOutput("Could not find player with that name to watch.");
		}

		if (character instanceof Player) {
			return doAction((Player) character, watched);
		} else {
			MyLogger.log(Level.WARNING,
					"Watch: Somehow non-Player Character tried to do Watch command which should have not been allowed.");
			return new ClientConnectionOutput("Only (Admin) Players can Watch others.");
		}
	}

	public ClientConnectionOutput doAction(Player watcherPlayer, Player watchedPlayer) {
		ClientConnectionOutput output = new ClientConnectionOutput();

		watchedPlayer.addListener(watcherPlayer);
		output.addPart(String.format("You are now watching %s.", watchedPlayer.getName()));

		return output;
	}

	public Pattern getPattern() {
		return pattern;
	}
}
