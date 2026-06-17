package com.autoparts.warehouse.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.autoparts.warehouse.entity.Part;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Delete;

@Mapper
public interface PartMapper extends BaseMapper<Part> {
    // 物理删除记录，绕过逻辑删除
    @Delete("DELETE FROM part_info WHERE id = #{id}")
    int deletePhysicallyById(Long id);
    
    // 根据配件编码删除已删除的记录
    @Delete("DELETE FROM part_info WHERE part_code = #{partCode} AND deleted = 1")
    int deleteDeletedByPartCode(String partCode);
}
