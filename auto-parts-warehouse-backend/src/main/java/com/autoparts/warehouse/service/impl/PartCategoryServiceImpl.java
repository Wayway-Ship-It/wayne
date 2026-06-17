package com.autoparts.warehouse.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.autoparts.warehouse.entity.PartCategory;
import com.autoparts.warehouse.mapper.PartCategoryMapper;
import com.autoparts.warehouse.service.PartCategoryService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PartCategoryServiceImpl extends ServiceImpl<PartCategoryMapper, PartCategory> implements PartCategoryService {
    @Override
    public List<PartCategory> listAll(String keyword) {
        QueryWrapper<PartCategory> queryWrapper = new QueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) {
            queryWrapper.like("name", keyword).or().like("description", keyword);
        }
        queryWrapper.orderByAsc("id");
        return list(queryWrapper);
    }

    @Override
    public boolean save(PartCategory category) {
        try {
            // 先尝试删除已删除的相同名称记录
            baseMapper.deleteDeletedByName(category.getName());
            // 再保存新分类
            return super.save(category);
        } catch (Exception e) {
            // 如果仍然冲突，说明有未删除的记录存在
            if (e.getMessage() != null && e.getMessage().contains("Duplicate entry") && e.getMessage().contains("name")) {
                throw new RuntimeException("分类名称已存在");
            }
            // 其他错误直接抛出
            throw e;
        }
    }
}
