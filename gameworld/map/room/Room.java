package fireengine.gameworld.map.room;

import java.util.ArrayList;
import java.util.logging.Level;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;

import org.hibernate.HibernateException;
import org.hibernate.Transaction;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import fireengine.character.Character;
import fireengine.character.player.Player;
import fireengine.client_io.ClientConnectionOutput;
import fireengine.gameworld.map.GameMap;
import fireengine.gameworld.map.Coordinate;
import fireengine.gameworld.map.Direction;
import fireengine.gameworld.map.Direction.DIRECTION;
import fireengine.gameworld.map.exception.MapExceptionDirectionNotSupported;
import fireengine.gameworld.map.exception.MapExceptionRoomNull;
import fireengine.gameworld.map.exit.RoomExit;
import fireengine.main.FireEngineMain;
import fireengine.util.CheckedHibernateException;
import fireengine.util.IDSequenceGenerator;
import fireengine.util.MyLogger;

/*
 *    Copyright 2019 Ben Hook
 *    Room.java
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
 * Default room type, with standard room functions.
 *
 * @author Ben Hook
 */
@Entity
@Table(name = "ROOM")
public class Room {
//	/**
//	 * TODO Might not need, only used to in saveRooms. NOT CURRENTLY USED. TODO Remove
//	 */
//	@Transient
//	private static ArrayList<Room> roomList = new ArrayList<>();

