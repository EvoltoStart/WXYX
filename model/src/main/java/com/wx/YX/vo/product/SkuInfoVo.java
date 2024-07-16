package com.wx.YX.vo.product;

import com.atguigu.ssyx.model.product.*;
import com.wx.YX.model.product.SkuAttrValue;
import com.wx.YX.model.product.SkuImage;
import com.wx.YX.model.product.SkuInfo;
import com.wx.YX.model.product.SkuPoster;
import com.wx.YX.model.product.SkuAttrValue;
import com.wx.YX.model.product.SkuImage;
import com.wx.YX.model.product.SkuInfo;
import com.wx.YX.model.product.SkuPoster;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
public class SkuInfoVo extends SkuInfo {

	@ApiModelProperty(value = "海报列表")
	private List<SkuPoster> skuPosterList;

	@ApiModelProperty(value = "属性值")
	private List<SkuAttrValue> skuAttrValueList;

	@ApiModelProperty(value = "图片")
	private List<SkuImage> skuImagesList;

}

