# 用法
## 项目结构说明
 1、 demo-erueka  服务注册中心
 
 2、 config-server-eureka 配置服务
 
 3、coolmq  实际包

 4、demo-provider  生产者服务

 5、demo-consumer  消费者服务

## 一、安装软件：
   1.rabbitMQ消息中间件(注意：安装mq之前先安装erlang,原因在于RabbitMQ服务端代码是使用并发式语言erlang编写的)
   2.Redis(这里redis的目的是保存消费次数)


## 二、Mq消息机制：
### 1、mq消息机制几个重要的组件：
    1）、生产者：生产者作为消息的发送体，一般是一个本地业务服务，当执行完业务逻辑以后发送消息。
    2）、转发器（exchange）：生产者发送的消息一般不会直接交给消费者队列，而是用转发器作为中转，生产者首先将消息发送到转发器，转发器根据路由规则决定将消息发送到哪个队列。
    3）、消费者：根据配置的转发器去指定的对列消费生产者发送的消息。

##  三、rabbitMQ处理事务的机制：
###  1、rabbitMQ处理事务采用最终一致性：
     最终一致性处理主要有以下三个模块：
     1）、上游应用，执行业务并发送 MQ 消息。
     2）、MQ 消息组件，协调上下游消息的传递，并确保上下游数据的一致性。
     3）、下游应用，监听 MQ 的消息并执行自身业务。
###  2、消息消费失败处理方式：
        当下游消息消费失败后，mq设有消息重试机制，当多次消费都失败后，消息会进入死信队列，然后通过短信或邮件形式通知用户消费失败。用户则对上游已经完成的业务进行人工处理。

## 四、 两阶段确认
### 1 发送确认
发送确认用来确保消息是否已送达消息队列。

发送端会维护一个ack回调，来监听消息是否送达消息队列。对于无法被路由的消息，一旦没法找到一个队列来消费它，就会触发确认无法消费，此时ack＝false。对于可以被路由的消息，当消息被queue接受时，会触发ack=true;对于设置了持久化（persistent）的消息，当消息成功的持久化到硬盘上才会触发；对于设置了镜像（mirror）的消息，那么是当所有的mirror接受到这个消息。

### 2 消费确认（Delivery Acknowledgements）
消费者若成功的消费了消息，同样会给消息队列返回一个消费成功的确认消息。
一旦有消费者成功注册到相应的消息服务，消息将会被消息服务通过basic.deliver推（push）给消费者，此时消息会包含一个deliver tag用来唯一的标识消息。如果此时是手动模式，就需要手动的确认消息已经被成功消费，否则消息服务将会重发消息（因为消息已经持久化到了硬盘上，所以无论消息服务是不是可能挂掉，都会重发消息）。而且必须确认，无论是成功或者失败，否则会引起非常严重的问题

## 五、 可能出现的异常情况
我们来看看可能发送异常的四种
### 1 直接无法到达消息服务
网络断了，抛出异常，业务直接回滚即可。如果出现connection closed错误，直接增加 connection数即可

   connectionFactory.setChannelCacheSize(100);
### 2 消息已经到达服务器，但返回的时候出现异常
rabbitmq提供了确认ack机制，可以用来确认消息是否有返回。因此我们可以在发送前在db中(内存或关系型数据库)先存一下消息，如果ack异常则进行重发

    /**confirmcallback用来确认消息是否有送达消息队列*/     
    rabbitTemplate.setConfirmCallback((correlationData, ack, cause) -> {
        if (!ack) {
            //try to resend msg
        } else {
            //delete msg in db
        }
    });
     /**若消息找不到对应的Exchange会先触发returncallback */
     rabbitTemplate.setReturnCallback((message, replyCode, replyText, tmpExchange, tmpRoutingKey) -> {
        try {
            Thread.sleep(Constants.ONE_SECOND);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    
        log.info("send message failed: " + replyCode + " " + replyText);
        rabbitTemplate.send(message);
    });

如果消息没有到exchange,则confirm回调,ack=false
如果消息到达exchange,则confirm回调,ack=true
但如果是找不到exchange，则会先触发returncallback

### 3 消息送达后，消息服务自己挂了
如果设置了消息持久化，那么ack= true是在消息持久化完成后，就是存到硬盘上之后再发送的，确保消息已经存在硬盘上，万一消息服务挂了，消息服务恢复是能够再重发消息

### 4 未送达消费者
消息服务收到消息后，消息会处于"UNACK"的状态，直到客户端确认消息

      channel.basicQos(1); // accept only one unack-ed message at a time (see below)
      final Consumer consumer = new DefaultConsumer(channel) {
        @Override
        public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
          String message = new String(body, "UTF-8");
    
          System.out.println(" [x] Received '" + message + "'");
          try {
            doWork(message);
          } finally {
             //确认收到消息
            channel.basicAck(envelope.getDeliveryTag(), false);
          }
        }
      };
    boolean autoAck = false;
    channel.basicConsume(TASK_QUEUE_NAME, autoAck, consumer);

### 5 确认消息丢失
消息返回时假设确认消息丢失了，那么消息服务会重发消息。注意，如果你设置了autoAck= false，但又没应答 channel.baskAck也没有应答 channel.baskNack，那么会导致非常严重的错误：消息队列会被堵塞住，可参考http://blog.sina.com.cn/s/blog_48d4cf2d0102w53t.html 所以，无论如何都必须应答

