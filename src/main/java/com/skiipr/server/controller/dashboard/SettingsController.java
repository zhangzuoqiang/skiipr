package com.skiipr.server.controller.dashboard;

import com.skiipr.server.components.FlashNotification;
import com.skiipr.server.components.LatLongGenerator;
import com.skiipr.server.components.SessionUser;
import com.skiipr.server.enums.Country;
import com.skiipr.server.enums.CurrencyType;
import com.skiipr.server.enums.MerchantType;
import com.skiipr.server.enums.Status;
import com.skiipr.server.model.Banned;
import com.skiipr.server.model.DAO.BannedDao;
import com.skiipr.server.model.DAO.MerchantDao;
import com.skiipr.server.model.DAO.PlanDao;
import com.skiipr.server.model.LoginUser;
import com.skiipr.server.model.Merchant;
import com.skiipr.server.model.Plan;
import com.skiipr.server.model.form.MerchantDetails;
import com.skiipr.server.model.validators.MerchantValidator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
public class SettingsController {
    
    @Autowired
    private MerchantDao merchantDao;
    
    @Autowired
    private PlanDao planDao;
    
    @Autowired
    private SessionUser sessionUser;
    
    @Autowired
    private MerchantValidator merchantValidator;
    
    @Autowired
    private BannedDao bannedDao;
    
    @RequestMapping(value = "/dashboard/settings", method = RequestMethod.POST)
    public String updateSettings(@ModelAttribute("merchantModel") MerchantDetails merchantModel, BindingResult bindingResult, ModelMap model, HttpServletRequest httpServletRequest){
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
            MerchantDetails merchantModel = new MerchantDetails();
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
    
    @RequestMapping(value = "/dashboard/settings/security", method = RequestMethod.GET)
    public String viewSecurity(ModelMap model, HttpServletRequest httpServletRequest){
        if(httpServletRequest.getSession().getAttribute("flash") != null){
            FlashNotification flash = (FlashNotification) httpServletRequest.getSession().getAttribute("flash");
            model.addAttribute("flash", flash);
            httpServletRequest.getSession().removeAttribute("flash");
        }
        List<Banned> banned = bannedDao.findAll();
        model.addAttribute("banned", banned);
        return "/dashboard/settings/security";
    }
    
    @RequestMapping(value = "/dashboard/settings/security/delete-ban/{id}", method = RequestMethod.GET)
    public String deleteBan(@PathVariable("id") Long id, ModelMap model, HttpServletRequest httpServletRequest){
        Banned ban = bannedDao.getBan(id);
        FlashNotification flash;
        if(ban == null){
           flash = FlashNotification.create(Status.FAILURE, "There was an error revoking this ban.");
        }else{
            bannedDao.delete(ban);
            flash = FlashNotification.create(Status.SUCCESS, "Ban revoked");
        }
        httpServletRequest.getSession().setAttribute("flash", flash);
        return "redirect:/dashboard/settings/security";
    }
}