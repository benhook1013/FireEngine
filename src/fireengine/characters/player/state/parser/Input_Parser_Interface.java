package fireengine.characters.player.state.parser;

import fireengine.characters.commands.Base_Command;

/*
 *    Copyright 2017 Ben Hook
 *    Input_Parser_Interface.java
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

public interface Input_Parser_Interface {
	// TODO clean, trim etc
	static String clean(String text) {
		return text;
	}

	public abstract Base_Command parse(String text);
}
