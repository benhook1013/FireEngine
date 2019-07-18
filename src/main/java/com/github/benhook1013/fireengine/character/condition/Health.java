package com.github.benhook1013.fireengine.character.condition;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import com.github.benhook1013.fireengine.character.condition.exception.HealthExceptionZeroHealth;

/*
 *    Copyright 2017 Ben Hook
 *    Health.java
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
@Table(name = "CHAR_HEALTH")
public class Health {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "CHAR_HEALTH_ID")
	private int id;

	@Column(name = "CHAR_HEALTH_HEALTH")
	private volatile int health;

	@SuppressWarnings("unused")
	private Health() {
	}

	public Health(int health) {
		this.health = health;
	}

	@SuppressWarnings("unused")
	private int getId() {
		return id;
	}

	@SuppressWarnings("unused")
	private void setId(int id) {
		this.id = id;
	}

	public int getHealth(int maxHealth) {
		if (health > maxHealth) {
			health = maxHealth;
		}
		return health;
	}

	public void setHealth(int maxHealth, int health) {
		if (health > maxHealth) {
			this.health = maxHealth;
		} else {
			this.health = health;
		}
	}

	public void addHealth(int maxHealth, int addHealth) {
		synchronized (this) {
			int healthDiff = maxHealth - this.health;
			if (healthDiff < addHealth) {
				addHealth = healthDiff;
			}
			this.health = this.health + addHealth;
		}
	}

	public void addHealthPercentMax(int maxHealth, int percent) {
		synchronized (this) {
			int addHealth = (int) Math.round((maxHealth / 100.0) * percent);

			addHealth(maxHealth, addHealth);
		}
	}

	public void addHealthPercentCurrent(int maxHealth, int percent) {
		synchronized (this) {
			int addHealth = (int) Math.round((this.health / 100.0) * percent);

			addHealth(maxHealth, addHealth);
		}
	}

	public void removeHealth(int maxHealth, int removeHealth) throws HealthExceptionZeroHealth {
		synchronized (this) {
			int newHealth = this.health - removeHealth;
			if (newHealth < 1) {
				this.health = 0;
				throw new HealthExceptionZeroHealth("Health: removehealth resulted in 0 health.");
			} else {
				this.health = newHealth;
			}
		}
	}

	public void removeHealthPercentMax(int maxHealth, int percent) throws HealthExceptionZeroHealth {
		synchronized (this) {
			int removeHealth = (int) Math.round((maxHealth / 100.0) * percent);

			removeHealth(maxHealth, removeHealth);
		}
	}

	public void removeHealthPercentCurrent(int maxHealth, int percent) throws HealthExceptionZeroHealth {
		synchronized (this) {
			int removeHealth = (int) Math.round((this.health / 100.0) * percent);

			removeHealth(maxHealth, removeHealth);
		}
	}
}
