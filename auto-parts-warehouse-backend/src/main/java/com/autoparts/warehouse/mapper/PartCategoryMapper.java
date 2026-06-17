package com.autoparts.warehouse.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.autoparts.warehouse.entity.PartCategory;
import org.apache.ibatis.annotations.Delete;

public interface PartCategoryMapper extends BaseMapper<PartCategory> {
    // 根据分类名称删除已删除的记录
    @Delete("DELETE FROM part_category WHERE name = #{name} AND deleted = 1")
    int deleteDeletedByName(String name);
}