package com.yb.activiti.config;

import org.activiti.engine.*;
import org.activiti.engine.impl.cfg.StandaloneProcessEngineConfiguration;
import org.activiti.engine.repository.Deployment;
import org.activiti.spring.SpringProcessEngineConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Description:添加配置文件，注入activiti的service到spring中,方便直接注入使用
 * author biaoyang
 * date 2019/2/21 002111:54
 */
@Configuration
public class ActivitiConfiguration {
    private static final Logger log = LoggerFactory.getLogger(ActivitiConfiguration.class);

    @Autowired
    private DataSource dataSource;
    @Autowired
    private PlatformTransactionManager platformTransactionManager;

    /**
     * 初始化配置,将创建20多张引擎基础表(实例化了引擎,也创建了表)
     * StandaloneProcessEngineConfiguration extends ProcessEngineConfigurationImpl
     * 所以实例化StandaloneProcessEngineConfiguration还是SpringProcessEngineConfiguration效果一样,后者功能更多
     * SpringProcessEngineConfiguration extends ProcessEngineConfigurationImpl implements ApplicationContextAware
     * ProcessEngineConfigurationImpl extends ProcessEngineConfiguration
     *------------------------------>最好就是为这些表单独建个库来存储,最好不要沿用业务
     * @return
     */
    @Bean
    public SpringProcessEngineConfiguration springProcessEngineConfiguration() {
        //创建的基础表有25个,6版本好像有28个,主要有ACT_HI_*(历史信息表),ACT_RU_*(运行时信息表),ACT_ID_*(身份信息类),ACT_RE_*(静态资源类)
        SpringProcessEngineConfiguration spec = new SpringProcessEngineConfiguration();
        //注意当执行了一次,建立了表之后,就改为false吧,可以节省些性能(不用再去检查更新了)
        spec.setDatabaseSchemaUpdate("true");
        //使用配置文件里的数据库连接
        spec.setDataSource(dataSource);
        //异步处理流程--false(不异步)
        spec.setAsyncExecutorActivate(false);
        //事务控制
        spec.setTransactionManager(platformTransactionManager);

        //Resource[] resources = null;
        //@@比较简略的部署方式,可以匹配多个文件
        //try {
        //    resources = new PathMatchingResourcePatternResolver()
        //           .getResources("classpath*:diagrams/activiti.bpmn");
        //} catch (IOException e) {
        //   log.info("类ActivitiConfiguration里的方法获取不到resource配置文件信息");
        //}
        //设置部署数据源
        //spec.setDeploymentResources(resources);
        //设置部署的名称
        //spec.setDeploymentName("请假流程");

//&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&

        //@@比较详细部署(可以设置很多的东西)
        Deployment deploy = spec.buildProcessEngine().getRepositoryService()
                //创建一个部署的构建器
                .createDeployment()
                //从类路径中添加资源,一次只能添加一个资源
                .addClasspathResource("diagrams/activiti.bpmn")
                //.addClasspathResource("diagrams/activiti_copy.bpmn")//这个图解析有点问题
                //设置部署的名称
                .name("请假流程")
                //设置部署的类别
                .category("办公类别")
                //部署
                .deploy();
        return spec;
    }

    /**
     * 来创建工作引擎
     * 可以通过上面的processEngine.getXXXX来获取RepositoryService,
     * RuntimeService,TaskService,HistoryService而不用逐个实例化逐个注入
     */
    @Bean
    public ProcessEngine processEngine() {
        return springProcessEngineConfiguration()
                .buildProcessEngine();
    }
}
