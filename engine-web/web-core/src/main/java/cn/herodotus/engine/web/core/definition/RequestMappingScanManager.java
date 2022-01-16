/*
 * Copyright (c) 2020-2030 ZHENGGENGWEI(码匠君)<herodotus@aliyun.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Eurynome Cloud 采用APACHE LICENSE 2.0开源协议，您在使用过程中，需要注意以下几点：
 *
 * 1.请不要删除和修改根目录下的LICENSE文件。
 * 2.请不要删除和修改Guns源码头部的版权声明。
 * 3.请保留源码和相关描述文件的项目出处，作者声明等。
 * 4.分发源码时候，请注明软件出处 https://gitee.com/herodotus/eurynome-cloud
 * 5.在修改包名，模块名称，项目代码等时，请注明软件出处 https://gitee.com/herodotus/eurynome-cloud
 * 6.若您的项目无法满足以上几点，可申请商业授权
 */

package cn.herodotus.engine.web.core.definition;

import cn.herodotus.engine.web.core.domain.RequestMapping;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.context.ApplicationContext;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Map;

/**
 * <p>Description: RequestMapping 扫描管理器 </p>
 *
 * @author : gengwei.zheng
 * @date : 2022/1/16 18:42
 */
public interface RequestMappingScanManager {

    /**
     * 获取 Spring Boot 应用上下文
     * @return 上下文 {@link ApplicationContext}
     */
    ApplicationContext getApplicationContext();

    /**
     * 获取是否执行扫描的标记注解。
     * @return 标记注解
     */
    Class<? extends Annotation> getScanAnnotationClass();

    /**
     * 是否是分布式架构。
     * @return true 分布式架构，false 单体架构。
     */
    boolean isDistributedArchitecture();

    /**
     * 是否满足执行扫描的条件。
     * 根据扫描标记注解 {@link #getScanAnnotationClass()} 以及 是否是分布式架构 {@link #isDistributedArchitecture()} 决定是否执行接口的扫描。
     *
     * 与 {@link #isEnabled()} 不同，isEnabled 是全局控制是否开启扫描，isPerformScan 是在 isEnabled 的条件下，判断执行条件是否满足。
     *
     * 分布式架构根据注解判断是否扫描，单体架构直接扫描即可无须判断
     *
     * @return true 执行， false 不执行
     */
    default boolean isPerformScan() {
        if(isDistributedArchitecture()) {
            if (ObjectUtils.isEmpty(getScanAnnotationClass()) || ObjectUtils.isEmpty(getApplicationContext())) {
                return false;
            }

            Map<String, Object> content = getApplicationContext().getBeansWithAnnotation(getScanAnnotationClass());
            return !MapUtils.isEmpty(content);
        }

        return true;
    }

    /**
     * 是否开启了接口扫描。
     * @return true 开启，false 关闭。
     */
    boolean isEnabled();

    /**
     * 发布远程事件，传送RequestMapping
     *
     * @param requestMappings    扫描到的RequestMapping
     * @param applicationContext {@link ApplicationContext}
     * @param serviceId          当前服务的service name。目前取的是：spring.application.name, applicationContext.getApplicationName取到的是空串
     * @param isDistributedArchitecture      是否是分布式架构
     */
    void postProcess(List<RequestMapping> requestMappings, ApplicationContext applicationContext, String serviceId, boolean isDistributedArchitecture);

    /**
     * 发布远程事件，传送RequestMapping
     *
     * @param requestMappings    扫描到的RequestMapping
     * @param applicationContext {@link ApplicationContext}
     * @param serviceId          当前服务的service name。目前取的是：spring.application.name, applicationContext.getApplicationName取到的是空串
     */
    default void postProcess(List<RequestMapping> requestMappings, ApplicationContext applicationContext, String serviceId) {
        postProcess(requestMappings, applicationContext, serviceId, isDistributedArchitecture());
    }
}