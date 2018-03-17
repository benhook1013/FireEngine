package fireengine.characters.player;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.HibernateException;
import org.hibernate.Transaction;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.query.Query;

import fireengine.characters.Base_Character;
import fireengine.characters.character_class.Character_Class;
import fireengine.characters.commands.Action_Command;
import fireengine.characters.commands.character_commands.general.Look;
import fireengine.characters.condition.Base_Condition;
import fireengine.characters.condition.PC_Condition;
import fireengine.characters.player.exceptions.PC_Exception_Null_Room;
import fireengine.characters.player.state.In_World;
import fireengine.characters.player.state.PC_State_Interface;
import fireengine.characters.player.state.parser.In_World_Parser;
import fireengine.client_io.ClientConnectionOutput;
import fireengine.gameworld.Gameworld;
import fireengine.gameworld.maps.BaseRoom;
import fireengine.gameworld.maps.Exceptions.Map_Exception_Out_Of_Bounds;
import fireengine.main.FireEngineMain;
import fireengine.session.Session;
import fireengine.utils.CheckedHibernateException;
import fireengine.utils.MyLogger;
import fireengine.utils.StringUtils;

/*
 *    Copyright 2017 Ben Hook
 *    Player_Character.java
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

@Entity
@Table(name = "PLAYER_CHARACTER")
public class Player_Character extends Base_Character {
	@Transient
	private static ArrayList<Player_Character> playerList = new ArrayList<>();

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "PC_ID")
	private int id;
	@Column(name = "PC_NAME")
	private String name; // 40 characters max
	@Column(name = "PC_PASSWORD")
	private String password; // 40 characters max
	@OneToOne(fetch = FetchType.EAGER)
	@Cascade(CascadeType.ALL)
	@JoinColumn(name = "PC_PC_SETTINGS_ID")
	private PC_Settings settings;

	@OneToOne(fetch = FetchType.EAGER)
	@Cascade(CascadeType.ALL)
	@JoinColumn(name = "PC_CHAR_CLASS_ID")
	private Character_Class charClass;

	@Transient
	private PC_State_Interface pcState;
	@OneToOne(fetch = FetchType.EAGER)
	@Cascade(CascadeType.ALL)
	@JoinColumn(name = "PC_PC_COND_ID")
	private PC_Condition condition;
	// TODO test cascade see if can remove saving sub classes individually.

	@Transient
	private Session session;
	// TODO Load room on character load.
	@Transient
	private volatile BaseRoom room;

	// TODO Periodic check for attached session, if not, protect or remove PC.

	private Player_Character() {
		super();
	}

	public Player_Character(String name, String password) {
		this();
		this.name = name;
		this.password = password;
		settings = new PC_Settings();
		charClass = new Character_Class();
		condition = new PC_Condition(1);
	}

	@Override
	public int getId() {
		return id;
	}

	@Override
	protected void setId(int id) {
		this.id = id;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public PC_Settings getSettings() {
		return settings;
	}

	@SuppressWarnings("unused")
	private void setSettings(PC_Settings settings) {
		this.settings = settings;
	}

	@Override
	public Character_Class getCharClass() {
		return this.charClass;
	}

	@Override
	public void setCharClass(Character_Class charClass) {
		this.charClass = charClass;
	}

	@Override
	public Base_Condition getCondition() {
		return condition;
	}

	@Override
	public void setCondition(PC_Condition condition) {
		this.condition = condition;
	}

	public void setSession(Session session) {
		this.session = session;
	}

	public Session getSession() {
		return session;
	}

	@Override
	public BaseRoom getRoom() {
		return room;
	}

	@Override
	public void setRoom(BaseRoom room) throws PC_Exception_Null_Room {
		if (room == null) {
			throw new PC_Exception_Null_Room("Player_Character: Player tried to be sent to null room.");
		}

		if (this.room != null) {
			this.room.removeCharacter(this);
		}

		this.room = room;
		this.room.addCharacter(this);
	}

	@Override
	public void acceptInput(String text) {
		Action_Command command = pcState.acceptInput(text);
		acceptInput(command);
	}

	@Override
	public void acceptInput(Action_Command command) {
		try {
			if (command == null) {
				ClientConnectionOutput output = new ClientConnectionOutput(2);
				output.addPart(In_World_Parser.getUnkownCommandText(), null, null);
				output.newLine();
				sendToListeners(output);
			} else {
				if (room != null)
					command.doAction(this);
				else {
					throw new PC_Exception_Null_Room(
							"Player_Character: Player tried to do action " + command.getClass() + " with no room.");
				}
			}
		} catch (PC_Exception_Null_Room e) {
			ClientConnectionOutput message = new ClientConnectionOutput(2);
			message.addPart("You are trying to do an action without being in any room!", null, null);
			message.addPart("We will try and move you somewhere...", null, null);
			sendToListeners(message);
			MyLogger.log(Level.WARNING, "Player_Character: PC_Exception_Null_Room error when trying to acceptInput.",
					e);

			BaseRoom sendRoom = null;
			try {
				sendRoom = Gameworld.findMap(1).getRoom(1, 1);
			} catch (Map_Exception_Out_Of_Bounds e1) {
				MyLogger.log(Level.WARNING,
						"Player_Character: Origin room out of bounds while trying to find origin room to send null room player to.",
						e);
				return;
			}

			if (sendRoom != null) {
				try {
					setRoom(sendRoom);
				} catch (PC_Exception_Null_Room e1) {
					MyLogger.log(Level.WARNING,
							"Player_Character: Null error when trying to send null room player to origin.", e);
					return;
				}
			} else {
				MyLogger.log(Level.WARNING, "Player_Character: Cannot find origin room to send null room player to.",
						e);
				return;
			}
		}
	}

	@Override
	public void sendToListeners(ClientConnectionOutput output) {
		if (session != null) {
			output.newLine();
			output.addPart(
					getCurrentHealth() + "/" + getMaxHealth() + "h, " + getCurrentMana() + "/" + getMaxMana() + "m - ",
					null, null);
			output.newLine();
			session.send(output);
		} else {
			MyLogger.log(Level.FINE,
					"Player_Character: Tried to send output to character '" + name + "' but no session was attached.");
		}
	}

	/**
	 * Used to connect a {@link Session} to the {@link Player_Character}.
	 * 
	 * @throws PC_Exception_Null_Room
	 */
	public void connect(Session sess) throws PC_Exception_Null_Room {
		connect(sess, this.room);
	}

	/**
	 * Used to connect a {@link Session} to the {@link Player_Character}, and
	 * entering the {@link BaseRoom} specified.
	 * 
	 * @throws PC_Exception_Null_Room
	 */
	public void connect(Session sess, BaseRoom room) throws PC_Exception_Null_Room {
		if (this.session != null) {
			this.session.send(
					new ClientConnectionOutput("Disconnecting; another session has connected to this character."));
			this.session.disconnect();
		}

		setSession(sess);

		try {
			setRoom(room);
		} catch (PC_Exception_Null_Room e) {
			sess.send(new ClientConnectionOutput("Failed to enter world."));
			setSession(null);
			throw new PC_Exception_Null_Room("Player_Character: Player tried to do enter world with null room.", e);
		}

		if (pcState instanceof In_World) {
			room.sendToRoom(new ClientConnectionOutput(getName() + " eyes light up and starts moving again."));
		} else {
			pcState = new In_World();
		}

		acceptInput(new Look());
	}

	/**
	 * Used to disconnect a {@link Session} from the {@link Player_Character}.
	 */
	public void disconnect() {
		setSession(null);
		BaseRoom room = getRoom();
		if (room != null) {
			room.sendToRoom(new ClientConnectionOutput(getName() + " slows down and appears frozen."));
		}
		// TODO Timer to force logout.
	}

	@Override
	public int getLevel() {
		return condition.getLevelNumber();
	}

	@Override
	public void setLevel(int level) {
		condition.setLevelNumber(level);
	}

	public int getExperience() {
		return condition.getExperience();
	}

	public void setExperience(int experience) {
		condition.setExperience(experience);
	}

	@Override
	public int getCurrentHealth() {
		return condition.getCurrentHealth();
	}

	@Override
	public int getMaxHealth() {
		return condition.getMaxHealth();
	}

	@Override
	public int getCurrentMana() {
		return condition.getCurrentMana();
	}

	@Override
	public int getMaxMana() {
		return condition.getMaxMana();
	}

	public static Player_Character findCharacter(String name) throws CheckedHibernateException {
		name = StringUtils.capitalise(name);

		for (Player_Character listPlayer : playerList) {
			if (listPlayer.getName().equals(name)) {
				return listPlayer;
			}
		}

		org.hibernate.Session hibSess = FireEngineMain.hibSessFactory.openSession();
		Transaction tx = null;
		Player_Character player = null;

		try {
			tx = hibSess.beginTransaction();

			Query<?> query = hibSess.createQuery("FROM Player_Character WHERE PC_NAME = :name");
			query.setParameter("name", name);

			List<?> players = query.list();
			tx.commit();

			if (players.isEmpty()) {
				return null;
			} else {
				if (players.size() > 1) {
					MyLogger.log(Level.WARNING, "Player_Character: Multiple DB results for player name.");
				}
				player = (Player_Character) players.get(0);
				addPlayerList(player);

				return player;
			}

		} catch (HibernateException e) {
			if (tx != null) {
				tx.rollback();
			}
			throw new CheckedHibernateException("Player_Character: Hibernate error while trying to findCharacter.", e);
		} finally {
			hibSess.close();
		}
	}

	public static void addPlayerList(Player_Character player) {
		for (Player_Character listPlayer : playerList) {
			if (listPlayer == player) {
				return;
			}
		}
		playerList.add(player);
	}

	public static void removePlayerList(Player_Character player) {
		for (Player_Character listPlayer : playerList) {
			if (listPlayer == player) {
				playerList.remove(listPlayer);
				return;
			}
		}
	}

	public static ArrayList<Player_Character> getPlayerList() {
		return playerList;
	}

	public static void sendToAllPlayers(ClientConnectionOutput output) {
		for (Player_Character player : playerList) {
			player.sendToListeners(output);
		}
	}

	// Return true if is OK
	public static boolean testNameRules(String name) {
		return true;
	}

	// Return true if is OK
	public static boolean testPasswordRules(String password) {
		return true;
	}

	public static Player_Character createCharacter(String name, String password) throws CheckedHibernateException {
		name = StringUtils.capitalise(name);
		Player_Character newPlayer = new Player_Character(name, password);

		saveCharacter(newPlayer);
		return Player_Character.findCharacter(name);
	}

	public static void saveCharacter(Player_Character pc) throws CheckedHibernateException {
		org.hibernate.Session hibSess = null;
		Transaction tx = null;

		try {
			hibSess = FireEngineMain.hibSessFactory.openSession();
			tx = hibSess.beginTransaction();

			// hibSess.saveOrUpdate(pc.getCondition().getLevel());
			// hibSess.saveOrUpdate(pc.getCondition().getHealth());
			// hibSess.saveOrUpdate(pc.getCondition().getMana());
			// hibSess.saveOrUpdate(pc.getCondition());
			hibSess.saveOrUpdate(pc);

			tx.commit();
		} catch (HibernateException e) {
			if (tx != null) {
				tx.rollback();
			}
			throw new CheckedHibernateException("Player_Character: Hibernate error while trying to saveCharacter.", e);
		} finally {
			if (hibSess != null) {
				hibSess.close();
			}
		}
	}

	public static ClientConnectionOutput getAllPlayerInfo() throws CheckedHibernateException {
		org.hibernate.Session hibSess = null;
		Transaction tx = null;

		ClientConnectionOutput info = null;

		try {
			hibSess = FireEngineMain.hibSessFactory.openSession();
			tx = hibSess.beginTransaction();

			Query<?> query = hibSess.createQuery("FROM Player_Character");

			List<?> players = query.list();
			tx.commit();

			if (!players.isEmpty()) {
				info = new ClientConnectionOutput();

				for (Iterator<?> iterator = players.iterator(); iterator.hasNext();) {
					Player_Character player = (Player_Character) iterator.next();
					info.addPart(
							"ID: " + player.getId() + ", Name: '" + player.getName() + "', Password: '"
									+ player.getPassword() + "', Class: '" + player.getCharClass() + "', Level: '"
									+ player.getLevel() + "', Experience: '" + player.getExperience() + "', Health: '"
									+ player.getCurrentHealth() + "', Mana: '" + player.getCurrentMana() + "'",
							null, null);
					if (iterator.hasNext()) {
						info.newLine();
					}
				}
			}

		} catch (HibernateException e) {
			if (tx != null) {
				tx.rollback();
			}
			throw new CheckedHibernateException("Player_Character: Hibernate error while trying to getAllPlayerInfo.",
					e);
		} finally {
			hibSess.close();
		}

		return info;
	}
}
