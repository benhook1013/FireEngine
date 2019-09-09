package fireengine.character.character_class;

import fireengine.gameworld.map.GameMap;
import fireengine.gameworld.map.room.Room;
import fireengine.util.IDSequenceGenerator;

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

//@Entity
//@Table(name = "CHAR_CLASS")
public class CharacterClass {
//	@Id
//	@Column(name = "ID", nullable = false)
//	@NotNull
	private int id;
//
//	@Column(name = "NAME", nullable = false)
//	@NotNull
	protected String className;

//	@Transient
//	private static MyClassLoader classLoader;

//	@Transient
//	protected static ArrayList<Class<Skillset>> skillsetClassList;

//	@Transient
//	protected ArrayList<Skillset> skillsetList;

//	@SuppressWarnings("unchecked")
//	public static void loadSkillsets() throws ClassNotFoundException {
//		classLoader = new MyClassLoader();
//
//		String skillsetNameString = ConfigLoader.getSetting("charClassSkillsetList");
//		ArrayList<String> skillsetNameArray = new ArrayList<>();
//		Collections.addAll(skillsetNameArray, skillsetNameString.split(";"));
//
//		skillsetClassList = new ArrayList<>();
//		for (String skillsetName : skillsetNameArray) {
//			MyLogger.log(Level.INFO, String.format("CharacterClass: skillset '%s'.", skillsetName));
//			skillsetClassList.add((Class<Skillset>) classLoader.loadClass(skillsetName));
//		}
//	}

	@SuppressWarnings("unused")
	private CharacterClass() {
	}

	// Placeholder to stop unnecessarily using up an ID/
	public CharacterClass(Boolean bool) {
		id = IDSequenceGenerator.getNextID("CharacterClass");
		className = "Novice";
	}

	public int getId() {
		return id;
	}

	public String getClassName() {
		return className;
	}

//	public void setClassName(String name) {
//		this.className = name;
//	}

//	protected void addSkillset(Skillset skillset) {
//		skillsetList.add(skillset);
//	}

	/**
	 * Custom implementation requires for proper JPA/Hibernate function.
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
	 * 
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
		GameMap other = (GameMap) obj;
		if (this.getId() == other.getId()) {
			return true;
		}
		return false;
	}
}
