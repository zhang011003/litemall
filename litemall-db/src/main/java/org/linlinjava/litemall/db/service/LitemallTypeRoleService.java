package org.linlinjava.litemall.db.service;

import org.linlinjava.litemall.db.dao.LitemallTypeRoleMapper;
import org.linlinjava.litemall.db.domain.LitemallTypeRole;
import org.linlinjava.litemall.db.domain.LitemallTypeRoleExample;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class LitemallTypeRoleService {
    @Resource
    private LitemallTypeRoleMapper typeRoleMapper;
    public LitemallTypeRole findByType(Byte type, LitemallTypeRole.Column... columns) {
        LitemallTypeRoleExample example = new LitemallTypeRoleExample();
        example.createCriteria().andTypeEqualTo(type);
        List<LitemallTypeRole> typeRoles = typeRoleMapper.selectByExampleSelective(example, columns);
        if (typeRoles.size() <= 0) {
            return new LitemallTypeRole();
        }
        return typeRoles.get(0);
    }
}
