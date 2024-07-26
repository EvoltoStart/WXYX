package com.wx.YX.sys.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wx.YX.model.sys.Region;
import com.wx.YX.sys.mapper.RegionMapper;
import com.wx.YX.sys.service.RegionService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

/**
 * <p>
 * 地区表 服务实现类
 * </p>
 *
 * @author meng
 * @since 2024-07-26
 */
@Service
public class RegionServiceImpl extends ServiceImpl<RegionMapper, Region> implements RegionService {

    @Override
    public List<Region> getRegionByKeyword(String keyword) {
        LambdaQueryWrapper<Region>wrapper=new LambdaQueryWrapper<>();
        wrapper.like(Region::getName,keyword);
        List<Region> list=baseMapper.selectList(wrapper);
        return list;
    }

    @Override
    public List<Region> getRegionByParentId(Long parentId) {
        LambdaQueryWrapper<Region> wrapper=new LambdaQueryWrapper<>();
        wrapper.like(Region::getParentId,parentId);

        return baseMapper.selectList(wrapper);
    }
}
