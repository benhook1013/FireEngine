package fireengine.characters.character_class.skillsets;

import javax.persistence.*;

/*
 *    Copyright 2017 Ben Hook
 *    Skillset_Martialism.java
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
public class Skillset_Martialism extends Base_Skillset {
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE)
	@Column(name = "SKILLSET_MARTIALISM_ID")
	private int id;

	public Skillset_Martialism() {
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
