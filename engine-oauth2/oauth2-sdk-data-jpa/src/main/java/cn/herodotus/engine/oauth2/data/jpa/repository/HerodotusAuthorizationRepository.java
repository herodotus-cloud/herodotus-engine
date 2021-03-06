/*
 * Copyright (c) 2020-2030 ZHENGGENGWEI(码匠君)<herodotus@aliyun.com>
 *
 * Dante Engine Licensed under the Apache License, Version 2.0 (the "License");
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
 * Dante Engine 采用APACHE LICENSE 2.0开源协议，您在使用过程中，需要注意以下几点：
 *
 * 1.请不要删除和修改根目录下的LICENSE文件。
 * 2.请不要删除和修改 Dante Engine 源码头部的版权声明。
 * 3.请保留源码和相关描述文件的项目出处，作者声明等。
 * 4.分发源码时候，请注明软件出处 https://gitee.com/herodotus/dante-engine
 * 5.在修改包名，模块名称，项目代码等时，请注明软件出处 https://gitee.com/herodotus/dante-engine
 * 6.若您的项目无法满足以上几点，可申请商业授权
 */

package cn.herodotus.engine.oauth2.data.jpa.repository;

import cn.herodotus.engine.data.core.repository.BaseRepository;
import cn.herodotus.engine.oauth2.data.jpa.entity.HerodotusAuthorization;
import org.springframework.data.jpa.repository.QueryHints;

import javax.persistence.QueryHint;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * <p>Description: HerodotusAuthorizationRepository </p>
 *
 * @author : gengwei.zheng
 * @date : 2022/2/25 21:05
 */
public interface HerodotusAuthorizationRepository extends BaseRepository<HerodotusAuthorization, String> {

    /**
     * 根据 State 查询 OAuth2 认证信息
     *
     * @param state OAuth2 Authorization Code 模式参数 State
     * @return OAuth2 认证信息 {@link HerodotusAuthorization}
     */
    @QueryHints(@QueryHint(name = org.hibernate.annotations.QueryHints.CACHEABLE, value = "true"))
    Optional<HerodotusAuthorization> findByState(String state);

    /**
     * 根据 authorizationCode 查询 OAuth2 认证信息
     *
     * @param authorizationCode OAuth2 Authorization Code 模式参数 code
     * @return OAuth2 认证信息 {@link HerodotusAuthorization}
     */
    @QueryHints(@QueryHint(name = org.hibernate.annotations.QueryHints.CACHEABLE, value = "true"))
    Optional<HerodotusAuthorization> findByAuthorizationCode(String authorizationCode);

    /**
     * 根据 Access Token 查询 OAuth2 认证信息
     *
     * @param accessToken OAuth2 accessToken
     * @return OAuth2 认证信息 {@link HerodotusAuthorization}
     */
    @QueryHints(@QueryHint(name = org.hibernate.annotations.QueryHints.CACHEABLE, value = "true"))
    Optional<HerodotusAuthorization> findByAccessToken(String accessToken);

    /**
     * 根据 Refresh Token 查询 OAuth2 认证信息
     *
     * @param refreshToken OAuth2 refreshToken
     * @return OAuth2 认证信息 {@link HerodotusAuthorization}
     */
    @QueryHints(@QueryHint(name = org.hibernate.annotations.QueryHints.CACHEABLE, value = "true"))
    Optional<HerodotusAuthorization> findByRefreshToken(String refreshToken);

    /**
     * 根据客户端ID和用户名称删除
     *
     * @param registeredClientId 客户端ID
     * @param principalName      用户名称
     */
    void deleteByRegisteredClientIdAndPrincipalName(String registeredClientId, String principalName);

    /**
     * 清除已过期token
     *
     * @param localDateTime 时间
     */
    void deleteByAccessTokenExpiresAtBefore(LocalDateTime localDateTime);

    /**
     * 根据客户端ID和用户名查询
     *
     * @param registeredClientId 客户端ID
     * @param principalName      用户名称
     * @return 认证信息列表
     */
    List<HerodotusAuthorization> findAllByRegisteredClientIdAndPrincipalName(String registeredClientId, String principalName);
}
