package com.skiipr.server.controller.dashboard;

import com.skiipr.server.components.FlashNotification;
import com.skiipr.server.components.SessionUser;
import com.skiipr.server.enums.Country;
import com.skiipr.server.enums.CouponType;
import com.skiipr.server.enums.CurrencyType;
import com.skiipr.server.enums.MerchantType;
import com.skiipr.server.enums.Status;
import com.skiipr.server.model.Banned;
import com.skiipr.server.model.Coupon;
import com.skiipr.server.model.DAO.BannedDao;
import com.skiipr.server.model.DAO.CouponDao;
import com.skiipr.server.model.DAO.MerchantDao;
import com.skiipr.server.model.DAO.PlanDao;
import com.skiipr.server.model.Merchant;
import com.skiipr.server.model.form.CouponForm;
import com.skiipr.server.model.form.SettingsForm;
import com.skiipr.server.model.form.PaymentOptions;
import com.skiipr.server.model.validators.MerchantValidator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import org.apache.commons.validator.EmailValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class SettingsController {
    
    @Autowired
    private MerchantDao merchantDao;
    
    @Autowired
    private PlanDao planDao;
    
    @Autowired
    private SessionUser sessionUser;
    
    @Autowired
    private CouponDao couponDao;
    
    @Autowired
    private MerchantValidator merchantValidator;
    
    @Autowired
    private BannedDao bannedDao;
    
    @RequestMapping(value = "/dashboard/settings", method = RequestMethod.POST)
    public String updateSettings(@ModelAttribute("merchantModel") SettingsForm merchantModel, BindingResult bindingResult, ModelMap model, HttpServletRequest httpServletRequest){
        if(merchantModel.validate(bindingResult, merchantDao)){
            model.addAttribute("flash", FlashNotification.create(Status.SUCCESS, "Merchant details updated."));
            Merchant merchant = merchantDao.findCurrentMerchant();
            merchantModel.setAttributes(merchant);
            merchantDao.update(merchant);
        }else{
            model.addAttribute("flash", FlashNotification.create(Status.FAILURE, "There was an error updating your details."));
        }
        model.addAttribute("merchantModel", merchantModel);
        return viewSettings(model);
    }
    
    @RequestMapping(value = "/dashboard/settings", method = RequestMethod.GET)
    public String viewSettings(ModelMap model){
        if(!model.containsKey("merchantModel")){
            Merchant merchant = merchantDao.findCurrentMerchant();
            SettingsForm merchantModel = new SettingsForm();
            merchantModel.getAttributes(merchant);
            model.addAttribute("merchantModel", merchantModel);
        }
        List<MerchantType> merchantTypes = new ArrayList<MerchantType>(Arrays.asList(MerchantType.values()));
        List<CurrencyType> currencyTypes = new ArrayList<CurrencyType>(Arrays.asList(CurrencyType.values()));
        List<Country> countries = new ArrayList<Country>(Arrays.asList(Country.values()));
        model.addAttribute("merchantTypes", merchantTypes);
        model.addAttribute("currencyTypes", currencyTypes);
        model.addAttribute("countries", countries);
        return "/dashboard/settings";
    }
    @RequestMapping(value = "/dashboard/settings/discountcodes", method = RequestMethod.GET)
    public String listCoupons(@RequestParam(value = "page", required = false) Integer page, @RequestParam(value = "size", required = false) Integer size, ModelMap modelMap) {
        Integer sizeNo = this.getCouponPageSize(size);
        Integer startPage = this.getCouponStartPage(page, sizeNo);
        List<CouponType> couponTypes = new ArrayList<CouponType>(Arrays.asList(CouponType.values()));
        modelMap.addAttribute("couponTypes", couponTypes);
        modelMap.addAttribute("coupons", couponDao.findRange(startPage, sizeNo));
        modelMap.addAttribute("couponModel", new Coupon());
        modelMap.addAttribute("maxPages", this.getCouponMaxPages(sizeNo));
        return "/dashboard/settings/discountcodes";
    }
    
    @RequestMapping(value = "/dashboard/settings/discountcodes", method = RequestMethod.PUT)
    public String create(CouponForm formCoupon, BindingResult bindingResult, ModelMap modelMap) {
       formCoupon.setCouponID(0l);
       if (!formCoupon.validate(couponDao, bindingResult)) {
            modelMap.addAttribute("flash", FlashNotification.create(Status.FAILURE, "There was an error creating your coupon"));
            
            modelMap.addAttribute("openCoupID", 0);
       }else{
            Coupon coupon = new Coupon();
            formCoupon.setAttributes(coupon);
            
            
            coupon.setMerchantID(sessionUser.getUser().getMerchantId());
            couponDao.save(coupon);
            modelMap.addAttribute("openCoupID", coupon.getCouponID());
            modelMap.addAttribute("flash", FlashNotification.create(Status.SUCCESS, "Coupon Created"));
        }
        return listCoupons(null, null, modelMap);
    }
     
    @RequestMapping(value = "/dashboard/settings/discountcodes/view/{id}", method = RequestMethod.GET)
    public String show(@PathVariable("id") Long id, Model uiModel) {
        uiModel.addAttribute("coupon", couponDao.findByIDandMerchant(id));
        uiModel.addAttribute("itemId", id);
        return "/dashboard/settings/discountcodes/view";
    }
    
    @RequestMapping(value = "/dashboard/settings/discountcodes", method = RequestMethod.DELETE)
    public String deleteCoupon(@RequestParam("couponID") Long id, ModelMap model, HttpServletRequest httpServletRequest) {
        Coupon coupon = couponDao.findByIDandMerchant(id);
        if(coupon == null){
           model.addAttribute("flash", FlashNotification.create(Status.FAILURE, "This Coupon does not belong to you or is invalid."));    
        }else{
            couponDao.delete(coupon);
            model.addAttribute("flash", FlashNotification.create(Status.SUCCESS, "Coupon deleted"));
        }
        return listCoupons(null, null, model);
        
    }
    
    @RequestMapping(value = "/dashboard/settings/security", method = RequestMethod.GET)
    public String viewSecurity(ModelMap model){
        List<Banned> banned = bannedDao.findAll();
        model.addAttribute("banned", banned);
        return "/dashboard/settings/security";
    }
    
    @RequestMapping(value = "/dashboard/settings/security", method = RequestMethod.DELETE)
    public String deleteBan(@RequestParam("bannedID") Long id, ModelMap model, HttpServletRequest httpServletRequest){
        Banned ban = bannedDao.getBan(id);
        FlashNotification flash;
        if(ban == null){
           flash = FlashNotification.create(Status.FAILURE, "There was an error revoking this ban.");
           model.addAttribute("flash", flash);
        }else{
            bannedDao.delete(ban);
            flash = FlashNotification.create(Status.SUCCESS, "Ban revoked");
            model.addAttribute("flash", flash);
        }
        return viewSecurity(model);
    }
    
    @RequestMapping(value = "/dashboard/settings/security", method = RequestMethod.PUT)
    public String addBan(@RequestParam("banned_email") String email, ModelMap model, HttpServletRequest httpServletRequest){
        FlashNotification flash;
        if(bannedDao.isBanned(email)){
           flash = FlashNotification.create(Status.FAILURE, "This email is already banned.");
           model.addAttribute("flash", flash); 
        }else if(!EmailValidator.getInstance().isValid(email)){
           flash = FlashNotification.create(Status.FAILURE, "An invalid email address was entered.");
           model.addAttribute("flash", flash);
        }else{
            Banned ban = new Banned();
            ban.setIdentifier(email);
            ban.setMerchantID(sessionUser.getUser().getMerchantId());
            bannedDao.save(ban);
            flash = FlashNotification.create(Status.SUCCESS, "Ban added.");
            model.addAttribute("flash", flash);
        }
        return viewSecurity(model);
    }
    
    @RequestMapping(value = "/dashboard/settings/discountcodes", method = RequestMethod.POST)
     public String updateCoupon(@RequestParam(value = "page", required = false) Integer page, @RequestParam(value = "size", required = false) Integer size, CouponForm formCoupon, BindingResult bindingResult, ModelMap modelMap) {
       if (!formCoupon.validate(couponDao, bindingResult)) {
            modelMap.addAttribute("flash", FlashNotification.create(Status.FAILURE, "There was an error updating your coupon"));
            return listCoupons(page, size, modelMap);
        }else{
            Coupon coupon = couponDao.findByIDandMerchant(formCoupon.getCouponID());
            if(coupon == null){
                modelMap.addAttribute("flash", FlashNotification.create(Status.FAILURE, "This coupon does not exist."));
            }else{
                
                formCoupon.setAttributes(coupon);
                
                couponDao.update(coupon);
                
                modelMap.addAttribute("flash", FlashNotification.create(Status.SUCCESS, "Coupon Updated"));
            }
        }
        modelMap.addAttribute("openCoupID", formCoupon.getCouponID());
        return listCoupons(page, size, modelMap);
    }
    
    @RequestMapping(value = "/dashboard/settings/paymentoptions", method = RequestMethod.POST)
    public String updatePaymentOptions(@ModelAttribute("merchantModel") PaymentOptions merchantModel, BindingResult bindingResult, ModelMap model, HttpServletRequest httpServletRequest){
        
        if(merchantModel.validate(bindingResult, merchantDao)){
            
            model.addAttribute("flash", FlashNotification.create(Status.SUCCESS, "Merchant details updated."));
            Merchant merchant = merchantDao.findCurrentMerchant();
            
            merchantModel.setAttributes(merchant, merchant.getPlan());
             if(merchant.getPlan().getCanCOD()){
                model.addAttribute("canCod", true);
            
        }
            if(merchant.getPlan().getCanPaypal()){
                model.addAttribute("canPaypal", true);
            
        }
            merchantDao.update(merchant);
        }else{
            model.addAttribute("flash", FlashNotification.create(Status.FAILURE, "There was an error updating your details."));
        }
        model.addAttribute("merchantModel", merchantModel);
        
        return viewPaymentOptions(model);
    }
    
    @RequestMapping(value = "/dashboard/settings/paymentoptions", method = RequestMethod.GET)
    public String viewPaymentOptions(ModelMap model){
        if(!model.containsKey("merchantModel")){
            Merchant merchant = merchantDao.findCurrentMerchant();
            
            PaymentOptions merchantModel = new PaymentOptions();
            merchantModel.getAttributes(merchant);
            if(merchantModel.getPlan().getCanCOD()){
                model.addAttribute("canCod", true);
            
        }
            if(merchantModel.getPlan().getCanPaypal()){
                model.addAttribute("canPaypal", true);
            
        }
            
            model.addAttribute("merchantModel", merchantModel);
        }
        return "/dashboard/settings/paymentoptions";
    }
    
    private Integer getCouponPageSize(Integer size){
        if(size == null){
            return 10;
        }else{
            return size;
        }
    }
    
    private Integer getCouponStartPage(Integer page, Integer pageSize){
        if(page == null){
            return 0;
        }else{
            return (page - 1) * pageSize;
        }
    }
    
    private Integer getCouponMaxPages(Integer sizeNo){
        float nrOfPages = (float) couponDao.countByMerchant() / sizeNo;
        if(nrOfPages > (int) nrOfPages || nrOfPages == 0.0){
            return (int) nrOfPages + 1;
        }else{
            return (int) nrOfPages;
        }
    }
}
    
   
