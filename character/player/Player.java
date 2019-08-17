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
import javax.validation.constraints.NotNull;

import org.hibernate.HibernateException;
import org.hibernate.Transaction;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.query.Query;

import fireengine.character.Character;
import fireengine.character.character_class.CharacterClass;
import fireengine.character.command.CommandAction;
import fireengine.character.command.character_comand.general.Look;
import fireengine.character.condition.Condition;
import fireengine.character.condition.ConditionPlayer;
import fireengine.character.player.exception.PlayerExceptionNullRoom;
import fireengine.character.player.state.StatePlayer;
import fireengine.character.player.state.StatePlayerInWorld;
import fireengine.character.player.state.parser.InputParserInWorld;
import fireengine.client_io.ClientConnectionOutput;
import fireengine.gameworld.GameWorld;
import fireengine.gameworld.map.room.Room;
import fireengine.main.FireEngineMain;
import fireengine.session.Session;
import fireengine.util.CheckedHibernateException;
import fireengine.util.MyLogger;
import fireengine.util.StringUtils;

/*
 *    Copyright 2019 Ben Hook
 *    Player.java
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
@Table(name = "PLAYER")
public class Player extends Character {
	@Transient
	private static ArrayList<Player> playerList = new ArrayList<>();

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ID", nullable = false)
	@NotNull
	private int id;

	@Column(name = "NAME")
	@NotNull
	private String name; // 40 characters max

	@Column(name = "PASSWORD", nullable = false)
	@NotNull
	private String password; // 40 characters max

	@OneToOne(fetch = FetchType.EAGER)
	@Cascade(CascadeType.ALL)
	@JoinColumn(name = "PLAYER_SETTING", nullable = false)
	@NotNull
	private PlayerSetting settings;

	// TODO Mixing different class names in variable and column name below.
	@OneToOne(fetch = FetchType.EAGER)
	@Cascade(CascadeType.ALL)
	@JoinColumn(name = "CHAR_CLASS", nullable = false)
	@NotNull
	private CharacterClass charClass;

	@Transient
	private StatePlayer playerState;

	@OneToOne(fetch = FetchType.EAGER)
	@Cascade(CascadeType.ALL)
	@JoinColumn(name = "COND_PLAYER", nullable = false)
	@NotNull
	private ConditionPlayer condition;
	// TODO test cascade see if can remove saving sub classes individually.

	@Transient
	private Session session;
	// TODO Load room on character load.

	@Transient
	private volatile Room room;

	// TODO Periodic check for attached session, if not, protect or remove Player.

	private Player() {
		super();
	}

	public Player(String name, String password) {
		this();
		this.name = name;
		this.password = password;
		settings = new PlayerSetting();
		charClass = new CharacterClass();
		condition = new ConditionPlayer(1);
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

	public PlayerSetting getSettings() {
		return settings;
	}

	@SuppressWarnings("unused")
	private void setSettings(PlayerSetting settings) {
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
	public Condition getCondition() {
		return condition;
	}

	@Override
	public void setCondition(ConditionPlayer condition) {
		this.condition = condition;
	}

	public void setSession(Session session) {
		this.session = session;
	}

	public Session getSession() {
		return session;
	}

	@Override
	public Room getRoom() {
		return room;
	}

	@Override
	public void setRoom(Room room) throws PlayerExceptionNullRoom {
		if (room == null) {
			throw new PlayerExceptionNullRoom("Player: Player tried to be sent to null room.");
		}

		if (this.room != null) {
			this.room.removeCharacter(this);
		}

		this.room = room;
		this.room.addCharacter(this);
	}

	@Override
	public void acceptInput(String text) {
		CommandAction command = playerState.acceptInput(text);
		acceptInput(command);
	}

	@Override
	public void acceptInput(CommandAction command) {
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
					throw new PlayerExceptionNullRoom(
							"Player: Player tried to do action " + command.getClass() + " with no room.");
				}
			}
		} catch (PlayerExceptionNullRoom e) {
			ClientConnectionOutput message = new ClientConnectionOutput(2);
			message.addPart("You are trying to do an action without being in any room!", null, null);
			message.addPart("We will try and move you somewhere...", null, null);
			sendToListeners(message);
			MyLogger.log(Level.WARNING, "Player: PlayerExceptionNullRoom error when trying to acceptInput.", e);

			Room sendRoom = GameWorld.getMainMap().getSpawnRoomOrCentre();

			if (sendRoom != null) {
				try {
					setRoom(sendRoom);
				} catch (PlayerExceptionNullRoom e1) {
					MyLogger.log(Level.WARNING, "Player: Null error when trying to send null room player to origin.",
							e);
					return;
				}
			} else {
				MyLogger.log(Level.WARNING, "Player: Cannot find origin room to send null room player to.", e);
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
					"Player: Tried to send output to character '" + name + "' but no session was attached.");
		}
	}

	/**
	 * Used to connect a {@link Session} to the {@link Player}.
	 *
	 * @throws PlayerExceptionNullRoom
	 */
	public void connect(Session sess) throws PlayerExceptionNullRoom {
		connect(sess, this.room);
	}

	/**
	 * Used to connect a {@link Session} to the {@link Player}, and entering the
	 * {@link Room} specified.
	 *
	 * @throws PlayerExceptionNullRoom
	 */
	public void connect(Session sess, Room room) throws PlayerExceptionNullRoom {
		if (this.session != null) {
			this.session.send(
					new ClientConnectionOutput("Disconnecting; another session has connected to this character."));
			this.session.disconnect();
		}

		setSession(sess);

		try {
			setRoom(room);
		} catch (PlayerExceptionNullRoom e) {
			sess.send(new ClientConnectionOutput("Failed to enter world."));
			setSession(null);
			throw new PlayerExceptionNullRoom("Player: Player tried to do enter world with null room.", e);
		}

		if (playerState instanceof StatePlayerInWorld) {
			room.sendToRoom(new ClientConnectionOutput(getName() + " eyes light up and starts moving again."));
		} else {
			playerState = new StatePlayerInWorld();
		}

		acceptInput(new Look());
	}

	/**
	 * Used to disconnect a {@link Session} from the {@link Player}.
	 */
	public void disconnect() {
		setSession(null);
		Room room = getRoom();
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

	public static Player findCharacter(String name) throws CheckedHibernateException {
		name = StringUtils.capitalise(name);

		for (Player foundPlayer : playerList) {
			if (foundPlayer.getName().equals(name)) {
				return foundPlayer;
			}
		}

		org.hibernate.Session hibSess = FireEngineMain.hibSessFactory.openSession();
		Transaction tx = null;
		Player loadedPlayer = null;

		try {
			tx = hibSess.beginTransaction();

			Query<?> query = hibSess.createQuery("FROM Player WHERE NAME = :name");
			query.setParameter("name", name);

			List<?> players = query.list();
			tx.commit();

			if (players.isEmpty()) {
				return null;
			} else {
				if (players.size() > 1) {
					MyLogger.log(Level.WARNING, "Player: Multiple DB results for player name.");
				}
				loadedPlayer = (Player) players.get(0);
				addPlayerList(loadedPlayer);

				return loadedPlayer;
			}

		} catch (HibernateException e) {
			if (tx != null) {
				tx.rollback();
			}
			throw new CheckedHibernateException("Player: Hibernate error while trying to findCharacter.", e);
		} finally {
			hibSess.close();
		}
	}

	public static void addPlayerList(Player player) {
		for (Player listPlayer : playerList) {
			if (listPlayer == player) {
				return;
			}
		}
		playerList.add(player);
	}

	public static void removePlayerList(Player player) {
		for (Player listPlayer : playerList) {
			if (listPlayer == player) {
				playerList.remove(listPlayer);
				return;
			}
		}
	}

	public static ArrayList<Player> getPlayerList() {
		return playerList;
	}

	public static void sendToAllPlayers(ClientConnectionOutput output) {
		for (Player player : playerList) {
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

	public static Player createCharacter(String name, String password) throws CheckedHibernateException {
		name = StringUtils.capitalise(name);
		Player newPlayer = new Player(name, password);

		saveCharacter(newPlayer);
		return Player.findCharacter(name);
	}

	public static void saveCharacter(Player player) throws CheckedHibernateException {
		org.hibernate.Session hibSess = null;
		Transaction tx = null;

		try {
			hibSess = FireEngineMain.hibSessFactory.openSession();
			tx = hibSess.beginTransaction();

			hibSess.saveOrUpdate(player);

			tx.commit();
		} catch (HibernateException e) {
			if (tx != null) {
				tx.rollback();
			}
			throw new CheckedHibernateException("Player: Hibernate error while trying to saveCharacter.", e);
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

			Query<?> query = hibSess.createQuery("FROM Player");

			List<?> players = query.list();
			tx.commit();

			if (!players.isEmpty()) {
				info = new ClientConnectionOutput();

				for (Iterator<?> iterator = players.iterator(); iterator.hasNext();) {
					Player player = (Player) iterator.next();
//					info.addPart("ID: " + player.getId() + ", Name: '" + player.getName() + "', Class: '"
//							+ player.getCharClass().getClassName() + "', Level: " + player.getLevel() + ", Experience: "
//							+ player.getExperience() + "", null, null);
					info.addPart(String.format("ID: %s, Name: '%s', Class: '%s', Level: %s, Experience: %s",
							player.getId(), player.getName(), player.getCharClass().getClassName(), player.getLevel(),
							player.getExperience()), null, null);
					if (iterator.hasNext()) {
						info.newLine();
					}
				}
			}

		} catch (HibernateException e) {
			if (tx != null) {
				tx.rollback();
			}
			throw new CheckedHibernateException("Player: Hibernate error while trying to getAllPlayerInfo.", e);
		} finally {
			hibSess.close();
		}

		return info;
	}
}
