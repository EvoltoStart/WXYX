package com.wx.YX.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wx.YX.model.product.Attr;
import com.wx.YX.product.mapper.AttrMapper;
import com.wx.YX.product.service.AttrService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

/**
 * <p>
 * 商品属性 服务实现类
 * </p>
 *
 * @author meng
 * @since 2024-07-28
 */
@Service
public class AttrServiceImpl extends ServiceImpl<AttrMapper, Attr> implements AttrService {

    @Override
    public List<Attr> selectAttrList(Long groupId) {
        LambdaQueryWrapper<Attr> wrapper=new LambdaQueryWrapper<>();

        wrapper.eq(Attr::getAttrGroupId,groupId);
        List<Attr> lsit=baseMapper.selectList(wrapper);
        return lsit;
    }

    //根据属性分组id 获取属性列表
    @Override
    public List<Attr> findByAttrGroupId(Long attrGroupId) {
        LambdaQueryWrapper<Attr> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Attr::getAttrGroupId,attrGroupId);
        List<Attr> attrList = baseMapper.selectList(wrapper);
        return attrList;
    }
}
