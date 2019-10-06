package com.qingcheng.service.system;

import com.qingcheng.entity.PageResult;
import com.qingcheng.pojo.system.Menu;

import java.util.*;

/**
 * menu业务逻辑层
 */
public interface MenuService {


    public List<Menu> findAll();


    public PageResult<Menu> findPage(int page, int size);


    public List<Menu> findList(Map<String, Object> searchMap);


    public PageResult<Menu> findPage(Map<String, Object> searchMap, int page, int size);


    public Menu findById(String id);

    public void add(Menu menu);


    public void update(Menu menu);


    public void delete(String id);


    /**
     * 查询所有菜单及其子菜单内容
     *
     * @return
     */
    public List<Map> findAllMenu();

    /**
     * 根据用户名查询对应Menus
     * @param loginName
     * @return
     */
    public List<Map> findMenuByLoginName( String loginName);
}

