package cyber_world.session.phase;

import cyber_world.Characters.Commands.Menu_Commands.Command_Back;
import cyber_world.Characters.Commands.Menu_Commands.Menu_Command;
import cyber_world.Characters.Player.Player_Character;
import cyber_world.Characters.Player.State.Parser.Menu_Parser;
import cyber_world.client_io.ClientConnectionOutput;
import cyber_world.main.CyberWorldMain;
import cyber_world.session.Session;
import cyber_world.utils.CheckedHibernateException;
import cyber_world.utils.StringUtils;

/*
 *    Copyright 2017 Ben Hook
 *    PhaseCreation.java
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
 * Phase of {@link Session} in which you create a {@link Player_Character}.
 * 
 * @author Ben Hook
 */
public class PhaseCreation implements PhaseInterface {
	private Session sess;
	private PhaseManager phaseManager;
	private Menu_Parser parser = Menu_Parser.getInstance();
	private states loginState;

	private String name;
	private String password;
	private Player_Character player;

	/**
	 * Enum signifying state of the Phase, either choosing Name or Password.
	 */
	private enum states {
		NAME, PASSWORD
	};

	/**
	 * Private no-arg constructor to avoid accidental instantiation.
	 */
	private PhaseCreation() {
	}

	public PhaseCreation(Session sess, PhaseManager phaseManager) {
		this();
		this.sess = sess;
		this.phaseManager = phaseManager;
		player = null;

		sendCreationPrompt();
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

	private void sendCreationPrompt() {
		sess.send(new ClientConnectionOutput("Enter \"BACK\" or \"0\" to return to the main menu."));
		sess.send(new ClientConnectionOutput(
				"DO NOTE USE ANY SENSITIVE INFO in name or password, they are publically viewable from main menu."));
	}

	private void sendNamePrompt() {
		loginState = states.NAME;
		sess.send(new ClientConnectionOutput("Enter the name you would like:"));
		sess.send(new ClientConnectionOutput());
	}

	private void testName(String name) {
		this.name = null;
		name = StringUtils.capitalise(name);
		sess.send(new ClientConnectionOutput("You sent name: '" + name + "'"));

		if (Player_Character.testNameRules(name)) {
			sess.send(new ClientConnectionOutput(
					"Name appears to not break any rules, lets see if its already taken..."));
		} else {
			sess.send(new ClientConnectionOutput("The name you sent violates rules, try again."));
			sendCreationPrompt();
			sendNamePrompt();
		}

		Player_Character playerSearch = null;
		try {
			playerSearch = Player_Character.findCharacter(name);
		} catch (CheckedHibernateException e) {
			CyberWorldMain.hibernateException(e);
			return;
		}

		if (playerSearch != null) {
			sess.send(new ClientConnectionOutput("Found player with name: " + playerSearch.getName()));
			sess.send(new ClientConnectionOutput("Please try another (free) name."));
			sess.send(new ClientConnectionOutput());
			sendCreationPrompt();
			sendNamePrompt();
		} else {
			sess.send(new ClientConnectionOutput("Found no player with name: " + name));
			this.name = name;
			sendPasswordPrompt();
		}
	}

	private void sendPasswordPrompt() {
		loginState = states.PASSWORD;
		sess.send(new ClientConnectionOutput("Enter the password for your new character:"));
		sess.send(new ClientConnectionOutput());
	}

	private void testPassword(String password) {
		sess.send(new ClientConnectionOutput("You sent password: '" + password + "'"));

		if (Player_Character.testPasswordRules(password)) {
			sess.send(new ClientConnectionOutput("Password appears to not break any rules."));
			this.password = password;

			try {
				player = Player_Character.createCharacter(this.name, this.password);
			} catch (CheckedHibernateException e) {
				CyberWorldMain.hibernateException(e);
				return;
			}

			if (player != null) {
				sess.send(new ClientConnectionOutput("Congratulations! Character created with:"));
				sess.send(new ClientConnectionOutput("name '" + player.getName() + "'"));
				sess.send(new ClientConnectionOutput("password '" + player.getPassword() + "'"));
				sess.send(new ClientConnectionOutput("Returning to main menu so you can log in."));
				sess.send(new ClientConnectionOutput());
			} else {
				sess.send(new ClientConnectionOutput(
						"Looks like something went unexpectedly wrong with character creation, returning to main menu."));

			}
			phaseManager.setPhase(new PhaseWelcome(sess, phaseManager));
		} else {
			sess.send(new ClientConnectionOutput("The name you sent violates rules, try again."));
			sendCreationPrompt();
			sendNamePrompt();
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
