package FireEngine.Characters.Commands.Character_Commands.General;

import FireEngine.Characters.Base_Character;
import FireEngine.Characters.Commands.Character_Commands.Character_Command;
import FireEngine.client_io.ClientConnectionOutput;
import FireEngine.utils.StringUtils;

/*
 *    Copyright 2017 Ben Hook
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

public class Say extends Character_Command {
	private String text;

	public Say(String text) {
		super();

		this.text = StringUtils.sentance(text);
	}

	@Override
	public void doAction(Base_Character character) {
		ClientConnectionOutput output = new ClientConnectionOutput(2);

		output.addPart(character.getName() + " says, \"" + text + "\"", null, null);

		character.getRoom().sendToRoom(output);
	}
}
