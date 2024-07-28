package com.wx.YX.product.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.wx.YX.model.product.Attr;

import java.util.List;

/**
 * <p>
 * 商品属性 服务类
 * </p>
 *
 * @author meng
 * @since 2024-07-28
 */
public interface AttrService extends IService<Attr> {

    List<Attr> selectAttrList(Long groupId);

    Object findByAttrGroupId(Long attrGroupId);
}
