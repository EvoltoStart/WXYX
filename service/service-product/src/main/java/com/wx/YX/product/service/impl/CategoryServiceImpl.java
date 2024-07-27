package com.wx.YX.product.service.impl;

import com.wx.YX.product.entity.Category;
import com.wx.YX.product.mapper.CategoryMapper;
import com.wx.YX.product.service.CategoryService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 商品三级分类 服务实现类
 * </p>
 *
 * @author meng
 * @since 2024-07-28
 */
@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {

}
