package com.example.easyioc;

import com.example.easyioc.context.ClassPathXmlApplicationContext;
import com.example.easyioc.moudle.NumberBConfig;
import com.example.easyioc.moudle.service.NumberB;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * @author geeksix
 * @create 2023/8/27 20:18
 */
public class Application {
    public static void main(String[] args){
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("petstore-v4.xml");
        NumberB numberB = (NumberB) context.getBean("numberB");
        System.out.println(numberB.getNumberA());
        System.out.println(numberB.getName());
    }
}
