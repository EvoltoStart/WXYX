package com.wx.YX.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wx.YX.common.constant.RedisConst;
import com.wx.YX.common.exception.yxException;
import com.wx.YX.common.result.ResultCodeEnum;
import com.wx.YX.model.product.SkuAttrValue;
import com.wx.YX.model.product.SkuImage;
import com.wx.YX.model.product.SkuInfo;
import com.wx.YX.model.product.SkuPoster;
import com.wx.YX.mq.constant.MqConst;
import com.wx.YX.mq.sercvice.RabbitService;
import com.wx.YX.product.mapper.SkuInfoMapper;
import com.wx.YX.product.service.SkuAttrValueService;
import com.wx.YX.product.service.SkuImageService;
import com.wx.YX.product.service.SkuInfoService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wx.YX.product.service.SkuPosterService;
import com.wx.YX.vo.product.SkuInfoQueryVo;
import com.wx.YX.vo.product.SkuInfoVo;
import com.wx.YX.vo.product.SkuStockLockVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;

/**
 * <p>
 * sku信息 服务实现类
 * </p>
 *
 * @author meng
 * @since 2024-07-28
 */
@Service
public class SkuInfoServiceImpl extends ServiceImpl<SkuInfoMapper, SkuInfo> implements SkuInfoService {


    //sku海报
    @Autowired
    private SkuPosterService skuPosterService;


    //sku图片
    @Autowired
    private SkuImageService skuImagesService;


    //sku平台属性
    @Autowired
    private SkuAttrValueService skuAttrValueService;

    //获取mq
    @Autowired
    private RabbitService rabbitService;

    @Autowired
    private RedisTemplate redisTemplate;


    //获取sku分页列表
    @Override
    public IPage<SkuInfo> selectPage(Page<SkuInfo> pageParam, SkuInfoQueryVo skuInfoQueryVo) {
        //获取条件值
        String keyword = skuInfoQueryVo.getKeyword();
        String skuType = skuInfoQueryVo.getSkuType();
        Long categoryId = skuInfoQueryVo.getCategoryId();
        //封装条件
        LambdaQueryWrapper<SkuInfo> wrapper = new LambdaQueryWrapper<>();
        if (!StringUtils.isEmpty(keyword)) {
            wrapper.like(SkuInfo::getSkuName, keyword);
        }
        if (!StringUtils.isEmpty(skuType)) {
            wrapper.eq(SkuInfo::getSkuType, skuType);
        }
        if (!StringUtils.isEmpty(categoryId)) {
            wrapper.eq(SkuInfo::getCategoryId, categoryId);
        }
        //调用方法查询
        IPage<SkuInfo> skuInfoPage = baseMapper.selectPage(pageParam, wrapper);
        return skuInfoPage;
    }

    //添加商品
    @Transactional(rollbackFor = {Exception.class})
    @Override
    public void saveSkuInfo(SkuInfoVo skuInfoVo) {
        //保存sku信息
        SkuInfo skuInfo = new SkuInfo();
        BeanUtils.copyProperties(skuInfoVo, skuInfo);
        baseMapper.insert(skuInfo);

        //保存sku海报
        List<SkuPoster> skuPosterList = skuInfoVo.getSkuPosterList();
        if (!CollectionUtils.isEmpty(skuPosterList)) {
            int sort = 1;
            for (SkuPoster skuPoster : skuPosterList) {
                skuPoster.setSkuId(skuInfo.getId());
                sort++;
            }
            skuPosterService.saveBatch(skuPosterList);
        }

        //保存sku图片
        List<SkuImage> skuImagesList = skuInfoVo.getSkuImagesList();
        if (!CollectionUtils.isEmpty(skuImagesList)) {
            int sort = 1;
            for (SkuImage skuImages : skuImagesList) {
                skuImages.setSkuId(skuInfo.getId());
                skuImages.setSort(sort);
                sort++;
            }
            skuImagesService.saveBatch(skuImagesList);
        }

        //保存sku平台属性
        List<SkuAttrValue> skuAttrValueList = skuInfoVo.getSkuAttrValueList();
        if (!CollectionUtils.isEmpty(skuAttrValueList)) {
            int sort = 1;
            for (SkuAttrValue skuAttrValue : skuAttrValueList) {
                skuAttrValue.setSkuId(skuInfo.getId());
                skuAttrValue.setSort(sort);
                sort++;
            }
            skuAttrValueService.saveBatch(skuAttrValueList);
        }

    }

