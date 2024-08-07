package com.wx.YX.search.repository;

import com.wx.YX.model.search.SkuEs;
import org.springframework.data.domain.Page;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.awt.print.Pageable;
import java.util.List;

public interface SkureRepository extends ElasticsearchRepository<SkuEs, Long>  {

    Page<SkuEs> findByOrderByHotScoreDesc(Pageable pageable);
}
