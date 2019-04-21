package session.phase;

import session.Session;
import utils.ConfigLoader;
import utils.MyClassLoader;

/*
 *    Copyright 2017 Ben Hook
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
 * Class to contain and manage/switch between Session phases
 * ({@link Phase}).
 * 
 * @author Ben Hook
 */
public class PhaseManager implements Phase {
	Session sess;
	private Phase phase;

	private static MyClassLoader classLoader;
	private static Class<Phase> welcomePhaseClass;

	public PhaseManager() {
	}

	/**
	 * @see fireengine.session.phase.Phase#setSession(fireengine.session.Session,
	 *      fireengine.session.phase.PhaseManager)
	 */
	@Override
	public void setSession(Session session, PhaseManager phaseManager) {
		sess = session;
	}

	@SuppressWarnings("unchecked")
	public static void loadWelcomePhase() throws ClassNotFoundException {
		classLoader = new MyClassLoader();
		welcomePhaseClass = (Class<Phase>) classLoader
				.loadClass(ConfigLoader.getSetting("welcomePhaseClassName"));
	}

	/**
	 * Assigns a phase to the {@link PhaseManager}.
	 * 
	 * @param phase
	 *            phase to set current
	 */
	public void setPhase(Phase phase) {
		synchronized (this) {
			this.phase = phase;
			this.phase.setSession(sess, this);
		}
	}

	/**
	 * 
	 * 
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 */
	public void setWelcomePhase() throws InstantiationException, IllegalAccessException {
		Phase welcomePhaseInstance;

		welcomePhaseInstance = welcomePhaseClass.newInstance();
		setPhase(welcomePhaseInstance);
	}

	@Override
	public void acceptInput(String input) {
		phase.acceptInput(input);
	}

	/**
	 * Used to disconnect and return to {@link PhaseWelcome}.
	 */
	@Override
	public void disconnect() {
		phase.disconnect();
		phase = null;
	}

	/**
	 * Closes current {@link Phase} and closes PhaseManager.
	 */
	@Override
	public void close() {
		phase.close();
		phase = null;
		sess = null;
	}
}
