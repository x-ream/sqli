# sqli 简单的SQL 拼写接口框架
   [http://sqli.xream.io](http://sqli.xream.io) 
   
[![license](https://img.shields.io/github/license/x-ream/sqli.svg)](https://www.apache.org/licenses/LICENSE-2.0.html)
[![maven](https://img.shields.io/maven-central/v/io.xream.sqli/sqli-parent.svg)](https://search.maven.org/search?q=io.xream)
[![Codacy Badge](https://app.codacy.com/project/badge/Grade/8e414bcc7a6944529c5a35b27b2d5e37)](https://www.codacy.com/gh/x-ream/sqli?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=x-ream/sqli&amp;utm_campaign=Badge_Grade)
[![Gitter](https://badges.gitter.im/x-ream/x-ream.svg)](https://gitter.im/x-ream/community)
    

    sqli/sqli-core
    sqli/sqli-dialect
    sqli/sqli-repo
        
## sqli-repo 

### 使用方法
    在x7项目里实现，spring boot的注解实现或使用的模板如下:
    
    @EnableX7Repostory  // code at x7/x7-repo/x7-spring-boot-starter
    public class App{
        main()
    
    更多代码片段:
    
    @Repository
    public interface FooRepository extends BaseRepository<Foo> {}

    @X.Mapping("t_foo")//默认是foo
    public class Foo {
        @X.Key
        private Long id;
        @X.Mapping("full_name") //默认是fullName
        private String fullName;
    }
    
    {
        @Autowired
        private TemporaryRepository temporaryRepository;
    }
    
### BaseRepository API
    
            1. in(InCondition) //in查询, 例如: 页面上需要的主表ID或记录已经查出后，补充查询其他表的文本说明数据时使用
            2. list(Object) //对象查列表
            3. find(Criteria) //标准拼接查询，返回对象形式记录，返回分页对象
            4. list(Criteria) //标准拼接查询，返回对象形式记录，不返回分页对象
            5. get(Id) //根据主键查询记录
            6. getOne(Object) //数据库只有一条记录时，就返回那条记录
            7. list() //无条件查全表, 几乎没使用场景
            8. find(ResultMapCriteria) //标准拼接查询，返回Map形式记录，返回分页对象
            9. list(ResultMapCriteria) //标准拼接查询，返回Map形式记录，不返回分页对象
            10. listPlainValue(Class<K>, ResultMapCriteria)//返回没有key的单列数据列表 (结果优化1)
            11. findToHandle(ResultMapCriteria, RowHandler<Map<String,Object>>) //流处理API
            12. creaet(Object) //插入一条
            13. createBatch(List<Object>) //批量插入
            14. refresh(RefreshCondition) //根据主键更新
            15. refreshUnSafe(RefreshCondition)//不根据主键更新
            16. remove(Id)//根据主键删除
            17. removeRefreshCreate(RemoveRefreshCreate<T>) //编辑页面列表时写数据库

### TemporaryRepository API   

            1. creaet(Object) //插入一条
            2. createBatch(List<Object>) //批量插入, 适用于数据导入场景     
            3. findToCreate(Class, Criteria.ResultMapCriteria) //builder.resultKey("foo.fooName","foo_name"), 
                    //仅在临时表需要设置foo_name, 如果不设置, sqli默认按顺序设置c0,c1...., 无法和临时表匹配
            4. createRepository(Class)
            5. dropRepository(Class) //在最后调用此API, 其他框架不会关闭连接而删除临时表
            
            提醒: 不建议基于临时表调用refresh(RefreshCondition), 建议尝试调用findToHandle(....)流处理API,
                  异步更新, 用fallback替代事务
            
### 标准拼接API

        CriteriaBuilder // 返回Criteria, 查出对象形式记录
        CriteriaBuilder.ResultMapBuilder //返回ResultMapCriteria, 查出Map形式记录，支持连表查询
        RefreshCondition //构建要更新的字段和条件
        
        代码片段:
            {
                CriteriaBuilder builder = CriteriaBuilder.builder(Order.class); 
                builder.eq("userId",obj.getUserId()).eq("status","PAID");
                Criteria criteria = builer.get();
                orderRepository.find(criteria);
            }
        
            {
                CriteriaBuilder.ResultMapBuilder builder = CriteriaBuilder.resultMapBuilder();
                builder.resultKey("o.id");
                builder.eq("o.status","PAID");
                builder.beginSub().gt("o.createAt",obj.getStartTime()).lt("o.createAt",obj.getEndTime()).endSub();
                builder.beginSub().eq("o.test",obj.getTest()).or().eq("i.test",obj.getTest()).endSub();
                builder.sourceScript("FROM order o INNER JOIN orderItem i ON i.orderId = o.id");
                builder.paged(obj);
                Criteria.ResultMapCriteria criteria = builder.build();
                orderRepository.find(criteria);
            }
            
            {
                orderRepository.refresh(
                    RefreshCondition.build().refresh("status","PAYING").eq("id",1).eq("status","UN_PAID")
                );
            }
        
        条件构建API  (CriteriaBuilder | ResultMapBuilder)
            1. and // AND 默认, 可省略，也可不省略
            2. or // OR
            3. eq // = (eq, 以及其它的API, 值为null，不会被拼接到SQL)
            4. ne // !=
            5. gt // >
            6. gte // >=
            7. lt // <
            8. lte // <=
            9. like //like %xxx%
            10. likeRight // like xxx%
            11. notLike // not like %xxx%
            12. in // in
            13. nin // not in
            14. isNull // is null
            15. nonNull // is not null
            16. x // 简单的手写sql片段， 例如 x("foo.amount = bar.price * bar.qty") , x("item.quantity = 0")
            17. beginSub // 左括号
            18. endSub // 右括号

        MAP查询结果构建API  (ResultMapBuilder)
            19. distinct //去重
            20. reduce //归并计算
                    // .reduce(ReduceType.SUM, "dogTest.petId") 
                    // .reduce(ReduceType.SUM, "dogTest.petId", Having.of(Op.GT, 1))
                    //含Having接口 (仅仅在reduc查询后,有限支持Having)
            21. groupBy //分组
            22. resultKey //指定返回列
            23. resultKeyFunction //返回列函数支持
                    // .resultKeyFunction(ResultKeyAlia.of("o","at"),"YEAR(?)","o.createAt")
            24. resultWithDottedKey //连表查询返回非JSON格式数据,map的key包含"."  (结果优化2)
           
        连表构建API  (ResultMapBuilder)
            25. sourceScript(joinSql) //简单的连表SQL，不支持LEFT JOIN  ON 多条件; 多条件，请用API[28]
            26. sourceScript("order").alia("o") //连表里的主表
            27. sourceScript().source("orderItem").alia("i").joinType(JoinType.LEFT_JOIN)
                                              .on("orderId", JoinFrom.of("o","id")) //fluent构建连表sql
            28.               .more().[1~18] // LEFT JOIN等, 更多条件
            
        分页及排序API  (ResultMapBuilder)
            29. paged(PagedRo) //前端请求参数构建分页及排序; 或者服务端编程API[30]
            30. paged().ignoreTotalRows().page(1).rows(10).sort("o.id", Direction.DESC) 
                                           
        更新构建API  (RefreshCondition)
            31. refresh
            
        框架优化
            sourceScript
                如果条件和返回都不包括sourceScript里的连表，框架会优化移除连接（但目标连接表有用时，中间表不会
                被移除）。
            in
                每500个条件会切割出一次in查询
            
        不支持项
            in(sql) // 和连表查询及二级缓存的设计有一定的冲突
            union // 过于复杂
            
## 二级缓存 

    在x7项目里实现，spring boot的注解实现或使用的模板如下:
        @EnableX7L2Caching
        public class App{
            main()

    二级缓存是基于redis.multiGet的高速缓存实现。

    二级缓存建议返回记录条数不超过20条。调用带二级缓存的API，返回记录条数超过了
    20条，请关闭二级缓存。
    如果需要开启二级缓存，所有对数据库的写操作项目都需要开启二级缓存。
    
    包含二级缓存的BaseRepository的API：
            1. in(InCondition)
            2. list(Object)
            3. find(Criteria)
            4. list(Criteria)
            5. get(Id)
            6. getOne(Object)
        
    不含二级缓存的BaseRepository的API:
            1. list()
            2. find(ResultMapCriteria)
            3. list(ResultMapCriteria)
            4. listPlainValue(ResultMapCriteria)
        
    以上设计意味着，如果in和list查询返回记录条数超过20条, 二级缓存
    会失去高速响应的效果，请务必关闭二级缓存. 
    如果需要返回很多条记录，需要自定义返回列, 请使用:
        find(ResultMapCriteria)
        list(ResultMapCriteria)
        listPlainValue(ResultMapCriteria)
        
    用户级的过滤
    {
        L2CacheFilter.filter(userId);
        this.orderRepository.create(order); // refresh and remove
    }
    
    {
        L2CacheFilter.filter(userId);
        this.orderRepository.find(criteria);
    }               
    