    //获取商品
    @Override
    public SkuInfoVo getSkuInfoVo(Long skuId) {
        return getSkuInfoDB(skuId);
    }



    private SkuInfoVo getSkuInfoDB(Long skuId) {
        SkuInfoVo skuInfoVo = new SkuInfoVo();

        SkuInfo skuInfo = baseMapper.selectById(skuId);
        //TODO skuImagesService  skuPosterService  skuAttrValueService分别添加方法
        List<SkuImage> skuImageList = skuImagesService.findBySkuId(skuId);
        List<SkuPoster> skuPosterList = skuPosterService.findBySkuId(skuId);
        List<SkuAttrValue> skuAttrValueList = skuAttrValueService.findBySkuId(skuId);

        //封装所有数据，返回
        BeanUtils.copyProperties(skuInfo, skuInfoVo);
        skuInfoVo.setSkuImagesList(skuImageList);
        skuInfoVo.setSkuPosterList(skuPosterList);
        skuInfoVo.setSkuAttrValueList(skuAttrValueList);
        return skuInfoVo;
    }

    @Override
    public void updateSkuInfo(SkuInfoVo skuInfoVo) {

        //修改sku基本信息
        SkuInfo s=new SkuInfo();
        BeanUtils.copyProperties(skuInfoVo,s);
        baseMapper.updateById(s);
        //海报信息
        Long skuId=skuInfoVo.getId();
        LambdaQueryWrapper<SkuPoster> wrapperSkuPost=new LambdaQueryWrapper<>();
        wrapperSkuPost.eq(SkuPoster::getSkuId,skuId);
        skuPosterService.remove(wrapperSkuPost);
        List<SkuPoster> skuPosterList=skuInfoVo.getSkuPosterList();
        if(!CollectionUtils.isEmpty(skuPosterList)){
            for( SkuPoster skuPoster:skuPosterList){
                skuPoster.setSkuId(skuId);
            }
            skuPosterService.saveBatch(skuPosterList);
        }

        //商品图片
        skuImagesService.remove(new LambdaQueryWrapper<SkuImage>().eq(SkuImage::getSkuId,skuId));
        List<SkuImage> skuImagesList=skuInfoVo.getSkuImagesList();
        if(!CollectionUtils.isEmpty(skuImagesList)){
            int sort=1;
            for (SkuImage skuImage : skuImagesList) {
                skuImage.setSort(sort);
                skuImage.setSkuId(skuId);
                sort++;
            }
            skuImagesService.saveBatch(skuImagesList);
        }
        //商品属性
        skuAttrValueService.remove(new LambdaQueryWrapper<SkuAttrValue>().eq(SkuAttrValue::getSkuId,skuId));
        List<SkuAttrValue> skuAttrValueList=skuInfoVo.getSkuAttrValueList();

        if(!CollectionUtils.isEmpty(skuAttrValueList)){
            int sort=1;
            for( SkuAttrValue skuAttrValue:skuAttrValueList){

                skuAttrValue.setSort(sort);
                skuAttrValue.setSkuId(skuId);
                sort++;

            }
            skuAttrValueService.saveBatch(skuAttrValueList);
        }
    }

    //商品审核
    @Transactional(rollbackFor = {Exception.class})
    @Override
    public void check(Long skuId, Integer status) {
        // 更改发布状态
        SkuInfo skuInfoUp = new SkuInfo();
        skuInfoUp.setId(skuId);
        skuInfoUp.setCheckStatus(status);
        baseMapper.updateById(skuInfoUp);
    }

