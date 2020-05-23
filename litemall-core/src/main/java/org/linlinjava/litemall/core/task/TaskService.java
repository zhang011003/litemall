package org.linlinjava.litemall.core.task;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import javax.annotation.PostConstruct;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.Executors;

@Component
@Slf4j
public class TaskService {
    private TaskService taskService;
    private DelayQueue<Task> delayQueue =  new DelayQueue<Task>();

    @PostConstruct
    private void init() {
        taskService = this;

        Executors.newSingleThreadExecutor().execute(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        Task task = delayQueue.take();
                        task.run();
                        if (task.needReenterQueue()) {
                            task.reset();
                            addTask(task);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    public void addTask(Task task){
        if(delayQueue.contains(task)){
            log.debug("Task is contained in queue ,can not be added, task:{}", task);
            return;
        }
        log.debug("Task is added to queue ,task:{}", task);
        delayQueue.add(task);
    }

    public void removeTask(Task task){
        boolean remove = delayQueue.remove(task);
        if (remove) {
            log.debug("Task is removed from queue ,task={}", task);
        }
    }

}
