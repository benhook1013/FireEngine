package fireengine.gameworld.map;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import fireengine.gameworld.map.room.Room;
import fireengine.util.IDSequenceGenerator;

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
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "MAP", nullable = false)
	@NotNull
	private GameMap map;

	@Column(name = "X", nullable = false)
	@NotNull
	private int x;

	@Column(name = "Y", nullable = false)
	@NotNull
	private int y;

	@Column(name = "Z", nullable = false)
	@NotNull
	private int z;

	@SuppressWarnings("unused")
	private Coordinate() {
	}

	public Coordinate(GameMap map, int x, int y, int z) {
		id = IDSequenceGenerator.getNextID("Coordinate");
		this.map = map;
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public int getId() {
		return id;
	}

	public GameMap getMap() {
		return map;
	}

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
		if (getId() != other.getId())
			return true;
		return false;
	}

	@Override
	public String toString() {
		return String.format("(%s, %s, %s)", x, y, z);
	}
}