package org.linlinjava.litemall.pay.bean.leshua;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import lombok.Data;

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

    @XStreamAlias("merchant_id")
    private String merchantId;

    @XStreamAlias("third_order_id")
    private String thirdOrderId;

    @XStreamAlias("leshua_order_id")
    private String leshuaOrderId;

    private String xmlString;

    public boolean isSuccess() {
        return getRespCode() == 0 && getResultCode() == 0;
    }
}
