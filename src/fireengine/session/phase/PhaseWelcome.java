package fireengine.session.phase;

import fireengine.characters.commands.menu_commands.*;
import fireengine.characters.player.Player_Character;
import fireengine.characters.player.state.parser.Menu_Parser;
import fireengine.client_io.ClientConnectionOutput;
import fireengine.client_io.Client_IO_Colour;
import fireengine.main.FireEngineMain;
import fireengine.session.Session;
import fireengine.utils.CheckedHibernateException;
import fireengine.utils.StringUtils;

/*
 *    Copyright 2017 Ben Hook
 *    PhaseWelcome.java
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
public class PhaseWelcome implements PhaseInterface {
	private Session sess;
	private PhaseManager phaseManager;
	private Menu_Parser parser = Menu_Parser.getInstance();

	/**
	 * Private no-arg constructor to avoid accidental instantiation.
	 */
	private PhaseWelcome() {
	}

	public PhaseWelcome(Session sess, PhaseManager phaseManager) {
		this();
		this.sess = sess;
		this.phaseManager = phaseManager;

		sendGreeting();
		menuDisplay();
	}

	private void sendGreeting() {
		this.sess.send(new ClientConnectionOutput("Welcome to " + FireEngineMain.serverName + "!",
				Client_IO_Colour.COLOURS.BRIGHTCYAN, null));
	}

	@Override
	public void acceptInput(String input) {
		Menu_Command command = parser.parse(input);
		menuAction(command);
	}

	private void menuDisplay() {
		ClientConnectionOutput menuMsg = new ClientConnectionOutput(3);
		menuMsg.addPart("Please select from the following options(enter number):", null, null);
		menuMsg.newLine();
		menuMsg.addPart("1    Proceed to login.", null, null);
		menuMsg.newLine();
		menuMsg.addPart("2    Proceed to character creation.", null, null);
		menuMsg.newLine();
		menuMsg.addPart("3    Display all colours.", null, null);
		menuMsg.newLine();
		menuMsg.addPart("4    Display this menu.", null, null);
		menuMsg.newLine();
		menuMsg.addPart("5    Display all player info.", null, null);
		menuMsg.newLine();
		menuMsg.addPart("6    String tests.", null, null);
		menuMsg.newLine();
		sess.send(menuMsg);
	}

	private void menuAction(Menu_Command command) {
		if (command != null) {
			System.out.println("Command: " + command.getClass().getName());
			if (command instanceof Command_1) {
				sess.send(new ClientConnectionOutput("You have chosen to Login."));
				sess.send(new ClientConnectionOutput());
				phaseManager.setPhase(new PhaseLogin(sess, phaseManager));
			} else if (command instanceof Command_2) {
				sess.send(new ClientConnectionOutput("You have chosen to Create Character."));
				sess.send(new ClientConnectionOutput());
				phaseManager.setPhase(new PhaseCreation(sess, phaseManager));
			} else if (command instanceof Command_3) {
				sess.send(Client_IO_Colour.showColours());
				sess.send(new ClientConnectionOutput());
				menuDisplay();
			} else if (command instanceof Command_4) {
				sess.send(new ClientConnectionOutput("You have chosen to display the Menu again."));
				sess.send(new ClientConnectionOutput());
				menuDisplay();
			} else if (command instanceof Command_5) {
				sess.send(new ClientConnectionOutput("You have chosen to display all Player Info."));

				ClientConnectionOutput playerInfo = null;
				try {
					playerInfo = Player_Character.getAllPlayerInfo();
				} catch (CheckedHibernateException e) {
					FireEngineMain.hibernateException(e);
					return;
				}
				if (playerInfo != null) {
					try {
						sess.send(Player_Character.getAllPlayerInfo());
					} catch (CheckedHibernateException e) {
						System.out.println("ERROR!");
						FireEngineMain.hibernateException(e);
						return;
					}
					sess.send(new ClientConnectionOutput("End of player information."));
				} else {
					sess.send(new ClientConnectionOutput("No player information found!"));
				}
				sess.send(new ClientConnectionOutput());
				menuDisplay();
			} else if (command instanceof Command_6) {
				sess.send(new ClientConnectionOutput("String tests:"));
				String input1 = "word";
				sess.send(new ClientConnectionOutput("Input1: '" + input1 + "'"));
				sess.send(new ClientConnectionOutput("capatalise output: '" + StringUtils.capitalise(input1) + "'"));
				sess.send(new ClientConnectionOutput("sentence output: '" + StringUtils.sentance(input1) + "'"));
				sess.send(new ClientConnectionOutput());
				String input2 = "Word";
				sess.send(new ClientConnectionOutput("Input2: '" + input2 + "'"));
				sess.send(new ClientConnectionOutput("capatalise output: '" + StringUtils.capitalise(input2) + "'"));
				sess.send(new ClientConnectionOutput("sentence output: '" + StringUtils.sentance(input2) + "'"));
				sess.send(new ClientConnectionOutput());
				String input3 = "some sentance blah blah";
				sess.send(new ClientConnectionOutput("Input3: '" + input3 + "'"));
				sess.send(new ClientConnectionOutput("capatalise output: '" + StringUtils.capitalise(input3) + "'"));
				sess.send(new ClientConnectionOutput("sentence output: '" + StringUtils.sentance(input3) + "'"));
				sess.send(new ClientConnectionOutput());
				String input4 = "Some sentance blah blah.";
				sess.send(new ClientConnectionOutput("Input4: '" + input4 + "'"));
				sess.send(new ClientConnectionOutput("capatalise output: '" + StringUtils.capitalise(input4) + "'"));
				sess.send(new ClientConnectionOutput("sentence output: '" + StringUtils.sentance(input4) + "'"));
				sess.send(new ClientConnectionOutput());
			}
		} else {
			System.out.println("Command: null");
			badCommand();
		}
	}

	private void badCommand() {
		// ClientConnectionOutput output = new ClientConnectionOutput();
		// output.addPart("That is not an acceptable option.", null, null);
		sess.send(new ClientConnectionOutput("That is not an acceptable option."));
		sess.send(new ClientConnectionOutput());
	}

	@Override
	public void disconnect() {
		close();
	}

	@Override
	public void close() {
	}
}
