package com.autoparts.warehouse.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.autoparts.warehouse.entity.PartCategory;

import java.util.List;

public interface PartCategoryService extends IService<PartCategory> {
    List<PartCategory> listAll(String keyword);
}
