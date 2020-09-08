package com.ghy.vo.ui.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class IndexController {
	
    @GetMapping(value={"/test","","/"})
    public String index(Model model) {
        return "test";
    }
   

}