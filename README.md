# sqli 简单的SQL拼写
   [http://sqli.xream.io](http://sqli.xream.io) 
   
[![license](https://img.shields.io/github/license/x-ream/sqli.svg)](https://www.apache.org/licenses/LICENSE-2.0.html)
[![maven](https://img.shields.io/maven-central/v/io.xream.sqli/sqli-parent.svg)](https://search.maven.org/search?q=io.xream)
[![Codacy Badge](https://app.codacy.com/project/badge/Grade/8e414bcc7a6944529c5a35b27b2d5e37)](https://www.codacy.com/gh/x-ream/sqli?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=x-ream/sqli&amp;utm_campaign=Badge_Grade)
[![Gitter](https://badges.gitter.im/x-ream/x-ream.svg)](https://gitter.im/x-ream/community)
    
   [WIKI](https://github.com/x-ream/sqli/wiki)
    
    sqli/sqli-builder
    sqli/sqli-core
    sqli/sqli-dialect
    sqli/sqli-repo
        
## sqli-repo 

### 使用方法
    sqli仅仅是SQL的编程接口,需要整合到已有的框架或项目中,
    在io.xream.x7项目里实现了和Spring-Boot/Spring-JdbcTemplate的整合
    
    @EnableX7Repostory  // code at x7/x7-spring-boot-starter
    public class App{
        main() 
        ....
    }
```xml        
    <dependency>
         <groupId>io.xream.x7</groupId>
         <artifactId>x7-spring-boot-starter</artifactId>
         ....
    </dependency>
```     
    更多代码片段:

    @Repository
    public interface FooRepository extends BaseRepository<Foo> {}
    @Repository
    public interface BarRepository extends RepositoryX {}

    @X.Mapping("t_foo")//默认是foo
    public class Foo {
        @X.Key //不指定主键的情况下，不支持根据get(id),remove(id)
        private Long id;
        @X.Mapping("full_name") //默认是fullName
        private String fullName;
    }

    @Service
    public class FooServiceImpl implements FooService {
    
        @Autowired
        private FooRepository fooRepository;
        @Autowired
        private FooFindRepository fooFindRepository;
        
        // 临时表, 原生SQL, 则直接注入, 不支持代理
        @Autowired
        private TemporaryRepository temporaryRepository;
        @Autowired
        private NativeRepository nativeRepository;


###    BaseRepository API

            1. in(property, inList) //in查询, 例如: 页面上需要的主表ID或记录已经查出后，补充查询其他表的文本说明数据时使用
            2. find(q) //标准拼接查询，返回对象形式记录，返回分页对象
            3. list(q) //标准拼接查询，返回对象形式记录，不返回分页对象
            4. get(Id) //根据主键查询记录
            5. getOne(q) //数据库只有一条记录时，就返回那条记录
            6. creaet(Object) //插入一条, 不支持返回自增键, 框架自带ID生成器
            7. createOrReplace(Object) //插入或替换一条
            8. createBatch(List<Object>) //批量插入
            9. refresh( qr) //根据主键更新
            10. refreshUnSafe( qr)//不根据主键更新
            11. remove(Id)//根据主键删除
            12. removeRefreshCreate(RemoveRefreshCreate<T>) //编辑页面列表时写数据库

###     RepositoryX API
            13. find(xq) //标准拼接查询，返回Map形式记录，返回分页对象
            14. list(xq) //标准拼接查询，返回Map形式记录，不返回分页对象
            15. listPlainValue(Class<K>, qx)//返回没有key的单列数据列表 (结果优化1)
            16. findToHandle(xq, RowHandler<Map<String,Object>>) //流处理API

###     QueryBuilder拼接API
        QB // 返回q, 查出对象形式记录
        QB.X //xq, 查出Map形式记录，支持连表查询
        QrB //构建要更新的字段和条件
        
        代码片段:
            {
                QB qb = QB.of(Order.class); 
                qb.eq("userId",obj.getUserId()).eq("status","PAID");
                Q q = qb.build();
                orderRepository.find(q);
            }
        
            {
                QB.X qbx =  QB.x();
                qbx.resultKey("o.id");
                qbx.eq("o.status","PAID");
                qbx.and(sub -> sub.gt("o.createAt",obj.getStartTime()).lt("o.createAt",obj.getEndTime()));
                qbx.or(sub -> sub.eq("o.test",obj.getTest()).or().eq("i.test",obj.getTest()));
                qbx.from("FROM order o INNER JOIN orderItem i ON i.orderId = o.id");
                qbx.paged(obj);
                Q.X xq = qbx.build();
                orderRepository.find(xq);
            }
            
            {
                orderRepository.refresh(
                    QrB.of(Order.class).refresh("status","PAYING").eq("id",1).eq("status","UN_PAID").build()
                );
            }
        
        条件构建API  (QB | QB.X)
            1. or(sub) // or(sql)
            2. or() // OR
            3. eq // = (eq, 以及其它的API, 值为null，不会被拼接到SQL)
            4. ne // !=
            5. gt // >
            6. gte // >=
            7. lt // <
            8. lte // <=
            9. like //like %xxx%, if likeLeftRight => xxx, likeLeft => xxx%, then like => %xxx%
            10. likeLeft // like xxx%
            11. notLike // not like %xxx%
            12. in // in
            13. nin // not in
            14. isNull // is null
            15. nonNull // is not null
            16. x // 简单的手写sql片段， 例如 x("foo.amount = bar.price * bar.qty") , x("item.quantity = 0")
            17. sub(sql, sub) //
            18. and(sub)

        MAP查询结果构建API  (QB.X)
            19. distinct //去重
            20. reduce //归并计算
                    // .reduce(ReduceType.SUM, "dogTest.petId") 
                    // .reduce(ReduceType.SUM, "dogTest.petId", Having.of(Op.GT, 1))
                    //含Having接口 (仅仅在reduc查询后,有限支持Having)
            21. groupBy //分组
            22. select //指定返回列
            23. selectWithFunc //返回列函数支持
                    // .selectWithFunc(ResultKeyAlia.of("o","at"),"FFF(o.createAt, ?)", 100000) 
            24. resultWithDottedKey //连表查询返回非JSON格式数据,map的key包含"."  (结果优化2)
           
        连表构建API  (QB.X)
            25. from(joinSql) //简单的连表SQL，不支持LEFT JOIN  ON 多条件; 多条件，请用API[28]
            26. fromBuilder.of(Order.class,"o") //连表里的主表, API: .fromX(FromX fromX)
            27. fromBuilder.JOIN(LEFT).of(OrderItem.class,"i")
                                              .on("i.orderId = o.id", 
            28                  on -> on.gt(...)) //LEFT JOIN等, 更多条件
            29. fromBuilder.sub(....,"i").JOIN("ANY INNER JOIN").on(....) //fluent构建连表sql
        
        分页及排序API  (QB | QB.X)
            30. sort("o.id", Direction.DESC)
            31. paged(pb -> pb.ignoreTotalRows().page(1).rows(10).last(10000)) //设置last(long),会忽略page(int); 
                                           
        更新构建API  ( qr)
            32. refresh
            
        框架优化
            froms/fromBuilder
                如果条件和返回都不包括sourceScript里的连表，框架会优化移除连接（但目标连接表有用时，中间表不会
                被移除）。
                关闭优化: qb.withoutOptimization()
            in
                每500个条件会切割出一次in查询
            
        不支持项
            union // 过于复杂

            
## 二级缓存 

    在x7项目里实现了springBoot的Enable
        
    @EnableX7L2Caching
    public class App{
        main()

    二级缓存是基于redis.multiGet的高速缓存实现。

    二级缓存建议返回记录条数不超过20条。调用带二级缓存的API，返回记录条数超过了
    20条，请关闭二级缓存。
    如果需要开启二级缓存，所有对数据库的写操作项目都需要开启二级缓存。
    
    支持二级缓存的BaseRepository的API：
            1. in(property, inList)
            2. find(q)
            3. list(q)
            4. get(Id)
            5. getOne(q)
        
    不支持二级缓存的BaseRepository, RepositoryX的API:
            1. findX(xq)
            2. listX(xq)
            3. listPlainValue(xq)
        
    以上设计意味着，如果in和list查询返回记录条数超过20条, 二级缓存
    会失去高速响应的效果，请务必关闭二级缓存. 
    如果需要返回很多条记录，需要自定义返回列, 请使用:
        findX(xq)
        listX(xq)
        listPlainValue(xq)
        
    用户级的过滤
    {
        CacheFilter.filter(userId);
        this.orderRepository.create(order); // refresh and remove
    }
    
    {
        CacheFilter.filter(userId);
        this.orderRepository.find(q);
    }               
    