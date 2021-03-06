package fireengine.session;

/*
 *    Copyright 2019 Ben Hook
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

import fireengine.character.player.Player;
import fireengine.client_io.ClientConnection;
import fireengine.client_io.ClientConnectionOutput;
import fireengine.client_io.ClientIOColour;
import fireengine.client_io.exception.ClientConnectionException;
import fireengine.main.FireEngineMain;
import fireengine.session.phase.PhaseManager;
import fireengine.util.MyLogger;
import mud_game.session.phase.PhaseWelcome;

/**
 * Session of the connection for a {@link Player}.
 *
 * @author Ben Hook
 */
public class Session {
	private final static ArrayList<Session> sessionList = new ArrayList<>();
	private Session sess;
	private ClientConnection ccon;
	private PhaseManager phaseManager;

	// TODO Options for ANSI.
	private boolean ansi = true;

	private volatile boolean closing;
	private volatile boolean closed;
	private Future<Integer> sessionFuture;

	/**
	 * Creates a new Session for the provided {@link ClientConnection}
	 *
	 * @param ccon ClientConnection to make the session for
	 */
	public Session(ClientConnection ccon) {
		synchronized (this) {
			this.ccon = ccon;
			this.sess = this;
			synchronized (sessionList) {
				sessionList.add(this);
			}

			// Seems unnecessary to be multi-threaded here but is called from ClientIO
			// thread.
			sessionFuture = FireEngineMain.sessionExecutor.submit(new Callable<Integer>() {
				@Override
				public Integer call() {
					try {
						ccon.setupConnection(sess);
					} catch (ClientConnectionException e) {
						MyLogger.log(Level.WARNING, "Session: Failed to setup ClientConnection.", e);
						sess.send(new ClientConnectionOutput(
								"Error setting up session, you may want to notify a God out of game."));
						sess.close();
						return 1;
					}

					MyLogger.log(Level.INFO, "Session: Instantiating Session...");
					closing = false;
					closed = false;
					phaseManager = new PhaseManager();
					phaseManager.setSession(sess);

					phaseManager.setWelcomePhase();
					ccon.acceptInput();
					return 0;
				}
			});
		}
	}

	/**
	 * Function to pass on {@link ClientConnectionOutput} from the Session to the
	 * {@link ClientConnection}.
	 *
	 * @param output ClientConnectionOutput to be sent
	 */
	public void send(ClientConnectionOutput output) {
		ccon.writeToConnection(output, ansi);
	}

	// TODO Needs to exit from call early if already processing command, and check
	// for more input after finished processing.
	/**
	 * Let the Session know that input has been received by the
	 * {@link ClientConnection}.
	 */
	public void notifyInput() {
		synchronized (this) {
			if (sessionFuture != null) {
				if (!sessionFuture.isDone()) {
					// Indicates that input is received while prior input still being processed
					// (such as when many commands received in quick succession).
					MyLogger.log(Level.FINE, "notifyInput WAITING");
					return;
				}
			}

			if (closed) {
				MyLogger.log(Level.WARNING, "Session: notifyInput recieved while session closed.");
				return;
			}

			sessionFuture = FireEngineMain.sessionExecutor.submit(new Callable<Integer>() {
				@Override
				public Integer call() throws Exception {
					String input;

					while ((input = ccon.readFromConnection()) != null) {
						try {
							phaseManager.acceptInput(input);
						} catch (Exception e) {
							MyLogger.log(Level.SEVERE,
									"Session: Unexpected exception caught from phaseManager.acceptInput(input).", e);
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
	 * Used to disconnect a Session from the {@link Player} but not end the Session,
	 * in such situations as someone logging in overtop another Session. Returns the
	 * Session to the {@link PhaseWelcome}.
	 */
	public void disconnect() {
		phaseManager.disconnect();
	}

	/**
	 * Used to signal the Session to gracefully finish parsing input and close down.
	 * Will lead to the Session telling the {@link ClientConnection} to respond once
	 * writing out is finished, allowing the Session to close down.
	 */
	public void end() {
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
		goodbyeMsg.addPart("Goodbye!", ClientIOColour.COLOURS.BRIGHTCYAN, null);
		return goodbyeMsg;
	}

	/**
	 * Used by the {@link ClientConnection} to let the Session know that it has
	 * finished writing out all data, to allow graceful closing of Session. May be
	 * called by ClientConnection in case where connection is unexpectedly
	 * terminated.
	 */
	public void notifyCconShutdown() {
		FireEngineMain.sessionExecutor.submit(new Callable<Integer>() {
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
			sess = null;

			synchronized (sessionList) {
				sessionList.remove(this);
			}
		}
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

	/**
	 * Returns number of alive Sessions.
	 *
	 * @return number of open Sessions
	 */
	public static ArrayList<Session> getSessions() {
		synchronized (sessionList) {
			return sessionList;
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
		MyLogger.log(Level.INFO, "Session: Starting forced shutdown of Sessions.");
		synchronized (sessionList) {
			for (Session sess : sessionList) {
				sess.close();
			}
		}
		MyLogger.log(Level.INFO, "Session: Finished forced shutdown of Sessions.");
	}
}
