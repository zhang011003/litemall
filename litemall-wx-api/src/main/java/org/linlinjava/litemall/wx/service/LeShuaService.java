package org.linlinjava.litemall.wx.service;

import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.linlinjava.litemall.core.config.LeShuaProperties;
import org.linlinjava.litemall.core.util.LeShuaUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
@Slf4j
public class LeShuaService {
    @Autowired
    private LeShuaProperties leShuaProperties;

    @Autowired
    private RestTemplate restTemplate;

    private Map<String, String> getPostDataMap(String orderSn, String openid, Map<String, String> otherValueMap) {
        Map<String,String> postDataMap = Maps.newHashMap();
        postDataMap.put("service","get_tdcode");
        postDataMap.put("pay_way", "WXZF");
        postDataMap.put("amount", "1");
        postDataMap.put("jspay_flag","1");
        postDataMap.put("third_order_id", orderSn);
        postDataMap.put("merchant_id", leShuaProperties.getMerchantId());
        postDataMap.put("nonce_str", String.valueOf(System.currentTimeMillis()));
        if (StringUtils.hasText(openid)) {
            postDataMap.put("sub_openid", openid);
        }
        if (StringUtils.hasText(leShuaProperties.getNotifyUrl())) {
            postDataMap.put("notify_url", leShuaProperties.getNotifyUrl());
        }
        postDataMap.putAll(otherValueMap);
        postDataMap.put("sign", LeShuaUtil.getSign(postDataMap, leShuaProperties.getKey()));
        return postDataMap;
    }

    public String invoke(String reqUrl, String orderSn, String openid, Map<String, String> otherValueMap) {
        Map<String, String> postDataMap = getPostDataMap(orderSn, openid, otherValueMap);
        StringBuilder postDataBuilder = postDataMap.keySet().stream().collect(StringBuilder::new, (x, y) -> x.append(y).append("=").append(postDataMap.get(y)).append("&"),(x, y)-> x.append(y));
        postDataBuilder.deleteCharAt(postDataBuilder.length() - 1);

        String url = reqUrl + "?" + postDataBuilder.toString();
        log.info("Invoke leshua, url is: " + url);
        ResponseEntity<String> responseEntity = restTemplate.postForEntity(url, null, String.class);
        String responseBody = responseEntity.getBody();
        log.info("Invoke leshua, result is: " + responseBody);
        return responseBody;
    }
}
