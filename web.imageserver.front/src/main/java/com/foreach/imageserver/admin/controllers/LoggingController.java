package com.foreach.imageserver.admin.controllers;


import com.foreach.imageserver.admin.viewHelpers.LogsViewHelper;
import com.foreach.imageserver.business.image.VariantImage;
import com.foreach.imageserver.dao.selectors.VariantImageSelector;
import com.foreach.imageserver.services.VariantImageService;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import java.util.Date;
import java.util.List;

@Controller
@RequestMapping( "/log" )
public class LoggingController {

    @Autowired
    private VariantImageService variantImageService;

    @RequestMapping(value = "/list", method = RequestMethod.GET)
	public final ModelAndView showHomePage()
	{
        //logs of last 1 hour
        Date fromLastHour = DateUtils.addHours(new Date(), -1);
        VariantImageSelector selector = VariantImageSelector.onCalledAfterThisDate( fromLastHour );

        List<VariantImage> variantImages = variantImageService.getVariantImages( selector );

        LogsViewHelper logs = new LogsViewHelper(variantImages);

        ModelAndView mav = new ModelAndView("log/list");

        mav.addObject("logs", logs);

		return mav;
	}
}
