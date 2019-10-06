package com.qingcheng.service.system;
import com.qingcheng.entity.PageResult;
import com.qingcheng.pojo.system.Role;
import com.qingcheng.pojo.system.RoleResourceResult;

import java.util.*;

/**
 * role业务逻辑层
 */
public interface RoleService {


    public List<Role> findAll();


    public PageResult<Role> findPage(int page, int size);


    public List<Role> findList(Map<String,Object> searchMap);


    public PageResult<Role> findPage(Map<String,Object> searchMap,int page, int size);

    /**
     * 根据id查询对应角色和权限
     * @param id
     * @return
     */
    public RoleResourceResult findById(Integer id);

    /**
     * 添加角色
     * @param roleResourceResult
     */
    public void add(RoleResourceResult roleResourceResult);

    /**
     * 改变角色和对应权限
     * @param roleResourceResult
     */
    public void update(RoleResourceResult roleResourceResult);


    public void delete(Integer id);

}
