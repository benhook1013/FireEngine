package fireengine.character.command.action.general.player_action;

import fireengine.character.command.action.Action;

/*
 *    Copyright 2019 Ben Hook
 *    PlayerAction.java
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

/**
 * Adds ability to add functionality to a Command. A Command is simply a way to
 * know what menu option was selected etc, but an Action is done by a Character
 * and does something.
 * 
 * @author Ben Hook
 */
public abstract class PlayerAction extends Action {
	protected PlayerAction() {
		super();
	}
}