package com.wx.YX.controller;

import com.wx.YX.activity.client.ActivityFenignClient;
import com.wx.YX.common.auth.AuthContextHolder;
import com.wx.YX.common.result.Result;
import com.wx.YX.model.order.CartInfo;
import com.wx.YX.service.CartInfoService;
import com.wx.YX.vo.order.OrderConfirmVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
@Api(tags = "购物车接口")
@RestController
@RequestMapping("/api/cart")
public class CartApiController {



    @Autowired
    private CartInfoService cartInfoService;
    @Autowired
    private ActivityFenignClient activityFenignClient;

    /**
     * 查询购物车列表
     *
     * @param request
     * @return
     */
    @ApiOperation("查询购物车列表")
    @GetMapping("cartList")
    public Result cartList(HttpServletRequest request) {
        // 获取用户Id
        Long userId = AuthContextHolder.getUserId();
        List<CartInfo> cartInfoList = cartInfoService.getCartList(userId);
        return Result.ok(cartInfoList);
    }

    /**
     * 添加购物车
     *
     * @param skuId
     * @param skuNum
     * @return
     */
    @ApiOperation(value = "添加购物车")
    @GetMapping("addToCart/{skuId}/{skuNum}")
    public Result addToCart(@PathVariable("skuId") Long skuId,
                            @PathVariable("skuNum") Integer skuNum) {
        // 如何获取userId
        Long userId = AuthContextHolder.getUserId();
        cartInfoService.addToCart(userId,skuId, skuNum);
        return Result.ok();
    }

    /**
     * 删除
     *
     * @param skuId
     * @param request
     * @return
     */
    @ApiOperation("删除购物车某商品")
    @DeleteMapping("deleteCart/{skuId}")
    public Result deleteCart(@PathVariable("skuId") Long skuId,
                             HttpServletRequest request) {
        // 如何获取userId
        Long userId = AuthContextHolder.getUserId();
        cartInfoService.deleteCart(skuId, userId);
        return Result.ok();
    }

    @ApiOperation(value="清空购物车")
    @DeleteMapping("deleteAllCart")
    public Result deleteAllCart(HttpServletRequest request){
        // 如何获取userId
        Long userId = AuthContextHolder.getUserId();
        cartInfoService.deleteAllCart(userId);
        return Result.ok();
    }

    @ApiOperation(value="批量删除购物车")
    @PostMapping("batchDeleteCart")
    public Result batchDeleteCart(@RequestBody List<Long> skuIdList, HttpServletRequest request){
        // 如何获取userId
        Long userId = AuthContextHolder.getUserId();
        cartInfoService.batchDeleteCart(skuIdList, userId);
        return Result.ok();
    }

    /**
     * 查询带优惠卷的购物车
     *
     * @return
     */
    @ApiOperation("查询带优惠卷的购物车")
    @GetMapping("activityCartList")
    public Result activityCartList() {
        // 获取用户Id
        Long userId = AuthContextHolder.getUserId();
        List<CartInfo> cartInfoList = cartInfoService.getCartList(userId);

        OrderConfirmVo orderTradeVo = activityFenignClient.findCartActivityAndCoupon(cartInfoList,userId);
        return Result.ok(orderTradeVo);
    }
}
