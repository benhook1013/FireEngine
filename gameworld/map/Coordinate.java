package fireengine.gameworld.map;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = "COORDINATE")
public class Coordinate {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ID", nullable = false)
	@NotNull
	private int id;

	@Column(name = "X", nullable = false)
	@NotNull
	private int x;

	@Column(name = "Y", nullable = false)
	@NotNull
	private int y;

	@Column(name = "Z", nullable = false)
	@NotNull
	private int z;

	public Coordinate() {
	}

	public Coordinate(int x, int y, int z) {
		this();
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public int getId() {
		return id;
	}

	@SuppressWarnings("unused")
	private void setId(int id) {
		this.id = id;
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

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Coordinate other = (Coordinate) obj;
		if (x != other.x)
			return false;
		if (y != other.y)
			return false;
		if (z != other.z)
			return false;
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 7;
		result = prime * result + x;
		result = prime * result + y;
		result = prime * result + z;
		return result;
	}

	@Override
	public String toString() {
		return String.format("(%s, %s, %s)", x, y, z);
	}
}