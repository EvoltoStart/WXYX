package com.wx.YX.order.service.impl;

import com.wx.YX.activity.client.ActivityFenignClient;
import com.wx.YX.cart.client.CartFeignClient;
import com.wx.YX.client.user.UserFeignClient;
import com.wx.YX.common.auth.AuthContextHolder;
import com.wx.YX.common.constant.RedisConst;
import com.wx.YX.common.exception.yxException;
import com.wx.YX.common.result.ResultCodeEnum;
import com.wx.YX.enums.SkuType;
import com.wx.YX.model.order.CartInfo;
import com.wx.YX.model.order.OrderInfo;
import com.wx.YX.order.mapper.OrderInfoMapper;
import com.wx.YX.order.service.OrderInfoService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wx.YX.vo.order.OrderConfirmVo;
import com.wx.YX.vo.order.OrderSubmitVo;
import com.wx.YX.vo.product.SkuStockLockVo;
import com.wx.YX.vo.user.LeaderAddressVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * <p>
 * 订单 服务实现类
 * </p>
 *
 * @author meng
 * @since 2024-08-12
 */
@Service
public class OrderInfoServiceImpl extends ServiceImpl<OrderInfoMapper, OrderInfo> implements OrderInfoService {
    @Autowired
    private UserFeignClient userFeignClient;
    @Autowired
    private CartFeignClient cartFeignClient;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private ActivityFenignClient activityFenignClient;

    @Override
    public OrderConfirmVo confirmOrder() {
        //获取用户id
        Long userId= AuthContextHolder.getUserId();
        //获取用户对应团长信息
        LeaderAddressVo leaderAddressVo = userFeignClient.getUserAddressByUserId(userId);

        //获取购物车选中商品
        List<CartInfo> cartInfoList=cartFeignClient.getCartCheckedList(userId);

        //订单唯一标识
        String s = "";
        String orderNo=System.currentTimeMillis()+ s;
        redisTemplate.opsForValue().set(RedisConst.ORDER_REPEAT+orderNo,orderNo,24, TimeUnit.HOURS);
        //获取购物车满足条件活动和优惠卷信息
        OrderConfirmVo orderConfirmVo=activityFenignClient.findCartActivityAndCoupon(cartInfoList,userId);


        //封装其他值
        orderConfirmVo.setOrderNo(orderNo);
        orderConfirmVo.setLeaderAddressVo(leaderAddressVo);


        return orderConfirmVo;
    }

    @Override
    public Long submitOrder(OrderSubmitVo orderParamVo) {
        //设置给哪个用户生成订单，设置orderParamVo的userId
        Long userId= AuthContextHolder.getUserId();
        orderParamVo.setUserId(userId);

        //订单不能重复提交，重复提交验证
        //通过redis+lua脚本进行判断
        //lua脚本保证原子性操作
        String orderNo=orderParamVo.getOrderNo();
        if(StringUtils.isEmpty(orderNo)){
            throw new yxException(ResultCodeEnum.DATA_ERROR);
        }

        //拿orderNo到redis查询 (get查询，如果有删除返回1，没有返回0)
        //redis官方提供lua脚本，解决分布式锁
        String script="if(redis.call('get', KEYS[1]) == ARGV[1]) then return redis.call('del', KEYS[1]) else return 0 end";
        //如何redis有相同orderNo，表示正常提交订单，把redis的orderNo删除（用户下单，页面没反应，然后又点击下单）
        Boolean flag =(Boolean) redisTemplate.execute(new DefaultRedisScript(script, Boolean.class),
                                            Arrays.asList(RedisConst.ORDER_REPEAT + orderNo), orderNo);
        //如果么没有相同orderNo，表示重复提交，不能再往后进行
        if(!flag){
            throw new yxException(ResultCodeEnum.REPEAT_SUBMIT);
        }

        //验证库存，并且锁定库存
        //库存充足，库存锁定（如要买2西红柿，有西红柿将2锁定，，没有真正减库存，如用户不买一段时间解锁）
        //1、获取当前用户购物车商品（选中商品）、
        List<CartInfo> cartInfoList = cartFeignClient.getCartCheckedList(userId);
        //2、商品类型不同，重点处理普通类型商品
        List<CartInfo> commonSkuList = cartInfoList.stream().filter(cartInfo -> cartInfo.getSkuType() == SkuType.COMMON.getCode()).collect(Collectors.toList());
        //3、把获取购物车普通商品list集合，转换list<SkuStockLockVo>
        if(!CollectionUtils.isEmpty(commonSkuList)){
            List<SkuStockLockVo> commonStockLockVoList = commonSkuList.stream().map(item -> {

                SkuStockLockVo skuStockLockVo = new SkuStockLockVo();
                skuStockLockVo.setSkuId(item.getSkuId());
                skuStockLockVo.setSkuNum(item.getSkuNum());
                return skuStockLockVo;
            }).collect(Collectors.toList());

            //4、远程调用service-product实现锁定商品
            //验证库存并锁定，保证原子性

        }





        //下单
        return null;
    }

    @Override
    public OrderInfo getOrderInfoById(Long orderId) {
        return null;
    }
}
