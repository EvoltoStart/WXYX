package com.wx.YX.receiver;

import com.rabbitmq.client.Channel;
import com.wx.YX.mq.constant.MqConst;
import com.wx.YX.service.CartInfoService;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CartReceiver {

    @Autowired
    private CartInfoService cartInfoService;


    /**
     * 删除购物车选项
     * @throws IOException
     */
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = MqConst.QUEUE_DELETE_CART, durable = "true"),
            exchange = @Exchange(value = MqConst.EXCHANGE_ORDER_DIRECT),
            key = {MqConst.ROUTING_DELETE_CART}
    ))
    public void deleteCart(Long userId, Message message, Channel channel) throws IOException {
        if (null != userId){
            // 获取用户id和skuIdList
            cartInfoService.deleteCartChecked(userId);
        }
        channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
    }
}