package com.skiipr.server.model.DAO.impl;

import com.skiipr.server.components.SessionUser;
import com.skiipr.server.model.DAO.CategoryDao;
import com.skiipr.server.model.DAO.ProductDao;
import com.skiipr.server.model.LoginUser;
import com.skiipr.server.model.Product;
import java.util.Collections;
import java.util.List;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;
import org.springframework.stereotype.Repository;

@Repository("ProductDao")
public class ProductDaoImpl extends HibernateDaoSupport implements ProductDao {

    @Autowired
    private SessionUser sessionUser;
    
    @Autowired
    private CategoryDao categoryDao;
    
    @Autowired
    public void init(SessionFactory factory) {
        setSessionFactory(factory);
    }
    
    @Override
    public void save(Product product) {
        getHibernateTemplate().save(product);
    }

    @Override
    public void update(Product product) {
        getHibernateTemplate().update(product);
    }

    @Override
    public void delete(Product product) {
        getHibernateTemplate().delete(product);
    }

    @Override
    public Product findByID(Long id) {
        Product product = (Product) getSession().get(Product.class, id);
	return product;
    }

    @Override
    public List<Product> findAll() {
        List list = getHibernateTemplate().find("from Product");
	return (List<Product>) list;
    }
    
    @Override
    public List<Product> findByCategoryID(Long id){
        List list = getHibernateTemplate().find("from Product where category.categoryID=?", id);
	return (List<Product>) list;   
    }

    @Override
    public Product findByIDNoRelation(Long id) {
        List products = getHibernateTemplate().find("from Product where productID=?", id);
        if(products.size() == 0){
            return null;
        }
	return (Product) products.get(0);
    }
    
    @Override
    public List<Product> findByCollection(List<Long> ids, Long merchantID){
        Criteria criteria = getSession().createCriteria(Product.class)
                .add(Restrictions.in("category", categoryDao.findByMerchantId(merchantID)))
                .add(Restrictions.in("productID", ids));
        List<Product> products = criteria.list();
        return products;
        
       
    }

    @Override
    public Product findByMerchant(Long productID) {
        LoginUser user = sessionUser.getUser();
        Long merchantID = user.getMerchantId();
        return findByMerchant(productID, merchantID);
    }

    @Override
    public Product findByMerchant(Long productID, Long merchantID) {
        String[] params = {"prodID", "merchID"};
        Object[] values = {productID, merchantID};
        List<Product> products = getHibernateTemplate().findByNamedParam("from Product where (productID = :prodID) AND (category.merchantID = :merchID)", params, values);
        if(products.size() == 0){
            return null;
        }
	return (Product) products.get(0);
    }
    
    @Override
    public List<Product> findAllByMerchant(){
        LoginUser user= sessionUser.getUser();
        Long merchantID = user.getMerchantId();
        return findAllByMerchant(merchantID);  
    }
    
    
    @Override
    public List<Product> findAllByMerchant(Long merchantID){
        List<Product> products = getHibernateTemplate().find("from Product where (category.merchantID = ?)", merchantID);
	return (List<Product>) products;
    }
    
    @Override
    public List<Product> findRange(Integer first, Integer max) {
        try{
            Criteria criteria = getSession().createCriteria(Product.class)
                .setMaxResults(max)
                .setFirstResult(first)
                .add(Restrictions.in("category", categoryDao.findByMerchantId()));
            List<Product> products = criteria.list();
            if(products.isEmpty()){
            return null;
        }
	return products;
            
        }
        catch(Exception e){
            return Collections.EMPTY_LIST;
            
        }
        
        
       
        
    }
    
     @Override
    public Integer countByMerchant() {
        
        return findAllByMerchant().size();
    }
    
}
