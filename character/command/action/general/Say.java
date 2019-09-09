package fireengine.character.command.action.general;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import fireengine.character.Character;
import fireengine.character.command.action.Action;
import fireengine.client_io.ClientConnectionOutput;
import fireengine.util.StringUtils;

/*
 *    Copyright 2019 Ben Hook
 *    Say.java
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

public class Say extends Action {
	private static Pattern pattern = compilePattern("SAY? (.+)");

	public Say() {
		super();
	}

	@Override
	public ClientConnectionOutput doAction(Character character, Matcher matcher) {
		String sayText = StringUtils.sentence(matcher.group(1));

		return doAction(character, sayText);
	}

	public ClientConnectionOutput doAction(Character character, String sayText) {
		ClientConnectionOutput output = new ClientConnectionOutput();

		output.addPart(character.getName() + " says, \"" + sayText + "\"", null, null);

		return output;
	}

	public Pattern getPattern() {
		return pattern;
	}
}
