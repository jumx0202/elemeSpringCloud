package org.example.config;

import com.alibaba.csp.sentinel.init.InitExecutor;
import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRule;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRuleManager;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import com.alibaba.csp.sentinel.slots.system.SystemRule;
import com.alibaba.csp.sentinel.slots.system.SystemRuleManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

/**
 * Sentinel 全局配置
 */
@Slf4j
@Configuration
public class SentinelConfig {
    
    /**
     * 初始化Sentinel规则
     */
    @Bean
    public CommandLineRunner initSentinelRules() {
        return args -> {
            log.info("开始初始化Sentinel规则");
            
            // 初始化Sentinel
            InitExecutor.doInit();
            
            // 配置限流规则
            initFlowRules();
            
            // 配置熔断降级规则
            initDegradeRules();
            
            // 配置系统规则
            initSystemRules();
            
            log.info("Sentinel规则初始化完成");
        };
    }
    
    /**
     * 初始化限流规则
     */
    private void initFlowRules() {
        List<FlowRule> rules = new ArrayList<>();
        
        // 用户服务限流规则
        FlowRule userRule = new FlowRule();
        userRule.setResource("user-service");
        userRule.setCount(100);
        userRule.setGrade(RuleConstant.FLOW_GRADE_QPS);
        userRule.setLimitApp("default");
        userRule.setStrategy(RuleConstant.STRATEGY_DIRECT);
        userRule.setControlBehavior(RuleConstant.CONTROL_BEHAVIOR_DEFAULT);
        rules.add(userRule);
        
        // 商家服务限流规则
        FlowRule businessRule = new FlowRule();
        businessRule.setResource("business-service");
        businessRule.setCount(150);
        businessRule.setGrade(RuleConstant.FLOW_GRADE_QPS);
        businessRule.setLimitApp("default");
        businessRule.setStrategy(RuleConstant.STRATEGY_DIRECT);
        businessRule.setControlBehavior(RuleConstant.CONTROL_BEHAVIOR_DEFAULT);
        rules.add(businessRule);
        
        // 食物服务限流规则
        FlowRule foodRule = new FlowRule();
        foodRule.setResource("food-service");
        foodRule.setCount(200);
        foodRule.setGrade(RuleConstant.FLOW_GRADE_QPS);
        foodRule.setLimitApp("default");
        foodRule.setStrategy(RuleConstant.STRATEGY_DIRECT);
        foodRule.setControlBehavior(RuleConstant.CONTROL_BEHAVIOR_DEFAULT);
        rules.add(foodRule);
        
        // 订单服务限流规则
        FlowRule orderRule = new FlowRule();
        orderRule.setResource("order-service");
        orderRule.setCount(80);
        orderRule.setGrade(RuleConstant.FLOW_GRADE_QPS);
        orderRule.setLimitApp("default");
        orderRule.setStrategy(RuleConstant.STRATEGY_DIRECT);
        orderRule.setControlBehavior(RuleConstant.CONTROL_BEHAVIOR_DEFAULT);
        rules.add(orderRule);
        
        // 支付服务限流规则
        FlowRule paymentRule = new FlowRule();
        paymentRule.setResource("payment-service");
        paymentRule.setCount(50);
        paymentRule.setGrade(RuleConstant.FLOW_GRADE_QPS);
        paymentRule.setLimitApp("default");
        paymentRule.setStrategy(RuleConstant.STRATEGY_DIRECT);
        paymentRule.setControlBehavior(RuleConstant.CONTROL_BEHAVIOR_DEFAULT);
        rules.add(paymentRule);
        
        // 通知服务限流规则
        FlowRule notificationRule = new FlowRule();
        notificationRule.setResource("notification-service");
        notificationRule.setCount(120);
        notificationRule.setGrade(RuleConstant.FLOW_GRADE_QPS);
        notificationRule.setLimitApp("default");
        notificationRule.setStrategy(RuleConstant.STRATEGY_DIRECT);
        notificationRule.setControlBehavior(RuleConstant.CONTROL_BEHAVIOR_DEFAULT);
        rules.add(notificationRule);
        
        // 验证码服务限流规则
        FlowRule captchaRule = new FlowRule();
        captchaRule.setResource("captcha-service");
        captchaRule.setCount(300);
        captchaRule.setGrade(RuleConstant.FLOW_GRADE_QPS);
        captchaRule.setLimitApp("default");
        captchaRule.setStrategy(RuleConstant.STRATEGY_DIRECT);
        captchaRule.setControlBehavior(RuleConstant.CONTROL_BEHAVIOR_DEFAULT);
        rules.add(captchaRule);
        
        FlowRuleManager.loadRules(rules);
        log.info("限流规则加载完成，规则数量：{}", rules.size());
    }
    
