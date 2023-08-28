# EasyIOC容器的实现

实现了一个简易的IOC容器，暂时省略了用构造器而是用注解来进行一个依赖注入。

接下来来简单说一下项目中的一个流程

# 流程

## 获取所有BeanDefinition

我们先通过自定的ContextPathXmlApplicationContext来获取xml文件，并且通过loadBeanDefinitions方法将其转换成字节流来获取其定义下的是否有被@Component注解的类，如果有则将其添加到专门存储BeanDefinition的beanDefinitionMap容器中。

## 生成Bean实例

### 首先

当获取完xml文件指定范围中BeanDefinition后，我们通过prepareRegister()方法来获取所有Bean实例并存放在singletonObjects容器中。

### 获取Bean

首先先查看是否有Bean（单例模式），如果没有则调用createBean方法来创造一个Bean。将对应的BeanDefinition传递过去，通过instantiateBean方法来创建Bean，instantiateBean通过Beandefinition来获取类名，并通过反射来获取类的一个构造器来构造该类的一个实例后返回。

### 注入属性

通过populateBean方法来对对象进行一个属性注入，通过反射来获取Bean中的所有字段，并且判断字段是否被@Autowierd注解注释，如果有则进行一个依赖注入。

### 最后

生成了Bean后通过调用registerSingleton方法（单例模式）来讲Bean添加到容器中。

## 测试

我们创建了一个NumberA，和一个NumberB（模拟service），我们假定NumberB依赖如NumberA，并且通过@Autowired注释在NumberA上。

我们在Application中来测试，将NumberB注册到容器中，判断是否有注册到，并且判断NumberA是否有被注入。
