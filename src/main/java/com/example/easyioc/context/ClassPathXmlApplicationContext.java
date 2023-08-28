package com.example.easyioc.context;

import com.example.easyioc.BeanDefinition;
import com.example.easyioc.BeanFactory;
import com.example.easyioc.annotation.Autowired;
import com.example.easyioc.annotation.Component;
import com.example.easyioc.config.AnnotationMetadata;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.springframework.asm.ClassReader;
import org.springframework.util.ClassUtils;
import org.springframework.validation.ObjectError;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.io.*;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.*;

/**
 * @author geeksix
 * @create 2023/8/27 16:29
 */
public class ClassPathXmlApplicationContext implements BeanFactory {

//    存放bean定义
    private final HashMap<String, BeanDefinition> beanDefinitionMap = new HashMap<>(64);

//    存放bean实例
    private final HashMap<String, Object> singletonObjects = new HashMap<>(64);

    public ClassPathXmlApplicationContext(String configFile){
        loadBeanDefinitions(configFile);
        prepareRegister();
    }


    /**
     *把所有被@Component注解注释的类注入到BeanDefination容器中
     * @param configFile
     */
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
                parseComponentElement(ele);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
    /**
     * ApplicationContext的特性,提前将所有Bean注入到容器中
     */
    private void prepareRegister(){
        for(String beanId: beanDefinitionMap.keySet()){
//            获取bean的定义
            BeanDefinition bd = this.beanDefinitionMap.get(beanId);
//            在单例模式在,查看是否已经生成了bean
            Object bean = this.getSingleton(beanId);
            if (bean == null){
                bean = createBean(bd);
                this.registerSingleton(beanId,bean);
            }
        }
    }

    /**
     * @param bd
     * @return
     */
    private Object createBean(BeanDefinition bd){
//        创建实例
        Object bean = instantiateBean(bd);
//        填充属性(依赖注入)
        populateBean(bean);
        return bean;
    }



    private Object instantiateBean(BeanDefinition bd){
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        String beanClassName = bd.getBeanClassName();
        try {
            Class<?> aClass = classLoader.loadClass(beanClassName);
            return aClass.newInstance();
        }catch (Exception e){
            System.out.println(e);
        }
        return null;
    }



    /**
     * 利用反射，将字段与对象关联
     * 没有 setter 方法，利用 Field 的field.set()；有 setter 方法，利用 Method 的 Method.invoke()
     *
     * @param bean
     */
    private void populateBean(Object bean){
        Field[] fields = bean.getClass().getDeclaredFields();
        try {
            for(Field field: fields){
                // 判断字段是否有 @Autowired 注解
                Annotation annotation = field.getAnnotation(Autowired.class);
                // 根据是否有 Autowired 注解来决定是否注入
                if (annotation != null){
                    // 实际上，这里不是简单的通过 name 获取依赖，而是根据类型获取 getAutowiredBean(bean)
                    Object value = getBean(field.getName());
                    if (value != null){
//                        设置字段可连接
                        field.setAccessible(true);
                        // 通过反射设置字段的值
                        field.set(bean,value);
                    }
                }

            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public Object getBean(String beanId){
        BeanDefinition bd = this.beanDefinitionMap.get(beanId);
        // 单例模式，一个类对应一个 Bean，不是通过 id。常规单例模式是多次调用方法，只生成一个实例。此处是只会调用依次生成实例方法。
        Object bean = this.getSingleton(beanId);
        if (bean == null){
            bean = createBean(bd);
            this.registerSingleton(beanId,bean);
        }
        return bean;
    }

    private void registerSingleton(String beanName, Object singletonObject) {
        Object oldObject = this.singletonObjects.get(beanName);
        if (oldObject != null) {
            System.out.println("error," + oldObject + " had already registered");
        }
        this.singletonObjects.put(beanName, singletonObject);
    }

    /**
     *
     * @param beanName
     * @return
     */
    private Object getSingleton(String beanName){
        return this.singletonObjects.get(beanName);
    }



    /**
     * 根据bean获取compone-scan指定的路径，找到路径下所有有@Component注解的Class文件，作为BeanDefinition
     * @param ele
     * @throws IOException
     */
    private void parseComponentElement(Element ele) throws IOException{
//        获取值为base-package的子元素
        String basePackageStr = ele.attributeValue("base-package");
//        获取路径
        String[] basePackage = basePackageStr.split(",");
//        从路径中获取所有Class文件
        File[] files = getFiles(basePackageStr);
//        遍历所有文件,判断是否有@Component注解
        for (File file : files){
            AnnotationMetadata annotationMetadata = getAnnotationMetadata(file);
//            判断是否有@Component注解
            if (annotationMetadata.hasAnnotation(Component.class.getName())){
                String beanId = (String) annotationMetadata.getAnnotationAttributes(Component.class.getName()).get("value");
                String beanClassName = annotationMetadata.getClassName();
                if (beanId == null){
//                    如果beanId为空,就通过路径获取类名并且小写
                    beanId = Introspector.decapitalize(ClassUtils.getShortName(beanClassName));
                }
                BeanDefinition bd = new BeanDefinition(beanId,beanClassName);
                this.beanDefinitionMap.put(beanId,bd);
            }
        }
    }




    /**
     * 利用字节码技术，将注解元数据存放在 AnnotationMetadata 中，一个 file 对应一个 AnnotationMetadata
     * <p>
     * 待优化：去除 AnnotationMetadata，直接获取注解
     *
     * @param file
     * @return
     * @throws IOException
     */
    public AnnotationMetadata getAnnotationMetadata(File file) throws IOException{

//        file相当于路径,根据路径获取当前当前文件的字节流
//        用到了Spring框架的ClassReader
        ClassReader classReader;
        try(InputStream io = new BufferedInputStream(new FileInputStream(file))){
            classReader = new ClassReader(io);
        }
        AnnotationMetadata visitor = new AnnotationMetadata();
//        利用classReader的字节码技术,冲文件流中读取元数据并设置到AnnotationMetadata中
        classReader.accept(visitor,ClassReader.SKIP_DEBUG);

        return visitor;
    }


    /**
     * 获取指定文件下的Class文件
     */
    private File[] getFiles(String basePackage) {
        String location = ClassUtils.convertClassNameToResourcePath(basePackage);
        URL url = Thread.currentThread().getContextClassLoader().getResource(location);
        File rootDir = new File(url.getFile());
        Set<File> matchingFiles = new LinkedHashSet<>(8);
        doRetrieveMatchingFiles(rootDir, matchingFiles);
        return matchingFiles.toArray(new File[0]);
    }


    /**
     * 通过递归获取文件夹下的文件
     */
    private void doRetrieveMatchingFiles(File dir, Set<File> result) {

        File[] dirContents = dir.listFiles();
        if (dirContents == null) {
            return;
        }
        for (File content : dirContents) {
            if (content.isDirectory()) {
                if (!content.canRead()) {
                } else {
                    doRetrieveMatchingFiles(content, result);
                }
            } else {
                result.add(content);
            }
        }
    }
}