    /**
     * 初始化熔断降级规则
     */
    private void initDegradeRules() {
        List<DegradeRule> rules = new ArrayList<>();
        
        // 创建订单熔断规则
        DegradeRule createOrderRule = new DegradeRule();
        createOrderRule.setResource("createOrder");
        createOrderRule.setGrade(RuleConstant.DEGRADE_GRADE_RT);
        createOrderRule.setCount(100); // 平均响应时间 100ms
        createOrderRule.setTimeWindow(10); // 熔断时长 10s
        createOrderRule.setMinRequestAmount(5); // 最小请求数
        createOrderRule.setSlowRatioThreshold(0.5); // 慢调用比例阈值
        createOrderRule.setStatIntervalMs(1000); // 统计时长
        rules.add(createOrderRule);
        
        // 支付处理熔断规则
        DegradeRule paymentRule = new DegradeRule();
        paymentRule.setResource("processPayment");
        paymentRule.setGrade(RuleConstant.DEGRADE_GRADE_RT);
        paymentRule.setCount(200); // 平均响应时间 200ms
        paymentRule.setTimeWindow(15); // 熔断时长 15s
        paymentRule.setMinRequestAmount(3); // 最小请求数
        paymentRule.setSlowRatioThreshold(0.6); // 慢调用比例阈值
        paymentRule.setStatIntervalMs(1000); // 统计时长
        rules.add(paymentRule);
        
        // 邮件发送熔断规则
        DegradeRule emailRule = new DegradeRule();
        emailRule.setResource("sendEmail");
        emailRule.setGrade(RuleConstant.DEGRADE_GRADE_RT);
        emailRule.setCount(500); // 平均响应时间 500ms
        emailRule.setTimeWindow(5); // 熔断时长 5s
        emailRule.setMinRequestAmount(3); // 最小请求数
        emailRule.setSlowRatioThreshold(0.4); // 慢调用比例阈值
        emailRule.setStatIntervalMs(1000); // 统计时长
        rules.add(emailRule);
        
        // 验证码生成熔断规则
        DegradeRule captchaRule = new DegradeRule();
        captchaRule.setResource("generateCaptcha");
        captchaRule.setGrade(RuleConstant.DEGRADE_GRADE_RT);
        captchaRule.setCount(50); // 平均响应时间 50ms
        captchaRule.setTimeWindow(3); // 熔断时长 3s
        captchaRule.setMinRequestAmount(5); // 最小请求数
        captchaRule.setSlowRatioThreshold(0.8); // 慢调用比例阈值
        captchaRule.setStatIntervalMs(1000); // 统计时长
        rules.add(captchaRule);
        
        // 异常比例熔断规则
        DegradeRule exceptionRule = new DegradeRule();
        exceptionRule.setResource("exception-ratio");
        exceptionRule.setGrade(RuleConstant.DEGRADE_GRADE_EXCEPTION_RATIO);
        exceptionRule.setCount(0.5); // 异常比例阈值 50%
        exceptionRule.setTimeWindow(10); // 熔断时长 10s
        exceptionRule.setMinRequestAmount(5); // 最小请求数
        exceptionRule.setStatIntervalMs(1000); // 统计时长
        rules.add(exceptionRule);
        
        // 异常数熔断规则
        DegradeRule exceptionCountRule = new DegradeRule();
        exceptionCountRule.setResource("exception-count");
        exceptionCountRule.setGrade(RuleConstant.DEGRADE_GRADE_EXCEPTION_COUNT);
        exceptionCountRule.setCount(10); // 异常数阈值 10个
        exceptionCountRule.setTimeWindow(10); // 熔断时长 10s
        exceptionCountRule.setMinRequestAmount(5); // 最小请求数
        exceptionCountRule.setStatIntervalMs(1000); // 统计时长
        rules.add(exceptionCountRule);
        
        DegradeRuleManager.loadRules(rules);
        log.info("熔断降级规则加载完成，规则数量：{}", rules.size());
    }
    
    /**
     * 初始化系统规则
     */
    private void initSystemRules() {
        List<SystemRule> rules = new ArrayList<>();
        
        // 系统负载规则
        SystemRule loadRule = new SystemRule();
        loadRule.setHighestSystemLoad(3.0); // 最高系统负载
        rules.add(loadRule);
        
        // 平均响应时间规则
        SystemRule avgRtRule = new SystemRule();
        avgRtRule.setAvgRt(25); // 平均响应时间 25ms
        rules.add(avgRtRule);
        
        // 最大线程数规则
        SystemRule maxThreadRule = new SystemRule();
        maxThreadRule.setMaxThread(10); // 最大线程数
        rules.add(maxThreadRule);
        
        // QPS规则
        SystemRule qpsRule = new SystemRule();
        qpsRule.setQps(20); // 最大QPS
        rules.add(qpsRule);
        
        // CPU使用率规则
        SystemRule cpuRule = new SystemRule();
        cpuRule.setHighestCpuUsage(0.7); // 最高CPU使用率 70%
        rules.add(cpuRule);
        
        SystemRuleManager.loadRules(rules);
        log.info("系统规则加载完成，规则数量：{}", rules.size());
    }
} 