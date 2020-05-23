package org.linlinjava.litemall.pay.util;

import com.google.common.collect.Lists;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.reflection.PureJavaReflectionProvider;
import com.thoughtworks.xstream.core.util.QuickWriter;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.io.xml.PrettyPrintWriter;
import com.thoughtworks.xstream.io.xml.XppDriver;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.util.XmlUtils;
import org.linlinjava.litemall.pay.bean.leshua.BaseLeShuaResponse;

import java.io.Writer;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
public class LeShuaUtil {
    private static final XppDriver XPP_DRIVER = new XppDriver() {
        public HierarchicalStreamWriter createWriter(Writer out) {
            return new PrettyPrintWriter(out, this.getNameCoder()) {
                private static final String PREFIX_CDATA = "<![CDATA[";
                private static final String SUFFIX_CDATA = "]]>";
                private static final String PREFIX_MEDIA_ID = "<MediaId>";
                private static final String SUFFIX_MEDIA_ID = "</MediaId>";

                protected void writeText(QuickWriter writer, String text) {
                    if (text.startsWith("<![CDATA[") && text.endsWith("]]>")) {
                        writer.write(text);
                    } else if (text.startsWith("<MediaId>") && text.endsWith("</MediaId>")) {
                        writer.write(text);
                    } else {
                        super.writeText(writer, text);
                    }

                }

                public String encodeNode(String name) {
                    return name;
                }
            };
        }
    };

    private static XStream getInstance() {
        XStream xstream = new XStream(new PureJavaReflectionProvider(), XPP_DRIVER);
        xstream.ignoreUnknownElements();
        xstream.setMode(1001);
        XStream.setupDefaultSecurity(xstream);
        xstream.allowTypesByWildcard(new String[]{"org.linlinjava.litemall.pay.**"});
        xstream.setClassLoader(Thread.currentThread().getContextClassLoader());
        return xstream;
    }

    public static <T extends BaseLeShuaResponse> T fromXML(String xmlString, Class<T> clazz) {
        XStream xstream = getInstance();
        xstream.processAnnotations(clazz);
        T result = (T) xstream.fromXML(xmlString);
        result.setXmlString(xmlString);
        return result;
    }

    public static String getSign(Map<String, String> map, String key) {
        List<String> keys = Lists.newArrayList(map.keySet());
        StringBuilder builder = map.keySet().stream().sorted(String::compareTo).collect(StringBuilder::new, (x, y) -> x.append(y).append("=").append(map.get(y)).append("&"),(x, y)-> x.append(y));
        builder.append("key=").append(key);
        return MD5.sign(builder.toString());
    }

    public static boolean verify(String xml, String key) {
        Map<String, Object> map = XmlUtils.xml2Map(xml);
        Map<String, String> stringMap = map.entrySet().stream()
                .filter(e -> !e.getKey().equals("sign"))
                .filter(e -> !e.getKey().equals("leshua"))
                .filter(e -> !e.getKey().equals("error_code"))
                .filter(e -> !e.getKey().equals("resp_code"))
                .collect(Collectors.toMap(t -> t.getKey(), t -> t.getValue().toString()));
        String sign = getSign(stringMap, key);
        boolean verifySuccess = sign.equalsIgnoreCase(map.get("sign").toString());
        if (!verifySuccess) {
            log.warn("Sign computed is:{}, from xml is:{}. Two signs are different", sign, map.get("sign"));
        } else {
            log.info("Sign verify passed");
        }
        return verifySuccess;
    }
}
