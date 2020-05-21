package org.linlinjava.litemall.wx.leshua;

import com.github.binarywang.wxpay.bean.result.BaseWxPayResult;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.reflection.PureJavaReflectionProvider;
import com.thoughtworks.xstream.core.util.QuickWriter;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.io.xml.PrettyPrintWriter;
import com.thoughtworks.xstream.io.xml.XppDriver;

import java.io.Writer;

public class LeShuaPayResult {
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
        xstream.allowTypesByWildcard(new String[]{"org.linlinjava.litemall.wx.**"});
        xstream.setClassLoader(Thread.currentThread().getContextClassLoader());
        return xstream;
    }

    public static <T extends BaseLeShuaResult> T fromXML(String xmlString, Class<T> clazz) {
        XStream xstream = getInstance();
        xstream.processAnnotations(clazz);
        T result = (T) xstream.fromXML(xmlString);
        result.setXmlString(xmlString);
        return result;
    }
}
