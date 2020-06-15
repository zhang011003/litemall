package org.linlinjava.litemall.db.domain;

import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.util.Arrays;

@Data
public class Admin extends LitemallAdmin {
    private LitemallAdminIntegration adminIntegration;
    private String bearerToken;
    public String getToken() {
        return bearerToken.substring(7);
    }
    public static Admin createAdmin(LitemallAdmin litemallAdmin, LitemallAdminIntegration adminIntegration) {
        Admin admin = new Admin();
        BeanUtils.copyProperties(litemallAdmin, admin);
        admin.setAdminIntegration(adminIntegration);
        return admin;
    }
    public enum Type {
        DEALER((byte)1), AGENT((byte)2);
        private byte type;
        private Type(byte type) {
            this.type = type;
        }

        public byte getType() {
            return type;
        }
        public static Type getType(byte type) {
            return Arrays.stream(Type.values())
                    .filter(p -> p.getType() == type)
                    .findFirst()
                    .orElseThrow(IllegalArgumentException::new);
        }
    }
}
