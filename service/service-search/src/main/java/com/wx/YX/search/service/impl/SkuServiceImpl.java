package com.wx.YX.search.service.impl;

import com.wx.YX.activity.client.ActivityFenignClient;
import com.wx.YX.client.product.ProductFeignClient;
import com.wx.YX.common.auth.AuthContextHolder;
import com.wx.YX.enums.SkuType;
import com.wx.YX.model.product.Category;
import com.wx.YX.model.product.SkuInfo;
import com.wx.YX.model.search.SkuEs;
import com.wx.YX.search.repository.SkureRepository;
import com.wx.YX.search.service.SkuService;
import com.wx.YX.vo.search.SkuEsQueryVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import sun.nio.cs.ext.SJIS;

import java.awt.print.Pageable;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class SkuServiceImpl implements SkuService {
    @Autowired
    private  SkureRepository skureRepository;
    @Autowired
    private ProductFeignClient productFeignClient;
    @Autowired
    private ActivityFenignClient activityFenignClient;

    @Autowired
    private RedisTemplate redisTemplate;

    //上架
    @Override
    public void upperSku(Long skuId) {
        //通过远程调用，根据skuid获取相关数据
        SkuInfo skuInfo=productFeignClient.getSkuInfo(skuId);
        if(skuInfo==null){
            return;
        }
        Category category=productFeignClient.getCategory(skuInfo.getCategoryId());

        //获取数据封装skuEs对象
        SkuEs skuEs=new SkuEs();
        //封装分类
        if(category!=null){
            skuEs.setCategoryId(category.getId());
            skuEs.setCategoryName(category.getName());
        }
        //封装skuInfo信息部分
        skuEs.setId(skuInfo.getId());
        skuEs.setKeyword(skuInfo.getSkuName()+","+skuEs.getCategoryName());
        skuEs.setWareId(skuInfo.getWareId());
        skuEs.setIsNewPerson(skuInfo.getIsNewPerson());
        skuEs.setImgUrl(skuInfo.getImgUrl());
        skuEs.setTitle(skuInfo.getSkuName());
        if(Objects.equals(skuInfo.getSkuType(), SkuType.COMMON.getCode())) {
            skuEs.setSkuType(0);
            skuEs.setPrice(skuInfo.getPrice().doubleValue());
            skuEs.setStock(skuInfo.getStock());
            skuEs.setSale(skuInfo.getSale());
            skuEs.setPerLimit(skuInfo.getPerLimit());
        } else {
            //TODO 待完善-秒杀商品

        }
        skureRepository.save(skuEs);
    }

    @Override
    public void lowerSku(Long skuId) {
        skureRepository.deleteById(skuId);

    }

    //获取爆款商品
    @Override
    public List<SkuEs> findHotSkuList() {
        //find  read get开头
        //关联条件关键字
        //0代表第一页
        Pageable pageable= (Pageable) PageRequest.of(0,10);
        Page<SkuEs> pageMode= skureRepository.findByOrderByHotScoreDesc(pageable);
        List<SkuEs> skuEsList=pageMode.getContent();

        return skuEsList;
    }

    @Override
    public Page<SkuEs> search(org.springframework.data.domain.Pageable pageable, SkuEsQueryVo skuEsQueryVo) {
        //向skuRsQueryVo设置wareId，当前登录用户的仓库
        skuEsQueryVo.setWareId(AuthContextHolder.getWareId());

        Page<SkuEs> pageModel=null;
        //调用skurepository方法，根据springData命名规则定义方法，进行条件查询
        //判断keyword是否为空，空根据仓库id+分类id查询
        String keyword=skuEsQueryVo.getKeyword();
        if(StringUtils.isEmpty(keyword)){
            pageModel=skureRepository.findByCategoryIdAndWareId(skuEsQueryVo.getCategoryId(),skuEsQueryVo.getWareId(),pageable);
        }else {
            //keyword不为空 根据仓库id+keyword查询
            pageModel=skureRepository.findByCategoryIdAndWareId(skuEsQueryVo.getKeyword(),skuEsQueryVo.getWareId(),pageable);

        }

        //查询商品参加的优惠活动
        List<SkuEs> list=pageModel.getContent();
        if(!CollectionUtils.isEmpty(list)){
            //遍历，得到所有skuid
            List<Long> skuIdList=list.stream().map(item->item.getId()).collect(Collectors.toList());
            //根据skuid列表远程调用，调用serivec-activity接口得到数据
            //返回map<Long,List<String>>
            //key是skuid，value是list集合
            //一个商品参加一个活动，一个活动有多个规则，比如满20减1元，满50减5元
            Map<Long,List<String>> skuIdToRuleListMap=activityFenignClient.findActivity(skuIdList);//TODO 远程调用
            //封装获得到skuES里的ruleList属性里
            if(skuIdToRuleListMap!=null){
                list.forEach(skuEs -> {
                    skuEs.setRuleList(skuIdToRuleListMap.get(skuEs.getId()));
                });
            }
        }


        return pageModel;
    }

    //更新商品热度
    @Override
    public void incrHotScore(Long skuId) {
        String key="hotScore";
        //保存数据，每次+1
        Double hotScore = redisTemplate.opsForZSet().incrementScore(key, "skuId:" + skuId, 1);
        //规则
        if(hotScore%10==0){
            //更新到es
            Optional<SkuEs> optional = skureRepository.findById(skuId);
            SkuEs skuEs = optional.get();
            skuEs.setHotScore(Math.round(hotScore));
            skureRepository.save(skuEs);
        }
    }
}
