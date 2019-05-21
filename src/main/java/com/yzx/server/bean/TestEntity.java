package com.yzx.server.bean;

import org.apache.ibatis.type.Alias;

@Alias("Test")
public class TestEntity {
    private String id;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
