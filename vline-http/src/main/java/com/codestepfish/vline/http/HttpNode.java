package com.codestepfish.vline.http;

import cn.hutool.core.thread.ThreadUtil;
import com.alibaba.fastjson2.JSON;
import com.codestepfish.vline.core.Node;
import com.codestepfish.vline.core.http.HttpProperties;
import com.codestepfish.vline.http.client.ForestHandler;
import com.codestepfish.vline.http.client.VlineForestClient;
import com.dtflys.forest.Forest;
import com.dtflys.forest.callback.OnError;
import com.dtflys.forest.callback.RetryWhen;
import com.dtflys.forest.callback.SuccessWhen;
import com.dtflys.forest.http.ForestRequest;
import com.dtflys.forest.http.ForestRequestType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

@Getter
@Setter
@Slf4j
@NoArgsConstructor
@Accessors(chain = true)
public class HttpNode<T> extends Node<T> {
    // key node name
    public static final Map<String, VlineForestClient> FOREST_REQUEST_MAP = new ConcurrentHashMap<>(2);

    // forest 初始化
    public static <T> void init(HttpNode node) {
        HttpProperties hp = node.getHttp();

        try {
            // 填充forest config参数
            ForestRequest<?> forestRequest = Forest.config().request()
                    .url(hp.getUrl())
                    .type(ForestRequestType.findType(hp.getMethod().name()))
                    .maxRetryCount(hp.getMaxRetryCount())
                    .maxRetryInterval(hp.getMaxRetryInterval());

            if (StringUtils.hasText(hp.getRetryWhen())) {
                Class<RetryWhen> retryWhenClazz = (Class<RetryWhen>) Objects.requireNonNull(ClassUtils.getDefaultClassLoader()).loadClass(hp.getRetryWhen());
                forestRequest.retryWhen(retryWhenClazz.getDeclaredConstructor().newInstance());
            }

            if (StringUtils.hasText(hp.getSuccessWhen())) {
                Class<SuccessWhen> successWhenClazz = (Class<SuccessWhen>) Objects.requireNonNull(ClassUtils.getDefaultClassLoader()).loadClass(hp.getSuccessWhen());
                forestRequest.successWhen(successWhenClazz.getDeclaredConstructor().newInstance());
            }

            if (StringUtils.hasText(hp.getOnError())) {
                Class<OnError> onErrorClazz = (Class<OnError>) Objects.requireNonNull(ClassUtils.getDefaultClassLoader()).loadClass(hp.getOnError());
                forestRequest.onError(onErrorClazz.getDeclaredConstructor().newInstance());
            }


            VlineForestClient vlineForestClient = null;
            if (StringUtils.hasText(hp.getHandler())) {
                Class<ForestHandler> handlerClazz = (Class<ForestHandler>) Objects.requireNonNull(ClassUtils.getDefaultClassLoader()).loadClass(hp.getHandler());
                // handlerClazz 必须重写 handle 方法
                vlineForestClient = new VlineForestClient(forestRequest, handlerClazz.getDeclaredConstructor().newInstance());
            } else {
                vlineForestClient = new VlineForestClient(forestRequest, new ForestHandler() {
                });
            }

            Assert.notNull(vlineForestClient, "http handler is null");

            FOREST_REQUEST_MAP.put(node.getName(), vlineForestClient);
        } catch (Exception e) {
            log.error("http forest init failed : ", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public void init() {
        log.info("node init: {}", JSON.toJSONString(this));
        // 初始化forest client
        ThreadUtil.execute(() -> init(this));
    }

    @Override
    public void destroy() {
        log.info("node destroy: {}", this.getName());
    }

    @Override
    public void sendData(T data) {
        VlineForestClient vlineForestClient = FOREST_REQUEST_MAP.get(this.getName());
        vlineForestClient.getHandler().handle(vlineForestClient.getRequest(), this.getHttp(), data);
    }
}
