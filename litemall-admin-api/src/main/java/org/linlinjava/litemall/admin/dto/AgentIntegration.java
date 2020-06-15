package org.linlinjava.litemall.admin.dto;

import lombok.Data;

import java.util.List;

@Data
public class AgentIntegration {
    @Data
    public static class Agent {
        private String nickName;
        private String username;
    }
    private List<Agent> content;
}
