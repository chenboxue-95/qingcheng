package com.qingcheng.service.impl;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONPath;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.qingcheng.dao.*;
import com.qingcheng.entity.PageResult;
import com.qingcheng.pojo.goods.*;
import com.qingcheng.service.goods.SkuService;
import com.qingcheng.service.goods.SpuService;
import com.qingcheng.util.IdWorker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import java.util.*;

@Service(interfaceClass = SpuService.class)
public class SpuServiceImpl implements SpuService {

    @Autowired
    private SpuMapper spuMapper;

    @Autowired
    private SkuMapper skuMapper;

    @Autowired
    private IdWorker idWorker;

    @Autowired
    private CategoryMapper categoryMapper;

    @Autowired
    private CategoryBrandMapper categoryBrandMapper;

    @Autowired
    private CheckMapper checkMapper;

    @Autowired
    private SkuService skuService;

    /**
     * 返回全部记录
     * @return
     */
    public List<Spu> findAll() {
        return spuMapper.selectAll();
    }

    /**
     * 分页查询
     * @param page 页码
     * @param size 每页记录数
     * @return 分页结果
     */
    public PageResult<Spu> findPage(int page, int size) {
        PageHelper.startPage(page,size);
        Page<Spu> spus = (Page<Spu>) spuMapper.selectAll();
        return new PageResult<Spu>(spus.getTotal(),spus.getResult());
    }

    /**
     * 条件查询
     * @param searchMap 查询条件
     * @return
     */
    public List<Spu> findList(Map<String, Object> searchMap) {
        Example example = createExample(searchMap);
        return spuMapper.selectByExample(example);
    }

    /**
     * 分页+条件查询
     * @param searchMap
     * @param page
     * @param size
     * @return
     */
    public PageResult<Spu> findPage(Map<String, Object> searchMap, int page, int size) {
        PageHelper.startPage(page,size);
        Example example = createExample(searchMap);
        Page<Spu> spus = (Page<Spu>) spuMapper.selectByExample(example);
        return new PageResult<Spu>(spus.getTotal(),spus.getResult());
    }

    /**
     * 根据Id查询
     * @param id
     * @return
     */
    public Spu findById(String id) {
        return spuMapper.selectByPrimaryKey(id);
    }

    /**
     * 新增
     * @param spu
     */
    public void add(Spu spu) {
        spuMapper.insert(spu);
    }

    /**
     * 修改
     * @param spu
     */
    public void update(Spu spu) {
        spuMapper.updateByPrimaryKeySelective(spu);
    }


    /**
     *  删除
     * @param id
     */
    public void delete(String id) {

        //删除缓存中的价格
        Map map =new HashMap();
        map.put("spuId",id);
        List<Sku> skuList = skuService.findList(map);
        for (Sku sku : skuList) {
            skuService.deletePriceFromRedis(sku.getId());
        }

        spuMapper.deleteByPrimaryKey(id);


    }



    /**
     *  添加商品
     * @param goods
     */
    @Override
    @Transactional
    public void save(Goods goods) {

//        添加spu
        Spu spu = goods.getSpu();
        if(spu.getId()==null){// 没有id 是添加功能 添加一个ID属性
            spu.setId(idWorker.nextId()+"");
            //      添加spu
            spuMapper.insert(spu);

        }else { // 有id 是更新功能
            //1 先删除原有的sku
            Example example = new Example(Sku.class);
            Example.Criteria criteria = example.createCriteria();
            criteria.andEqualTo("spuId",spu.getId());
            skuMapper.deleteByExample(example);
//            2 更新spu
            spuMapper.updateByPrimaryKeySelective(spu);
        }


    //根据spu数据查询  分类对象
        Category category = categoryMapper.selectByPrimaryKey(spu.getCategory3Id());

//        添加sku
        List<Sku> skuList = goods.getSkuList();
        for (Sku sku : skuList) {
//            需要设置的字段 id  squId Name CreateTime UpdateTime CategoryId CategoryName CommentNum SaleNum
            if(sku.getId()==null){// 无id   是添加操作
                // 1 设置id
                sku.setId(idWorker.nextId()+"");
                // 2 设置CreateTime
                sku.setCreateTime(new Date());
            }

//            判断规格列表是否为空
            if(sku.getSpec()==null||"".equals(sku.getSpec())){
                sku.setSpec("{}");
            }

//           3 设置 Name =squName+ sku规格名称
            String name = spu.getName();
            //sku.getSpec()  {"颜色":"红","机身内存":"64G"
            Map<String,String> specMap = JSON.parseObject(sku.getSpec(), Map.class);
            for (String value : specMap.values()) {
                System.out.println(specMap.values());
                name += "" +value;
            }
            sku.setName(name);
            sku.setSpuId(spu.getId());
            sku.setUpdateTime(new Date());// 4 设置更新UpdateTime
            sku.setCategoryId(spu.getCategory3Id());   //5 设置CategoryId  种类id
            sku.setCategoryName(category.getName());    // 6 设置CategoryName  分类名称
            sku.setCommentNum(0); //设置评论数
            sku.setSaleNum(0);//设置销售数量
//          保存sku

            skuMapper.insert(sku);

            //重新更新价格到缓存
            skuService.savePriceToRedisById(sku.getId(),sku.getPrice());



        }


//       添加关联表数据
        CategoryBrand categoryBrand = new CategoryBrand();
        categoryBrand.setBrandId(spu.getBrandId());
        categoryBrand.setCategoryId(spu.getCategory3Id());
        int count = categoryBrandMapper.selectCount(categoryBrand);
        if (count==0){
            categoryBrandMapper.insert(categoryBrand);
        }

    }

