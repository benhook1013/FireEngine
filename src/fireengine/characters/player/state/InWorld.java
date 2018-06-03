package fireengine.characters.player.state;

import fireengine.characters.commands.Action_Command;
import fireengine.characters.player.state.parser.InWorldParser;

/*
 *    Copyright 2017 Ben Hook
 *    InWorld.java
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

public class InWorld implements PCStateInterface {
	InWorldParser inWorldParser;

	public InWorld() {
		inWorldParser = InWorldParser.getInstance();
	}

	@Override
	public Action_Command acceptInput(String text) {
		return inWorldParser.parse(text);
	}
}
