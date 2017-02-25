package br.com.ndevfactory.persistence.model;

import java.io.Serializable;
import java.util.List;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;

import br.com.ndevfactory.persistence.annotation.Transactional;

public abstract class AbstractRepository<T extends Object> implements EntityRepository<T>, Serializable {

	private static final long serialVersionUID = 1L;
	
	private Class<T> entityClass;
	
	@Inject
	protected EntityManager entityManager;
	
	public AbstractRepository(Class<T> entityClass) {
		this.entityClass = entityClass;
	}

	@Override
	public boolean contains(T entity) {
		return entityManager.contains(entity);
	};

	@Override
	public Long count() {
		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<Long> criteriaQuery = criteriaBuilder.createQuery(Long.class);

		criteriaQuery.select(criteriaBuilder.count(criteriaQuery.from(entityClass)));

		return entityManager.createQuery(criteriaQuery).getSingleResult();
	}

	@Override
	public List<T> findAll() {
		String jpql = "from " + entityClass.getSimpleName();
		TypedQuery<T> query = entityManager.createQuery(jpql, entityClass);
		return query.getResultList();
	}
	
	@Override
	public T findOne(Object id) {
		return entityManager.find(entityClass, id);
	}

	@Transactional
	@Override
	public boolean insert(T entity) {
		try {
			entityManager.persist(entity);
			return true;
		} catch (Exception e) {
			throw e;
		}
	}
	
	@Transactional
	@Override
	public boolean update(T entity) {
		try {
			entityManager.merge(entity);
			return true;
		} catch (Exception e) {
			throw e;
		}
	}
	
	@Transactional
	@Override
	public boolean delete(T entity) {
		try {
			entity = entityManager.merge(entity);
			entityManager.remove(entity);
			return true;
		} catch (Exception e) {
			throw e;
		}
	}
}
