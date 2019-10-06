package com.qingcheng.pojo.system;

import javax.persistence.Table;
import java.io.Serializable;
import java.util.List;

/**
 * 角色和权限(资源)组合实体类
 */

public class RoleResourceResult implements Serializable {

    private Role role;
    private List<Resource> resources;

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public List<Resource> getResources() {
        return resources;
    }

    public void setResources(List<Resource> resources) {
        this.resources = resources;
    }
}
