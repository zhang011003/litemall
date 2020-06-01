package org.linlinjava.litemall.db.util;

import java.util.Arrays;

public class AdminUtil {
    public enum Type {
        DEALER(0), AGENT1(1), AGENT2(2);

        private int type;
        Type(int type) {
            this.type = type;
        }
        public int getType() {
            return type;
        }
        public static Type getType(int type) {
            return Arrays.stream(Type.values()).filter(t -> t.getType() == type)
                    .findFirst()
                    .orElseThrow(IllegalArgumentException::new);
        }
    }
}
