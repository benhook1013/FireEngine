package com.github.benhook1013.fireengine.characters.character_class.skillsets;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;

/*
 *    Copyright 2017 Ben Hook
 *    SkillsetMartialism.java
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
@Table(name = "SKILLSET_MARTIALISM")
@PrimaryKeyJoinColumn(name = "SKILLSET_MARTIALISM_BASE_SKILLSET_ID")
public class SkillsetMartialism extends Skillset {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "SKILLSET_MARTIALISM_ID")
	private int id;

	public SkillsetMartialism() {
		setSkillsetName("Martialism");
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	protected class Weaponry extends Base_Skill {
		protected Weaponry() {
			this.skillName = "Weaponry";
		}
	}
}