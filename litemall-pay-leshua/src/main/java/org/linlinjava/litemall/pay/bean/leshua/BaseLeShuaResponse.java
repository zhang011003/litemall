package org.linlinjava.litemall.pay.bean.leshua;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import lombok.Data;
import me.chanjar.weixin.common.util.XmlUtils;
import org.linlinjava.litemall.pay.properties.LeShuaProperties;
import org.linlinjava.litemall.pay.util.LeShuaUtil;

import java.util.Map;

@Data
public abstract class BaseLeShuaResponse {
    @XStreamAlias("result_code")
    private int resultCode;

    @XStreamAlias("resp_code")
    private int respCode;

    @XStreamAlias("resp_msg")
    private int respMsg;

    @XStreamAlias("error_code")
    private int errorCode;

    @XStreamAlias("error_msg")
    private String errorMsg;

    @XStreamAlias("merchant_id")
    private String merchantId;

    @XStreamAlias("third_order_id")
    private String thirdOrderId;

    @XStreamAlias("leshua_order_id")
    private String leshuaOrderId;

    @XStreamAlias("sub_merchant_id")
    private String subMerchantId;

    private String xmlString;

    public boolean isSuccess(LeShuaProperties leShuaProperties) {
        return getRespCode() == 0 && getResultCode() == 0 && LeShuaUtil.verify(xmlString, getKey(leShuaProperties));
    }

    protected String getKey(LeShuaProperties leShuaProperties) {
        return leShuaProperties.getKey();
    }
}
