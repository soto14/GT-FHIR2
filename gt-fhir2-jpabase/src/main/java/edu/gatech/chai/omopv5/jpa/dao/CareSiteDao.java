package edu.gatech.chai.omopv5.jpa.dao;

import org.springframework.stereotype.Repository;

import edu.gatech.chai.omopv5.jpa.entity.CareSite;

@Repository
public class CareSiteDao extends BaseEntityDao<CareSite> {
	@Override
	public void add(CareSite baseEntity) {
		getEntityManager().persist(baseEntity);
	}

	@Override
	public void update(CareSite baseEntity) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public CareSite findById(Long id) {
		return getEntityManager().find(CareSite.class, id);
	}

	@Override
	public void delete(Long id) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void merge(CareSite baseEntity) {
		getEntityManager().merge(baseEntity);
	}
}
