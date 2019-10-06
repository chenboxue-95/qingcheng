package com.qingcheng.controller.system;

import com.alibaba.dubbo.config.annotation.Reference;
import com.qingcheng.entity.PageResult;
import com.qingcheng.entity.Result;
import com.qingcheng.pojo.system.Role;
import com.qingcheng.pojo.system.RoleResourceResult;
import com.qingcheng.service.system.RoleService;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/role")
public class RoleController {

    @Reference
    private RoleService roleService;

    @GetMapping("/findAll")
    public List<Role> findAll() {
        return roleService.findAll();
    }

    @GetMapping("/findPage")
    public PageResult<Role> findPage(int page, int size) {
        return roleService.findPage(page, size);
    }

    @PostMapping("/findList")
    public List<Role> findList(@RequestBody Map<String, Object> searchMap) {
        return roleService.findList(searchMap);
    }

    @PostMapping("/findPage")
    public PageResult<Role> findPage(@RequestBody Map<String, Object> searchMap, int page, int size) {
        return roleService.findPage(searchMap, page, size);
    }

    /**
     * 根据id查询对应角色和对应权限
     * @param id
     * @return
     */
    @GetMapping("/findById")
    public RoleResourceResult findById(Integer id) {
        return roleService.findById(id);
    }

    /**
     * 添加角色
     * @param roleResourceResult
     * @return
     */
    @PostMapping("/add")
    public Result add(@RequestBody RoleResourceResult roleResourceResult) {
        roleService.add(roleResourceResult);
        return new Result();
    }

    /**
     * 更改角色和对应权限
     * @param roleResourceResult
     * @return
     */
    @PostMapping("/update")
    public Result update(@RequestBody RoleResourceResult roleResourceResult) {
        roleService.update(roleResourceResult);
        return new Result();
    }

    @GetMapping("/delete")
    public Result delete(Integer id) {
        roleService.delete(id);
        return new Result();
    }

}
