package cyber_world.session.phase;

import java.util.logging.Level;

import cyber_world.Characters.Commands.Menu_Commands.Command_Back;
import cyber_world.Characters.Commands.Menu_Commands.Menu_Command;
import cyber_world.Characters.Player.Player_Character;
import cyber_world.Characters.Player.Exceptions.PC_Exception_Null_Room;
import cyber_world.Characters.Player.State.Parser.Menu_Parser;
import cyber_world.client_io.ClientConnectionOutput;
import cyber_world.gameworld.Gameworld;
import cyber_world.gameworld.maps.BaseRoom;
import cyber_world.gameworld.maps.GameMap;
import cyber_world.gameworld.maps.Exceptions.Map_Exception_Out_Of_Bounds;
import cyber_world.main.CyberWorldMain;
import cyber_world.session.Session;
import cyber_world.utils.CheckedHibernateException;
import cyber_world.utils.MyLogger;
import cyber_world.utils.StringUtils;

/*
 *    Copyright 2017 Ben Hook
 *    PhaseLogin.java
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
 * Phase of {@link Session} in which you login a {@link Player_Character}.
 * 
 * @author Ben Hook
 */
public class PhaseLogin implements PhaseInterface {
	private Session sess;
	private PhaseManager phaseManager;
	private Menu_Parser parser = Menu_Parser.getInstance();
	private states loginState;

	private Player_Character player;

	/**
	 * Enum signifying state of the Phase, either verifying Name or Password.
	 */
	private enum states {
		NAME, PASSWORD
	};

	/**
	 * Private no-arg constructor to avoid accidental instantiation.
	 */
	private PhaseLogin() {
	}

	public PhaseLogin(Session sess, PhaseManager phaseManager) {
		this();
		this.sess = sess;
		this.phaseManager = phaseManager;

		sendLoginPrompt();
		sendNamePrompt();
	}

	@Override
	public void acceptInput(String input) {
		Menu_Command command = parser.parse(input);
		if (command instanceof Command_Back) {
			phaseManager.setPhase(new PhaseWelcome(sess, phaseManager));
		} else if (loginState == states.NAME) {
			testName(input);
		} else if (loginState == states.PASSWORD) {
			testPassword(input);
		}
	}

	private void sendLoginPrompt() {
		sess.send(new ClientConnectionOutput("Enter \"BACK\" or \"0\" to return to the main menu."));
	}

	private void sendNamePrompt() {
		loginState = states.NAME;
		player = null;
		sess.send(new ClientConnectionOutput("Enter the name of your character:"));
		sess.send(new ClientConnectionOutput());
	}

	private void testName(String name) {
		name = StringUtils.capitalise(name);

		try {
			player = Player_Character.findCharacter(name);
		} catch (CheckedHibernateException e) {
			CyberWorldMain.hibernateException(e);
			return;
		}

		sess.send(new ClientConnectionOutput("You sent name: '" + name + "'"));
		if (player != null) {
			sess.send(new ClientConnectionOutput("Found player with name: " + player.getName()));
			sendPasswordPrompt();
		} else {
			sess.send(new ClientConnectionOutput("Found no player with name: " + name));
			sendLoginPrompt();
			sendNamePrompt();
		}
	}

	private void sendPasswordPrompt() {
		loginState = states.PASSWORD;
		sess.send(new ClientConnectionOutput("Enter the password for " + player.getName()));
		sess.send(new ClientConnectionOutput());
	}

	private void testPassword(String password) {
		sess.send(new ClientConnectionOutput("You sent password: '" + password + "'"));
		if (player.getPassword().equals(password)) {
			login();
		} else {
			sess.send(new ClientConnectionOutput("Password was incorrect! Try again or go back to main menu."));
			sendLoginPrompt();
			sendPasswordPrompt();
		}
	}

	private void login() {
		sess.send(new ClientConnectionOutput("You have logged in!"));
		sess.send(new ClientConnectionOutput());

		BaseRoom room = null;
		GameMap mainMap = Gameworld.findMap(1);
		if (mainMap == null) {
			ClientConnectionOutput output = new ClientConnectionOutput(4);
			output.addPart("Oh no! Couldn't find the main map!", null, null);
			output.newLine();
			output.addPart("Returning you to the main menu...", null, null);
			output.newLine();
			sess.send(output);

			phaseManager.setPhase(new PhaseWelcome(sess, phaseManager));
			return;
		}
		try {
			room = mainMap.getRoom(0, 0);
		} catch (Map_Exception_Out_Of_Bounds e) {
			MyLogger.log(Level.WARNING, "PhaseLogin: Origin room 0,0 of main map is out of bounds.", e);
			return;
		}
		if (room == null) {
			ClientConnectionOutput output = new ClientConnectionOutput(4);
			output.addPart("Oh no! Couldn't find the login room!", null, null);
			output.newLine();
			output.addPart("Returning you to the main menu...", null, null);
			output.newLine();
			sess.send(output);

			phaseManager.setPhase(new PhaseWelcome(sess, phaseManager));
			return;
		}

		try {
			player.connect(sess, room);
			sess.send(new ClientConnectionOutput("Sucessfully sent to " + room.getRoomName()));
			sess.send(new ClientConnectionOutput());
			phaseManager.setPhase(new PhaseInWorld(player));
		} catch (PC_Exception_Null_Room e) {
			MyLogger.log(Level.WARNING, "PhaseLogin: Tried to enterWorld to a null room.", e);

			ClientConnectionOutput output = new ClientConnectionOutput(4);
			output.addPart("Something went wrong trying to send you into the world.", null, null);
			output.newLine();
			output.addPart("Returning you to the main menu...", null, null);
			output.newLine();
			sess.send(output);

			phaseManager.setPhase(new PhaseWelcome(sess, phaseManager));
			return;
		}
	}

	@Override
	public void disconnect() {
		close();
	}

	@Override
	public void close() {
		player = null;
	}
}
