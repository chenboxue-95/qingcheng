package com.qingcheng.controller.system;

import com.alibaba.dubbo.config.annotation.Reference;
import com.qingcheng.entity.PageResult;
import com.qingcheng.entity.Result;
import com.qingcheng.pojo.system.Admin;
import com.qingcheng.pojo.system.AdminRoleResult;
import com.qingcheng.service.system.AdminService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/admin")
public class AdminController {

    @Reference
    private AdminService adminService;

    @GetMapping("/findAll")
    public List<Admin> findAll() {
        return adminService.findAll();
    }

    @GetMapping("/findPage")
    public PageResult<Admin> findPage(int page, int size) {
        return adminService.findPage(page, size);
    }

    @PostMapping("/findList")
    public List<Admin> findList(@RequestBody Map<String, Object> searchMap) {
        return adminService.findList(searchMap);
    }

    @PostMapping("/findPage")
    public PageResult<Admin> findPage(@RequestBody Map<String, Object> searchMap, int page, int size) {
        return adminService.findPage(searchMap, page, size);
    }

    /**
     * 根据用户id查询对应用户和角色
     * @param id
     * @return
     */
    @GetMapping("/findById")
    public AdminRoleResult findById(Integer id) {
        return adminService.findById(id);
    }

    /**
     * 添加用户
     * @param adminRoleResult 添加参数
     * @return
     */
    @PostMapping("/add")
    public Result add(@RequestBody AdminRoleResult adminRoleResult) {
        adminService.add(adminRoleResult);
        return new Result();
    }

    /**
     * 修改用户u
     * @param adminRoleResult
     * @return
     */
    @PostMapping("/update")
    public Result update(@RequestBody AdminRoleResult adminRoleResult) {
        adminService.update(adminRoleResult);
        return new Result();
    }

    @GetMapping("/delete")
    public Result delete(Integer id) {
        adminService.delete(id);
        return new Result();
    }

//    /**
//     * 修改密码
//     * @param password
//     * @param newPassword
//     * @return
//     */
//    @PostMapping("/updateLoginName")
//    public Result updateLoginName(String password, String newPassword) {
////      根据登陆用户查到对应用户和密码
//        String loginName = SecurityContextHolder.getContext().getAuthentication().getName();
//        Map map = new HashMap();
//        map.put("loginName", loginName);
//        List<Admin> list = adminService.findList(map);
//        if (list == null) {
//            return new Result(0, "用户不存在");
//        }if (BCrypt.checkpw(newPassword, list.get(0).getPassword())) {
//            String hashpw = BCrypt.hashpw(password, BCrypt.gensalt()); //对新密码加密
//            Admin admin = list.get(0);
//            admin.setLoginName(loginName);
//            admin.setPassword(hashpw);//设置新密码
//            adminService.update(admin);
//        }
//        return new Result(0,"修改成功!");
//
//
//    }




}
