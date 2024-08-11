package com.wx.YX.activity.service.impl;


import com.alibaba.nacos.common.utils.StringUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wx.YX.activity.mapper.ActivityInfoMapper;
import com.wx.YX.activity.mapper.ActivityRuleMapper;
import com.wx.YX.activity.mapper.ActivitySkuMapper;
import com.wx.YX.activity.service.ActivityInfoService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wx.YX.activity.service.CouponInfoService;
import com.wx.YX.client.product.ProductFeignClient;
import com.wx.YX.enums.ActivityType;
import com.wx.YX.model.activity.ActivityInfo;
import com.wx.YX.model.activity.ActivityRule;
import com.wx.YX.model.activity.ActivitySku;
import com.wx.YX.model.activity.CouponInfo;
import com.wx.YX.model.order.CartInfo;
import com.wx.YX.model.product.SkuInfo;
import com.wx.YX.vo.activity.ActivityRuleVo;
import com.wx.YX.vo.order.CartInfoVo;
import com.wx.YX.vo.order.OrderConfirmVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 * 活动表 服务实现类
 * </p>
 *
 * @author meng
 * @since 2024-08-01
 */
@Service
public class ActivityInfoServiceImpl extends ServiceImpl<ActivityInfoMapper, ActivityInfo> implements ActivityInfoService {

    @Autowired
    private ActivityInfoMapper activityInfoMapper;

    @Autowired
    private ActivityRuleMapper activityRuleMapper;

    @Autowired
    private ActivitySkuMapper activitySkuMapper;

    @Autowired
    private ProductFeignClient productFeignClient;
    @Autowired
    private CouponInfoService couponInfoService;

    @Override
    public IPage<ActivityInfo> selectPage(Page<ActivityInfo> pageParam) {
        //列表
        IPage<ActivityInfo> pageModel = baseMapper.selectPage(pageParam,null);

        //分页查询对象获取列表数据
        List<ActivityInfo> activityInfoList=pageModel.getRecords();

        //遍历list，得到每个activityInfo对象，向activityinfo对象封装活动类型打牌actiivitytypestring属性里
        activityInfoList.stream().forEach(item->{
            item.setActivityTypeString(item.getActivityType().getComment());
        });

        return pageModel;
    }

    //活动规则列表方法
    @Override
    public Map<String, Object> findActivityRuleList(Long activityId) {
        Map<String, Object> result = new HashMap<>();

        QueryWrapper queryWrapper = new QueryWrapper<ActivityRule>();
        queryWrapper.eq("activity_id",activityId);
        List<ActivityRule> activityRuleList = activityRuleMapper.selectList(queryWrapper);
        result.put("activityRuleList", activityRuleList);

        QueryWrapper activitySkuQueryWrapper = new QueryWrapper<ActivitySku>();
        activitySkuQueryWrapper.eq("activity_id",activityId);
        List<ActivitySku> activitySkuList = activitySkuMapper.selectList(activitySkuQueryWrapper);
        List<Long> skuIdList = activitySkuList.stream().map(ActivitySku::getSkuId).collect(Collectors.toList());
        List<SkuInfo> skuInfoList = productFeignClient.findSkuInfoList(skuIdList);
        result.put("skuInfoList", skuInfoList);
        return result;
    }

    //保存活动规则
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void saveActivityRule(ActivityRuleVo activityRuleVo) {
        activityRuleMapper.delete(new QueryWrapper<ActivityRule>().eq("activity_id",activityRuleVo.getActivityId()));
        activitySkuMapper.delete(new QueryWrapper<ActivitySku>().eq("activity_id",activityRuleVo.getActivityId()));

        List<ActivityRule> activityRuleList = activityRuleVo.getActivityRuleList();
        List<ActivitySku> activitySkuList = activityRuleVo.getActivitySkuList();
        List<Long> couponIdList = activityRuleVo.getCouponIdList();

        ActivityInfo activityInfo = activityInfoMapper.selectById(activityRuleVo.getActivityId());
        //获取规则列表数据
        for(ActivityRule activityRule : activityRuleList) {
            activityRule.setActivityId(activityRuleVo.getActivityId());
            activityRule.setActivityType(activityInfo.getActivityType());
            activityRuleMapper.insert(activityRule);
        }

        //获取规则范围数据
        for(ActivitySku activitySku : activitySkuList) {
            activitySku.setActivityId(activityRuleVo.getActivityId());
            activitySkuMapper.insert(activitySku);
        }
    }

