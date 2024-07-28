package com.wx.YX.product.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.wx.YX.model.product.AttrGroup;
import com.wx.YX.vo.product.AttrGroupQueryVo;
import com.wx.YX.vo.product.CategoryQueryVo;

import java.util.List;

/**
 * <p>
 * 属性分组 服务类
 * </p>
 *
 * @author meng
 * @since 2024-07-28
 */
public interface AttrGroupService extends IService<AttrGroup> {


    //平台属性分组列表
    IPage<AttrGroup> selectPage(Page<AttrGroup> pageParam, AttrGroupQueryVo attrGroupQueryVo);

    //查询所有属性分组
    List<AttrGroup> findAllList();
}
