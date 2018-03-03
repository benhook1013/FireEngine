package cyber_world.session.phase;

import cyber_world.Characters.Player.Player_Character;
import cyber_world.session.Session;

/*
 *    Copyright 2017 Ben Hook
 *    PhaseInWorld.java
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
 * Phase of {@link Session} which connects the Session to the
 * {@link Player_Character} in the world.
 * 
 * @author Ben Hook
 */
public class PhaseInWorld implements PhaseInterface {
	private Player_Character player;

	/**
	 * Private no-arg constructor to avoid accidental instantiation.
	 */
	private PhaseInWorld() {
	}

	public PhaseInWorld(Player_Character player) {
		this();
		this.player = player;
	}

	@Override
	public void acceptInput(String input) {
		player.acceptInput(input);
	}

	@Override
	public void disconnect() {
		player.disconnect();
		close();
	}

	@Override
	public void close() {
		player = null;
	}
}
