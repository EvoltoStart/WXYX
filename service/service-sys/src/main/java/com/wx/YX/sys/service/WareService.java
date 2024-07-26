package com.wx.YX.sys.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.wx.YX.model.sys.Ware;

/**
 * <p>
 * 仓库表 服务类
 * </p>
 *
 * @author meng
 * @since 2024-07-26
 */
public interface WareService extends IService<Ware> {

    void saveWare(Ware ware);
}
