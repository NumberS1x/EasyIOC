package com.example.easyioc;

/**
 * @author geekSix
 * @create 2023/8/27 16:24
 * 存放Bean的一些属性，如Bean的id，name等等
 */


public class BeanDefinition {
//    id是bean的唯一表示，一般是类名第一个字母小写
    private String id;
//    bean的class类路径
    private String beanClassName;

    public BeanDefinition(String id,String beanClassName){
        this.id = id;
        this.beanClassName = beanClassName;
    }

    public String getId() {
        return id;
    }

    public String getBeanClassName() {
        return this.beanClassName;
    }
}
