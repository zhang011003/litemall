package org.linlinjava.litemall.db.util;

import java.util.Arrays;

public class AccountUtil {
    public enum Type {
        INCOME((byte)1), OUTGOINGS((byte)2);
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
    public enum AccountType {
        ACCOUNT((byte)1), PROFIT((byte)2);
        private byte accountType;
        private AccountType(byte accountType) {
            this.accountType = accountType;
        }

        public byte getAccountType() {
            return accountType;
        }
        public static AccountType getAccountType(byte accountType) {
            return Arrays.stream(AccountType.values())
                    .filter(p -> p.getAccountType() == accountType)
                    .findFirst()
                    .orElseThrow(IllegalArgumentException::new);
        }
    }
    public enum AccountStatus {
        CASHOUTING((short) 1), FINISH((short)2);
        private short accountStatus;
        private AccountStatus(short accountType) {
            this.accountStatus = accountType;
        }

        public short getAccountStatus() {
            return accountStatus;
        }
        public static AccountStatus getAccountStatus(short accountStatus) {
            return Arrays.stream(AccountStatus.values())
                    .filter(p -> p.getAccountStatus() == accountStatus)
                    .findFirst()
                    .orElseThrow(IllegalArgumentException::new);
        }
    }
}
