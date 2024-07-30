package com.wx.YX.search.repository;

import com.wx.YX.model.search.SkuEs;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface SkureRepository extends ElasticsearchRepository<SkuEs, Long>  {
}
