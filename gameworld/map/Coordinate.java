package fireengine.gameworld.map;

import java.util.logging.Level;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.hibernate.HibernateException;
import org.hibernate.Transaction;

import fireengine.gameworld.map.exception.MapExceptionCoordinateNull;
import fireengine.gameworld.map.exception.MapExceptionRoomNull;
import fireengine.gameworld.map.room.Room;
import fireengine.main.FireEngineMain;
import fireengine.util.CheckedHibernateException;
import fireengine.util.IDSequenceGenerator;
import fireengine.util.MyLogger;

@Entity
@Table(name = "COORDINATE")
public class Coordinate {
	@Id
	@Column(name = "ID", nullable = false)
	@NotNull
	private int id;

	/**
	 * Used for making the equals function unique, as multiple maps can have the
	 * same z,y,z coordinate filled.
	 */
//	@ManyToOne(fetch = FetchType.EAGER)
//	@JoinColumn(name = "MAP", nullable = false)
//	@NotNull
//	private GameMap map;

	@Column(name = "X", nullable = false)
	@NotNull
	private int x;

	@Column(name = "Y", nullable = false)
	@NotNull
	private int y;

	@Column(name = "Z", nullable = false)
	@NotNull
	private int z;

	private Coordinate() {
	}

	private Coordinate(GameMap map, int x, int y, int z) {
		this();
		id = IDSequenceGenerator.getNextID("Coordinate");
//		this.map = map;
		this.x = x;
		this.y = y;
		this.z = z;
//		try {
//			saveCoord(this);
//		} catch (MapExceptionRoomNull e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (CheckedHibernateException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
	}

	public int getId() {
		return id;
	}

//	public GameMap getMap() {
//		return map;
//	}

	/**
	 * @return the x position
	 */
	public int getX() {
		return x;
	}

	/**
	 * @param x the x position to set
	 */
	public void setX(int x) {
		this.x = x;
	}

	/**
	 * @return the y position
	 */
	public int getY() {
		return y;
	}

	/**
	 * @param y the y position to set
	 */
	public void setY(int y) {
		this.y = y;
	}

	/**
	 * @return the z position
	 */
	public int getZ() {
		return z;
	}

	/**
	 * @param z the z position to set
	 */
	public void setZ(int z) {
		this.z = z;
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
		Coordinate other = (Coordinate) obj;
		if (getId() == other.getId())
			return true;
		return false;
	}

	@Override
	public String toString() {
		return String.format("(%s, %s, %s)", x, y, z);
	}

	public static Coordinate createCoord(GameMap map, int x, int y, int z) throws CheckedHibernateException {
		Coordinate newCoord = new Coordinate(map, x, y, z);
		try {
			saveCoord(newCoord);
		} catch (MapExceptionCoordinateNull e) {
			MyLogger.log(Level.SEVERE,
					"Coordinate: Coordinate null error on save while creating new Coordinate. Should not happen.");
			return null;
		}
		return newCoord;
	}

	/**
	 * Saves/persists the {@link Coordinate} into the database. Required as the
	 * cascade on the GameMaps map only cascades to the rooms Map's value.
	 *
	 * @param coord
	 * @throws MapExceptionRoomNull
	 * @throws CheckedHibernateException
	 * @throws MapExceptionCoordinateNull
	 */
	public static void saveCoord(Coordinate coord) throws CheckedHibernateException, MapExceptionCoordinateNull {
		if (coord == null) {
			throw new MapExceptionCoordinateNull("Coordinate: Tried to saveCoord on a null Coordinate.");
		}

		org.hibernate.Session hibSess = null;
		Transaction tx = null;

		try {
			hibSess = FireEngineMain.hibSessFactory.openSession();
			tx = hibSess.beginTransaction();

			hibSess.saveOrUpdate(coord);

			tx.commit();
		} catch (HibernateException e) {
			if (tx != null) {
				tx.rollback();
			}
			throw new CheckedHibernateException("Room: Hibernate error while trying to saveCoord.", e);
		} finally {
			if (hibSess != null) {
				hibSess.close();
			}
		}
	}

	/**
	 * Deletes the {@link Coordinate} from the database. Required as the cascade on
	 * the GameMaps map only cascades to the rooms Map's value.
	 *
	 * @param coord
	 * @throws MapExceptionRoomNull
	 * @throws CheckedHibernateException
	 * @throws MapExceptionCoordinateNull
	 */
	public static void deleteCoord(Coordinate coord) throws CheckedHibernateException, MapExceptionCoordinateNull {
		if (coord == null) {
			throw new MapExceptionCoordinateNull("Coordinate: Tried to deleteCoordf on a null Coordinate.");
		}

		org.hibernate.Session hibSess = null;
		Transaction tx = null;

		try {
			hibSess = FireEngineMain.hibSessFactory.openSession();
			tx = hibSess.beginTransaction();

			hibSess.delete(coord);

			tx.commit();
		} catch (HibernateException e) {
			if (tx != null) {
				tx.rollback();
			}
			throw new CheckedHibernateException("Room: Hibernate error while trying to deleteCoord.", e);
		} finally {
			if (hibSess != null) {
				hibSess.close();
			}
		}
	}
}