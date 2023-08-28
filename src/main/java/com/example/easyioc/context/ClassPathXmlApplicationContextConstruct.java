package com.example.easyioc.context;

import com.example.easyioc.BeanDefinition;
import com.example.easyioc.BeanFactory;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.springframework.util.StringUtils;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;

/**
 * @author geeksix
 * @create 2023/8/27 17:02
 */
public class ClassPathXmlApplicationContextConstruct{

    //    存放bean定义
    private final HashMap<String, BeanDefinition> beanDefinitionMap = new HashMap<>(64);

    //    存放bean实例
    private final HashMap<String, Object> singletonObjects = new HashMap<>(64);

    public ClassPathXmlApplicationContextConstruct(String configFile){

    }


    private void loadBeanDefinitions(String configFile){

//        将文件转换成字节流
        try(InputStream io = Thread.currentThread().getContextClassLoader().getResourceAsStream(configFile)) {

//            将字节流转换成文档格式
            SAXReader reader = new SAXReader();
            Document document = reader.read(io);
//            获取文档中的根元素,也是全部数据
            Element root = document.getRootElement();
//            获取根元素的迭代器,用来遍历所有子元素,也就是xml文件中的一个个bean标签
            Iterator iterator = root.elementIterator();

            while(iterator.hasNext()){
//                获取子元素
                Element ele = (Element) iterator.next();
//              有两种方法获取beanDefinition,一种是通过构造方法,在xml中用ref-arg,但是这种方法会导致xml配置文件过于复杂,
//              所以这边使用注解来简化bean的初始化和注入，@Component注解的作用就是<bean>,@Autowired注解的作用就是<constructor-arg>
                String id = ele.attributeValue("id");
                String className = ele.attributeValue("class");
                BeanDefinition bd = new BeanDefinition(id,className);
            }


        } catch (Exception e) {
            e.printStackTrace();
        }

    }

//    解析bean中指定的构造方法进行依赖注入
    private void parsConstructorArgElement(Element beanElement, BeanDefinition bd){
//        获得Element元素的迭代器，来遍历其子元素，例如<construct-arg>
//        获取 XML 元素 <bean> 下的所有名为 "construct-arg" 的子元素的迭代器。
        Iterator iterator = beanElement.elementIterator("construct-arg");
        while(iterator.hasNext()){
            Element ele = (Element) iterator.next();
//            获得构造方法的名字
            String argName = ele.attributeValue("ref");
            if (!StringUtils.hasLength(argName)){
                return;
            }
        }
    }

}
