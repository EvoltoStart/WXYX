package com.wx.YX.order.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wx.YX.activity.client.ActivityFenignClient;
import com.wx.YX.cart.client.CartFeignClient;
import com.wx.YX.client.product.ProductFeignClient;
import com.wx.YX.client.user.UserFeignClient;
import com.wx.YX.common.auth.AuthContextHolder;
import com.wx.YX.common.constant.RedisConst;
import com.wx.YX.common.exception.yxException;
import com.wx.YX.common.result.ResultCodeEnum;
import com.wx.YX.common.utils.DateUtil;
import com.wx.YX.enums.*;
import com.wx.YX.model.activity.ActivityRule;
import com.wx.YX.model.activity.CouponInfo;
import com.wx.YX.model.order.CartInfo;
import com.wx.YX.model.order.OrderInfo;
import com.wx.YX.model.order.OrderItem;
import com.wx.YX.mq.constant.MqConst;
import com.wx.YX.mq.sercvice.RabbitService;
import com.wx.YX.order.mapper.OrderInfoMapper;
import com.wx.YX.order.mapper.OrderItemMapper;
import com.wx.YX.order.service.OrderInfoService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wx.YX.order.service.OrderItemService;
import com.wx.YX.vo.order.CartInfoVo;
import com.wx.YX.vo.order.OrderConfirmVo;
import com.wx.YX.vo.order.OrderSubmitVo;
import com.wx.YX.vo.order.OrderUserQueryVo;
import com.wx.YX.vo.product.SkuStockLockVo;
import com.wx.YX.vo.user.LeaderAddressVo;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
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
    @Autowired
    private ProductFeignClient productFeignClient;
    @Autowired
    private OrderItemService orderItemService;
    @Autowired
    private RabbitService rabbitService;
    @Autowired
    private OrderItemMapper orderItemMapper;


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
            Boolean isLockSuccess = productFeignClient.checkAndLock(commonStockLockVoList, orderNo);
            if(isLockSuccess){
               //库存锁定失败
               throw new yxException(ResultCodeEnum.ORDER_STOCK_FALL);
            }

        }

        //下单
        //向两张表添加数据，order_info,order_item
        Long orderId= this.saveOrder(orderParamVo,cartInfoList);
        //下单完成，删除购物车记录
        //发送mq消息
        rabbitService.sendMessage(MqConst.EXCHANGE_ORDER_DIRECT,
                      MqConst.ROUTING_DELETE_CART,orderParamVo.getUserId());
        return orderId;
    }

    @Transactional(rollbackFor = {Exception.class})//事务，要么都成功，要么都失败
    public Long saveOrder(OrderSubmitVo orderParamVo, List<CartInfo> cartInfoList) {
        if(CollectionUtils.isEmpty(cartInfoList)){
            throw new yxException(ResultCodeEnum.DATA_ERROR);
        }
        //查询用户提货点和团长信息\
        Long userId= AuthContextHolder.getUserId();
        LeaderAddressVo leaderAddressVo = userFeignClient.getUserAddressByUserId(userId);
        if(leaderAddressVo==null){
            throw new yxException(ResultCodeEnum.DATA_ERROR);
        }
        //计算金额
        //营销活动，优惠券金额
        Map<String, BigDecimal> activitySplitAmount = this.computeActivitySplitAmount(cartInfoList);
        //优惠券金额
        Map<String, BigDecimal> couponInfoSplitAmount = this.computeCouponInfoSplitAmount(cartInfoList, orderParamVo.getCouponId());
        //封装订单项数据
        List<OrderItem> orderItemList=new ArrayList<>();
        for (CartInfo cartInfo : cartInfoList) {
            OrderItem orderItem = new OrderItem();
            orderItem.setId(null);
            orderItem.setCategoryId(cartInfo.getCategoryId());
            if(cartInfo.getSkuType() == SkuType.COMMON.getCode()) {
                orderItem.setSkuType(SkuType.COMMON);
            } else {
                orderItem.setSkuType(SkuType.SECKILL);
            }
            orderItem.setSkuId(cartInfo.getSkuId());
            orderItem.setSkuName(cartInfo.getSkuName());
            orderItem.setSkuPrice(cartInfo.getCartPrice());
            orderItem.setImgUrl(cartInfo.getImgUrl());
            orderItem.setSkuNum(cartInfo.getSkuNum());
            orderItem.setLeaderId(orderParamVo.getLeaderId());
            //营销活动金额
            BigDecimal activityAmount = activitySplitAmount.get("activity:" + orderItem.getSkuId());
            if(activityAmount==null){
                activityAmount=new BigDecimal(0);
            }
            orderItem.setSplitActivityAmount(activityAmount);
            //优惠券金额
            BigDecimal couponAmount = couponInfoSplitAmount.get("coupon:" + orderItem.getSkuId());
            if(couponAmount==null){
                couponAmount=new BigDecimal(0);
            }
            orderItem.setSplitCouponAmount(couponAmount);
            //总金额
            BigDecimal skuTotalAmount = orderItem.getSkuPrice().multiply(new BigDecimal(orderItem.getSkuNum()));
            //优惠之后金额
            BigDecimal splitTotalAmount = skuTotalAmount.subtract(activityAmount).subtract(couponAmount);
            orderItem.setSplitTotalAmount(splitTotalAmount);


            orderItemList.add(orderItem);
        }
        //封装订单部分
        OrderInfo orderInfo=new OrderInfo();
        orderInfo.setUserId(userId);
        orderInfo.setOrderNo(orderParamVo.getOrderNo());
        orderInfo.setOrderStatus(OrderStatus.UNPAID);
        orderInfo.setLeaderId(orderParamVo.getLeaderId());
        orderInfo.setLeaderName(leaderAddressVo.getLeaderName());
        orderInfo.setLeaderPhone(leaderAddressVo.getLeaderPhone());
        orderInfo.setTakeName(leaderAddressVo.getTakeName());
        orderInfo.setReceiverName(orderParamVo.getReceiverName());
        orderInfo.setReceiverPhone(orderParamVo.getReceiverPhone());
        orderInfo.setReceiverProvince(leaderAddressVo.getProvince());
        orderInfo.setReceiverCity(leaderAddressVo.getCity());
        orderInfo.setReceiverDistrict(leaderAddressVo.getDistrict());
        orderInfo.setReceiverAddress(leaderAddressVo.getDetailAddress());
        orderInfo.setWareId(cartInfoList.get(0).getWareId());
        orderInfo.setProcessStatus(ProcessStatus.UNPAID);

        //计算订单金额
        BigDecimal originalTotalAmount = this.computeTotalAmount(cartInfoList);
        BigDecimal activityAmount = activitySplitAmount.get("activity:total");
        if(null == activityAmount) activityAmount = new BigDecimal(0);
        BigDecimal couponAmount = couponInfoSplitAmount.get("coupon:total");
        if(null == couponAmount) couponAmount = new BigDecimal(0);
        BigDecimal totalAmount = originalTotalAmount.subtract(activityAmount).subtract(couponAmount);
        //计算订单金额
        orderInfo.setOriginalTotalAmount(originalTotalAmount);
        orderInfo.setActivityAmount(activityAmount);
        orderInfo.setCouponAmount(couponAmount);
        orderInfo.setTotalAmount(totalAmount);

        //计算团长佣金
        BigDecimal profitRate = new BigDecimal(0);
        BigDecimal commissionAmount = orderInfo.getTotalAmount().multiply(profitRate);
        orderInfo.setCommissionAmount(commissionAmount);
        //添加数据到顶单基本信息表
        baseMapper.insert(orderInfo);

        //保存订单项
        for(OrderItem orderItem : orderItemList) {
            orderItem.setOrderId(orderInfo.getId());
        }
        orderItemService.saveBatch(orderItemList);

        //更新优惠券使用状态
        if(null != orderInfo.getCouponId()) {
            activityFenignClient.updateCouponInfoUseStatus(orderInfo.getCouponId(), userId, orderInfo.getId());
        }

        //下单成功，记录用户商品购买个数,redis
        String orderSkuKey = RedisConst.ORDER_SKU_MAP + orderParamVo.getUserId();
        BoundHashOperations<String, String, Integer> hashOperations = redisTemplate.boundHashOps(orderSkuKey);
        cartInfoList.forEach(cartInfo -> {
            if(hashOperations.hasKey(cartInfo.getSkuId().toString())) {
                Integer orderSkuNum = hashOperations.get(cartInfo.getSkuId().toString()) + cartInfo.getSkuNum();
                hashOperations.put(cartInfo.getSkuId().toString(), orderSkuNum);
            }
        });
        redisTemplate.expire(orderSkuKey, DateUtil.getCurrentExpireTimes(), TimeUnit.SECONDS);

        //发送订单id
        return orderInfo.getId();

    }

    private BigDecimal computeTotalAmount(List<CartInfo> cartInfoList) {
        BigDecimal total = new BigDecimal(0);
        for (CartInfo cartInfo : cartInfoList) {
            BigDecimal itemTotal = cartInfo.getCartPrice().multiply(new BigDecimal(cartInfo.getSkuNum()));
            total = total.add(itemTotal);
        }
        return total;
    }

    /**
     * 计算购物项分摊的优惠减少金额
     * 打折：按折扣分担
     * 现金：按比例分摊
     * @param cartInfoParamList
     * @return
     */
    private Map<String, BigDecimal> computeActivitySplitAmount(List<CartInfo> cartInfoParamList) {
        Map<String, BigDecimal> activitySplitAmountMap = new HashMap<>();

        //促销活动相关信息
        List<CartInfoVo> cartInfoVoList =activityFenignClient.findCartActivityList(cartInfoParamList);

        //活动总金额
        BigDecimal activityReduceAmount = new BigDecimal(0);
        if(!CollectionUtils.isEmpty(cartInfoVoList)) {
            for(CartInfoVo cartInfoVo : cartInfoVoList) {
                ActivityRule activityRule = cartInfoVo.getActivityRule();
                List<CartInfo> cartInfoList = cartInfoVo.getCartInfoList();
                if(null != activityRule) {
                    //优惠金额， 按比例分摊
                    BigDecimal reduceAmount = activityRule.getReduceAmount();
                    activityReduceAmount = activityReduceAmount.add(reduceAmount);
                    if(cartInfoList.size() == 1) {
                        activitySplitAmountMap.put("activity:"+cartInfoList.get(0).getSkuId(), reduceAmount);
                    } else {
                        //总金额
                        BigDecimal originalTotalAmount = new BigDecimal(0);
                        for(CartInfo cartInfo : cartInfoList) {
                            BigDecimal skuTotalAmount = cartInfo.getCartPrice().multiply(new BigDecimal(cartInfo.getSkuNum()));
                            originalTotalAmount = originalTotalAmount.add(skuTotalAmount);
                        }
                        //记录除最后一项是所有分摊金额， 最后一项=总的 - skuPartReduceAmount
                        BigDecimal skuPartReduceAmount = new BigDecimal(0);
                        if (activityRule.getActivityType() == ActivityType.FULL_REDUCTION) {
                            for(int i=0, len=cartInfoList.size(); i<len; i++) {
                                CartInfo cartInfo = cartInfoList.get(i);
                                if(i < len -1) {
                                    BigDecimal skuTotalAmount = cartInfo.getCartPrice().multiply(new BigDecimal(cartInfo.getSkuNum()));
                                    //sku分摊金额
                                    BigDecimal skuReduceAmount = skuTotalAmount.divide(originalTotalAmount, 2, RoundingMode.HALF_UP).multiply(reduceAmount);
                                    activitySplitAmountMap.put("activity:"+cartInfo.getSkuId(), skuReduceAmount);

                                    skuPartReduceAmount = skuPartReduceAmount.add(skuReduceAmount);
                                } else {
                                    BigDecimal skuReduceAmount = reduceAmount.subtract(skuPartReduceAmount);
                                    activitySplitAmountMap.put("activity:"+cartInfo.getSkuId(), skuReduceAmount);
                                }
                            }
                        } else {
                            for(int i=0, len=cartInfoList.size(); i<len; i++) {
                                CartInfo cartInfo = cartInfoList.get(i);
                                if(i < len -1) {
                                    BigDecimal skuTotalAmount = cartInfo.getCartPrice().multiply(new BigDecimal(cartInfo.getSkuNum()));

                                    //sku分摊金额
                                    BigDecimal skuDiscountTotalAmount = skuTotalAmount.multiply(activityRule.getBenefitDiscount().divide(new BigDecimal("10")));
                                    BigDecimal skuReduceAmount = skuTotalAmount.subtract(skuDiscountTotalAmount);
                                    activitySplitAmountMap.put("activity:"+cartInfo.getSkuId(), skuReduceAmount);

                                    skuPartReduceAmount = skuPartReduceAmount.add(skuReduceAmount);
                                } else {
                                    BigDecimal skuReduceAmount = reduceAmount.subtract(skuPartReduceAmount);
                                    activitySplitAmountMap.put("activity:"+cartInfo.getSkuId(), skuReduceAmount);
                                }
                            }
                        }
                    }
                }
            }
        }
        activitySplitAmountMap.put("activity:total", activityReduceAmount);
        return activitySplitAmountMap;
    }

    private Map<String, BigDecimal> computeCouponInfoSplitAmount(List<CartInfo> cartInfoList, Long couponId) {
        Map<String, BigDecimal> couponInfoSplitAmountMap = new HashMap<>();

        if(null == couponId) return couponInfoSplitAmountMap;
        CouponInfo couponInfo = activityFenignClient.findRangeSkuIdList(cartInfoList, couponId);

        if(null != couponInfo) {
            //sku对应的订单明细
            Map<Long, CartInfo> skuIdToCartInfoMap = new HashMap<>();
            for (CartInfo cartInfo : cartInfoList) {
                skuIdToCartInfoMap.put(cartInfo.getSkuId(), cartInfo);
            }
            //优惠券对应的skuId列表
            List<Long> skuIdList = couponInfo.getSkuIdList();
            if(CollectionUtils.isEmpty(skuIdList)) {
                return couponInfoSplitAmountMap;
            }
            //优惠券优化总金额
            BigDecimal reduceAmount = couponInfo.getAmount();
            if(skuIdList.size() == 1) {
                //sku的优化金额
                couponInfoSplitAmountMap.put("coupon:"+skuIdToCartInfoMap.get(skuIdList.get(0)).getSkuId(), reduceAmount);
            } else {
                //总金额
                BigDecimal originalTotalAmount = new BigDecimal(0);
                for (Long skuId : skuIdList) {
                    CartInfo cartInfo = skuIdToCartInfoMap.get(skuId);
                    BigDecimal skuTotalAmount = cartInfo.getCartPrice().multiply(new BigDecimal(cartInfo.getSkuNum()));
                    originalTotalAmount = originalTotalAmount.add(skuTotalAmount);
                }
                //记录除最后一项是所有分摊金额， 最后一项=总的 - skuPartReduceAmount
                BigDecimal skuPartReduceAmount = new BigDecimal(0);
                if (couponInfo.getCouponType() == CouponType.CASH || couponInfo.getCouponType() == CouponType.FULL_REDUCTION) {
                    for(int i=0, len=skuIdList.size(); i<len; i++) {
                        CartInfo cartInfo = skuIdToCartInfoMap.get(skuIdList.get(i));
                        if(i < len -1) {
                            BigDecimal skuTotalAmount = cartInfo.getCartPrice().multiply(new BigDecimal(cartInfo.getSkuNum()));
                            //sku分摊金额
                            BigDecimal skuReduceAmount = skuTotalAmount.divide(originalTotalAmount, 2, RoundingMode.HALF_UP).multiply(reduceAmount);
                            couponInfoSplitAmountMap.put("coupon:"+cartInfo.getSkuId(), skuReduceAmount);

                            skuPartReduceAmount = skuPartReduceAmount.add(skuReduceAmount);
                        } else {
                            BigDecimal skuReduceAmount = reduceAmount.subtract(skuPartReduceAmount);
                            couponInfoSplitAmountMap.put("coupon:"+cartInfo.getSkuId(), skuReduceAmount);
                        }
                    }
                }
            }
            couponInfoSplitAmountMap.put("coupon:total", couponInfo.getAmount());
        }
        return couponInfoSplitAmountMap;
    }

    @Override
    public OrderInfo getOrderInfoById(Long orderId) {
        //根据orderId查询订单基本信息
        OrderInfo  orderInfo=baseMapper.selectById(orderId);
        //根据orderI查询订单所有订单项list集合
        List<OrderItem> orderItemList=orderItemMapper.selectList(
                new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderId, orderInfo.getId()));

        orderInfo.setOrderItemList(orderItemList);
        return orderInfo;
    }

    //根据orderNo查询订单信息
    @Override
    public OrderInfo getOrderInfoByOrderNo(String orderNo) {
        OrderInfo orderInfo = baseMapper.selectOne(new LambdaQueryWrapper<OrderInfo>().eq(OrderInfo::getOrderNo, orderNo));
        return orderInfo;
    }

    @Override
    public void orderPay(String orderNo) {
        //查询订单状态是否已经修改完成支付状态
        OrderInfo orderInfo = this.getOrderInfoByOrderNo(orderNo);
        if(orderInfo==null||orderInfo.getOrderStatus()!=OrderStatus.UNPAID){
            return;
        }
        //更新状态
        this.updateOrderStatus(orderInfo.getId());

        //扣减库存
        rabbitService.sendMessage(MqConst.EXCHANGE_ORDER_DIRECT,MqConst.ROUTING_MINUS_STOCK,orderNo);
    }


    //分页查询
    @Override
    public IPage<OrderInfo> findUserOrderPage(Page<OrderInfo> pageParam, OrderUserQueryVo orderUserQueryVo) {
       LambdaQueryWrapper<OrderInfo> wrapper=new LambdaQueryWrapper<>();
       wrapper.eq(OrderInfo::getUserId, orderUserQueryVo.getUserId());
       wrapper.eq(OrderInfo::getOrderStatus, orderUserQueryVo.getOrderStatus());
        Page<OrderInfo> pageModel = baseMapper.selectPage(pageParam, wrapper);
        //获取每个订单把每个订单里订单项查询封装
        List<OrderInfo> orderInfoList=pageModel.getRecords();
        for (OrderInfo orderInfo:orderInfoList) {
            //根据订单id查询所有订单项列表
            List<OrderItem> orderItemList = orderItemMapper.selectList(
                    new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderId, orderInfo.getId())
            );
            //把订单项集合封装到每个订单里
            orderInfo.setOrderItemList(orderItemList);
            //封装订单状态名称
            orderInfo.getParam().put("orderStatusName", orderInfo.getOrderStatus());
        }
        return pageModel;
    }

    private void updateOrderStatus(Long id) {
        OrderInfo orderInfo = baseMapper.selectById(id);
        orderInfo.setOrderStatus(OrderStatus.WAITING_DELEVER);
        orderInfo.setProcessStatus(ProcessStatus.WAITING_DELEVER);
        baseMapper.updateById(orderInfo);
    }
}