    //商品上架
    @Transactional(rollbackFor = {Exception.class})
    @Override
    public void publish(Long skuId, Integer status) {
        // 更改发布状态
        if(status == 1) {
            SkuInfo skuInfoUp = new SkuInfo();
            skuInfoUp.setId(skuId);
            skuInfoUp.setPublishStatus(1);
            baseMapper.updateById(skuInfoUp);
            //TODO 商品上架 后续会完善：发送mq消息更新es数据
            rabbitService.sendMessage(MqConst.EXCHANGE_GOODS_DIRECT,MqConst.ROUTING_GOODS_UPPER,skuId);
        } else {
            SkuInfo skuInfoUp = new SkuInfo();
            skuInfoUp.setId(skuId);
            skuInfoUp.setPublishStatus(0);
            baseMapper.updateById(skuInfoUp);
            //TODO 商品下架 后续会完善：发送mq消息更新es数据
            rabbitService.sendMessage(MqConst.EXCHANGE_GOODS_DIRECT,MqConst.ROUTING_GOODS_LOWER,skuId);
        }
    }

    //新人专享
    @Override
    public void isNewPerson(Long skuId, Integer status) {
        SkuInfo skuInfoUp = new SkuInfo();
        skuInfoUp.setId(skuId);
        skuInfoUp.setIsNewPerson(status);
        baseMapper.updateById(skuInfoUp);
    }

    @Override
    public List<SkuInfo> findSkuInfoList(List<Long> skuIdList) {

       return baseMapper.selectBatchIds(skuIdList);
    }

    @Override
    public List<SkuInfo> findSkuInfoByKeyword(String keyword) {
       return baseMapper.selectList(new LambdaQueryWrapper<SkuInfo>().like(SkuInfo::getSkuName,keyword));
    }

    @Override
    public List<SkuInfo> findNewPersonSkuInfoList() {
        //条件一：is_new_person=1 是否为新用户
        //条件二：publish_status=1 上架或下架状态
        //条件三：显示其中三个
        Page<SkuInfo> pageParam=new Page<>(1,3);
        LambdaQueryWrapper<SkuInfo> wrapper=new LambdaQueryWrapper<>();
        wrapper.eq(SkuInfo::getIsNewPerson,1);
        wrapper.eq(SkuInfo::getPublishStatus,1);
        wrapper.orderByDesc(SkuInfo::getStock);
        IPage<SkuInfo> skuInfoIPage=baseMapper.selectPage(pageParam,wrapper);
        List<SkuInfo> skuInfoList=skuInfoIPage.getRecords();
        return skuInfoList;
    }

    @Override
    public Boolean checkAndLock(List<SkuStockLockVo> skuStockLockVoList, String orderNo) {
        //1、判断skuStockLockVoList是否为空
        if(CollectionUtils.isEmpty(skuStockLockVoList)){
            throw new yxException(ResultCodeEnum.DATA_ERROR);
        }

        //2、遍历集合每个商品，验证库存并锁定库存，具备原子性
        skuStockLockVoList.forEach(skuStockLockVo -> {
            this.checkLock(skuStockLockVo);
        });
        //3、只要一个商品锁定失败，所有锁定成功商品都解锁
        boolean flag = skuStockLockVoList.stream().anyMatch(skuStockLockVo -> !skuStockLockVo.getIsLock());
        if(flag){
            //所有锁定车工商品解锁
            skuStockLockVoList.stream().filter(SkuStockLockVo::getIsLock)
                    .forEach(skuStockLockVo -> {
                        baseMapper.unlockStock(skuStockLockVo.getSkuId(),skuStockLockVo.getSkuNum());
                    });
            //返回失败状态
            return false;
        }
        //4、如果所有商品都锁定成功，redis缓存相关数据，为方便后面解锁和减库存
        redisTemplate.opsForValue().set(RedisConst.SROCK_INFO+orderNo,skuStockLockVoList);
        return true;
    }

    private void checkLock(SkuStockLockVo skuStockLockVo) {

    }


}
