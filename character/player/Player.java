package fireengine.character.player;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
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
import fireengine.character.command.action.general.Look;
import fireengine.character.condition.Condition;
import fireengine.character.condition.ConditionPlayer;
import fireengine.character.exception.CharacterExceptionNullRoom;
import fireengine.character.player.state.StatePlayer;
import fireengine.character.player.state.StatePlayerInWorld;
import fireengine.character.skillset.General;
import fireengine.character.skillset.Skillset;
import fireengine.client_io.ClientConnectionOutput;
import fireengine.gameworld.map.Direction;
import fireengine.gameworld.map.room.Room;
import fireengine.main.FireEngineMain;
import fireengine.session.Session;
import fireengine.util.CheckedHibernateException;
import fireengine.util.IDSequenceGenerator;
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

	@Transient
	private CharacterClass charClass;

	// TODO This doesnt look right.
	@OneToMany(fetch = FetchType.EAGER, orphanRemoval = true)
	@Cascade(CascadeType.ALL)
	@JoinColumn(name = "PLAYER_ID")
	private Set<Skillset> skillsetList;

	@Transient
	private StatePlayer playerState;

	/**
	 * Last room a player occupied to save for next login.
	 */
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "LAST_ROOM")
	private Room lastRoom;

	@OneToOne(fetch = FetchType.EAGER)
	@Cascade(CascadeType.ALL)
	@JoinColumn(name = "COND_PLAYER", nullable = false)
	@NotNull
	private ConditionPlayer condition;

	@Transient
	private Session session;

	@Transient
	private final List<Player> listenerList;

	// TODO Periodic check for attached session, if not, protect or remove Player.

	private Player() {
		charClass = new CharacterClass(true);
		listenerList = new ArrayList<Player>();
	}

	public Player(String name, String password, Room spawnRoom) {
		this();
		id = IDSequenceGenerator.getNextID("Player");
		settings = new PlayerSetting(true);
		condition = new ConditionPlayer(true);
		skillsetList = new TreeSet<Skillset>();
		refreshSkillsetList();
		this.name = name;
		this.password = password;
		lastRoom = spawnRoom;
	}

	@Override
	public int getId() {
		return id;
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
	public Set<Skillset> getSkillsetList() {
		return skillsetList;
	}

	@Override
	public void refreshSkillsetList() {
		addSkillsetIfMissing(General.class);
	}

	private <T extends Skillset> void addSkillsetIfMissing(Class<T> addSkillset) {
		for (Skillset foundSkillset : skillsetList) {
			if (addSkillset.isInstance(foundSkillset)) {
				return;
			}
		}
		Skillset newSkillset;
		try {
			newSkillset = addSkillset.getDeclaredConstructor(Boolean.class).newInstance(true);
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException | SecurityException e) {
			MyLogger.log(Level.SEVERE, "Player: Exception while trying to add missing Skillset to player skillsetList.",
					e);
			return;
		}
		skillsetList.add(newSkillset);
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
	public Boolean isInWorld() {
		if (playerState instanceof StatePlayerInWorld) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	public Room getRoom() {
		synchronized (this) {
			if (isInWorld()) {
				return ((StatePlayerInWorld) playerState).getRoom();
			} else {
				return null;
			}
		}
	}

	@Override
	public void setRoom(Room room) {
		synchronized (this) {
			if (room == null) {
				MyLogger.log(Level.SEVERE, "Player: Tried to setRoom with a null room.",
						new CharacterExceptionNullRoom("Player: Tried to setRoom with a null room."));
				return;
			}

			lastRoom = room;

			if (isInWorld()) {
				((StatePlayerInWorld) playerState).setRoom(room);
			}
		}
	}

	@Override
	public void acceptInput(String text) {
		ClientConnectionOutput actionOutput;
		synchronized (this) {
			text = StringUtils.cleanInput(text);
			actionOutput = playerState.acceptInput(text);
		}
		sendToListeners(actionOutput);
	}

	/**
	 * Debatable this is necessary. Might be required later. Can add in prompt lines
	 * etc here.
	 * <p>
	 * Returns output generated from an input command.
	 * </p>
	 */
	@Override
	protected void sendOutput(ClientConnectionOutput output) {
		if (session == null) {
			MyLogger.log(Level.FINE, "Player: Tried to send output to character '" + name + "' but session was null.");
			return;
		}

		output.newLine();
		// TODO String.format this
		output.addPart(
				String.format("%d/%dh, %d/%dm - ", getCurrentHealth(), getMaxHealth(), getCurrentMana(), getMaxMana()));
		output.newLine();

		session.send(output);
	}

	@Override
	public void sendToListeners(ClientConnectionOutput output) {
		sendOutput(new ClientConnectionOutput(output));

		if (listenerList.size() > 0) {
			ClientConnectionOutput listenerOutput = new ClientConnectionOutput(output);

//			listenerOutput.addPart(String.format("Sent to %s: ", this.getName()), true);

//			ClientConnectionOutput prependOutput = new ClientConnectionOutput(
//					String.format("Sent to %s: ", this.getName()));
//			prependOutput.newLine();
//			listenerOutput.addOutput(prependOutput, true);

			// Inserts new line at start of output and adds text to that new line
			listenerOutput.newLine(true);
			listenerOutput.addPart(String.format("Sent to %s: ", this.getName()), true);

			for (Player listener : listenerList) {
				if (listener.getListeners().contains(this)) {
					MyLogger.log(Level.WARNING, String.format(
							"Player: sendToListeners was going to send output to a Player that is listened to by this Player, causing a circular loop."
									+ " Aborting sending %s's output to %s.",
							this.getName(), listener.getName()));
				} else {
					listener.sendToListeners(new ClientConnectionOutput(listenerOutput));
				}
			}
		}
	}

	/**
	 * Adds another Player to receive output text sent to this player.
	 * 
	 * @param addPlayer other player to start eavesdropping
	 */
	public void addListener(Player addPlayer) {
		synchronized (listenerList) {
			listenerList.add(addPlayer);
		}
	}

	/**
	 * Removes another Player receiving output text sent to this player.
	 * 
	 * @param removePlayer other player to stop eavesdropping
	 */
	public void removeListener(Player removePlayer) {
		synchronized (listenerList) {
			listenerList.remove(removePlayer);
		}
	}

	public List<Player> getListeners() {
		return listenerList;
	}

//	/**
//	 * Used to connect a {@link Session} to the {@link Player}.
//	 *
//	 * @throws CharacterExceptionNullRoom
//	 */
//	public void connect(Session sess) throws CharacterExceptionNullRoom {
//		synchronized (room) {
//			connect(sess, this.room);
//		}
//	}

	/**
	 * Used to connect a {@link Session} to the {@link Player}, and entering the
	 * {@link Room} specified.
	 * 
	 * @param sess
	 * @param room
	 * @throws CharacterExceptionNullRoom
	 */
	public void connect(Session sess) throws CharacterExceptionNullRoom {
		if (this.session != null) {
			this.session.send(
					new ClientConnectionOutput("Disconnecting; another session has connected to this character."));
			this.session.disconnect();
		}

		setSession(sess);

		if (playerState instanceof StatePlayerInWorld) {
			getRoom().sendToRoomExcluding(
					new ClientConnectionOutput(String.format("%s's eyes light up and starts moving again.", getName())),
					this);
		} else {
			playerState = new StatePlayerInWorld(this, lastRoom);
			getRoom().sendToRoomExcluding(new ClientConnectionOutput(String.format(
					"%s arrives from without following a divine herald, who gives a curt nod and shoots off to some other task.",
					getName())), this);
		}

		sendToListeners(new Look().doAction(this, (Direction.DIRECTION) null));
	}

	/**
	 * Used to disconnect a {@link Session} from the {@link Player}.
	 */
	// TODO Timer to force logout.
	public void disconnect() {
		setSession(null);
		if (isInWorld()) {
			getRoom().sendToRoomExcluding(
					new ClientConnectionOutput(String.format("%s slows down and appears frozen in time.", getName())),
					this);
		}
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

	public static Player findCharacter(String name) {
		synchronized (playerList) {
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
				MyLogger.log(Level.WARNING, "Player: Hibernate error while trying to findCharacter.", e);
				return null;
			} finally {
				hibSess.close();
			}
		}
	}

	public static void addPlayerList(Player player) {
		synchronized (playerList) {
			for (Player listPlayer : playerList) {
				if (listPlayer.equals(player)) {
					return;
				}
			}
			playerList.add(player);
		}
	}

	public static void removePlayerList(Player player) {
		synchronized (playerList) {
			for (Player listPlayer : playerList) {
				if (listPlayer.equals(player)) {
					playerList.remove(listPlayer);
					return;
				}
			}
		}
	}

	public static ArrayList<Player> getPlayerList() {
		synchronized (playerList) {
			return playerList;
		}
	}

	public static void sendToAllPlayers(ClientConnectionOutput output) {
		synchronized (playerList) {
			for (Player player : playerList) {
				player.sendToListeners(output);
			}
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

	public static Player createCharacter(String name, String password, Room spawnRoom)
			throws CheckedHibernateException {
		name = StringUtils.capitalise(name);
		Player newPlayer = new Player(name, password, spawnRoom);

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

	public static void saveAllCharacters() {
		synchronized (playerList) {
			for (Player player : playerList) {
				try {
					saveCharacter(player);
				} catch (CheckedHibernateException e) {
					MyLogger.log(Level.SEVERE, String.format(
							"Player: Hibernate exception while trying to saveCharater on %s.", player.getName()), e);
				}
			}
		}
	}

	/**
	 * @return ClientConnectionOutput representing all players, or null if no players exist
	 * @throws CheckedHibernateException
	 */
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
//					info.addPart("ID: " + player.getId() + ", Name: '" + player.getName() + "', CharacterClass: '"
//							+ player.getCharClass().getClassName() + "', Level: " + player.getLevel() + ", Experience: "
//							+ player.getExperience() + "", null, null);
					info.addPart(String.format("ID: %s, Name: '%s', CharacterClass: '%s', Level: %s, Experience: %s",
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

	/**
	 * Custom implementation requires for proper JPA/Hibernate function.
	 * 
	 * <p>
	 * See relevant information or both hashCode and equals in
	 * {@link Room#hashCode()}
	 * </p>
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 7;
		result = (prime * result) + getId();
		return result;
	}

	/**
	 * Custom implementation requires for proper JPA/Hibernate function.
	 * <p>
	 * See relevant information or both hashCode and equals in
	 * {@link Room#hashCode()}
	 * </p>
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Player other = (Player) obj;
		if (getId() == other.getId())
			return true;
		return false;
	}
}
