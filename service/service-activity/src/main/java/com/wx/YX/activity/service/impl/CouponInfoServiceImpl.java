package com.wx.YX.activity.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wx.YX.activity.mapper.CouponInfoMapper;
import com.wx.YX.activity.mapper.CouponRangeMapper;
import com.wx.YX.activity.service.CouponInfoService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wx.YX.client.product.ProductFeignClient;
import com.wx.YX.enums.CouponRangeType;
import com.wx.YX.enums.CouponType;
import com.wx.YX.model.activity.CouponInfo;
import com.wx.YX.model.activity.CouponRange;
import com.wx.YX.model.product.Category;
import com.wx.YX.model.product.SkuInfo;
import com.wx.YX.vo.activity.CouponRuleVo;
import com.wx.YX.vo.product.SkuInfoVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 * 优惠券信息 服务实现类
 * </p>
 *
 * @author meng
 * @since 2024-08-01
 */
@Service
public class CouponInfoServiceImpl extends ServiceImpl<CouponInfoMapper, CouponInfo> implements CouponInfoService {

    @Autowired
    private CouponRangeMapper couponRangeMapper;
    @Autowired
    private ProductFeignClient productFeignClient;
    @Autowired
    private CouponInfoMapper couponInfoMapper;

    @Override
    public IPage<CouponInfo> selectpage(Long page, Long limit) {
        Page<CouponInfo> pageParams = new Page<>(page, limit);
        IPage<CouponInfo> pageModel = baseMapper.selectPage(pageParams, null);
        List<CouponInfo> couponInfoList= pageModel.getRecords();
        //封装优惠券类型
        couponInfoList.stream().forEach(item->{
            item.setCouponTypeString(item.getCouponType().getComment());
            CouponRangeType rangeType=item.getRangeType();
            if(rangeType!=null){
                item.setRangeTypeString(rangeType.getComment());
            }
        });
        return pageModel;
    }

    @Override
    public CouponInfo getCouponInfo(String id) {
      CouponInfo couponInfo =baseMapper.selectById(id);
      couponInfo.setCouponTypeString(couponInfo.getCouponType().getComment());
      if(couponInfo.getRangeType()!=null){
          couponInfo.setRangeTypeString(couponInfo.getRangeType().getComment());
      }
      return couponInfo;
    }

    @Override
    public Map<String, Object> findCouponRuleList(Long id) {
        //根据优惠劵id查询优惠劵信息 coupon——info表
        CouponInfo couponInfo=baseMapper.selectById(id);
        //根据优惠劵id查询coupon_rang，查询对应rang_ id
        List<CouponRange> couponRangeList=couponRangeMapper.selectList(new LambdaQueryWrapper<CouponRange>().eq(CouponRange::getCouponId,id));
        List<Long> rangIdList=couponRangeList.stream().map(CouponRange::getRangeId).collect(Collectors.toList());

        Map<String,Object> result=new HashMap<>();
        //分别判断分装不同数据
        if(!CollectionUtils.isEmpty(rangIdList)){
            if(couponInfo.getRangeType()==CouponRangeType.SKU){
                List<SkuInfo>skuInfosList=productFeignClient.findSkuInfoList(rangIdList);
                result.put("skuInfoList",skuInfosList);
            }else if(couponInfo.getCouponType()== CouponType.CASH) {
                List<Category> categoryList=productFeignClient.findCategoryList(rangIdList);
                result.put("categoryList",categoryList);
            }
        }

        return result;
    }

    @Override
    public void saveCouponRule(CouponRuleVo couponRuleVo) {

        //根据id删除规则数据
        couponRangeMapper.delete(new LambdaQueryWrapper<CouponRange>().eq(CouponRange::getCouponId,couponRuleVo.getCouponId()));
        //更新优惠劵基本信息
        CouponInfo couponInfo=baseMapper.selectById(couponRuleVo.getCouponId());
        couponInfo.setRangeType(couponRuleVo.getRangeType());
        couponInfo.setConditionAmount(couponRuleVo.getConditionAmount());
        couponInfo.setAmount(couponRuleVo.getAmount());
        couponInfo.setConditionAmount(couponRuleVo.getConditionAmount());
        couponInfo.setRangeDesc(couponRuleVo.getRangeDesc());

        baseMapper.updateById(couponInfo);

        //  插入优惠券的规则 couponRangeList
        List<CouponRange> couponRangeList = couponRuleVo.getCouponRangeList();
        for (CouponRange couponRange : couponRangeList) {
            //重新设置couponId
            couponRange.setCouponId(couponRuleVo.getCouponId());
            //  插入数据
            couponRangeMapper.insert(couponRange);
        }
    }

    //根据关键字获取sku列表，活动使用
    @Override
    public List<CouponInfo> findCouponByKeyword(String keyword) {
        //  模糊查询
        QueryWrapper<CouponInfo> couponInfoQueryWrapper = new QueryWrapper<>();
        couponInfoQueryWrapper.like("coupon_name",keyword);
        return couponInfoMapper.selectList(couponInfoQueryWrapper);
    }

    //根据skuid+userid查询优惠卷信息
    @Override
    public List<CouponInfo> findCouponInfoList(Long skuId,Long userId) {
        //远程调用：根据skuid获取skuinfo
        SkuInfoVo skuInfoVo = productFeignClient.getSkuInfoVo(skuId);

        //根据条件查询：skuid+分类id+userid
        List<CouponInfo> couponInfoList= baseMapper.seletcCouponInfoList(skuInfoVo.getId(),skuInfoVo.getCategoryId(),userId);
        return couponInfoList;
    }
}
