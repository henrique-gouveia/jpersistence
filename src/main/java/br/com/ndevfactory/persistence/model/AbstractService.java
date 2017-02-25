package br.com.ndevfactory.persistence.model;

import java.io.Serializable;
import java.util.List;

import javax.inject.Inject;

public abstract class AbstractService<T extends Object> implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	@Inject
	protected EntityRepository<T> entityRepository;
	
	public Long count() {
		return entityRepository.count();
	}

	public List<T> findAll() {
		return entityRepository.findAll();
	}
	
	public T findOne(Object id) {
		return entityRepository.findOne(id);
	}
	
	public boolean save(T entity) {
		if (this.isNew(entity)) {
			return entityRepository.update(entity);
		} else {
			return entityRepository.insert(entity);
		}
	}
	
	public boolean remove(T entity) {
		return entityRepository.delete(entity);
	}
	
	public abstract boolean isNew(T entity);
}
