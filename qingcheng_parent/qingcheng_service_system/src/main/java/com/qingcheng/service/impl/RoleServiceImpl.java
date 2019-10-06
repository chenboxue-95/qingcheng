package com.qingcheng.service.impl;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.qingcheng.dao.ResourceMapper;
import com.qingcheng.dao.RoleMapper;
import com.qingcheng.dao.RoleResourceMapper;
import com.qingcheng.entity.PageResult;
import com.qingcheng.pojo.system.Resource;
import com.qingcheng.pojo.system.Role;
import com.qingcheng.pojo.system.RoleResource;
import com.qingcheng.pojo.system.RoleResourceResult;
import com.qingcheng.service.system.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import java.util.List;
import java.util.Map;

@Service(interfaceClass = RoleService.class)
public class RoleServiceImpl implements RoleService {

    @Autowired
    private RoleMapper roleMapper;

    @Autowired
    private ResourceMapper resourceMapper;

    @Autowired
    private RoleResourceMapper roleResourceMapper;

    /**
     * 返回全部记录
     * @return
     */
    public List<Role> findAll() {
        return roleMapper.selectAll();
    }

    /**
     * 分页查询
     * @param page 页码
     * @param size 每页记录数
     * @return 分页结果
     */
    public PageResult<Role> findPage(int page, int size) {
        PageHelper.startPage(page,size);
        Page<Role> roles = (Page<Role>) roleMapper.selectAll();
        return new PageResult<Role>(roles.getTotal(),roles.getResult());
    }

    /**
     * 条件查询
     * @param searchMap 查询条件
     * @return
     */
    public List<Role> findList(Map<String, Object> searchMap) {
        Example example = createExample(searchMap);
        return roleMapper.selectByExample(example);
    }

    /**
     * 分页+条件查询
     * @param searchMap
     * @param page
     * @param size
     * @return
     */
    public PageResult<Role> findPage(Map<String, Object> searchMap, int page, int size) {
        PageHelper.startPage(page,size);
        Example example = createExample(searchMap);
        Page<Role> roles = (Page<Role>) roleMapper.selectByExample(example);
        return new PageResult<Role>(roles.getTotal(),roles.getResult());
    }

    /**
     * 根据Id查询对应角色和权限
     * @param id
     * @return
     */
    @Transactional
    public RoleResourceResult findById(Integer id) {
        RoleResourceResult roleResourceResult = new RoleResourceResult();
//        查找角色
        Role role = roleMapper.selectByPrimaryKey(id);
        roleResourceResult.setRole(role);

//        通过中间表查询角色对应权限
        List<Resource> resources = resourceMapper.findResourceById(id);
        roleResourceResult.setResources(resources);

        return roleResourceResult;
    }

    /**
     * 添加角色
     * @param roleResourceResult
     */
    @Override
    public void add(RoleResourceResult roleResourceResult) {
//        添加角色
        Role role = roleResourceResult.getRole();
        roleMapper.insert(role);
//        添加角色权限关系
        List<Resource> resources = roleResourceResult.getResources();
        for (Resource resource : resources) {
            RoleResource roleResource = new RoleResource();
            roleResource.setRoleId(role.getId());
            roleResource.setResourceId(resource.getId());
            roleResourceMapper.insert(roleResource);
        }


    }


    /**
     * 更改对应角色和权限
     * @param roleResourceResult
     */
    @Override
    public void update(RoleResourceResult roleResourceResult) {
        Role role = roleResourceResult.getRole();
//        改变角色
        roleMapper.updateByPrimaryKey(role);

//        改变权限
        //通过中间表删除角色对应权限关系
        Example example=new Example(RoleResource.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("roleId",role.getId());
        roleResourceMapper.deleteByExample(example);

        //添加角色
        List<Resource> resources = roleResourceResult.getResources();
        for (Resource resource : resources) {//循环添加关系
            RoleResource roleResource = new RoleResource();
            roleResource.setResourceId(resource.getId());
            roleResource.setRoleId(role.getId());
        }
    }


    /**
     *  删除
     * @param id
     */
    public void delete(Integer id) {
        roleMapper.deleteByPrimaryKey(id);
    }

    /**
     * 构建查询条件
     * @param searchMap
     * @return
     */
    private Example createExample(Map<String, Object> searchMap){
        Example example=new Example(Role.class);
        Example.Criteria criteria = example.createCriteria();
        if(searchMap!=null){
            // 角色名称
            if(searchMap.get("name")!=null && !"".equals(searchMap.get("name"))){
                criteria.andLike("name","%"+searchMap.get("name")+"%");
            }

            // ID
            if(searchMap.get("id")!=null ){
                criteria.andEqualTo("id",searchMap.get("id"));
            }

        }
        return example;
    }

}
