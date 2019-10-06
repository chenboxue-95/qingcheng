package com.qingcheng.dao;

import com.qingcheng.pojo.system.Role;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface RoleMapper extends Mapper<Role> {

    /**
     * 根据用户id通过关联表查询对应角色
     * @param id
     * @return
     */
    @Select("SELECT id,name FROM `tb_role` WHERE id IN (SELECT role_id FROM `tb_admin_role` WHERE admin_id=#{id})")
    public List<Role> findRoles(@Param("id") Integer id);

}
