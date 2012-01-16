/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.skiipr.server.model.DAO.impl;


import com.skiipr.server.model.Coupon;
import com.skiipr.server.model.DAO.CouponDao;

import java.util.List;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;
import org.springframework.stereotype.Repository;

/**
 *
 * @author Michael
 */
@Repository("CouponDao")
public class CouponDaoImpl extends HibernateDaoSupport implements CouponDao {
    
    @Autowired
    public void init(SessionFactory factory){
        setSessionFactory(factory);
    }
    

    @Override
    public Coupon findByCode(int couponCode) {
        List list = getHibernateTemplate().find("from Coupon where couponCode=?", couponCode);
        if(list.isEmpty()){
            return null;
        }
	return (Coupon)list.get(0);
        
        
        
        
  
        
        
    }
    
    
}
