package com.yzx.server.service;

import com.yzx.server.bean.TestEntity;
import com.yzx.server.dao.TestDao;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class TestServiceImpl implements TestService{
    @Resource
    private TestDao dao;
    @Override
    public void test() {
        String test = dao.getUserById();
        System.out.println(test);
        System.out.println("TestServiceImpl");
    }
}
