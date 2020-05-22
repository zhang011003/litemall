package org.linlinjava.litemall.pay.service;

import com.google.common.collect.Maps;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.linlinjava.litemall.pay.bean.Param;
import org.linlinjava.litemall.pay.bean.leshua.LeShuaRequest;
import org.linlinjava.litemall.pay.properties.LeShuaProperties;
import org.linlinjava.litemall.pay.util.LeShuaUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.Map;

@Slf4j
public class LeShuaService {
    @Autowired
    private LeShuaProperties leShuaProperties;

    @Autowired
    private RestTemplate restTemplate;

    @SneakyThrows
    private Map<String, String> getPostDataMap(LeShuaRequest request) {
        Map<String,String> postDataMap = Maps.newHashMap();
        Arrays.stream(request.getClass().getDeclaredFields())
            .filter(f -> StringUtils.hasText((String) f.get(request)))
            .filter(f -> f.getAnnotation(Param.class) == null || !f.getAnnotation(Param.class).ignore())
            .forEach(f -> {
                Param param = f.getAnnotation(Param.class);
                if (param == null) {
                    postDataMap.put(f.getName(), (String) f.get(request));
                } else {
                    postDataMap.put(param.value(), (String) f.get(request));
                }
            });
        // TODO: 测试时设置金额为1分钱
        postDataMap.put("amount", "1");
        postDataMap.put("merchant_id", leShuaProperties.getMerchantId());
        postDataMap.put("nonce_str", String.valueOf(System.currentTimeMillis()));
        postDataMap.put("sign", LeShuaUtil.getSign(postDataMap, leShuaProperties.getKey()));
        return postDataMap;
    }

    public String invoke(LeShuaRequest request) {
        Map<String, String> postDataMap = getPostDataMap(request);
        StringBuilder postDataBuilder = postDataMap.keySet().stream().collect(StringBuilder::new, (x, y) -> x.append(y).append("=").append(postDataMap.get(y)).append("&"),(x, y)-> x.append(y));
        postDataBuilder.deleteCharAt(postDataBuilder.length() - 1);

        String url = request.getRequestUrl() + "?" + postDataBuilder.toString();
        log.info("Invoke leshua, url is: " + url);
        ResponseEntity<String> responseEntity = restTemplate.postForEntity(url, null, String.class);
        String responseBody = responseEntity.getBody();
        log.info("Invoke leshua, result is: " + responseBody);
        return responseBody;
    }

}
