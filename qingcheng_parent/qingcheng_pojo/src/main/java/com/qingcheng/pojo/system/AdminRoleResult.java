package com.qingcheng.pojo.system;

import java.io.Serializable;
import java.util.List;

/**
 * 用户和角色组合实体类
 */
public class AdminRoleResult implements Serializable {

    private Admin admin;
    private List<Role> roles;

    public Admin getAdmin() {
        return admin;
    }

    public void setAdmin(Admin admin) {
        this.admin = admin;
    }

    public List<Role> getRoles() {
        return roles;
    }

    public void setRoles(List<Role> roles) {
        this.roles = roles;
    }
}
