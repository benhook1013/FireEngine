package cyber_world.main;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;

import org.hibernate.HibernateException;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import cyber_world.Characters.Player.Player_Character;
import cyber_world.client_io.ClientConnectionOutput;
import cyber_world.client_io.ClientIOTelnet;
import cyber_world.client_io.exceptions.Client_IO_Telnet_Exception;
import cyber_world.gameworld.Gameworld;
import cyber_world.session.Session;
import cyber_world.utils.CheckedHibernateException;
import cyber_world.utils.MyLogger;

/*
 *    Copyright 2017 Ben Hook
 *    CyberWorldMain.java
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
 * Main Thread, initiates and starts the various services.
 * 
 * @author Ben Hook
 */
public class CyberWorldMain {
	static volatile boolean running;

	// Hibernate stuff
	public static SessionFactory hibSessFactory;

	/**
	 * Number of times IO failure is accepted until server will shutdown.
	 */
	static final int CLIENT_IO_FAILURE_LIMIT = 5;
	/**
	 * Maximum length of client input in characters.
	 */
	public static final int CLIENT_IO_INPUT_MAX_LENGTH = 5000;

	static ClientIOTelnet telnet;
	static int client_IO_Telnet_Failures = 0;
	static final String telnetAddress = "192.168.1.179";
	static final int telnetPort = 1024;

	/**
	 * Executor used to process user input.
	 */
	static public ExecutorService session_Executor;
	/**
	 * Number of threads/executors in the pool available to {@link Session}s for
	 * user input processing.
	 */
	static public final int SESSION_EXECUTOR_POOL = 10;

	public static void main(String[] args) {
		try {
			setUp();
		} catch (Exception e) {
			MyLogger.log(Level.SEVERE, "CyberWorldMain: Failed to start up app.", e);
			shutdown();
		}
		try {
			run();
		} catch (Exception e) {
			MyLogger.log(Level.SEVERE, "CyberWorldMain: Exception while running app.", e);
		} finally {
			shutdown();
		}
	}

	/**
	 * Setup the world and start accepting connections.
	 * 
	 * @throws Exception
	 *             general {@link Exception} catching to allow for logging and
	 *             purposeful shutdown.
	 */
	private static void setUp() throws Exception {
		MyLogger.log(Level.INFO, "CyberWorldMain: Bootstrapping cyber_world!");

		try {
			MyLogger.log(Level.INFO, "CyberWorldMain: Initiating Hibernate");
			hibSessFactory = new Configuration().configure().buildSessionFactory();
		} catch (HibernateException e) {
			throw new CyberWorldMainSetUpException("CyberWorldMain: Hibernate Exception", e);
		}

		MyLogger.log(Level.INFO, "CyberWorldMain: Setting up Gameworld");
		Gameworld.setupGameworld();

		MyLogger.log(Level.INFO, "CyberWorldMain: Initiating Session Executors");
		session_Executor = Executors.newFixedThreadPool(SESSION_EXECUTOR_POOL);

		startClientIO();
		telnet.setAccepting(true);
	}

