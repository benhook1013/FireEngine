package characters.player.state.parser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import characters.commands.ActionCommand;
import characters.commands.character_commands.general.Look;
import characters.commands.character_commands.general.Map;
import characters.commands.character_commands.general.Move;
import characters.commands.character_commands.general.Say;
import characters.commands.character_commands.general.Who;
import characters.commands.character_commands.general.player_only.QuitGame;
import utils.MathUtils;

/*
 *    Copyright 2017 Ben Hook
 *    InputParserMatcherInWorldGeneral.java
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

public class InputParserMatcherInWorldGeneral implements InputParserMatcher {
	private Pattern lookPattern = Pattern.compile("(?i)L(?:OOK)?(?: (\\w+))?");
	private Pattern mapPattern = Pattern.compile("(?i)MAP(?: (\\w+))?");
	private Pattern movePattern = Pattern.compile(
			"(?i)(?:(?:MOVE|GO|WALK) )?(N(?:ORTH)?(?:(?:W(?:EST)?)?|E(?:AST)?)|E(?:AST)?|S(?:OUTH)?(?:(?:W(?:EST)?)?|E(?:AST)?)|W(?:EST)?)");
	private Pattern quitGamePattern = Pattern.compile("(?i)QQ|QUIT(?: GAME)");
	private Pattern sayPattern = Pattern.compile("(?i)SAY? (.+)");
	private Pattern whoPattern = Pattern.compile("(?i)WHO");

	public InputParserMatcherInWorldGeneral() {
	}

	@Override
	public ActionCommand match(String text) {
		Matcher matcher;

		if ((matcher = lookPattern.matcher(text)).matches()) {
			if (matcher.group(1) != null) {
				return new Look(matcher.group(1));
			} else {
				return new Look();
			}
		} else if ((matcher = mapPattern.matcher(text)).matches()) {
			if (matcher.group(1) != null) {
				return new Map(MathUtils.parseInt(matcher.group(1), 3));
			} else {
				return new Map();
			}
		} else if ((matcher = movePattern.matcher(text)).matches()) {
			return new Move(matcher.group(1));
		} else if ((matcher = quitGamePattern.matcher(text)).matches()) {
			return new QuitGame();
		} else if ((matcher = sayPattern.matcher(text)).matches()) {
			return new Say(matcher.group(1));
		} else if ((matcher = whoPattern.matcher(text)).matches()) {
			return new Who();
		} else {
			return null;
		}
	}
}
