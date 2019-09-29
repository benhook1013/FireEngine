package fireengine.session.phase;

import java.util.logging.Level;

import fireengine.client_io.ClientConnectionOutput;
import fireengine.session.Session;
import fireengine.util.ConfigLoader;
import fireengine.util.MyClassLoader;
import fireengine.util.MyLogger;
import mud_game.session.phase.PhaseWelcome;

/*
 *    Copyright 2019 Ben Hook
 *    PhaseManager.java
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
 * Class to contain and manage/switch between Session phases ({@link Phase}).
 *
 * @author Ben Hook
 */
public class PhaseManager {
	Session sess;
	private Phase phase;

	private static MyClassLoader classLoader;
	private static Class<Phase> welcomePhaseClass;

	public PhaseManager() {
	}

	/**
	 * @param session
	 */
	public void setSession(Session session) {
		sess = session;
	}

	@SuppressWarnings("unchecked")
	public static void loadWelcomePhase() throws ClassNotFoundException {
		classLoader = new MyClassLoader();
		welcomePhaseClass = (Class<Phase>) classLoader.loadClass(ConfigLoader.getSetting("welcomePhaseClassName"));
	}

	/**
	 * Assigns a phase to the {@link PhaseManager}.
	 * 
	 * TODO Review this with a better idea, as its messy that the call stack has the
	 * old phase as caller after new phase set on PhaseManager.
	 *
	 * @param phase phase to set current
	 */
	public void setPhase(Phase phase) {
		synchronized (this) {
			this.phase = phase;
			this.phase.startPhase();
		}
	}

	/**
	 *
	 *
	 */
	public void setWelcomePhase() {
		Phase welcomePhaseInstance;

		try {
			welcomePhaseInstance = welcomePhaseClass.getConstructor(Session.class, PhaseManager.class).newInstance(sess,
					this);
		} catch (Exception e) {
			MyLogger.log(Level.SEVERE, "PhaseManager: Error thrown while trying to phaseManager.setWelcomePhase().", e);
			sess.send(new ClientConnectionOutput(
					"Error occured: This has been logged and will be reviewed by a developer.", null, null));
			sess.end();
			return;
		}
		setPhase(welcomePhaseInstance);
	}

	public void acceptInput(String input) {
		phase.acceptInput(input);
	}

	/**
	 * Used to disconnect from a potential player, close current Phase, and return
	 * to {@link PhaseWelcome}.
	 */
	public void disconnect() {
		if (phase != null) {
			phase.close();
		}

		setWelcomePhase();
	}

	/**
	 * Closes current {@link Phase} and closes PhaseManager.
	 */
	public void close() {
		disconnect();

		phase = null;
		sess = null;
	}
}