    //根据关键字查询sku信息列表
    @Override
    public List<SkuInfo> findSkuInfoByKeyword(String keyword) {
        List<SkuInfo> skuInfoList = productFeignClient.findSkuInfoByKeyword(keyword);
        List<Long> skuIdList = skuInfoList.stream().map(SkuInfo::getId).collect(Collectors.toList());

        //判断:如果关键字查询不到匹配内容,直接返回空集合
        if (skuInfoList.size()==0){
            return skuInfoList;
        }
        List<SkuInfo> notExistSkuInfoList = new ArrayList<>();
        //已经存在的skuId，一个sku只能参加一个促销活动，所以存在的得排除
        List<Long> existSkuIdList = activityInfoMapper.selectExistSkuIdList(skuIdList);
        //排除已经参加活动商品
        for(SkuInfo skuInfo: skuInfoList) {
            if(!existSkuIdList.contains(skuInfo.getId())){
                notExistSkuInfoList.add(skuInfo);
            }
        }
        return notExistSkuInfoList;
    }

    //查询商品获取规则数据
    @Override
    public List<ActivityRule> findActivityRule(Long skuId) {
        List<ActivityRule> activityRuleList = activityInfoMapper.selectActivityRuleList(skuId);
        if(!CollectionUtils.isEmpty(activityRuleList)) {
            for(ActivityRule activityRule : activityRuleList) {
                activityRule.setRuleDesc(this.getRuleDesc(activityRule));
            }
        }
        return activityRuleList;
    }



    //构造规则名称的方法
    private String getRuleDesc(ActivityRule activityRule) {
        ActivityType activityType = activityRule.getActivityType();
        StringBuffer ruleDesc = new StringBuffer();
        if (activityType == ActivityType.FULL_REDUCTION) {
            ruleDesc
                    .append("满")
                    .append(activityRule.getConditionAmount())
                    .append("元减")
                    .append(activityRule.getBenefitAmount())
                    .append("元");
        } else {
            ruleDesc
                    .append("满")
                    .append(activityRule.getConditionNum())
                    .append("元打")
                    .append(activityRule.getBenefitDiscount())
                    .append("折");
        }
        return ruleDesc.toString();
    }


    @Override
    public Map<Long, List<String>> findActivity(List<Long> skuIdList) {
        Map<Long, List<String>> result = new HashMap<>();
        //skuidList遍历，得到每个skuid
        skuIdList.forEach(skuId -> {
            //根据skuid查询，查询sku对应活动里的规则列表
           List<ActivityRule> activityRuleList=baseMapper.findActivityRule(skuId);

            //数据封装，规则名称
            if(!CollectionUtils.isEmpty(activityRuleList)){
                List<String> ruleList=new ArrayList<>();
                //把规则名称处理
                for(ActivityRule activityRule : activityRuleList) {
                    //activityRule.setRuleDesc(this.getRuleDesc(activityRule));
                    ruleList.add(this.getRuleDesc(activityRule));
                }
               //activityRuleList.stream().map(activityRule -> activityRule.getRuleDesc()).collect(Collectors.toList());
                result.put(skuId,ruleList);

            }
        });




        return result;
    }

    @Override
    public Map<String, Object> findActivityAndCoupon(Long skuId, Long userId) {
        //根据skuid获取sku营销活动，一个活动有多个规则
        List<ActivityRule> activityRuleList = this.findActivityRuleListBySkuId(skuId);


        //根据skuid+userid查询优惠券信息
        List<CouponInfo> couponInfoList= couponInfoService.findCouponInfoList(skuId,userId);


        //封装map
        Map<String,Object> map=new HashMap<>();
        map.put("couponInfoList",couponInfoList);
        map.put("activityRuleList",activityRuleList);
        return map;
    }

    @Override
    public List<ActivityRule> findActivityRuleListBySkuId(Long skuId) {
        List<ActivityRule> activityRuleList = baseMapper.findActivityRule(skuId);
        for(ActivityRule activityRule:activityRuleList){
            String ruleDesc = this.getRuleDesc(activityRule);
            activityRule.setRuleDesc(ruleDesc);
        }
        return activityRuleList;
    }

