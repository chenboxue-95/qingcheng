package com.qingcheng.service.impl;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.qingcheng.dao.AdminMapper;
import com.qingcheng.dao.AdminRoleMapper;
import com.qingcheng.dao.RoleMapper;
import com.qingcheng.entity.PageResult;
import com.qingcheng.pojo.system.Admin;
import com.qingcheng.pojo.system.AdminRole;
import com.qingcheng.pojo.system.AdminRoleResult;
import com.qingcheng.pojo.system.Role;
import com.qingcheng.service.system.AdminService;
import com.qingcheng.util.jbcrypt.BCrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;
import java.util.List;
import java.util.Map;

@Service(interfaceClass =AdminService.class )
public class AdminServiceImpl implements AdminService {

    @Autowired
    private AdminMapper adminMapper;

    @Autowired
    private AdminRoleMapper adminRoleMapper;

    @Autowired
    private RoleMapper roleMapper;

    /**
     * 返回全部记录
     * @return
     */
    public List<Admin> findAll() {
        return adminMapper.selectAll();
    }

    /**
     * 分页查询
     * @param page 页码
     * @param size 每页记录数
     * @return 分页结果
     */
    public PageResult<Admin> findPage(int page, int size) {
        PageHelper.startPage(page,size);
        Page<Admin> admins = (Page<Admin>) adminMapper.selectAll();
        return new PageResult<Admin>(admins.getTotal(),admins.getResult());
    }

    /**
     * 条件查询
     * @param searchMap 查询条件
     * @return
     */
    public List<Admin> findList(Map<String, Object> searchMap) {
        Example example = createExample(searchMap);
        return adminMapper.selectByExample(example);
    }

    /**
     * 分页+条件查询
     * @param searchMap
     * @param page
     * @param size
     * @return
     */
    public PageResult<Admin> findPage(Map<String, Object> searchMap, int page, int size) {
        PageHelper.startPage(page,size);
        Example example = createExample(searchMap);
        Page<Admin> admins = (Page<Admin>) adminMapper.selectByExample(example);
        return new PageResult<Admin>(admins.getTotal(),admins.getResult());
    }

    /**
     * 根据id查询对应用户和角色信息
     * @param id
     * @return
     */
    @Override
    public AdminRoleResult findById(Integer id) {
        AdminRoleResult adminRoleResult = new AdminRoleResult();
//        查询admin
        Admin admin = adminMapper.selectByPrimaryKey(id);
        admin.setPassword(null);  //密码设为空
        adminRoleResult.setAdmin(admin);

//        查询对应角色
        List<Role> roles = roleMapper.findRoles(id);
        adminRoleResult.setRoles(roles);
        return adminRoleResult;
    }

    /**
     * 新增用户
     * @param adminRoleResult
     */
    @Transactional
    @Override
    public void add(AdminRoleResult adminRoleResult) {

//        添加用户
        Admin admin = adminRoleResult.getAdmin();
        String password = admin.getPassword();//获得密码  进行加密
        String hashpw = BCrypt.hashpw(password, BCrypt.gensalt());//获得加密密码
        admin.setPassword(hashpw);
        adminMapper.insert(admin);

//        添加用户和角色关系
        List<Role> roles = adminRoleResult.getRoles();
        for (Role role : roles) {
            AdminRole adminRole = new AdminRole();
            adminRole.setRoleId(role.getId());
            adminRole.setAdminId(admin.getId());
            adminRoleMapper.insert(adminRole);

        }

    }

    /**
     * 修改用户
     * @param adminRoleResult
     */
    @Override
    public void update(AdminRoleResult adminRoleResult) {

//        更新用户
        Admin admin = adminRoleResult.getAdmin();
        adminMapper.updateByPrimaryKey(admin);

//        更新用户和角色关系表
        //先删除
        Example example =new Example(AdminRole.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("adminId",admin.getId());
        adminRoleMapper.deleteByExample(example);
        //再添加
        List<Role> roles = adminRoleResult.getRoles();
        for (Role role : roles) {
            AdminRole adminRole = new AdminRole();
            adminRole.setRoleId(role.getId());
            adminRole.setAdminId(admin.getId());
            adminRoleMapper.insert(adminRole);
        }

    }


    /**
     *  删除
     * @param id
     */
    public void delete(Integer id) {
        adminMapper.deleteByPrimaryKey(id);
    }



    /**
     * 构建查询条件
     * @param searchMap
     * @return
     */
    private Example createExample(Map<String, Object> searchMap){
        Example example=new Example(Admin.class);
        Example.Criteria criteria = example.createCriteria();
        if(searchMap!=null){
            // 用户名
            if(searchMap.get("loginName")!=null && !"".equals(searchMap.get("loginName"))){
                criteria.andEqualTo("loginName",searchMap.get("loginName"));
            }
            // 密码
            if(searchMap.get("password")!=null && !"".equals(searchMap.get("password"))){
                criteria.andEqualTo("password",searchMap.get("password"));
            }
            // 状态
            if(searchMap.get("status")!=null && !"".equals(searchMap.get("status"))){
                criteria.andEqualTo("status",searchMap.get("status"));
            }

            // id
            if(searchMap.get("id")!=null ){
                criteria.andEqualTo("id",searchMap.get("id"));
            }

        }
        return example;
    }

}
