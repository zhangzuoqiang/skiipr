/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.skiipr.server.model.DAO.impl;

import com.skiipr.server.model.DAO.OrderDao;
import com.skiipr.server.model.Merchant;
import com.skiipr.server.model.Order;
import java.util.List;
import junit.framework.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 *
 * @author Michael
 */

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath*:testApplicationContext.xml"})
public class OrderDaoImplTest {
    
    @Autowired
    private OrderDao orderDao;
    
    @Test
    public void testFindAll(){
        List<Order> orders = orderDao.findAll();
        Order order1 = orders.get(0);
        Assert.assertEquals(order1.getPaypalAddress(), "order1");
        Assert.assertEquals(orders.size(), 2);
        
        
    }
    
    @Test
    public void testFindByID(){
        Order order = orderDao.findByID(2l);
        Assert.assertEquals(order.getPaypalAddress(), "order2");
        
        
    }
    
    @Test
    public void testSave(){
        Order order = new Order();
        order.setLastUpdated(123l);
        order.setOrderTime(1234l);
        order.setOrderType(123l);
        order.setPaypalAddress("winning");
        order.setPaypalRef(1234l);
        order.setStatus(1l);
        order.setTotal(1234);
        Merchant merchant = new Merchant();
        merchant.setMerchantID(3l);
        order.setMerchant(merchant);
        orderDao.save(order);
        List<Order> orders = orderDao.findAll();
        Assert.assertEquals(orders.size(), 3);
        order = orders.get(2);
        Assert.assertEquals(order.getPaypalAddress(), "winning");
    }
    
    
    
    
    
}