package com.example.easyioc;

/**
 * @author geeksix
 * @create 2023/8/27 16:28
 */
public interface BeanFactory {
    Object getBean(String beanId);
}
