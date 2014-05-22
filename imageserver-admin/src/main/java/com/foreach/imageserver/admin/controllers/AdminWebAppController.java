package com.foreach.imageserver.admin.controllers;

import com.foreach.across.modules.adminweb.controllers.AdminWebController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@AdminWebController
public class AdminWebAppController {

    @RequestMapping
    public String loadApp() {
        return "th/imageserver/admin/app";
    }

    @RequestMapping("{view}")
    public String view( @PathVariable("view") String path ) {
        return "th/imageserver/admin/" + path;
    }

    @RequestMapping("/upload")
    public String showUpload() {
        return "th/imageserver/admin/upload";
    }

    @RequestMapping("/view")
    public String seeImage() {
        return "th/imageserver/admin/view";
    }
}