    /**
     * 查询商品
     * @param id
     * @return
     */
    @Override
    @Transactional
    public Goods findGoodsById(String id) {
        //封装squ
        Spu spu = spuMapper.selectByPrimaryKey(id);
        //封装sku
        Example example = new Example(Sku.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("spuId",id);
        List<Sku> skus = skuMapper.selectByExample(example);
        System.out.println(skus);

        //组装goods
        Goods goods = new Goods();
        goods.setSpu(spu);
        goods.setSkuList(skus);

        return goods;
    }

    /**
     * 商品审核功能
     * @param id
     * @param status
     * @param message
     */
    @Override
    @Transactional
    public void audit(String id, String status, String message) {
//        1   根据审核信息更改商品是否上架
        Spu spu = new Spu();
        spu.setId(id);
        spu.setStatus(status);
        if ("1".equals(status)){//如果审核状态为 1  上架
            spu.setIsMarketable("1");  //自动上架
            spuMapper.updateByPrimaryKeySelective(spu);

        }

//      2 记录审核检查记录
        Check check = new Check();
        check.setTime(new Date());
        check.setUser("周健吃屎長大");
        check.setResult(status);
        check.setMessage(message);
        checkMapper.insert(check);

    }

    /**
     * 下架功能
     * @param id
     */
    @Override
    public void pull(String id) {
        Spu spu = new Spu();
        spu.setId(id);
        spu.setIsMarketable("0");
        spuMapper.updateByPrimaryKeySelective(spu);

    }

    /**
     * 上架
     * @param id
     */
    @Override
    @Transactional
    public void put(String id) {
        Spu spu = spuMapper.selectByPrimaryKey(id);
        if("1".equals(spu.getStatus())){//审核通过
            spu.setIsMarketable("1");  //  上架
            spuMapper.updateByPrimaryKeySelective(spu);
        }else {
            throw new RuntimeException("此商品还未上架");
        }

    }

    /**
     * 批量上架
     * @param ids
     */
    @Override
    public int putMany(String[] ids) {
//        for (String id : ids) {
//            Spu spu = spuMapper.selectByPrimaryKey(id);
//            if("1".equals(spu.getStatus())){//审核通过
//                spu.setIsMarketable("1");  //上架
//                spuMapper.updateByPrimaryKeySelective(spu);
//            }
//
//        }
        Spu spu = new Spu();
        spu.setIsMarketable("1");

        Example example = new Example(Spu.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andIn("id", Arrays.asList(ids));//要上架的商品id
        criteria.andEqualTo("isMarketable","0"); //下架的可以上架
        criteria.andEqualTo("status","1");//审核为1 的可以上架
        int count = spuMapper.updateByExampleSelective(spu, example);//spu 更新的内容  example 更新的条件
        return count;

    }

    /**
     * 商品批量删除和还原
     * @param ids
     * @param status
     * @return
     */
    @Override
    public int deleteGoods(String[] ids, String status) {
        //2 设置需要更改的数据
        Spu spu = new Spu();
        spu.setIsDelete(status);

//        3.设置符合更改的条件
        Example example = new Example(Spu.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andIn("id",Arrays.asList(ids));//需要删除的商品id

//        1 根据条件批量更改isdelete状态为1
        int i = spuMapper.updateByExampleSelective(spu, example);
        return i;

    }


    /**
     * 构建查询条件
     * @param searchMap
     * @return
     */
    private Example createExample(Map<String, Object> searchMap){
        Example example=new Example(Spu.class);
        Example.Criteria criteria = example.createCriteria();
        if(searchMap!=null){
            // 主键
            if(searchMap.get("id")!=null && !"".equals(searchMap.get("id"))){
                criteria.andLike("id","%"+searchMap.get("id")+"%");
            }
            // 货号
            if(searchMap.get("sn")!=null && !"".equals(searchMap.get("sn"))){
                criteria.andLike("sn","%"+searchMap.get("sn")+"%");
            }
            // SPU名
            if(searchMap.get("name")!=null && !"".equals(searchMap.get("name"))){
                criteria.andLike("name","%"+searchMap.get("name")+"%");
            }
            // 副标题
            if(searchMap.get("caption")!=null && !"".equals(searchMap.get("caption"))){
                criteria.andLike("caption","%"+searchMap.get("caption")+"%");
            }
            // 图片
            if(searchMap.get("image")!=null && !"".equals(searchMap.get("image"))){
                criteria.andLike("image","%"+searchMap.get("image")+"%");
            }
            // 图片列表
            if(searchMap.get("images")!=null && !"".equals(searchMap.get("images"))){
                criteria.andLike("images","%"+searchMap.get("images")+"%");
            }
            // 售后服务
            if(searchMap.get("saleService")!=null && !"".equals(searchMap.get("saleService"))){
                criteria.andLike("saleService","%"+searchMap.get("saleService")+"%");
            }
            // 介绍
            if(searchMap.get("introduction")!=null && !"".equals(searchMap.get("introduction"))){
                criteria.andLike("introduction","%"+searchMap.get("introduction")+"%");
            }
            // 规格列表
            if(searchMap.get("specItems")!=null && !"".equals(searchMap.get("specItems"))){
                criteria.andLike("specItems","%"+searchMap.get("specItems")+"%");
            }
            // 参数列表
            if(searchMap.get("paraItems")!=null && !"".equals(searchMap.get("paraItems"))){
                criteria.andLike("paraItems","%"+searchMap.get("paraItems")+"%");
            }
            // 是否上架
            if(searchMap.get("isMarketable")!=null && !"".equals(searchMap.get("isMarketable"))){
                criteria.andLike("isMarketable","%"+searchMap.get("isMarketable")+"%");
            }
            // 是否启用规格
            if(searchMap.get("isEnableSpec")!=null && !"".equals(searchMap.get("isEnableSpec"))){
                criteria.andLike("isEnableSpec","%"+searchMap.get("isEnableSpec")+"%");
            }
            // 是否删除
            if(searchMap.get("isDelete")!=null && !"".equals(searchMap.get("isDelete"))){
                criteria.andLike("isDelete","%"+searchMap.get("isDelete")+"%");
            }
            // 审核状态
            if(searchMap.get("status")!=null && !"".equals(searchMap.get("status"))){
                criteria.andLike("status","%"+searchMap.get("status")+"%");
            }

            // 品牌ID
            if(searchMap.get("brandId")!=null ){
                criteria.andEqualTo("brandId",searchMap.get("brandId"));
            }
            // 一级分类
            if(searchMap.get("category1Id")!=null ){
                criteria.andEqualTo("category1Id",searchMap.get("category1Id"));
            }
            // 二级分类
            if(searchMap.get("category2Id")!=null ){
                criteria.andEqualTo("category2Id",searchMap.get("category2Id"));
            }
            // 三级分类
            if(searchMap.get("category3Id")!=null ){
                criteria.andEqualTo("category3Id",searchMap.get("category3Id"));
            }
            // 模板ID
            if(searchMap.get("templateId")!=null ){
                criteria.andEqualTo("templateId",searchMap.get("templateId"));
            }
            // 运费模板id
            if(searchMap.get("freightId")!=null ){
                criteria.andEqualTo("freightId",searchMap.get("freightId"));
            }
            // 销量
            if(searchMap.get("saleNum")!=null ){
                criteria.andEqualTo("saleNum",searchMap.get("saleNum"));
            }
            // 评论数
            if(searchMap.get("commentNum")!=null ){
                criteria.andEqualTo("commentNum",searchMap.get("commentNum"));
            }

        }
        return example;
    }

}
