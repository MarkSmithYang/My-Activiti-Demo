package com.yb.activiti;

import org.activiti.engine.*;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.engine.task.TaskQuery;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.CollectionUtils;

import javax.sql.DataSource;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ActivitiApplicationTests {

    @Autowired
    private ProcessEngine processEngine;

    //一般的流程步骤:
    //1.流程部署
    //2.启动流程实例
    //3.查看任务
    //4.完成任务

    /**
     * 1.流程部署
     */
    @Test
    public void deploy() {
        //获取仓库服务 ：管理流程定义
        RepositoryService repositoryService = processEngine.getRepositoryService();
        Deployment deploy = repositoryService
                //创建一个部署的构建器
                .createDeployment()
                //从类路径中添加资源,一次只能添加一个资源
                .addClasspathResource("diagrams/activiti.bpmn")
                //设置部署的名称
                .name("请求单流程")
                //设置部署的类别
                .category("办公类别")
                //部署
                .deploy();

        System.err.println("部署的id" + deploy.getId());
        System.err.println("部署的名称" + deploy.getName());
    }

    /**
     * 2.启动流程实例
     */
    @Test
    public void startProcess() {
        ProcessInstance pi = processEngine.getRuntimeService()
                .startProcessInstanceByKey("_2");
        //流程实例id
        System.err.println("流程实例id:" + pi.getId());
        //输出流程定义的id
        System.err.println("流程定义id:" + pi.getProcessDefinitionId());
    }

    /**
     * 3.查看任务
     */
    @Test
    public void queryTask() {
        //创建一个任务查询对象
        TaskQuery taskQuery = processEngine.getTaskService().createTaskQuery();
        //办理人的任务列表
        List<Task> list = taskQuery.taskAssignee("小明").list();
        //处理数据
        if (!CollectionUtils.isEmpty(list)) {
            list.forEach(task -> {
                System.err.println("任务的办理人：" + task.getAssignee());
                System.err.println("任务的id：" + task.getId());
                System.err.println("任务的名称：" + task.getName());
            });
        }
    }

    /**
     * 4.完成任务
     */
    @Test
    public void completeTask() {
        //任务id
        String taskId = "15002";
        //完成任务
        processEngine.getTaskService().complete(taskId);
        System.err.println("任务完成");
    }

}
