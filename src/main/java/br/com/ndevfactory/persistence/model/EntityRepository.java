package br.com.ndevfactory.persistence.model;

import java.util.List;

public interface EntityRepository<T extends Object> {
	
	boolean contains(T entity);
	
	Long count();

	List<T> findAll();
	
	T findOne(Object id);
	
	boolean insert(T entity);
	
	boolean update(T entity);
	
	boolean delete(T entity);
}
