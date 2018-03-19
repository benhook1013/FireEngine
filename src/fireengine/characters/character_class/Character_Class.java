package fireengine.characters.character_class;

import java.util.ArrayList;
import java.util.Collections;

import javax.persistence.*;

import fireengine.characters.character_class.skillsets.Base_Skillset;
import fireengine.utils.ConfigLoader;
import fireengine.utils.MyClassLoader;

/*
 *    Copyright 2017 Ben Hook
 *    Character_Class.java
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
@Table(name = "CHARACTER_CLASS")
public class Character_Class {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "CHAR_CLASS_ID")
	private int id;

	@Column(name = "CHAR_CLASS_NAME")
	protected String className;

	@Transient
	private static MyClassLoader classLoader;

	@Transient
	protected static ArrayList<Class<Base_Skillset>> skillsetClassList;

	@Transient
	protected ArrayList<Base_Skillset> skillsetList;

	@SuppressWarnings("unchecked")
	public static void loadSkillsets() throws ClassNotFoundException {
		classLoader = new MyClassLoader();

		String skillsetNameString = ConfigLoader.getSetting("charClassSkillsetList");
		ArrayList<String> skillsetNameArray = new ArrayList<>();
		Collections.addAll(skillsetNameArray, skillsetNameString.split(";"));

		for (String skillsetName : skillsetNameArray) {
			System.out.println("skillsetName: '" + skillsetName + "'");
			skillsetClassList.add((Class<Base_Skillset>) classLoader.loadClass(skillsetName));
		}
	}

	public Character_Class() {
		className = "Novice";
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getClassName() {
		return className;
	}

	public void setClassName(String name) {
		this.className = name;
	}

	protected void addSkillset(Base_Skillset skillset) {
		skillsetList.add(skillset);
	}
}
