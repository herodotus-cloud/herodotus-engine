/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2019-2021 Zhenggengwei<码匠君>, herodotus@aliyun.com
 *
 * This file is part of Herodotus Cloud.
 *
 * Herodotus Cloud is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser
 * General Public License as published by the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * Herodotus Cloud is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with with Herodotus Cloud;
 * if no see <https://gitee.com/herodotus/herodotus-cloud>
 *
 * - Author: Zhenggengwei<码匠君>
 * - Contact: herodotus@aliyun.com
 * - License: GNU Lesser General Public License (LGPL)
 * - Blog and source code availability: https://gitee.com/herodotus/herodotus-cloud
 */

package cn.herodotus.engine.rest.crypto.enhance;

import cn.herodotus.engine.assistant.core.constants.HttpHeaders;
import cn.herodotus.engine.assistant.core.constants.SecurityConstants;
import cn.herodotus.engine.rest.core.annotation.Crypto;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.MethodParameter;
import org.springframework.core.ResolvableType;
import org.springframework.http.HttpMethod;
import org.springframework.lang.Nullable;
import org.springframework.util.CollectionUtils;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.annotation.RequestParamMapMethodArgumentResolver;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.Part;
import java.util.Map;

/**
 * <p>Description: @RequestParam 解密处理器</p>
 * <p>
 * 主要针对具备以下几种条件情况，用该处理器进行处理
 * 1. 使用未指定名称的 @RequestParam 注解，同时参数类型为Map的。
 * 2. 使用了@Crypto注解，且requestDecrypt参数为 true。或者 请求路径是 '/oauth/token'
 *
 * @author : gengwei.zheng
 * @date : 2021/10/5 16:40
 */
public class DecryptRequestParamMapResolver implements HandlerMethodArgumentResolver {

    private static final Logger log = LoggerFactory.getLogger(DecryptRequestParamMapResolver.class);

    private InterfaceCryptoProcessor interfaceCryptoProcessor;
    private RequestParamMapMethodArgumentResolver requestParamMapMethodArgumentResolver;

    public void setInterfaceCryptoProcessor(InterfaceCryptoProcessor interfaceCryptoProcessor) {
        this.interfaceCryptoProcessor = interfaceCryptoProcessor;
    }

    public void setRequestParamMapMethodArgumentResolver(RequestParamMapMethodArgumentResolver requestParamMapMethodArgumentResolver) {
        this.requestParamMapMethodArgumentResolver = requestParamMapMethodArgumentResolver;
    }

    @Override
    public boolean supportsParameter(MethodParameter methodParameter) {

        String methodName = methodParameter.getMethod().getName();
        boolean isSupports = requestParamMapMethodArgumentResolver.supportsParameter(methodParameter);

        log.trace("[Herodotus] |- Is DecryptRequestParamMapResolver supports method [{}] ? Status is [{}].", methodName, isSupports);
        return isSupports;
    }

    /**
     * 判断该接口方法是否用@Crypto注解标记，同时requestDecrypt的值是true
     *
     * @param methodParameter {@link MethodParameter}
     * @return 是否开启了自定义@Crypto
     */
    private boolean isConfigCrypto(MethodParameter methodParameter) {
        Crypto crypto = methodParameter.getMethodAnnotation(Crypto.class);
        return ObjectUtils.isNotEmpty(crypto) && crypto.requestDecrypt();
    }

    /**
     * 是否 Post /oauth/token 请求
     *
     * @param uri    请求uri
     * @param method 请求类型
     * @return 是否 Post /oauth/token 请求
     */
    private boolean isOauthTokenRequest(String uri, String method) {
        return StringUtils.equals(uri, SecurityConstants.ENDPOINT_OAUTH_TOKEN) && StringUtils.equalsIgnoreCase(method, HttpMethod.POST.name());
    }

    /**
     * 是否是常规Map
     *
     * @param methodParameter {@link MethodParameter}
     * @return boolean
     */
    private boolean isRegularMap(MethodParameter methodParameter) {
        if (!MultiValueMap.class.isAssignableFrom(methodParameter.getParameterType())) {
            ResolvableType resolvableType = ResolvableType.forMethodParameter(methodParameter);
            Class<?> valueType = resolvableType.asMap().getGeneric(1).resolve();
            if (!(valueType == MultipartFile.class || valueType == Part.class)) {
                return true;
            }
        }

        return false;
    }

    @Override
    @Nullable
    public Object resolveArgument(MethodParameter methodParameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {

        HttpServletRequest request = webRequest.getNativeRequest(HttpServletRequest.class);
        String requestURI = request.getRequestURI();
        String requestMethod = request.getMethod();
        String sessionId = request.getHeader(HttpHeaders.X_HERODOTUS_SESSION);

        if (isConfigCrypto(methodParameter) || isOauthTokenRequest(requestURI, requestMethod)) {

            if (StringUtils.isNotBlank(sessionId)) {

                if (isRegularMap(methodParameter)) {
                    Map<String, String[]> parameterMap = webRequest.getParameterMap();
                    Map<String, String> result = CollectionUtils.newLinkedHashMap(parameterMap.size());
                    parameterMap.forEach((key, values) -> {
                        if (values.length > 0) {
                            String value = interfaceCryptoProcessor.decryptToString(sessionId, values[0]);
                            result.put(key, value);
                        }
                    });

                    return result;
                }
            } else {
                log.warn("[Herodotus] |- Cannot find Herodotus Cloud custom session header. Use interface crypto founction need add X_HERODOTUS_SESSION to request header.");
            }
        }

        log.debug("[Herodotus] |- The decryption conditions are not met DecryptRequestParamMapResolver, skip! to next!");
        return requestParamMapMethodArgumentResolver.resolveArgument(methodParameter, mavContainer, webRequest, binderFactory);
    }
}