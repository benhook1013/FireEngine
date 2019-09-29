package fireengine.character.player;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import fireengine.gameworld.map.room.Room;
import fireengine.util.IDSequenceGenerator;

/*
 *    Copyright 2019 Ben Hook
 *    PlayerSetting.java
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
@Table(name = "PLAYER_SETTING")
public class PlayerSetting {
	@Id
	@Column(name = "ID", nullable = false)
	@NotNull
	private int id;

	@Column(name = "ADMIN", nullable = false)
	@NotNull
	private boolean admin;

	@Column(name = "MAP_EDITOR", nullable = false)
	@NotNull
	private boolean mapEditor;

	@SuppressWarnings("unused")
	private PlayerSetting() {
	}

	public PlayerSetting(Boolean bool) {
		id = IDSequenceGenerator.getNextID("PlayerSetting");
		mapEditor = false;
	}

	public int getId() {
		return id;
	}

	public boolean isAdmin() {
		return admin;
	}

	public void setAdmin(boolean admin) {
		this.admin = admin;
	}

	public boolean isMapEditor() {
		return mapEditor;
	}

	public void setMapEditor(boolean mapEditor) {
		this.mapEditor = mapEditor;
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
		PlayerSetting other = (PlayerSetting) obj;
		if (getId() == other.getId())
			return true;
		return false;
	}
}
