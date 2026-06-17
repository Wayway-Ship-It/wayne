package com.autoparts.warehouse.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.autoparts.warehouse.entity.Stock;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface StockMapper extends BaseMapper<Stock> {
}
