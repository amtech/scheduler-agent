package com.sinosoft.one.scheduler.agent.core;

import org.quartz.*;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.MethodDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.Map;

/**
 * Created by bin on 14-5-15.
 * 负责创建trigger临时文件和初始化trigger
 */
public class AgentTrigger {

    /**
     *创建trigger临时文件和初始化trigger
     * @param jobName
     * @param jobGroup
     * @param triggerMap
     * @param scheduler
     */
    public void createTrigger(String jobName,String jobGroup,Map<String,Object> triggerMap,Scheduler scheduler)throws Exception{
        try {
            JobKey jobKey = new JobKey(jobName, jobGroup);
            JobDetail jobDetail = scheduler.getJobDetail(jobKey);
            if(jobDetail == null) {
                throw new IllegalArgumentException("No such job '" + jobKey + "'");
            }

            String triggerClassName = (String) triggerMap.remove("triggerClass");
            if(triggerClassName == null) {
                throw new IllegalArgumentException("No triggerClass specified");
            }
            Class<?> triggerClass = Class.forName(triggerClassName);
            Trigger trigger = (Trigger) triggerClass.newInstance();

            for(Map.Entry<String, Object> entry : triggerMap.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();
                if("jobDataMap".equals(key)) {
                    value = new JobDataMap((Map<?, ?>)value);
                }
                invokeSetter(trigger, key, value);
            }

            org.quartz.impl.triggers.AbstractTrigger<?> at = (org.quartz.impl.triggers.AbstractTrigger<?>)trigger;
            at.setKey(new TriggerKey(at.getName(), at.getGroup()));

            Date startDate = at.getStartTime();
            if(startDate == null || startDate.before(new Date())) {
                at.setStartTime(new Date());
            }
            scheduler.scheduleJob(trigger);
        } catch (Exception e) {
            throw newPlainException(e);
        }
    }

    private static void invokeSetter(Object target, String attribute, Object value) throws Exception {
        String setterName = "set" + Character.toUpperCase(attribute.charAt(0)) + attribute.substring(1);
        Class<?>[] argTypes = {value.getClass()};
        Method setter = findMethod(target.getClass(), setterName, argTypes);
        if(setter != null) {
            setter.invoke(target, value);
        } else {
            throw new Exception("Unable to find setter for attribute '" + attribute
                    + "' and value '" + value + "'");
        }
    }

    private static Method findMethod(Class<?> targetType, String methodName,
                                     Class<?>[] argTypes) throws IntrospectionException {
        BeanInfo beanInfo = Introspector.getBeanInfo(targetType);
        if (beanInfo != null) {
            for(MethodDescriptor methodDesc: beanInfo.getMethodDescriptors()) {
                Method method = methodDesc.getMethod();
                Class<?>[] parameterTypes = method.getParameterTypes();
                if (methodName.equals(method.getName()) && argTypes.length == parameterTypes.length) {
                    boolean matchedArgTypes = true;
                    for (int i = 0; i < argTypes.length; i++) {
                        if (getWrapperIfPrimitive(argTypes[i]) != parameterTypes[i]) {
                            matchedArgTypes = false;
                            break;
                        }
                    }
                    if (matchedArgTypes) {
                        return method;
                    }
                }
            }
        }
        return null;
    }

    private static Class<?> getWrapperIfPrimitive(Class<?> c) {
        Class<?> result = c;
        try {
            Field f = c.getField("TYPE");
            f.setAccessible(true);
            result = (Class<?>) f.get(null);
        } catch (Exception e) {
            /**/
        }
        return result;
    }

    private Exception newPlainException(Exception e) {
        String type = e.getClass().getName();
        if(type.startsWith("java.") || type.startsWith("javax.")) {
            return e;
        } else {
            Exception result = new Exception(e.getMessage());
            result.setStackTrace(e.getStackTrace());
            return result;
        }
    }
}
