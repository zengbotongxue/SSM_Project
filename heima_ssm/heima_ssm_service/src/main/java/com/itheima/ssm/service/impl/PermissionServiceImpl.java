package com.itheima.ssm.service.impl;

import com.itheima.ssm.dao.IPermissionsDao;
import com.itheima.ssm.domain.Permission;
import com.itheima.ssm.service.IPermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PermissionServiceImpl implements IPermissionService {

    @Autowired
    private IPermissionsDao permissionsDao;

    @Override
    public List<Permission> findAll() throws Exception {
        return permissionsDao.findAll();
    }

    @Override
    public void save(Permission permission) throws Exception {
        permissionsDao.save(permission);
    }
}
