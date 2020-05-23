package org.linlinjava.litemall.pay.util;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.SignatureException;
import org.apache.commons.codec.digest.DigestUtils;

/** 
* 功能：MD5签名
* 版本：3.3
* 修改日期：2012-08-17
* */
public class MD5 {
    public static void main(String[] args) {
        String str = "amount=1&attach=&bank_type=OTHERS&buyer_pay_amount=1&channel_datetime=2020-05-22 22:13:02&channel_order_id=4200000545202005229174017338&coupon=0&discount_amount=0&goods_tag=&leshua_order_id=9000287013220143&merchant_id=4014116132&openid=o8uJ6uH1yk8cnA2yYX5I-taenykQ&out_transaction_id=4200000545202005229174017338&pay_time=2020-05-22 22:13:03&pay_way=WXZF&settlement_amount=1&status=2&sub_merchant_id=337641711&sub_openid=oFpyN0W0AztKAJeKhyOfbL_9m2Yk&third_order_id=20200522849097&trade_type=JSAPI";
        System.out.println(MD5.sign(str, "&key=EE085A0ABA9D22493B7971B2E1AEB4DF", "utf-8"));
        
    }
    public static String sign(String text) {
        return DigestUtils.md5Hex(getContentBytes(text, StandardCharsets.UTF_8.displayName())).toUpperCase();
    }

    /**
     * 签名字符串
     * @param text 需要签名的字符串
     * @param key 密钥
     * @param input_charset 编码格式
     * @return 签名结果
     */
    public static String sign(String text, String key, String input_charset) {
    	text = text + key;
    	System.out.println(text);
        return DigestUtils.md5Hex(getContentBytes(text, input_charset)).toUpperCase();
    }

    /**
     * 签名字符串
     * @param text 需要签名的字符串
     * @param sign 签名结果
     * @param key 密钥
     * @param input_charset 编码格式
     * @return 签名结果
     */
    public static boolean verify(String text, String sign, String key, String input_charset) {
    	text = text + key;
    	String mysign = DigestUtils.md5Hex(getContentBytes(text, input_charset));
    	if(mysign.equals(sign)) {
    		return true;
    	}
    	else {
    		return false;
    	}
    }

    /**
     * @param content
     * @param charset
     * @return
     * @throws SignatureException
     * @throws UnsupportedEncodingException 
     */
    private static byte[] getContentBytes(String content, String charset) {
        if (charset == null || "".equals(charset)) {
            return content.getBytes();
        }
        try {
            return content.getBytes(charset);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("MD5签名过程中出现错误,指定的编码集不对,您目前指定的编码集是:" + charset);
        }
    }

}