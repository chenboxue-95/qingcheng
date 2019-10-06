package com.qingcheng.dao;

import com.qingcheng.pojo.system.Menu;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;
import java.util.Map;


public interface MenuMapper extends Mapper<Menu> {

    //根据用户名查询对应Menus
    @Select("SELECT id,name,icon,url,parent_id parentId FROM tb_menu where id in( " +
            "SELECT menu_id FROM tb_resource_menu where resource_id in(  " +
            "SELECT resource_id from tb_role_resource where role_id in(  " +
            "SELECT role_id from tb_admin_role where admin_id in( " +
            "SELECT id from tb_admin WHERE login_name='admin' " +
            ") " +
            ") " +
            ") " +
            ") " +
            "UNION " +
            "SELECT id,name,icon,url,parent_id parentId FROM tb_menu where id in( " +
            "SELECT parent_id FROM tb_menu where id in( " +
            "SELECT menu_id FROM tb_resource_menu where resource_id in(  " +
            "SELECT resource_id from tb_role_resource where role_id in(  " +
            "SELECT role_id from tb_admin_role where admin_id in( " +
            "SELECT id from tb_admin WHERE login_name='admin' " +
            ") " +
            ") " +
            ") " +
            ") " +
            ")UNION  " +
            "SELECT id,name,icon,url,parent_id parentId FROM tb_menu where id in( " +
            "SELECT parent_id FROM tb_menu where id in( " +
            "SELECT parent_id FROM tb_menu where id in( " +
            "SELECT menu_id FROM tb_resource_menu where resource_id in(  " +
            "SELECT resource_id from tb_role_resource where role_id in(  " +
            "SELECT role_id from tb_admin_role where admin_id in( " +
            "SELECT id from tb_admin WHERE login_name='admin' " +
            ") " +
            ") " +
            ") " +
            ") " +
            ") " +
            ")")
    public List<Menu> findMenuByLoginName(@Param("loginName") String loginName);


}
