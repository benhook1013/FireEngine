package FireEngine.session;

/*
 *    Copyright 2017 Ben Hook
 *    Session.java
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

import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.logging.Level;

import FireEngine.Characters.Player.Player_Character;
import FireEngine.client_io.ClientConnectionOutput;
import FireEngine.client_io.Client_Connection_Interface;
import FireEngine.client_io.Client_IO_Colour;
import FireEngine.client_io.exceptions.Client_Connection_Exception;
import FireEngine.main.CyberWorldMain;
import FireEngine.session.phase.PhaseManager;
import FireEngine.session.phase.PhaseWelcome;
import FireEngine.utils.MyLogger;

/**
 * Session of the connection for a {@link Player_Character}.
 * 
 * @author Ben Hook
 */
public class Session {
	private static ArrayList<Session> sessionList = new ArrayList<>();
	private Session sess;
	private Client_Connection_Interface ccon;
	private PhaseManager phaseManager;

	// TODO Options for ANSI.
	private boolean ansi = true;

	private volatile boolean closing;
	private volatile boolean closed;
	private volatile Future<Integer> sessionFuture;
	private volatile Object sessionFutureLock = new Object();

	/**
	 * Creates a new Session for the provided {@link Client_Connection_Interface}
	 * 
	 * @param ccon
	 *            Client_Connection_Interface to make the session for
	 */
	public Session(Client_Connection_Interface ccon) {
		synchronized (sessionFutureLock) {
			this.ccon = ccon;
			this.sess = this;
			synchronized (sessionList) {
				sessionList.add(this);
			}

			sessionFuture = CyberWorldMain.session_Executor.submit(new Callable<Integer>() {
				@Override
				public Integer call() throws Exception {
					try {
						ccon.setupConnection(sess);
					} catch (Client_Connection_Exception e) {
						MyLogger.log(Level.WARNING, "Session: Failed to setup Client_Connection_Interface.", e);
						sess.close();
						return 1;
					}

					MyLogger.log(Level.INFO, "Session: Instantiating Session...");
					closing = false;
					closed = false;
					phaseManager = new PhaseManager(sess);
					phaseManager.setPhase(new PhaseWelcome(sess, phaseManager));
					ccon.acceptInput();
					return 0;
				}
			});
		}
	}

	/**
	 * Function to pass on {@link ClientConnectionOutput} from the Session to the
	 * {@link Client_Connection_Interface}.
	 * 
	 * @param output
	 *            ClientConnectionOutput to be sent
	 */
	public void send(ClientConnectionOutput output) {
		ccon.writeToConnection(output, ansi);
	}

	// TODO SYNCH
	// TODO Keep alive thread.
	/**
	 * Let the Session know that input has been received by the
	 * {@link Client_Connection_Interface}.
	 */
	public void notifyInput() {
		synchronized (sessionFutureLock) {
			if (sessionFuture != null) {
				if (!sessionFuture.isDone()) {
					System.out.println("notifyInput WAITING");
					return;
				}
			}

			if (closed) {
				return;
			}

			sessionFuture = CyberWorldMain.session_Executor.submit(new Callable<Integer>() {
				@Override
				public Integer call() throws Exception {
					String input;

					while ((input = ccon.readFromConnection()) != null) {
						try {
							phaseManager.acceptInput(input);
						} catch (Exception e) {
							MyLogger.log(Level.WARNING, "Session: Unexpected exception caught.", e);
						}
					}

					if (closing) {
						send(endMsg());
						closed = true;
						ccon.shutdown();
					}

					return 0;
				}
			});
		}
	}

	/**
	 * Used to disconnect a Session from the {@link Player_Character} but not end
	 * the Session, in such situations as someone logging in overtop another
	 * Session. Returns the Session to the {@link PhaseWelcome}.
	 */
	public void disconnect() {
		phaseManager.disconnect();
	}

	/**
	 * Used to signal the Session to gracefully finish parsing input and close down.
	 * Will lead to the Session telling the {@link Client_Connection_Interface} to
	 * respond once writing out is finished, allowing the Session to close down.
	 */
	private void end() {
		ccon.refuseInput();
		closing = true;
		notifyInput();
	}

	/**
	 * Just a weird little way of decoupling the goodbye message out, so its obvious
	 * where to change it later.
	 * 
	 * @return {@link ClientConnectionOutput} containing goodbye message
	 */
	private ClientConnectionOutput endMsg() {
		ClientConnectionOutput goodbyeMsg = new ClientConnectionOutput(1);
		goodbyeMsg.addPart("Goodbye!", Client_IO_Colour.COLOURS.BRIGHTCYAN, null);
		return goodbyeMsg;
	}

	/**
	 * Used by the {@link Client_Connection_Interface} to let the Session know that
	 * it has finished writing out all data, to allow graceful closing of Session.
	 */
	public void notifyCconFinished() {
		CyberWorldMain.session_Executor.submit(new Callable<Integer>() {
			@Override
			public Integer call() throws Exception {
				close();

				return 0;
			}
		});
	}

	/**
	 * The function that actually closes out the Session, doing the work of closing
	 * off objects and such.
	 */
	private void close() {
		synchronized (this) {
			phaseManager.close();
			if (ccon != null) {
				ccon.close();
				ccon = null;
			}
			sessionFuture = null;
			sessionFutureLock = null;
			sess = null;
		}
		synchronized (sessionList) {
			sessionList.remove(this);
		}
	}

	/**
	 * Asks all open Sessions to end, typically used in graceful application
	 * shutdown.
	 */
	public static void endSessions() {
		synchronized (sessionList) {
			for (Session sess : sessionList) {
				sess.end();
			}
		}
	}

	/**
	 * Force closes all open Sessions, typically used when trying to shutdown but
	 * Sessions have not closed by themselves gracefully.
	 */
	public static void closeSessions() {
		MyLogger.log(Level.INFO, "Session: Starting forced shutdownSessions.");
		synchronized (sessionList) {
			for (Session sess : sessionList) {
				sess.close();
			}
		}
		MyLogger.log(Level.INFO, "Session: Finished forced shutdownSessions.");
	}

	/**
	 * Returns number of alive Sessions.
	 * 
	 * @return number of open Sessions
	 */
	public static int numSessions() {
		synchronized (sessionList) {
			return sessionList.size();
		}
	}
}
