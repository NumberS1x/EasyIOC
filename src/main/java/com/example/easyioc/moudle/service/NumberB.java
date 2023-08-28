package com.example.easyioc.moudle.service;

import com.example.easyioc.annotation.Autowired;
import com.example.easyioc.annotation.Component;
import com.example.easyioc.moudle.dao.NumberA;

/**
 * @author geeksix
 * @create 2023/8/27 20:13
 */
@Component(value = "numberB")
public class NumberB {
    @Autowired
    private NumberA numberA;

    String name = "numberB";

    public NumberA getNumberA(){
        return this.numberA;
    }

    public String getName() {
        return name;
    }
}
