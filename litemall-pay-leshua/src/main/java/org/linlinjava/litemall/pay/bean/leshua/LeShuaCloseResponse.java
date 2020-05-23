package org.linlinjava.litemall.pay.bean.leshua;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import lombok.Data;
import org.linlinjava.litemall.pay.properties.LeShuaProperties;

@XStreamAlias("leshua")
@Data
public class LeShuaCloseResponse extends BaseLeShuaResponse {
    @XStreamAlias("status")
    private String status;

    public LeShuaStatus getLeShuaStatus() {
        return LeShuaStatus.getLeShuaStatus(getStatus());
    }

    @Override
    public boolean isSuccess(LeShuaProperties leShuaProperties) {
        return super.isSuccess(leShuaProperties) && getLeShuaStatus() == LeShuaStatus.CANCELLED;
    }
}
