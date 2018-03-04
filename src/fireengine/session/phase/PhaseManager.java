package fireengine.session.phase;

import fireengine.session.Session;
import fireengine.session.phase.PhaseWelcome;

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
 * ({@link PhaseInterface}).
 * 
 * @author Ben Hook
 */
public class PhaseManager implements PhaseInterface {
	Session sess;
	private PhaseInterface phase;

	public PhaseManager(Session sess) {
		this.sess = sess;
		phase = null;
	}

	/**
	 * Assigns a phase to the {@link PhaseManager}.
	 * 
	 * @param phase phase to set current
	 */
	public void setPhase(PhaseInterface phase) {
		synchronized (this) {
			this.phase = phase;
		}
	}
	

	/**
	 * phaseManager.setPhase(new PhaseWelcome(sess, phaseManager));
	 */
	public void setWelcomePhase() {
		setPhase(new PhaseWelcome(sess, this));
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
		setWelcomePhase();
	}

	/**
	 * Closes current {@link PhaseInterface} and closes PhaseManager.
	 */
	@Override
	public void close() {
		phase.close();
		phase = null;
		sess = null;
	}
}
