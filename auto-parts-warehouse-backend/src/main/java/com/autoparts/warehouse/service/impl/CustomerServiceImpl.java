package com.autoparts.warehouse.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.autoparts.warehouse.entity.Customer;
import com.autoparts.warehouse.mapper.CustomerMapper;
import com.autoparts.warehouse.service.CustomerService;
import org.springframework.stereotype.Service;

@Service
public class CustomerServiceImpl extends ServiceImpl<CustomerMapper, Customer> implements CustomerService {
}