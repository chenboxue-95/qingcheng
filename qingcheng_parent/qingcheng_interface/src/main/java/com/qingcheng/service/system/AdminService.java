package com.qingcheng.service.system;

import com.qingcheng.entity.PageResult;
import com.qingcheng.pojo.system.Admin;
import com.qingcheng.pojo.system.AdminRoleResult;

import java.util.*;

/**
 * admin业务逻辑层
 */
public interface AdminService {


    public List<Admin> findAll();


    public PageResult<Admin> findPage(int page, int size);


    public List<Admin> findList(Map<String, Object> searchMap);


    public PageResult<Admin> findPage(Map<String, Object> searchMap, int page, int size);

    /**
     * 根据id查找用户和对应角色集合
     * @param id
     * @return
     */
    public AdminRoleResult findById(Integer id);

    /**
     * 新增用户
     * @param adminRoleResult
     */
    public void add(AdminRoleResult adminRoleResult);

    /**
     * 修改用户
     * @param adminRoleResult
     */
    public void update(AdminRoleResult adminRoleResult);


    public void delete(Integer id);



}
