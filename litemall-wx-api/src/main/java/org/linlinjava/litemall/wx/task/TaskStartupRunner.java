package org.linlinjava.litemall.wx.task;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.linlinjava.litemall.core.system.SystemConfig;
import org.linlinjava.litemall.core.task.TaskService;
import org.linlinjava.litemall.db.domain.LitemallOrder;
import org.linlinjava.litemall.db.service.LitemallOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Component
public class TaskStartupRunner implements ApplicationRunner {

    @Autowired
    private LitemallOrderService orderService;
    @Autowired
    private TaskService taskService;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        List<LitemallOrder> orderList = orderService.queryUnpaid(SystemConfig.getOrderUnpaid());
        for(LitemallOrder order : orderList){
            LocalDateTime add = order.getAddTime();
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime expire =  add.plusMinutes(SystemConfig.getOrderUnpaid());

            // 已经支付，但没有收到通知，状态还没有改变的情况
            if (StringUtils.hasText(order.getPayType())) {
                // 增加查询订单状态的任务，只有未完成支付的才需要查询
                taskService.addTask(new OrderStatusQueryTask(order.getId()));
            } else {
                if(expire.isBefore(now)) {
                    // 已经过期，则加入延迟队列
                    taskService.addTask(new OrderUnpaidTask(order.getId(), 0));
                }
                else{
                    // 还没过期，则加入延迟队列
                    long delay = ChronoUnit.MILLIS.between(now, expire);
                    taskService.addTask(new OrderUnpaidTask(order.getId(), delay));
                }
            }
        }
    }
}