package fireengine.util;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;

import org.hibernate.HibernateException;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import fireengine.gameworld.map.room.Room;
import fireengine.main.FireEngineMain;

/*
 *    Copyright 2019 Ben Hook
 *    IDSequenceGenerator.java
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
@Table(name = "ID_SEQUENCE_GEN")
public class IDSequenceGenerator implements Comparable<IDSequenceGenerator> {
	@Transient
	private static Set<IDSequenceGenerator> genList = new TreeSet<IDSequenceGenerator>();

	@Transient
	private final static Object lock = new Object();

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ID", nullable = false)
	@NotNull
	private int id;

	/**
	 * String representing sequence type for IDs.
	 */
	@Column(name = "ID_TYPE", nullable = false)
	@NotNull
	private String idType;

	/**
	 * Current ID already assigned. Next ID will be this + 1.
	 */
	@Column(name = "CUR_ID", nullable = false)
	@NotNull
	private int currentID;

	private IDSequenceGenerator() {
	}

	protected IDSequenceGenerator(String idType) {
		this();
		this.idType = idType;
		currentID = 0;
	}

	private int getId() {
		return id;
	}

	private String getIdType() {
		return idType;
	}

	private int getCurrentId() {
		return currentID;
	}

	private void setCurrentId(int newCurentId) {
		currentID = newCurentId;
	}

	public static int getNextID(String idType) {
		synchronized (lock) {
			IDSequenceGenerator gen = null;
			idType = idType.toUpperCase();

			for (IDSequenceGenerator foundGen : genList) {
				if (foundGen.getIdType().equals(idType)) {
					gen = foundGen;
				}
			}

			if (gen == null) {
				try {
					gen = createOrLoadGen(idType);
				} catch (CheckedHibernateException e) {
					MyLogger.log(Level.SEVERE,
							"IDSequenceGenerator: Hibernate error while trying to create or load generator.", e);
					return -1;
				}
			}

			int newId = gen.getCurrentId() + 1;
			gen.setCurrentId(newId);
			try {
				saveGen(gen);
			} catch (CheckedHibernateException e) {
				MyLogger.log(Level.SEVERE,
						"IDSequenceGenerator: Hibernate error while trying to save updated generator.", e);
				return -1;
			}

			return newId;
		}
	}

	private static IDSequenceGenerator createOrLoadGen(String idType) throws CheckedHibernateException {
		org.hibernate.Session hibSess = FireEngineMain.hibSessFactory.openSession();
		Transaction tx = null;
		IDSequenceGenerator loadedGen = null;

		try {
			tx = hibSess.beginTransaction();

			Query<?> query = hibSess.createQuery("FROM IDSequenceGenerator WHERE ID_TYPE = :idType");
			query.setParameter("idType", idType);

			@SuppressWarnings("unchecked")
			List<IDSequenceGenerator> foundGens = (List<IDSequenceGenerator>) query.list();
			tx.commit();

			if (foundGens.isEmpty()) {
				loadedGen = new IDSequenceGenerator(idType);
				saveGen(loadedGen);
				return loadedGen;
			} else {
				if (foundGens.size() > 1) {
					MyLogger.log(Level.WARNING, "IDSequenceGenerator: Multiple DB results for same idType.");
				}
				loadedGen = foundGens.get(0);
				genList.add(loadedGen);
				return loadedGen;
			}

		} catch (HibernateException e) {
			if (tx != null) {
				tx.rollback();
			}
			throw new CheckedHibernateException("IDSequenceGenerator: Hibernate error while trying to createOrLoadGen.",
					e);
		} finally {
			hibSess.close();
		}
	}

	private static void saveGen(IDSequenceGenerator gen) throws CheckedHibernateException {
		org.hibernate.Session hibSess = null;
		Transaction tx = null;

		try {
			hibSess = FireEngineMain.hibSessFactory.openSession();
			tx = hibSess.beginTransaction();

			hibSess.saveOrUpdate(gen);

			tx.commit();
		} catch (HibernateException e) {
			if (tx != null) {
				tx.rollback();
			}
			throw new CheckedHibernateException("IDSequenceGenerator: Hibernate error while trying to saveNewGen.", e);
		} finally {
			if (hibSess != null) {
				hibSess.close();
			}
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
		result = (prime * result);
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
		IDSequenceGenerator other = (IDSequenceGenerator) obj;
		if (getId() == other.getId())
			return true;
		return false;
	}

	@Override
	public int compareTo(IDSequenceGenerator other) {
		// compareTo should return < 0 if this is supposed to be
		// less than other, > 0 if this is supposed to be greater than
		// other and 0 if they are supposed to be equal
		return this.getIdType().compareToIgnoreCase(other.getIdType());
	}
}
