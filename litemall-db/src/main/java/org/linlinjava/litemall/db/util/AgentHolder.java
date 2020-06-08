package org.linlinjava.litemall.db.util;

import org.linlinjava.litemall.common.util.Consts;
import org.linlinjava.litemall.common.util.ThreadLocalUtil;
import org.linlinjava.litemall.db.domain.LitemallAdmin;

public class AgentHolder {

    public static void setAgent(LitemallAdmin agent) {
        ThreadLocalUtil.<LitemallAdmin>getThreadLocal(Consts.AGENT_THREAD_LOCAL).set(agent);
    }

    public static LitemallAdmin getAgent() {
        return ThreadLocalUtil.<LitemallAdmin>getThreadLocal(Consts.AGENT_THREAD_LOCAL).get();
    }

    public static void clear() {
        ThreadLocalUtil.<LitemallAdmin>getThreadLocal(Consts.AGENT_THREAD_LOCAL).remove();
    }
}
