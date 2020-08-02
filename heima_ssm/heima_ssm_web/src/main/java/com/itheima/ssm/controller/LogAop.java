package com.itheima.ssm.controller;

import com.itheima.ssm.domain.SysLog;
import com.itheima.ssm.service.ISysLogService;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.util.Date;

@Component
@Aspect
public class LogAop {

    @Autowired
    private HttpServletRequest request;

    @Autowired
    private ISysLogService sysLogService;

    private Date visitTime;//开始时间
    private Class clazz;//访问的类
    private Method method;//访问的方法


    //前置通知 主要获取开始时间，执行的是哪一个类，哪一个方法
    @Before("execution(* com.itheima.ssm.controller.*.*(..))")
    public void doBefore(JoinPoint jp) throws NoSuchMethodException {
        visitTime = new Date();//当前时间是开始访问的时间
        clazz = jp.getTarget().getClass();
        String methodName = jp.getSignature().getName();
        Object[] args = jp.getArgs();
        if(args==null||args.length==0){
            method = clazz.getMethod(methodName);//只能获取无参数的方法
        }else {
            //获取有参数的方法
            Class[] classArgs = new Class[args.length];
            for (int i = 0; i <args.length ; i++) {
                classArgs[i] = args[i].getClass();
            }
            method = clazz.getMethod(methodName,classArgs);
        }

    }

    //后置通知
    @After("execution(* com.itheima.ssm.controller.*.*(..))")
    public void doAfter(JoinPoint jp) throws Exception {
        //获取时长
        long time = new Date().getTime()-visitTime.getTime();

        //获取url 通过反射技术
        String url ="";
        if(clazz!=null&&method!=null&&clazz!=LogAop.class){
            //获取类上的@RequestMapping("/orders")
            RequestMapping classAnnotation = (RequestMapping)clazz.getAnnotation(RequestMapping.class);
            if(classAnnotation!=null){
                String[] classValue = classAnnotation.value();

                //获取方法上的@RequestMapping("/findAll.do")
                RequestMapping methodAnnotation = method.getAnnotation(RequestMapping.class);
                if(methodAnnotation!=null){
                    String[] methodValue = methodAnnotation.value();
                    url = classValue[0]+methodValue[0];

                    //获取IP地址 在web.xml中配置监听器RequestContextListener
                    String ip = request.getRemoteAddr();

                    //获取当前的操作者
                    SecurityContext context = SecurityContextHolder.getContext();//从上下文中获取当前的登录用户
                    User user = (User)context.getAuthentication().getPrincipal();
                    String username = user.getUsername();

                    //将日志相关信息封装SysLog
                    SysLog sysLog = new SysLog();
                    sysLog.setExecutionTime(time);
                    sysLog.setIp(ip);
                    sysLog.setUsername(username);
                    sysLog.setUrl(url);
                    sysLog.setMethod("[类名] "+clazz.getName()+"[方法名] "+method.getName());
                    sysLog.setVisitTime(visitTime);

                    //调用Service操作
                    sysLogService.save(sysLog);
                }
            }
        }
    }
}
