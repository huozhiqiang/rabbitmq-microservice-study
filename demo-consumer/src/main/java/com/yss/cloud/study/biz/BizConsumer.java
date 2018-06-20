package com.yss.cloud.study.biz;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.stereotype.Component;

import com.yss.amqp.listener.AbstractMessageListener;

import java.security.Provider;

@Component
public class BizConsumer extends AbstractMessageListener  {
	
	Logger logger = LoggerFactory.getLogger(getClass());
	int j=0;
	
	@Override
	public void receiveMessage(Message message, MessageConverter messageConverter) {
		Object bizObj = messageConverter.fromMessage(message);
		//模仿异常，调试，当消费失败后是否会进行重新消费是否进行重发
	//    bizObj="dd";
		logger.info("get message success:"+bizObj.toString());
		System.out.println("bizObj==========="+bizObj);
		int j=1;
		if(null !=bizObj){
			j++;
			System.out.println(j+"obj====="+bizObj);
		}else{
			throw new RuntimeException();
		}
	}
}
