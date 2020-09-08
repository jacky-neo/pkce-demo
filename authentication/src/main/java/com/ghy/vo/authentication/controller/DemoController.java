package com.ghy.vo.authentication.controller;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class DemoController {


    @RequestMapping("/res1/showData")
    public Object showData(){
        Map<String,String> map = new HashMap<String,String>();
        map.put("t1","this is test1");
        map.put("t2","this is test2");
        return map;
    }


    @RequestMapping(value = "/res/showData")
    public Object showData2(){
        Map<String,String> map = new HashMap<String,String>();
        map.put("t1","this is test1");
        map.put("t2","this is test2");
        return map;
    }

}
