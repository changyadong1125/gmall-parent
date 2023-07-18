package com.atguigu.gmall.list.repository;

import com.atguigu.gmall.model.list.Goods;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

/**
 * project:gmall-parent
 * package:com.atguigu.gmall.list.repository
 * class:GoodsRepository
 *
 * @author: smile
 * @create: 2023/7/17-15:24
 * @Version: v1.0
 * @Description:
 */
public interface GoodsRepository extends ElasticsearchRepository<Goods,Long> {
}
