package com.wx.YX.sys.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.wx.YX.model.sys.Region;

import java.util.List;

/**
 * <p>
 * 地区表 服务类
 * </p>
 *
 * @author meng
 * @since 2024-07-26
 */
public interface RegionService extends IService<Region> {

    List<Region> getRegionByKeyword(String keyword);

    List<Region> getRegionByParentId(Long parentId);
}
