package cyber_world.Characters.Player.State.Parser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cyber_world.Characters.Commands.Action_Command;
import cyber_world.Characters.Commands.Character_Commands.General.Look;
import cyber_world.Characters.Commands.Character_Commands.General.Map;
import cyber_world.Characters.Commands.Character_Commands.General.Move;
import cyber_world.Characters.Commands.Character_Commands.General.Say;
import cyber_world.utils.MathUtils;

/*
 *    Copyright 2017 Ben Hook
 *    In_World_Matcher_General.java
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

public class In_World_Matcher_General implements Input_Parser_Matcher_Interface {
	private Pattern lookPattern = Pattern.compile("(?i)L(?:OOK)?(?: (\\w+))?");
	private Pattern mapPattern = Pattern.compile("(?i)MAP(?: (\\w+))?");
	private Pattern movePattern = Pattern.compile(
			"(?i)(?:(?:MOVE|GO|WALK) )?(N(?:ORTH)?(?:(?:W(?:EST)?)?|E(?:AST)?)|E(?:AST)?|S(?:OUTH)?(?:(?:W(?:EST)?)?|E(?:AST)?)|W(?:EST)?)");
	private Pattern sayPattern = Pattern.compile("(?i)SAY? (.+)");

	public In_World_Matcher_General() {
	}

	@Override
	public Action_Command match(String text) {
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
		} else if ((matcher = sayPattern.matcher(text)).matches()) {
			return new Say(matcher.group(1));
		} else {
			return null;
		}
	}
}
