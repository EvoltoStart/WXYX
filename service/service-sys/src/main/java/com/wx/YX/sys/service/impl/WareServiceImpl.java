package com.wx.YX.sys.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wx.YX.common.exception.yxException;
import com.wx.YX.common.result.ResultCodeEnum;
import com.wx.YX.model.sys.Ware;
import com.wx.YX.sys.mapper.WareMapper;
import com.wx.YX.sys.service.WareService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 仓库表 服务实现类
 * </p>
 *
 * @author meng
 * @since 2024-07-26
 */
@Service
public class WareServiceImpl extends ServiceImpl<WareMapper, Ware> implements WareService {

    @Override
    public void saveWare(Ware ware) {

        LambdaQueryWrapper<Ware> wrapper=new LambdaQueryWrapper<>();
        wrapper.eq(Ware::getName,ware.getName());
        Integer count= baseMapper.selectCount(wrapper);
        if(count>0){
            throw new yxException(ResultCodeEnum.WARE_EXISTS);
        }
        baseMapper.insert(ware);
    }
}
