package com.wx.YX.sys.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wx.YX.common.exception.yxException;
import com.wx.YX.common.result.ResultCodeEnum;
import com.wx.YX.model.sys.RegionWare;
import com.wx.YX.vo.sys.RegionWareQueryVo;
import com.wx.YX.sys.mapper.RegionWareMapper;
import com.wx.YX.sys.service.RegionWareService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * <p>
 * 城市仓库关联表 服务实现类
 * </p>
 *
 * @author meng
 * @since 2024-07-26
 */
@Service
public class RegionWareServiceImpl extends ServiceImpl<RegionWareMapper, RegionWare> implements RegionWareService {

    @Override
    public IPage<RegionWare> selectPageRegionWare(Page<RegionWare> pageParm, RegionWareQueryVo regionWareQueryVo) {
        String keyword=regionWareQueryVo.getKeyword();
        LambdaQueryWrapper<RegionWare>wrapper=new LambdaQueryWrapper<>();
        if(!StringUtils.isEmpty(keyword)){
            //根据区域名称或者仓库名称查询
            wrapper.like(RegionWare::getRegionName,keyword).or().like(RegionWare::getWareName,keyword);
        }
        IPage<RegionWare> page = baseMapper.selectPage(pageParm, wrapper);
        return page;
    }

    //添加开通区域
    @Override
    public void saveRegionWare(RegionWare regionWare) {
        LambdaQueryWrapper<RegionWare> wrapper=new LambdaQueryWrapper<>();
        wrapper.eq(RegionWare::getRegionId,regionWare.getRegionId());
        Integer count=baseMapper.selectCount(wrapper);
        if(count>0){//表示已经存在
            throw  new yxException(ResultCodeEnum.REGION_OPEN);
        }
        baseMapper.insert(regionWare);

    }

    @Override
    public void updateStatus(Long id, Integer status) {
        RegionWare regionWare=baseMapper.selectById(id);
        regionWare.setStatus(status);

        baseMapper.updateById(regionWare);

    }
}
