package fireengine.session.phase;

import fireengine.character.player.Player;
import fireengine.session.Session;
import mud_game.session.phase.PhaseInWorld;
import mud_game.session.phase.PhaseLogin;

/*
 *    Copyright 2019 Ben Hook
 *    Phase.java
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
 * Superclass for phases of the {@link Session}, handled by
 * {@link PhaseManager}.
 *
 * @author Ben Hook
 */
public abstract class Phase {
	protected Session sess;
	protected PhaseManager phaseManager;

//	/**
//	 * Set the {@link Session} and {@link PhaseManager} for the Phase.
//	 *
//	 * @param session
//	 * @param phaseManager
//	 */
//	public abstract void setSession(Session session, PhaseManager phaseManager);

	public abstract void startPhase();

	/**
	 * Passes input into the relevant Session phase, such as {@link PhaseLogin} to
	 * handle logging in or {@link PhaseInWorld} to do actions with your
	 * {@link Player}.
	 *
	 * @param input String input passed into Phase for processing.
	 */
	public abstract void acceptInput(String input);

	/**
	 * Used in the process of closing off the {@link Session}.
	 */
	public abstract void close();
}
