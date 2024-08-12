package com.wx.YX.service.impl;

import com.wx.YX.client.product.ProductFeignClient;
import com.wx.YX.common.constant.RedisConst;
import com.wx.YX.common.exception.yxException;
import com.wx.YX.common.result.ResultCodeEnum;
import com.wx.YX.enums.SkuType;
import com.wx.YX.model.order.CartInfo;
import com.wx.YX.model.product.SkuInfo;
import com.wx.YX.service.CartInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class CartInfoServiceImpl implements CartInfoService {
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private ProductFeignClient productFeignClient;

    private String getCartKey(Long userId){
        //user:userId:cart
       return RedisConst.USER_KEY_PREFIX+userId+RedisConst.USER_CART_KEY_SUFFIX;
    }

    private void setCartKeyExpire(String key){
        redisTemplate.expire(key,RedisConst.USER_CART_EXPIRE, TimeUnit.SECONDS);
    }

    //添加到购物车
    @Override
    public void addToCart(Long userId, Long skuId, Integer skuNum) {
        //购物车数据存储redis，
        //从redis中根据key获取key，key包含userid
        String cartKey=this.getCartKey(userId);
        BoundHashOperations<String,String, CartInfo> hashOperations = redisTemplate.boundHashOps(cartKey);


        //判断是否第一次添加商品到购物车
        //根据第一步查询结果，得到skuid+skunum
        //进行判断，判断结果是否欧skuid
        CartInfo cartInfo=null;
        if(hashOperations.hasKey(skuId.toString())){
            //有skuid，不是第一次添加
            //根据skuid，获得对应数量，更新数量
             cartInfo = hashOperations.get(skuId.toString());
            Integer currentSkuNum = cartInfo.getSkuNum()+skuNum;
            if(currentSkuNum<1){
                return;
            }
            //更新cart info对象
            cartInfo.setSkuNum(currentSkuNum);
            cartInfo.setCurrentBuyNum(currentSkuNum);
            //判断商品数量不大于限购数量
            Integer perLimit=cartInfo.getPerLimit();
            if(currentSkuNum>perLimit){
                throw  new yxException(ResultCodeEnum.SKU_LIMIT_ERROR);
            }
            //更新其他值
            cartInfo.setIsChecked(1);
            cartInfo.setUpdateTime(new Date());
        }else {
            //结果没有skuid，是第一次添加
            //直接添加
            //第一次添加只能添加一个
            skuNum = 1;
            cartInfo=new CartInfo();
            //远程调用service-product
            SkuInfo skuInfo=productFeignClient.getSkuInfo(skuId);
            if(skuInfo==null){
                throw new yxException(ResultCodeEnum.DATA_ERROR);
            }
            //封装sku对象
            cartInfo.setSkuId(skuId);
            cartInfo.setCategoryId(skuInfo.getCategoryId());
            cartInfo.setSkuType(skuInfo.getSkuType());
            cartInfo.setIsNewPerson(skuInfo.getIsNewPerson());
            cartInfo.setUserId(userId);
            cartInfo.setCartPrice(skuInfo.getPrice());
            cartInfo.setSkuNum(skuNum);
            cartInfo.setCurrentBuyNum(skuNum);
            cartInfo.setSkuType(SkuType.COMMON.getCode());
            cartInfo.setPerLimit(skuInfo.getPerLimit());
            cartInfo.setImgUrl(skuInfo.getImgUrl());
            cartInfo.setSkuName(skuInfo.getSkuName());
            cartInfo.setWareId(skuInfo.getWareId());
            cartInfo.setIsChecked(1);
            cartInfo.setStatus(1);
            cartInfo.setCreateTime(new Date());
            cartInfo.setUpdateTime(new Date());

        }


        //更新redis缓存
        hashOperations.put(skuId.toString(),cartInfo);

        //设置redis有效时间有效时间
        this.setCartKeyExpire(cartKey);

    }

    //根据skuid删除购物车
    @Override
    public void deleteCart(Long skuId, Long userId) {
        BoundHashOperations<String,String,CartInfo> hashOperations = redisTemplate.boundHashOps(this.getCartKey(userId));
        if(Boolean.TRUE.equals(hashOperations.hasKey(skuId.toString()))){
            hashOperations.delete(skuId.toString());
        }
    }

    //清空购物车
    @Override
    public void deleteAllCart(Long userId) {
        String cartKey = this.getCartKey(userId);
        BoundHashOperations<String,String,CartInfo> boundHashOperations = redisTemplate.boundHashOps(cartKey);
        List<CartInfo> cartInfoList = boundHashOperations.values();
        for (CartInfo cartInfo : cartInfoList) {
            boundHashOperations.delete(cartInfo.getSkuId().toString());
        }
    }

    //批量删除购物车
    @Override
    public void batchDeleteCart(List<Long> skuIdList, Long userId) {
        String cartKey = this.getCartKey(userId);
        BoundHashOperations<String,String,CartInfo> boundHashOperations = redisTemplate.boundHashOps(cartKey);
        skuIdList.forEach(skuId->{
            boundHashOperations.delete(skuId.toString());
        });
    }

    @Override
    public List<CartInfo> getCartList(Long userId) {
        //判断userid
        List<CartInfo> cartInfoList = new ArrayList<>();
        if(StringUtils.isEmpty(userId)){
            return cartInfoList;
        }
        //从redis获取购物车信息
        String cartKey = this.getCartKey(userId);
        BoundHashOperations<String,String,CartInfo> boundHashOperations = redisTemplate.boundHashOps(cartKey);
        cartInfoList = boundHashOperations.values();
        if(!CollectionUtils.isEmpty(cartInfoList)){
            //根据商品添加时间按降序排序
            cartInfoList.sort(new Comparator<CartInfo>() {
                @Override
                public int compare(CartInfo o1, CartInfo o2) {
                    return o2.getCreateTime().compareTo(o1.getCreateTime());
                }
            });
        }
        return cartInfoList;
    }

    @Override
    public void checkCart(Long userId, Integer isChecked, Long skuId) {
        //获取redis的key
        String cartKey=this.getCartKey(userId);
        //cartKey 获取field-value
        BoundHashOperations<String,String,CartInfo> boundHashOperations = redisTemplate.boundHashOps(cartKey);
        //根据field（skuid）获取cartinfo
        CartInfo cartInfo=boundHashOperations.get(skuId.toString());
        if(cartInfo!=null){
            cartInfo.setIsChecked(isChecked);
            //更新
            boundHashOperations.put(skuId.toString(),cartInfo);
            //设置key过期时间
            this.setCartKeyExpire(cartKey);
        }
    }

    @Override
    public void checkAllCart(Long userId, Integer isChecked) {
        String cartKey =this.getCartKey(userId);
        BoundHashOperations<String,String,CartInfo> boundHashOperations = redisTemplate.boundHashOps(cartKey);
        List<CartInfo> cartInfoList = boundHashOperations.values();
        cartInfoList.forEach(cartInfo -> {
            cartInfo.setIsChecked(isChecked);
            boundHashOperations.put(cartInfo.getSkuId().toString(),cartInfo);
        });
    }

    @Override
    public void batchCheckCart(List<Long> skuIdList, Long userId, Integer isChecked) {
        String cartKey = getCartKey(userId);
        //获取缓存对象
        BoundHashOperations<String, String, CartInfo> hashOperations = redisTemplate.boundHashOps(cartKey);
        skuIdList.forEach(skuId -> {
            CartInfo cartInfo = hashOperations.get(skuId.toString());
            cartInfo.setIsChecked(isChecked);
            hashOperations.put(cartInfo.getSkuId().toString(), cartInfo);
        });
    }

    @Override
    public List<CartInfo> getCartCheckedList(Long userId) {
        String cartKey=this.getCartKey(userId);
        BoundHashOperations<String,String,CartInfo> boundHashOperations = redisTemplate.boundHashOps(cartKey);
        List<CartInfo> cartInfoList = boundHashOperations.values();
        List<CartInfo> cartInfoListNew = cartInfoList.stream().filter(cartInfo -> {
            return cartInfo.getIsChecked().intValue() == 1;
        }).collect(Collectors.toList());
        return cartInfoListNew;
    }


}
