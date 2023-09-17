package com.lagou.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lagou.domain.Product;

import java.util.List;

public interface ProductService extends IService<Product> {

    List<Product> findAll();

}
