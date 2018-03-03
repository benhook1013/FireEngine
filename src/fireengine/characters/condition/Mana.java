package fireengine.characters.condition;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import fireengine.characters.condition.exceptions.Mana_Exception_0_Mana;

/*
 *    Copyright 2017 Ben Hook
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
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "CHAR_MANA_ID")
	private int id;

	@Column(name = "CHAR_MANA_MANA")
	private volatile int mana;

	@SuppressWarnings("unused")
	private Mana() {
	}

	public Mana(int mana) {
		this.mana = mana;
	}

	@SuppressWarnings("unused")
	private int getId() {
		return id;
	}

	@SuppressWarnings("unused")
	private void setId(int id) {
		this.id = id;
	}

	public int getMana(int maxMana) {
		if (mana > maxMana) {
			mana = maxMana;
		}
		return mana;
	}

	public void setMana(int maxMana, int mana) {
		if (mana > maxMana) {
			this.mana = maxMana;
		} else {
			this.mana = mana;
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

	public void removeMana(int maxMana, int removeMana) throws Mana_Exception_0_Mana {
		synchronized (this) {
			int newMana = this.mana - removeMana;
			if (newMana < 1) {
				this.mana = 0;
				throw new Mana_Exception_0_Mana("Mana: removeMana resulted in 0 mana.");
			} else {
				this.mana = newMana;
			}
		}
	}

	public void removeManaPercentMax(int maxMana, int percent) throws Mana_Exception_0_Mana {
		synchronized (this) {
			int removeMana = (int) Math.round((maxMana / 100.0) * percent);

			removeMana(maxMana, removeMana);
		}
	}

	public void removeManaPercentCurrent(int maxMana, int percent) throws Mana_Exception_0_Mana {
		synchronized (this) {
			int removeMana = (int) Math.round((this.mana / 100.0) * percent);

			removeMana(maxMana, removeMana);
		}
	}
}
