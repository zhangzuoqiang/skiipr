package com.skiipr.server.model.DAO.impl;

import com.skiipr.server.components.SessionUser;
import com.skiipr.server.enums.CurrencyType;
import com.skiipr.server.model.DAO.MerchantDao;
import com.skiipr.server.model.LoginUser;
import com.skiipr.server.model.Merchant;
import java.util.List;
import java.util.Set;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath*:testApplicationContext.xml"})
public class MerchantDaoImplTest {
    
    @InjectMocks
    @Autowired
    private MerchantDao merchantDao;
    
    @Mock
    LoginUser loginUser;
    
    @Mock
    SessionUser sessionUser;
    
    @Before
    public void setUpClass() throws Exception {
        MockitoAnnotations.initMocks(this);
        
        //sessionUser
        Mockito.when(sessionUser.getUser()).thenReturn(loginUser);
        
        //loginUser
        Mockito.when(loginUser.getMerchantId()).thenReturn(3l);
    }

    @Test
    public void testFindById(){
        Merchant merchant = merchantDao.findById(1l);
        Assert.assertEquals(merchant.getUsername(), "fred");
        Assert.assertEquals(CurrencyType.HK_DOLLAR.getCurrencyCode(), merchant.getCurrencyType());
    }
    
    @Test
    public void testFindByUsername(){
        Merchant merchant = merchantDao.findByUsername("fred");
        Long id = 1l;
        Assert.assertEquals(merchant.getMerchantID(), id); 
    }
    
    @Test
    public void testSave(){
        Merchant merchant = merchantDao.findByUsername("fred");
        merchant.setUsername("alex");
        merchant.setName("round club");
        merchant.setOpen(Boolean.TRUE);
        merchantDao.save(merchant);
        merchant = merchantDao.findByUsername("alex");
        Assert.assertEquals(merchant.getName(), "round club");
    }
    
    @Test
    public void testUpdate(){
        Merchant merchant = merchantDao.findById(2l);
        merchant.setUsername("foobar");
        merchant.setAddressNumberStreet("green street");
        merchantDao.update(merchant);
        merchant = merchantDao.findById(2l);
        Assert.assertEquals(merchant.getUsername(), "foobar");
        Assert.assertEquals(merchant.getAddressNumberStreet(), "green street");
    }
    
    @Test
    public void testDelete(){
        Merchant merchant = merchantDao.findByUsername("fred");
        merchant.setUsername("bobby");
        merchantDao.save(merchant);
        merchant = merchantDao.findByUsername("bobby");
        merchantDao.delete(merchant);
        merchant = merchantDao.findByUsername("bobby");
        Assert.assertNull(merchant);
    }
    
    @Test
    public void testFindAll(){
        List<Merchant> merchants = merchantDao.findAll();
        Assert.assertEquals(merchants.size(), 3);
    }
    
    @Test
    public void testRadius(){
        double lat = -27.70828;
        double lon = 150.37253;
        double radius = 10;
        List<Merchant> merchants = merchantDao.findWithinRadius(lat, lon, radius);
        Assert.assertEquals(merchants.size(), 1);
    }
    
    @Test
    public void testFindByName(){
        List<Merchant> merchants = merchantDao.findByName("big");
        Long id = 1l;
        Assert.assertEquals(merchants.get(0).getMerchantID(), id);
        Assert.assertEquals(merchants.size(), 1);
        merchants = merchantDao.findByName("bar");
        Assert.assertEquals(merchants.size(), 3);
        id = 2l;
        Assert.assertEquals(merchants.get(1).getMerchantID(), id);
    }
    
    @Test
    public void testCategoryRelationship(){
        Merchant merchant = merchantDao.findById(1l);
        Set categories = merchant.getCategories();
        Assert.assertEquals(2, categories.size());
    }
    
    @Test
    public void testUsernameAvailable(){
        Assert.assertEquals(true, merchantDao.usernameAvailable("random"));
        Assert.assertEquals(true, merchantDao.usernameAvailable("bob"));
        Assert.assertEquals(false, merchantDao.usernameAvailable("fred"));
    }
    
    @Test
    public void testFindCurrentMerchant(){
        Merchant merchant = merchantDao.findCurrentMerchant();
        Assert.assertEquals("bob", merchant.getUsername());
    }
    
    @Test
    public void testTradingNameAvailable(){
        Assert.assertEquals(true, merchantDao.tradingNameAvailable("zimmos"));
        Assert.assertEquals(true, merchantDao.tradingNameAvailable("Bogan Bar"));
        Assert.assertEquals(false, merchantDao.tradingNameAvailable("big bar"));   
    }
}