package fireengine.character.character_class;

import java.util.ArrayList;
import java.util.Collections;
import java.util.logging.Level;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;

import fireengine.character.character_class.skillset.Skillset;
import fireengine.util.ConfigLoader;
import fireengine.util.MyClassLoader;
import fireengine.util.MyLogger;

/*
 *    Copyright 2019 Ben Hook
 *    CharacterClass.java
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
@Table(name = "CHAR_CLASS")
public class CharacterClass {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ID", nullable = false)
	@NotNull
	private int id;

	@Column(name = "NAME", nullable = false)
	@NotNull
	protected String className;

	@Transient
	private static MyClassLoader classLoader;

	@Transient
	protected static ArrayList<Class<Skillset>> skillsetClassList;

	@Transient
	protected ArrayList<Skillset> skillsetList;

	@SuppressWarnings("unchecked")
	public static void loadSkillsets() throws ClassNotFoundException {
		classLoader = new MyClassLoader();

		String skillsetNameString = ConfigLoader.getSetting("charClassSkillsetList");
		ArrayList<String> skillsetNameArray = new ArrayList<>();
		Collections.addAll(skillsetNameArray, skillsetNameString.split(";"));

		skillsetClassList = new ArrayList<>();
		for (String skillsetName : skillsetNameArray) {
			MyLogger.log(Level.INFO, String.format("CharacterClass: skillset '%s'.", skillsetName));
			skillsetClassList.add((Class<Skillset>) classLoader.loadClass(skillsetName));
		}
	}

	public CharacterClass() {
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

	protected void addSkillset(Skillset skillset) {
		skillsetList.add(skillset);
	}
}
