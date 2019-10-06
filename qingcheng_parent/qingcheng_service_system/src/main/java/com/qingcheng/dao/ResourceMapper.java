package com.qingcheng.dao;

import com.qingcheng.pojo.system.Resource;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

/**
 * Resource 数据访问层
 */
public interface ResourceMapper extends Mapper<Resource> {
    /**
     * 根据角色ID通过中间表查询对应权限
     * @param id
     * @return
     */
    @Select("SELECT id,res_key resKey,res_name resName,parent_id parentId  FROM `tb_resource` WHERE id IN (SELECT resource_id FROM `tb_role_resource` WHERE role_id=#{id});")
    public List<Resource> findResourceById(@Param("id") Integer id);



    //    通过登录名查询对应资源res_key列表
    @Select("SELECT res_key FROM tb_resource where id in(  " +
            " SELECT resource_id from tb_role_resource where role_id in(  " +
            "  SELECT role_id from tb_admin_role where admin_id in( " +
            "   SELECT id from tb_admin WHERE login_name=#{loginName} " +
            ") " +
            ") " +
            ")")
    public List<String> findResourcesByLoginName(@Param("loginName") String loginName);

}
