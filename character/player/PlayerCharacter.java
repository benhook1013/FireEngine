package fireengine.character.player;

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

import fireengine.character.BaseCharacter;
import fireengine.character.character_class.CharacterClass;
import fireengine.character.command.ActionCommand;
import fireengine.character.command.character_comand.general.Look;
import fireengine.character.condition.BaseCondition;
import fireengine.character.condition.PCCondition;
import fireengine.character.player.exception.PCExceptionNullRoom;
import fireengine.character.player.state.PCState;
import fireengine.character.player.state.PCStateInWorld;
import fireengine.character.player.state.parser.InputParserInWorld;
import fireengine.client_io.ClientConnectionOutput;
import fireengine.gameworld.Gameworld;
import fireengine.gameworld.map.BaseRoom;
import fireengine.gameworld.map.exception.MapExceptionOutOfBounds;
import fireengine.main.FireEngineMain;
import fireengine.session.Session;
import fireengine.util.CheckedHibernateException;
import fireengine.util.MyLogger;
import fireengine.util.StringUtils;

/*
 *    Copyright 2017 Ben Hook
 *    PlayerCharacter.java
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
public class PlayerCharacter extends BaseCharacter {
	@Transient
	private static ArrayList<PlayerCharacter> playerList = new ArrayList<>();

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
	private PCSetting settings;

	@OneToOne(fetch = FetchType.EAGER)
	@Cascade(CascadeType.ALL)
	@JoinColumn(name = "PC_CHAR_CLASS_ID")
	private CharacterClass charClass;

	@Transient
	private PCState pcState;
	@OneToOne(fetch = FetchType.EAGER)
	@Cascade(CascadeType.ALL)
	@JoinColumn(name = "PC_PC_COND_ID")
	private PCCondition condition;
	// TODO test cascade see if can remove saving sub classes individually.

	@Transient
	private Session session;
	// TODO Load room on character load.
	@Transient
	private volatile BaseRoom room;

	// TODO Periodic check for attached session, if not, protect or remove PC.

	private PlayerCharacter() {
		super();
	}

	public PlayerCharacter(String name, String password) {
		this();
		this.name = name;
		this.password = password;
		settings = new PCSetting();
		charClass = new CharacterClass();
		condition = new PCCondition(1);
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

	public PCSetting getSettings() {
		return settings;
	}

	@SuppressWarnings("unused")
	private void setSettings(PCSetting settings) {
		this.settings = settings;
	}

	@Override
	public CharacterClass getCharClass() {
		return this.charClass;
	}

	@Override
	public void setCharClass(CharacterClass charClass) {
		this.charClass = charClass;
	}

	@Override
	public BaseCondition getCondition() {
		return condition;
	}

	@Override
	public void setCondition(PCCondition condition) {
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
	public void setRoom(BaseRoom room) throws PCExceptionNullRoom {
		if (room == null) {
			throw new PCExceptionNullRoom("PlayerCharacter: Player tried to be sent to null room.");
		}

		if (this.room != null) {
			this.room.removeCharacter(this);
		}

		this.room = room;
		this.room.addCharacter(this);
	}

	@Override
	public void acceptInput(String text) {
		ActionCommand command = pcState.acceptInput(text);
		acceptInput(command);
	}

	@Override
	public void acceptInput(ActionCommand command) {
		try {
			if (command == null) {
				ClientConnectionOutput output = new ClientConnectionOutput(2);
				output.addPart(InputParserInWorld.getUnkownCommandText(), null, null);
				output.newLine();
				sendToListeners(output);
			} else {
				if (room != null) {
					command.doAction(this);
				} else {
					throw new PCExceptionNullRoom(
							"PlayerCharacter: Player tried to do action " + command.getClass() + " with no room.");
				}
			}
		} catch (PCExceptionNullRoom e) {
			ClientConnectionOutput message = new ClientConnectionOutput(2);
			message.addPart("You are trying to do an action without being in any room!", null, null);
			message.addPart("We will try and move you somewhere...", null, null);
			sendToListeners(message);
			MyLogger.log(Level.WARNING, "PlayerCharacter: PCExceptionNullRoom error when trying to acceptInput.", e);

			BaseRoom sendRoom = null;
			try {
				sendRoom = Gameworld.findMap(1).getRoom(1, 1);
			} catch (MapExceptionOutOfBounds e1) {
				MyLogger.log(Level.WARNING,
						"PlayerCharacter: Origin room out of bounds while trying to find origin room to send null room player to.",
						e);
				return;
			}

			if (sendRoom != null) {
				try {
					setRoom(sendRoom);
				} catch (PCExceptionNullRoom e1) {
					MyLogger.log(Level.WARNING,
							"PlayerCharacter: Null error when trying to send null room player to origin.", e);
					return;
				}
			} else {
				MyLogger.log(Level.WARNING, "PlayerCharacter: Cannot find origin room to send null room player to.", e);
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
					"PlayerCharacter: Tried to send output to character '" + name + "' but no session was attached.");
		}
	}

	/**
	 * Used to connect a {@link Session} to the {@link PlayerCharacter}.
	 *
	 * @throws PCExceptionNullRoom
	 */
	public void connect(Session sess) throws PCExceptionNullRoom {
		connect(sess, this.room);
	}

	/**
	 * Used to connect a {@link Session} to the {@link PlayerCharacter}, and
	 * entering the {@link BaseRoom} specified.
	 *
	 * @throws PCExceptionNullRoom
	 */
	public void connect(Session sess, BaseRoom room) throws PCExceptionNullRoom {
		if (this.session != null) {
			this.session.send(
					new ClientConnectionOutput("Disconnecting; another session has connected to this character."));
			this.session.disconnect();
		}

		setSession(sess);

		try {
			setRoom(room);
		} catch (PCExceptionNullRoom e) {
			sess.send(new ClientConnectionOutput("Failed to enter world."));
			setSession(null);
			throw new PCExceptionNullRoom("PlayerCharacter: Player tried to do enter world with null room.", e);
		}

		if (pcState instanceof PCStateInWorld) {
			room.sendToRoom(new ClientConnectionOutput(getName() + " eyes light up and starts moving again."));
		} else {
			pcState = new PCStateInWorld();
		}

		acceptInput(new Look());
	}

	/**
	 * Used to disconnect a {@link Session} from the {@link PlayerCharacter}.
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

	public static PlayerCharacter findCharacter(String name) throws CheckedHibernateException {
		name = StringUtils.capitalise(name);

		for (PlayerCharacter listPlayer : playerList) {
			if (listPlayer.getName().equals(name)) {
				return listPlayer;
			}
		}

		org.hibernate.Session hibSess = FireEngineMain.hibSessFactory.openSession();
		Transaction tx = null;
		PlayerCharacter player = null;

		try {
			tx = hibSess.beginTransaction();

			Query<?> query = hibSess.createQuery("FROM PlayerCharacter WHERE PC_NAME = :name");
			query.setParameter("name", name);

			List<?> players = query.list();
			tx.commit();

			if (players.isEmpty()) {
				return null;
			} else {
				if (players.size() > 1) {
					MyLogger.log(Level.WARNING, "PlayerCharacter: Multiple DB results for player name.");
				}
				player = (PlayerCharacter) players.get(0);
				addPlayerList(player);

				return player;
			}

		} catch (HibernateException e) {
			if (tx != null) {
				tx.rollback();
			}
			throw new CheckedHibernateException("PlayerCharacter: Hibernate error while trying to findCharacter.", e);
		} finally {
			hibSess.close();
		}
	}

	public static void addPlayerList(PlayerCharacter player) {
		for (PlayerCharacter listPlayer : playerList) {
			if (listPlayer == player) {
				return;
			}
		}
		playerList.add(player);
	}

	public static void removePlayerList(PlayerCharacter player) {
		for (PlayerCharacter listPlayer : playerList) {
			if (listPlayer == player) {
				playerList.remove(listPlayer);
				return;
			}
		}
	}

	public static ArrayList<PlayerCharacter> getPlayerList() {
		return playerList;
	}

	public static void sendToAllPlayers(ClientConnectionOutput output) {
		for (PlayerCharacter player : playerList) {
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

	public static PlayerCharacter createCharacter(String name, String password) throws CheckedHibernateException {
		name = StringUtils.capitalise(name);
		PlayerCharacter newPlayer = new PlayerCharacter(name, password);

		saveCharacter(newPlayer);
		return PlayerCharacter.findCharacter(name);
	}

	public static void saveCharacter(PlayerCharacter pc) throws CheckedHibernateException {
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
			throw new CheckedHibernateException("PlayerCharacter: Hibernate error while trying to saveCharacter.", e);
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

			Query<?> query = hibSess.createQuery("FROM PlayerCharacter");

			List<?> players = query.list();
			tx.commit();

			if (!players.isEmpty()) {
				info = new ClientConnectionOutput();

				for (Iterator<?> iterator = players.iterator(); iterator.hasNext();) {
					PlayerCharacter player = (PlayerCharacter) iterator.next();
					info.addPart("ID: " + player.getId() + ", Name: '" + player.getName() + "', Class: '"
							+ player.getCharClass().getClassName() + "', Level: " + player.getLevel() + ", Experience: "
							+ player.getExperience() + "", null, null);
					if (iterator.hasNext()) {
						info.newLine();
					}
				}
			}

		} catch (HibernateException e) {
			if (tx != null) {
				tx.rollback();
			}
			throw new CheckedHibernateException("PlayerCharacter: Hibernate error while trying to getAllPlayerInfo.",
					e);
		} finally {
			hibSess.close();
		}

		return info;
	}
}
