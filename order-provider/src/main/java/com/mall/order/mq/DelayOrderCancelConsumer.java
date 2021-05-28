package com.mall.order.mq;
/*
      延迟消息的消费者，会在指定的延迟时间之后，消费到这条消息：
      1. message中包含了订单id，获取延迟消息中的订单id
      2. 去数据库，查询订单状态，有没有被成功支付方，如果已支付对已支付的订单什么都不做
      3. 如果，发现该orderId对应的订单，没有被支付，此时，我们的消费逻辑就是取消订单
         a. 修改数据库中，已经存储的订单信息，将其状态改为已取消 ORDER_STATUS_TRANSACTION_CANCEL=
         b. 将订单中的每一个商品对应的锁定库存，在库存表中，还原
         c. 在订单条目表中，将取消订单中的每一个商品条目的库存状态信息改为 2
 */

import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.common.message.MessageExt;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DelayOrderCancelConsumer {


    DefaultMQPushConsumer delayCancelOrderConsumer;

    public void init() {

        delayCancelOrderConsumer
                = new DefaultMQPushConsumer("delay_cancel_order_consumer");

        delayCancelOrderConsumer.setNamesrvAddr("127.0.0.1:9876");

        try {
            delayCancelOrderConsumer.subscribe(DelayOrderCancelProducer.DELAY_CANCEL_TOPIC,"*");

            delayCancelOrderConsumer.registerMessageListener(new MessageListenerConcurrently() {
                @Override
                public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgs, ConsumeConcurrentlyContext context) {
                    // 执行延迟取消订单的业务逻辑
                    return null;
                }
            });

            // 启动消费者
            delayCancelOrderConsumer.start();
        } catch (MQClientException e) {
            e.printStackTrace();
        }

    }



}
