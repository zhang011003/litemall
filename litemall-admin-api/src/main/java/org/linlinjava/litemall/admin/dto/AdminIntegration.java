package org.linlinjava.litemall.admin.dto;

import lombok.Data;

@Data
public class AdminIntegration {
    @Data
    public class User {
        private String username;
        private String avatar;
        private String nickName;
    }
    private Integer level;
    private String token;
    private User user;
    private String namePaths;

}
