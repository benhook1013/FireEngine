package fireengine.character.condition;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import fireengine.character.condition.exception.ManaExceptionZeroMana;
import fireengine.gameworld.map.room.Room;
import fireengine.util.IDSequenceGenerator;

/*
 *    Copyright 2019 Ben Hook
 *    Mana.java
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
@Table(name = "CHAR_MANA")
public class Mana {
	@Id
	@Column(name = "ID", nullable = false)
	@NotNull
	private int id;

	@Column(name = "MANA", nullable = false)
	@NotNull
	private int mana;

	@SuppressWarnings("unused")
	private Mana() {
	}

	public Mana(int mana) {
		synchronized (this) {
			id = IDSequenceGenerator.getNextID("Mana");
			this.mana = mana;
		}
	}

	private int getId() {
		return id;
	}

	public int getMana(int maxMana) {
		synchronized (this) {
			if (mana > maxMana) {
				mana = maxMana;
			}
			return mana;
		}
	}

	public void setMana(int maxMana, int mana) {
		synchronized (this) {
			if (mana > maxMana) {
				this.mana = maxMana;
			} else {
				this.mana = mana;
			}
		}
	}

	public void addMana(int maxMana, int addMana) {
		synchronized (this) {
			int manaDiff = maxMana - this.mana;
			if (manaDiff < addMana) {
				addMana = manaDiff;
			}
			this.mana = this.mana + addMana;
		}
	}

	public void addManaPercentMax(int maxMana, int percent) {
		synchronized (this) {
			int addMana = (int) Math.round((maxMana / 100.0) * percent);

			addMana(maxMana, addMana);
		}
	}

	public void addManaPercentCurrent(int maxMana, int percent) {
		synchronized (this) {
			int addMana = (int) Math.round((this.mana / 100.0) * percent);

			addMana(maxMana, addMana);
		}
	}

	public void removeMana(int maxMana, int removeMana) throws ManaExceptionZeroMana {
		synchronized (this) {
			int newMana = this.mana - removeMana;
			if (newMana < 1) {
				this.mana = 0;
				throw new ManaExceptionZeroMana("Mana: removeMana resulted in 0 mana.");
			} else {
				this.mana = newMana;
			}
		}
	}

	public void removeManaPercentMax(int maxMana, int percent) throws ManaExceptionZeroMana {
		synchronized (this) {
			int removeMana = (int) Math.round((maxMana / 100.0) * percent);

			removeMana(maxMana, removeMana);
		}
	}

	public void removeManaPercentCurrent(int maxMana, int percent) throws ManaExceptionZeroMana {
		synchronized (this) {
			int removeMana = (int) Math.round((this.mana / 100.0) * percent);

			removeMana(maxMana, removeMana);
		}
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
		Mana other = (Mana) obj;
		if (getId() != other.getId())
			return true;
		return false;
	}
}