    @Override
    public OrderConfirmVo findCartActivityAndCoupon(List<CartInfo> cartInfoList, Long userId) {
        //获取购物车每个购物项参与活动，根据活动规则分组(一个商品只能参加一个活动)map
        // 一个规则对应多个商品，获得所有活动id，返回set<Long>,根据所有活动id，查询活动规则表activity_rule(Set<Long> activity_id),返回activityRuleList
        //根据查询结果，根据活动id进行分组，返回map集合（Map<Long(活动id),List<ActivityRule>>（规则列表））
        //遍历活动分组map（得到key：活动id；得到key对应value，每个活动伦理的skuid列表）
        //通过所有购物项list（参数中集合），把参加活动的购物项获取出来（遍历所有购物项list集合，判断skuid是否存在于map几个value值）；目的：把参加活动那个的购物项取出来
        //进行相关计算（购物项总金额和总数量），封装数据CartinfoVo   获取没有参加活动购物项，封装

        List<CartInfoVo> cartInfoVoList = this.findCartActivityList(cartInfoList);

        //计算参与活动之后金额
        BigDecimal activityReduceAmount = cartInfoVoList.stream().filter(cartInfoVo -> cartInfoVo.getActivityRule() != null)
                .map(cartInfoVo -> cartInfoVo.getActivityRule().getReduceAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        //获取购物车可用优惠券列表
        List<CouponInfo> couponInfoList=couponInfoService.findCartCouponInfo(cartInfoList,userId);
        //计算商品使用优惠卷之后金额，一次只能使用一张优惠券

        BigDecimal couponReduceAmount=new BigDecimal(0);
        if(!CollectionUtils.isEmpty(couponInfoList)){
            couponReduceAmount= couponInfoList.stream().filter(couponInfo -> couponInfo.getIsOptimal().intValue()==1)
                    .map(couponInfo -> couponInfo.getAmount())
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        }
        //计算没有参与活动，没有使用优惠券原始金额

        BigDecimal originalTotalAmount = cartInfoList.stream().filter(cartInfo -> cartInfo.getIsChecked() == 1)
                .map(cartInfo -> cartInfo.getCartPrice()
                        .multiply(new BigDecimal(cartInfo.getSkuNum())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        //参与活动，计算最后金额
        BigDecimal totalAmount = originalTotalAmount.subtract(activityReduceAmount).subtract(couponReduceAmount);

        //封装数据到cartinfoVo
        OrderConfirmVo orderTradeVo = new OrderConfirmVo();
        orderTradeVo.setCarInfoVoList(cartInfoVoList);
        orderTradeVo.setActivityReduceAmount(activityReduceAmount);
        orderTradeVo.setCouponInfoList(couponInfoList);
        orderTradeVo.setCouponReduceAmount(couponReduceAmount);
        orderTradeVo.setOriginalTotalAmount(originalTotalAmount);
        orderTradeVo.setTotalAmount(totalAmount);
        return orderTradeVo;





    }

    @Override
    public List<CartInfoVo> findCartActivityList(List<CartInfo> cartInfoList) {
        //最终返回集合
        List<CartInfoVo> cartInfoVoList = new ArrayList<>();
        //获取所有skuid
        List<Long> skuIdList = cartInfoList.stream().map(CartInfo::getSkuId).collect(Collectors.toList());
        //根据所有skuid获取参与活动
        List<ActivitySku> activitySkuList=baseMapper.selectCartActivityList(skuIdList);

        //根据活动进行分组，每个活动有哪些skuid信息
        //map里的key是分组字段，活动id
        //value是每组skuId列表,set集合
        Map<Long, Set<Long>>  activityIdToSkuIdListMap = activitySkuList.stream().collect(
                Collectors.groupingBy(
                        ActivitySku::getActivityId,
                        Collectors.mapping(ActivitySku::getSkuId, Collectors.toSet())
                )
        );
        //获取活动规则数据
        //key是活动id，value是活动里规则列表数据
        Map<Long,List<ActivityRule>> activityIdToActivityRuleListMap = new HashMap<>();
        //所有活动id
        Set<Long> activityIdSet = activitySkuList.stream().map(ActivitySku::getActivityId).collect(Collectors.toSet());
        if(!CollectionUtils.isEmpty(activityIdSet)){
            //activity_rule表
            LambdaQueryWrapper<ActivityRule> wrapper=new LambdaQueryWrapper<>();
            wrapper.orderByDesc(ActivityRule::getConditionAmount,ActivityRule::getConditionNum);
            wrapper.in(ActivityRule::getActivityId,activityIdSet);
            List<ActivityRule> activityRuleList = activityRuleMapper.selectList(wrapper);
            //封装activityIdToActivityRuleListMap
            //根据活动id分组
            activityRuleList.stream().collect(Collectors.groupingBy(activityRule -> activityRule.getActivityId()));

        }

        //有活动购物项skuid
        Set<Long> activitySkuIdSet =new HashSet<>();
        if(!CollectionUtils.isEmpty(activityIdToSkuIdListMap)){
            //遍历activityIdToSkuIdListMap集合
            Iterator<Map.Entry<Long, Set<Long>>> iterator = activityIdToSkuIdListMap.entrySet().iterator();
            while(iterator.hasNext()){
                //获得活动id和skuid列表
                Map.Entry<Long, Set<Long>> entry = iterator.next();
                Long activityId = entry.getKey();
                Set<Long> currentActivitySkuIdSet = entry.getValue();
                //获取当前活动对应购物项列表
                List<CartInfo> currentActivityCartInfoList = cartInfoList.stream().filter(cartInfo -> currentActivitySkuIdSet.contains(cartInfo.getSkuId())).collect(Collectors.toList());
                //计数购物项总金额和总数量
                BigDecimal activityTotalAmount = this.computeTotalAmount(currentActivityCartInfoList);
                int activityTotalNum = this.computeCartNum(currentActivityCartInfoList);
                //计算活动对应规则
                //根据活动id获取活动对应规则
                List<ActivityRule> currentActivityRuleList = activityIdToActivityRuleListMap.get(activityId);
                ActivityType activityType = currentActivityRuleList.get(0).getActivityType();
                //判断活动类型：满减和打折
                ActivityRule activityRule=null;
                if(activityType == ActivityType.FULL_REDUCTION){//满减
                   activityRule = this.computeFullReduction(activityTotalAmount, currentActivityRuleList);
                }else {//满量
                    activityRule = this.computeFullDiscount(activityTotalNum, activityTotalAmount, currentActivityRuleList);
                }

                //cartinfovo封装
                CartInfoVo cartInfoVo=new CartInfoVo();
                cartInfoVo.setActivityRule(activityRule);
                cartInfoVo.setCartInfoList(currentActivityCartInfoList);
                cartInfoVoList.add(cartInfoVo);

                //记录那些购物项参加活动
                activitySkuIdSet.addAll(currentActivitySkuIdSet);
            }

        }
        //没有活动购物项skuid
        //获取那些skuid没有参加活动
        skuIdList.removeAll(activitySkuIdSet);
        if(!CollectionUtils.isEmpty(skuIdList)){
            //获取sskuid对应购物项
            Map<Long, CartInfo> skuIdCartInfoMap = cartInfoList.stream().collect(Collectors.toMap(CartInfo::getSkuId, CartInfo -> CartInfo));
            for(Long skuId:skuIdList){
                CartInfoVo cartInfoVo=new CartInfoVo();
                cartInfoVo.setActivityRule(null);
                List<CartInfo> cartInfos=new ArrayList<>();
                CartInfo cartInfo = skuIdCartInfoMap.get(skuId);
                cartInfos.add(cartInfo);
                cartInfoVo.setCartInfoList(cartInfos);
                cartInfoVoList.add(cartInfoVo);
            }
        }



        return cartInfoVoList;

    }


    private BigDecimal computeTotalAmount(List<CartInfo> cartInfoList) {
        BigDecimal total = new BigDecimal("0");
        for (CartInfo cartInfo : cartInfoList) {
            //是否选中
            if(cartInfo.getIsChecked().intValue() == 1) {
                BigDecimal itemTotal = cartInfo.getCartPrice().multiply(new BigDecimal(cartInfo.getSkuNum()));
                total = total.add(itemTotal);
            }
        }
        return total;
    }

    private int computeCartNum(List<CartInfo> cartInfoList) {
        int total = 0;
        for (CartInfo cartInfo : cartInfoList) {
            //是否选中
            if(cartInfo.getIsChecked().intValue() == 1) {
                total += cartInfo.getSkuNum();
            }
        }
        return total;
    }


    /**
     * 计算满量打折最优规则
     * @param totalNum
     * @param activityRuleList //该活动规则skuActivityRuleList数据，已经按照优惠折扣从大到小排序了
     */
    private ActivityRule computeFullDiscount(Integer totalNum, BigDecimal totalAmount, List<ActivityRule> activityRuleList) {
        ActivityRule optimalActivityRule = null;
        //该活动规则skuActivityRuleList数据，已经按照优惠金额从大到小排序了
        for (ActivityRule activityRule : activityRuleList) {
            //如果订单项购买个数大于等于满减件数，则优化打折
            if (totalNum.intValue() >= activityRule.getConditionNum()) {
                BigDecimal skuDiscountTotalAmount = totalAmount.multiply(activityRule.getBenefitDiscount().divide(new BigDecimal("10")));
                BigDecimal reduceAmount = totalAmount.subtract(skuDiscountTotalAmount);
                activityRule.setReduceAmount(reduceAmount);
                optimalActivityRule = activityRule;
                break;
            }
        }
        if(null == optimalActivityRule) {
            //如果没有满足条件的取最小满足条件的一项
            optimalActivityRule = activityRuleList.get(activityRuleList.size()-1);
            optimalActivityRule.setReduceAmount(new BigDecimal("0"));
            optimalActivityRule.setSelectType(1);

            StringBuffer ruleDesc = new StringBuffer()
                    .append("满")
                    .append(optimalActivityRule.getConditionNum())
                    .append("元打")
                    .append(optimalActivityRule.getBenefitDiscount())
                    .append("折，还差")
                    .append(totalNum-optimalActivityRule.getConditionNum())
                    .append("件");
            optimalActivityRule.setRuleDesc(ruleDesc.toString());
        } else {
            StringBuffer ruleDesc = new StringBuffer()
                    .append("满")
                    .append(optimalActivityRule.getConditionNum())
                    .append("元打")
                    .append(optimalActivityRule.getBenefitDiscount())
                    .append("折，已减")
                    .append(optimalActivityRule.getReduceAmount())
                    .append("元");
            optimalActivityRule.setRuleDesc(ruleDesc.toString());
            optimalActivityRule.setSelectType(2);
        }
        return optimalActivityRule;
    }

    /**
     * 计算满减最优规则
     * @param totalAmount
     * @param activityRuleList //该活动规则skuActivityRuleList数据，已经按照优惠金额从大到小排序了
     */
    private ActivityRule computeFullReduction(BigDecimal totalAmount, List<ActivityRule> activityRuleList) {
        ActivityRule optimalActivityRule = null;
        //该活动规则skuActivityRuleList数据，已经按照优惠金额从大到小排序了
        for (ActivityRule activityRule : activityRuleList) {
            //如果订单项金额大于等于满减金额，则优惠金额
            if (totalAmount.compareTo(activityRule.getConditionAmount()) > -1) {
                //优惠后减少金额
                activityRule.setReduceAmount(activityRule.getBenefitAmount());
                optimalActivityRule = activityRule;
                break;
            }
        }
        if(null == optimalActivityRule) {
            //如果没有满足条件的取最小满足条件的一项
            optimalActivityRule = activityRuleList.get(activityRuleList.size()-1);
            optimalActivityRule.setReduceAmount(new BigDecimal("0"));
            optimalActivityRule.setSelectType(1);

            StringBuffer ruleDesc = new StringBuffer()
                    .append("满")
                    .append(optimalActivityRule.getConditionAmount())
                    .append("元减")
                    .append(optimalActivityRule.getBenefitAmount())
                    .append("元，还差")
                    .append(totalAmount.subtract(optimalActivityRule.getConditionAmount()))
                    .append("元");
            optimalActivityRule.setRuleDesc(ruleDesc.toString());
        } else {
            StringBuffer ruleDesc = new StringBuffer()
                    .append("满")
                    .append(optimalActivityRule.getConditionAmount())
                    .append("元减")
                    .append(optimalActivityRule.getBenefitAmount())
                    .append("元，已减")
                    .append(optimalActivityRule.getReduceAmount())
                    .append("元");
            optimalActivityRule.setRuleDesc(ruleDesc.toString());
            optimalActivityRule.setSelectType(2);
        }
        return optimalActivityRule;
    }


}
