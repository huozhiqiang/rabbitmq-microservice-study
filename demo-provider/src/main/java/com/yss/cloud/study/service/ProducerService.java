package com.yss.cloud.study.service;

import com.yss.amqp.annotation.MqSender;
import com.yss.amqp.util.MQConstants;
import com.yss.cloud.study.entity.Provider;
import com.yss.cloud.study.mapper.ProviderMapper;
import com.yss.ms.common.biz.BusinessBiz;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProducerService  extends BusinessBiz<ProviderMapper,Provider> {

    @MqSender(exchange = MQConstants.BUSINESS_EXCHANGE,routingkey= MQConstants.BUSINESS_KEY)
    @Transactional(rollbackFor = Exception.class)
    public Provider test(Provider provider){
        Provider provider1=null;
        if(null!=provider.getName()){
            System.out.println("新增一条数据");
            bizMapper.insertSelective(provider);
            System.out.println("插入成功");
             provider1=bizMapper.selectAll().get(0);

        }else{
           throw  new RuntimeException();
        }
        return provider1;
    }
}
