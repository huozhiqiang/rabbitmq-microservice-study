package com.yss.cloud.study.rest;


import com.yss.cloud.study.entity.Provider;
import com.yss.cloud.study.service.ProducerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("producerRest")
public class ProducerRest {

    @Autowired
    ProducerService producerService;

    @PostMapping(value = "")
    public String  test11(@RequestBody Provider provider){
           producerService.test(provider);
           return "sucess";
    }
}
