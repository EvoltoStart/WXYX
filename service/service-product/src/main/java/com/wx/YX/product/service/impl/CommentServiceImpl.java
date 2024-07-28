package com.wx.YX.product.service.impl;

import com.wx.YX.model.product.Comment;
import com.wx.YX.product.mapper.CommentMapper;
import com.wx.YX.product.service.CommentService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 商品评价 服务实现类
 * </p>
 *
 * @author meng
 * @since 2024-07-28
 */
@Service
public class CommentServiceImpl extends ServiceImpl<CommentMapper, Comment> implements CommentService {

}
