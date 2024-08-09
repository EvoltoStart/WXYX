package com.wx.YX.search.repository;

import com.wx.YX.model.search.SkuEs;
import org.springframework.data.domain.Page;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.awt.print.Pageable;
import java.util.List;

public interface SkureRepository extends ElasticsearchRepository<SkuEs, Long>  {

    //spring Date帮我们进行实现

    Page<SkuEs> findByOrderByHotScoreDesc(Pageable pageable);
    //判断keyword是否为空，空根据仓库id+分类id查询
    Page<SkuEs> findByCategoryIdAndWareId(Long categoryId, Long wareId, org.springframework.data.domain.Pageable pageable);

    Page<SkuEs> findByCategoryIdAndWareId(String keyword, Long wareId, org.springframework.data.domain.Pageable pageable);
    //keyword不为空 根据仓库id+keyword查询
   
}
