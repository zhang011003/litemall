package org.linlinjava.litemall.db.service;

import org.linlinjava.litemall.db.dao.LitemallAdminIntegrationMapper;
import org.linlinjava.litemall.db.domain.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class LitemallAdminIntegrationService {
    @Autowired
    private LitemallAdminIntegrationMapper adminIntegrationMapper;
    @Autowired
    private LitemallTypeRoleService typeRoleService;
    public Integer[] getRoleIds(Admin admin) {
        Byte type = admin.getAdminIntegration().getType();
        return getRoleIds(type);
    }
    public Integer[] getRoleIds(Byte type) {
        return typeRoleService.findByType(type, LitemallTypeRole.Column.roleIds).getRoleIds();
    }

    public LitemallAdminIntegration findById(Integer id) {
        return adminIntegrationMapper.selectByPrimaryKey(id);
    }
    public List<LitemallAdminIntegration> findByIds(List<Integer> idList) {
        LitemallAdminIntegrationExample example = new LitemallAdminIntegrationExample();
        example.or().andIdIn(idList);
        return adminIntegrationMapper.selectByExample(example);
    }

    public int add(LitemallAdminIntegration litemallAdminIntegration) {
        return adminIntegrationMapper.insert(litemallAdminIntegration);
    }

    public int updateById(LitemallAdminIntegration adminIntegration) {
        return adminIntegrationMapper.updateByPrimaryKeySelective(adminIntegration);
    }
}
