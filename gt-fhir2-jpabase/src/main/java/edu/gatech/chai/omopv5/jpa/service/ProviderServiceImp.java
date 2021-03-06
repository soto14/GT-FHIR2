package edu.gatech.chai.omopv5.jpa.service;

import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import edu.gatech.chai.omopv5.jpa.dao.ProviderDao;
import edu.gatech.chai.omopv5.jpa.entity.Provider;

@Service
public class ProviderServiceImp implements ProviderService {

	@Autowired
	private ProviderDao providerDao;

	@Transactional(readOnly = true)
	@Override
	public Provider findById(Long id) {
		return providerDao.findById(id);
	}

	@Transactional(readOnly = true)
	@Override
	public List<Provider> searchByColumnString(String column, String value) {
		EntityManager em = providerDao.getEntityManager();
		
		String query = "SELECT t FROM Provider t WHERE "+column+" like :value";
		List<Provider> results = em.createQuery(query, Provider.class)
				.setParameter("value",  value).getResultList();
		if (results.size() > 0)
			return results;
		else
			return null;	
	}

	@Transactional
	@Override
	public Provider createOrUpdate(Provider entity) {
		if (entity.getId() != null) {
			providerDao.merge(entity);
		} else {
			providerDao.add(entity);
		}
		return entity;
	}

	@Transactional(readOnly = true)
	@Override
	public Long getSize() {
		EntityManager em = providerDao.getEntityManager();
		
		String query = "SELECT COUNT(t) FROM Provider t";
		Long totalSize = em.createQuery(query, Long.class).getSingleResult();
		return totalSize;
	}

	@Transactional(readOnly = true)
	@Override
	public Long getSize(Map<String, List<ParameterWrapper>> paramMap) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Provider> searchWithoutParams(int fromIndex, int toIndex) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Provider> searchWithParams(int fromIndex, int toIndex, Map<String, List<ParameterWrapper>> paramMap) {
		// TODO Auto-generated method stub
		return null;
	}
	
}