	/**
	 * Starts the telnet thread and starts accepting connections.
	 * 
	 * @throws CyberWorldMainSetUpException
	 */
	private static void startClientIO() throws CyberWorldMainSetUpException {
		MyLogger.log(Level.INFO, "CyberWorldMain: Starting Client_IO...");

		if (telnet != null) {
			MyLogger.log(Level.INFO, "CyberWorldMain: Cleaning up old Client_IO.");
			telnet.clearResources();
		}

		try {
			telnet = null;
			telnet = new ClientIOTelnet(telnetAddress, telnetPort);
			telnet.start();
		} catch (Client_IO_Telnet_Exception e) {
			throw new CyberWorldMainSetUpException("CyberWorldMain: Failed to create and start Client_Telnet_IO.", e);
		}
		while (telnet.getState() != Thread.State.RUNNABLE) {
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				MyLogger.log(Level.WARNING,
						"CyberWorldMain: Thread interrupted while waiting for ClientIOTelnet to start.", e);
				continue;
			}
		}
	}

	/**
	 * Main run loop for main thread. Will loop through monitoring server status and
	 * shutdown if too many IO failures occur. Will attempt to restart IO upon
	 * exception.
	 */
	public static void run() {
		MyLogger.log(Level.INFO, "CyberWorldMain: Starting cyber_world...");
		running = true;

		while (running) {
			// MyLogger.log(Level.INFO, "Running main thread loop.");

			if (!telnet.isAlive()) {
				MyLogger.log(Level.SEVERE,
						"CyberWorldMain: ClientIOTelnet thread stopped without being asked to stop.");
				client_IO_Telnet_Failures++;
				if (client_IO_Telnet_Failures >= CLIENT_IO_FAILURE_LIMIT) {
					MyLogger.log(Level.SEVERE,
							"CyberWorldMain: ClientIOTelnet thread has stopped unexpectedly too many times, shutting down.");
					stop();
					break;
				} else {
					MyLogger.log(Level.SEVERE, "CyberWorldMain: Restarting ClientIOTelnet.");
					try {
						startClientIO();
					} catch (CyberWorldMainSetUpException e) {
						MyLogger.log(Level.SEVERE,
								"CyberWorldMain: Error while trying to restart ClientIOTelnet, shutting down.", e);
						stop();
						break;
					}
				}
			}

			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				MyLogger.log(Level.INFO, "CyberWorldMain: Main thread running loop sleep interrupted.", e);
			}
			// stop();
			// break;
		}
	}

	/**
	 * Function used to indicate that the main thread should start shutdown process
	 * upon next loop.
	 */
	public static void stop() {
		running = false;
	}

	/**
	 * Starts shutdown process, attempting to gracefully close threads and parts of
	 * application.
	 */
	private static void shutdown() {
		MyLogger.log(Level.INFO, "CyberWorldMain: Starting cyber_world shutdown.");
		shutdownClientIO();
		// TODO Save persistent classes
		cleanUp();
		MyLogger.log(Level.INFO, "CyberWorldMain: Finished cyber_world shutdown.");
	}

	/**
	 * Tries to gracefully attempt {@link Session} and {@link ClientIOTelnet}
	 * shutdown, and will forcefully stop thread if it doesn't shut itself down
	 * within 5 seconds.
	 */
	private static void shutdownClientIO() {
		int timerCount;

		if (telnet != null) {
			telnet.setAccepting(false);
		}

		Session.endSessions();
		timerCount = 0;
		while (Session.numSessions() > 0) {
			if (timerCount > 50) {
				MyLogger.log(Level.WARNING, "CyberWorldMain: Sessions took longer then 5 seconds to close.");
			}
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				MyLogger.log(Level.WARNING, "CyberWorldMain: InterruptedException while waitingfor Sessions to close.",
						e);
			}
			timerCount += 1;
		}
		if (Session.numSessions() > 0) {
			MyLogger.log(Level.WARNING, "CyberWorldMain: Not all Sessions closed gracefully, force closing.");
			Session.closeSessions();
			MyLogger.log(Level.INFO, "CyberWorldMain: Finished force closing Sessions.");
		}
		MyLogger.log(Level.INFO, "CyberWorldMain: Finished closing Sessions.");

		if (telnet != null) {
			telnet.stopRunning();
			timerCount = 0;
			while (telnet.isAlive()) {
				if (timerCount > 50) {
					MyLogger.log(Level.WARNING,
							"CyberWorldMain: ClientIOTelnet thread took longer then 5 seconds to shutdown.");
					break;
				}
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					MyLogger.log(Level.WARNING,
							"CyberWorldMain: InterruptedException while waitingfor ClientIOTelnet to shutdown.", e);
				}
				timerCount += 1;
			}
			if (telnet.isAlive()) {
				MyLogger.log(Level.WARNING,
						"CyberWorldMain: ClientIOTelnet thread did not shutdown, continuing anyway.");
			}
		}
	}

	/**
	 * Starts post shutdown cleanup.
	 */
	private static void cleanUp() {
		MyLogger.log(Level.INFO, "CyberWorldMain: Starting shutdown cleanup.");
		if (hibSessFactory != null) {
			hibSessFactory.close();
		}
		// cleanUpClientIO();
		MyLogger.log(Level.INFO, "CyberWorldMain: Finished shutdown cleanup.");
	}

	// /**
	// * Post shutdown cleanup of IO related stuff.
	// */
	// private static void cleanUpClientIO() {
	// MyLogger.log(Level.INFO, "CyberWorldMain: Cleaning up Client IO....");
	// MyLogger.log(Level.INFO, "CyberWorldMain: Finished cleaning up Client IO.");
	// }

	/**
	 * Custom handling of {@link HibernateException} which is usually unchecked, but
	 * using a {@link CheckedHibernateException} we pass to this function, which
	 * allows us to recognise the Hibernate problem and shutdown gracefully.
	 * 
	 * @param e
	 *            The {@link CheckedHibernateException} thrown.
	 */
	public static void hibernateException(CheckedHibernateException e) {
		ClientConnectionOutput playerMessage = new ClientConnectionOutput(1);
		playerMessage.addPart("Fatal Database error detected, shutting down.", null, null);
		Player_Character.sendToAllPlayers(playerMessage);
		stop();
		MyLogger.log(Level.SEVERE, "CyberWorldMain: Fatal Hibernate exception caught, initiating shutdown.", e);
	}
}
