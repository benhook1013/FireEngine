package fireengine.character.player.state;

import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import fireengine.character.command.exception.CommandExceptionNoPattern;
import fireengine.character.skillset.Skillset;
import fireengine.character.skillset.Skillset.SkillsetCategory;
import fireengine.character.skillset.exception.SkillsetExceptionLackExperience;
import fireengine.client_io.ClientConnectionOutput;
import fireengine.util.MyLogger;

/*
 *    Copyright 2019 Ben Hook
 *    StatePlayerInWorld.java
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

public class StatePlayerInWorld implements StatePlayer {
	fireengine.character.Character character;

	public StatePlayerInWorld(fireengine.character.Character character) {
		this.character = character;
		this.character.refreshSkillsetList();
	}

	@Override
	public ClientConnectionOutput acceptInput(String text) {
		ClientConnectionOutput result;
		for (Skillset skillset : character.getSkillsetList()) {
			result = checkSkillset(text, skillset);
			if (result != null) {
				return result;
			}
		}

		return new ClientConnectionOutput("I don't know what you mean.");
	}

	/**
	 * Checks the text against the Patterns for each skill in a Skillset.
	 * 
	 * @param text
	 * @param skillset
	 * @return
	 * @throws SkillsetExceptionLackExperience
	 */
	private ClientConnectionOutput checkSkillset(String text, Skillset skillset) {
		Matcher matcher;
		Pattern pattern;
		ClientConnectionOutput output;
		for (SkillsetCategory.ActionEntry actionEntry : skillset.getSkillEntries()) {
			try {
				pattern = actionEntry.getAction().getPattern();
				matcher = pattern.matcher(text);
				if (matcher.matches()) {
					output = new ClientConnectionOutput();
					try {
						output.addOutput(actionEntry.doAction(character, matcher));
					} catch (SkillsetExceptionLackExperience e) {
						output.addPart("You have not yet learned this skill.", null, null);
					}
					return output;
				}
			} catch (CommandExceptionNoPattern e) {
				MyLogger.log(Level.SEVERE, String.format("StatePlayerInWorld: Action '%s' does not have pattern set.",
						actionEntry.getAction().getClass().getName()));
			}
		}
		return null;
	}
}