	@Id
	@Column(name = "ID", nullable = false)
	@NotNull
	private int id;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "MAP", nullable = false)
	@NotNull
	private GameMap map;

	@Column(name = "NAME")
	@NotNull
	private String name;

	@Column(name = "DESCRIPTION")
	@NotNull
	private String description;

	// TODO Should probably store the below directional exits into a direction keyed
	// map.
	@OneToOne(fetch = FetchType.EAGER)
	@Cascade(CascadeType.ALL)
	@JoinColumn(name = "EXIT_U")
	private RoomExit upExit;

	@OneToOne(fetch = FetchType.EAGER)
	@Cascade(CascadeType.ALL)
	@JoinColumn(name = "EXIT_D")
	private RoomExit downExit;

	@OneToOne(fetch = FetchType.EAGER)
	@Cascade(CascadeType.ALL)
	@JoinColumn(name = "EXIT_N")
	private RoomExit northExit;

	@OneToOne(fetch = FetchType.EAGER)
	@Cascade(CascadeType.ALL)
	@JoinColumn(name = "EXIT_NE")
	private RoomExit northEastExit;

	@OneToOne(fetch = FetchType.EAGER)
	@Cascade(CascadeType.ALL)
	@JoinColumn(name = "EXIT_E")
	private RoomExit eastExit;

	@OneToOne(fetch = FetchType.EAGER)
	@Cascade(CascadeType.ALL)
	@JoinColumn(name = "EXIT_SE")
	private RoomExit southEastExit;

	@OneToOne(fetch = FetchType.EAGER)
	@Cascade(CascadeType.ALL)
	@JoinColumn(name = "EXIT_S")
	private RoomExit southExit;

	@OneToOne(fetch = FetchType.EAGER)
	@Cascade(CascadeType.ALL)
	@JoinColumn(name = "EXIT_SW")
	private RoomExit southWestExit;

	@OneToOne(fetch = FetchType.EAGER)
	@Cascade(CascadeType.ALL)
	@JoinColumn(name = "EXIT_W")
	private RoomExit westExit;

	@OneToOne(fetch = FetchType.EAGER)
	@Cascade(CascadeType.ALL)
	@JoinColumn(name = "EXIT_NW")
	private RoomExit northWestExit;

	@Transient
	private ArrayList<Player> playerList;

	@SuppressWarnings("unused")
	private Room() {
		playerList = new ArrayList<>();
	}

	public Room(GameMap map) {
		this();
		id = IDSequenceGenerator.getNextID("Room");
		this.map = map;
	}

	public int getId() {
		return id;
	}

	public GameMap getMap() {
		return this.map;
	}

	@SuppressWarnings("unused")
	private void setMap(GameMap map) {
		this.map = map;
	}

	/**
	 * @return the Room's Coordinate
	 */
	public Coordinate getCoord() {
		return map.getCoord(this);
	}

	public String getName() {
		if (name == null) {
			return "Null name";
		} else {
			return name;
		}
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		if (description == null) {
			return "Null description.";
		} else {
			return description;
		}
	}

	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * Adds a {@link Character} to the {@link Room}'s player list (typically on room
	 * enter), so far only used to add {@link Player}s. Always use AFTER setting
	 * room on Character, as this will check to make sure both match(check will
	 * result in hidden-to-player error).
	 *
	 * @param player
	 */
	public void addCharacter(Character player) {
		if (player.getRoom() != this) {
			MyLogger.log(Level.WARNING, "Room: addCharacter onto room that is not Character's current room.");
		}

		if (player instanceof Player) {
			synchronized (this) {
				if (!playerList.contains(player)) {
					playerList.add((Player) player);
				}
			}
		} else {
			MyLogger.log(Level.WARNING, "Room: addCharacter on Character type that is not a player.");
		}
	}

	/**
	 * Removes a {@link Character} from the rooms player list (typically on room
	 * exit), so far only used to remove {@link Player}s. Always use AFTER setting
	 * room on Character, as this will check to make sure isn't removing current
	 * player room(check will result in hidden-to-player error).
	 *
	 * @param player
	 */
	public void removeCharacter(Character player) {
		if (player.getRoom() == this) {
			MyLogger.log(Level.WARNING, "Room: removeCharacter from room that is still Character's current room.");
		}

		if (player instanceof Player) {
			synchronized (playerList) {
				playerList.remove(player);
			}
		}
	}

	/**
	 * Returns list of {@link Player}s in the room.
	 *
	 * @return
	 */
	public ArrayList<Player> getPlayers() {
		synchronized (playerList) {
			for (Player player : playerList) {
				if (player.getRoom() != this) {
					MyLogger.log(Level.WARNING,
							"Room: Player found in playerList of room, that is not current room of Player. Player id: "
									+ player.getId());
				}
			}

			return new ArrayList<>(playerList);
		}
	}

	/**
	 * Sends to listeners of room, without any exclusion.
	 *
	 * <p>
	 * See {@link Room#sendToRoomExcluding(ClientConnectionOutput, Character)}
	 * </p>
	 *
	 * @param output
	 */
	public void sendToRoom(ClientConnectionOutput output) {
		sendToRoomExcluding(output, null);
	}

	/**
	 * Sends to listeners of the room, apart from notPlayer, if one is specified.
	 * Currently only {@link Player}s inside the room.
	 *
	 * @param output    Output to be sent.
	 * @param notPlayer Player, if specified, to be excluded from output.
	 */
	public void sendToRoomExcluding(ClientConnectionOutput output, Character ignoreCharacter) {
		synchronized (playerList) {
			for (Player player : playerList) {
				if (ignoreCharacter == null) {
					player.sendToListeners(new ClientConnectionOutput(output));
				} else {
					if (!(player == ignoreCharacter)) {
						player.sendToListeners(new ClientConnectionOutput(output));
					}
				}
			}
		}
	}

	/**
	 * Returns {@link RoomExit} of {@link Room} for given
	 * {@link fireengine.gameworld.map.Direction.DIRECTION}.
	 *
	 * @param direction
	 * @return
	 * @throws MapExceptionDirectionNotSupported
	 */
	public RoomExit getExit(Direction.DIRECTION direction) throws MapExceptionDirectionNotSupported {
		switch (direction) {
		case UP: {
			return upExit;
		}
		case DOWN: {
			return downExit;
		}
		case NORTH: {
			return northExit;
		}
		case NORTHEAST: {
			return northEastExit;
		}
		case EAST: {
			return eastExit;
		}
		case SOUTHEAST: {
			return southEastExit;
		}
		case SOUTH: {
			return southExit;
		}
		case SOUTHWEST: {
			return southWestExit;
		}
		case WEST: {
			return westExit;
		}
		case NORTHWEST: {
			return northWestExit;
		}
		default: {
			throw new MapExceptionDirectionNotSupported(
					"Room: getExit missing case for direction " + direction.toString() + ".");
		}
		}
	}

	/**
	 * Assigns {@link RoomExit} to {@link Room}, of given
	 * {@link fireengine.gameworld.map.Direction.DIRECTION}.
	 *
	 * @param direction
	 * @param newExit
	 * @throws MapExceptionDirectionNotSupported
	 * @throws CheckedHibernateException
	 * @throws MapExceptionRoomNull
	 */
	public void setExit(Direction.DIRECTION direction, RoomExit newExit)
			throws MapExceptionDirectionNotSupported, MapExceptionRoomNull, CheckedHibernateException {
		switch (direction) {
		case UP: {
			this.upExit = newExit;
			break;
		}
		case DOWN: {
			this.downExit = newExit;
			break;
		}
		case NORTH: {
			this.northExit = newExit;
			break;
		}
		case NORTHEAST: {
			this.northEastExit = newExit;
			break;
		}
		case EAST: {
			this.eastExit = newExit;
			break;
		}
		case SOUTHEAST: {
			this.southEastExit = newExit;
			break;
		}
		case SOUTH: {
			this.southExit = newExit;
			break;
		}
		case SOUTHWEST: {
			this.southWestExit = newExit;
			break;
		}
		case WEST: {
			this.westExit = newExit;
			break;
		}
		case NORTHWEST: {
			this.northWestExit = newExit;
			break;
		}
		default: {
			throw new MapExceptionDirectionNotSupported(
					"Room: setExit missing case for direction " + direction.toString() + ".");
		}
		}
		saveRoom(this);
	}

	/**
	 * Returns true if {@link Room} contains {@link RoomExit} for any
	 * {@link Direction.DIRECTION}. Mainly used to test if room is safe for
	 * deletion, having had all exits removed.
	 *
	 * @return
	 */
	protected boolean hasExit() {
		for (DIRECTION direction : DIRECTION.values()) {
			try {
				if (getExit(direction) != null) {
					return true;
				}
			} catch (MapExceptionDirectionNotSupported e) {
				MyLogger.log(Level.WARNING,
						"Room: MapExceptionDirectionNotSupported while trying to hasExit; direction: "
								+ direction.toString(),
						e);
			}
		}
		return false;
	}

	/**
	 * To take away, do not use database (or Hibernate) generated id in hashing (can
	 * in comparing). Note that is OK to set a static hashCode (if nothing else),
	 * even if it has a negative impact in performance somewhat. It is more
	 * important to retain a same hashCode return during the lifetime of the object.
	 * Need to specify a proper equals for persisted classes (that will not change
	 * over time with normal modifiable fields).
	 * <p>
	 * Notes from: https://vladmihalcea.com/hibernate-facts-equals-and-hashcode/
	 * </p>
	 * <p>
	 * We can’t use an auto-incrementing database id in the hashCode method since
	 * the transient and the attached object versions will no longer be located in
	 * the same hashed bucket.
	 * </p>
	 * <p>
	 * We can’t rely on the default Object equals and hashCode implementations since
	 * two entities loaded in two different persistence contexts will end up as two
	 * different Java objects, therefore breaking the all-states equality rule.
	 * </p>
	 * <p>
	 * So, if Hibernate uses the equality to uniquely identify an Object, for its
	 * whole lifetime, we need to find the right combination of properties
	 * satisfying this requirement. So, the business key must be set from the very
	 * moment we are creating the Entity and then never change it.
	 * </p>
	 * <p>
	 * Also see this from the 'net: The purpose of the hashCode() method is to
	 * provide a numeric representation of an object's contents so as to provide an
	 * alternate mechanism to loosely identify it. By default the hashCode() returns
	 * an integer that represents the internal memory address of the object.
	 * </p>
	 * <p>
	 * Also see:
	 * </p>
	 * <p>
	 * https://thoughts-on-java.org/ultimate-guide-to-implementing-equals-and-hashcode-with-hibernate/
	 * </p>
	 * <p>
	 * https://vladmihalcea.com/the-best-way-to-implement-equals-hashcode-and-tostring-with-jpa-and-hibernate/
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
	 * A proper custom implementation of equals is requires for correct
	 * JPA/Hibernate persisting.
	 * 
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
		Room other = (Room) obj;
		if (this.getId() == other.getId()) {
			return true;
		}
		return false;
	}

	/**
	 * NOT TO BE CALLED DIRECTLY. This function does not check for existing room
	 * etc; use the room creation in the {@link GameMap} class.
	 *
	 * Creates new {@link Room} at specified coordinates and returns new room.
	 *
	 * @return
	 * @throws MapExceptionRoomNull
	 * @throws CheckedHibernateException
	 */
	public static Room createRoom(GameMap map) throws MapExceptionRoomNull, CheckedHibernateException {
		Room newRoom = new Room(map);
		return newRoom;
	}

	/**
	 * Saves/persists the {@link Room} into the database.
	 *
	 * @param room
	 * @throws MapExceptionRoomNull
	 * @throws CheckedHibernateException
	 */
	public static void saveRoom(Room room) throws MapExceptionRoomNull, CheckedHibernateException {
		if (room == null) {
			throw new MapExceptionRoomNull("Room: Tried to saveRoom on a null room.");
		}

		org.hibernate.Session hibSess = null;
		Transaction tx = null;

		try {
			hibSess = FireEngineMain.hibSessFactory.openSession();
			tx = hibSess.beginTransaction();

			hibSess.saveOrUpdate(room);

			tx.commit();
		} catch (HibernateException e) {
			if (tx != null) {
				tx.rollback();
			}
			throw new CheckedHibernateException("Room: Hibernate error while trying to saveRoom.", e);
		} finally {
			if (hibSess != null) {
				hibSess.close();
			}
		}
	}
}
