package cyber_world.Characters.Player.State;

import cyber_world.Characters.Commands.Action_Command;
import cyber_world.Characters.Player.State.Parser.In_World_Parser;

/*
 *    Copyright 2017 Ben Hook
 *    In_World.java
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

public class In_World implements PC_State_Interface {
	In_World_Parser inWorldParser;

	public In_World() {
		inWorldParser = In_World_Parser.getInstance();
	}

	@Override
	public Action_Command acceptInput(String text) {
		return inWorldParser.parse(text);
	}
}
