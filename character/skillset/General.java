package fireengine.character.skillset;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import fireengine.character.command.action.general.*;
import fireengine.character.command.action.general.player_action.QuitGame;
import fireengine.character.command.action.general.player_action.admin.Shutdown;
import fireengine.character.command.action.general.player_action.map_editor.CreateExit;
import fireengine.character.command.action.general.player_action.map_editor.CreateRoom;
import fireengine.character.command.action.general.player_action.map_editor.DestroyExit;
import fireengine.character.command.action.general.player_action.map_editor.DestroyRoom;
import fireengine.gameworld.map.room.Room;
import fireengine.util.IDSequenceGenerator;

/*
 *    Copyright 2019 Ben Hook
 *    General.java
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
@Table(name = "SKILLSET_GENERAL")
@PrimaryKeyJoinColumn(name = "SKILLSET_ID")
public class General extends Skillset {
	@Id
	@Column(name = "ID", nullable = false)
	@NotNull
	private int id;

	private static String name = "General";

	@SuppressWarnings("unused")
	private General() {
		SkillsetCategory movementCategory = new SkillsetCategory("Movement");
		movementCategory.addSkillEntry(0, new Move());
		movementCategory.addSkillEntry(0, new Map());
		addCategory(movementCategory);

		SkillsetCategory otherCategory = new SkillsetCategory("Other");
		otherCategory.addSkillEntry(0, new Look());
		otherCategory.addSkillEntry(0, new Say());
		otherCategory.addSkillEntry(0, new Who());
		addCategory(otherCategory);

		SkillsetCategory playerCategory = new SkillsetCategory("Player only");
		playerCategory.addSkillEntry(0, new QuitGame());
		addCategory(playerCategory);

		SkillsetCategory mapEditorCategory = new SkillsetCategory("Map Editor only");
		mapEditorCategory.addSkillEntry(0, new CreateExit());
		mapEditorCategory.addSkillEntry(0, new CreateRoom());
		mapEditorCategory.addSkillEntry(0, new DestroyExit());
		mapEditorCategory.addSkillEntry(0, new DestroyRoom());
		addCategory(mapEditorCategory);

		SkillsetCategory adminCategory = new SkillsetCategory("Admin only");
		adminCategory.addSkillEntry(0, new Shutdown());
		addCategory(adminCategory);
	}

	public General(Boolean bool) {
		super(true);
		id = IDSequenceGenerator.getNextID("General");
	}

	@Override
	public String getName() {
		return name;
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
		General other = (General) obj;
		if (getId() != other.getId())
			return true;
		return false;
	}
}
