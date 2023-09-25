# sqli
   [http://sqli.xream.io](http://sqli.xream.io) 
   
[![license](https://img.shields.io/github/license/x-ream/sqli.svg)](https://www.apache.org/licenses/LICENSE-2.0.html)
[![maven](https://img.shields.io/maven-central/v/io.xream.sqli/sqli-parent.svg)](https://search.maven.org/search?q=io.xream)
[![Codacy Badge](https://app.codacy.com/project/badge/Grade/8e414bcc7a6944529c5a35b27b2d5e37)](https://www.codacy.com/gh/x-ream/sqli?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=x-ream/sqli&amp;utm_campaign=Badge_Grade)
[![Gitter](https://badges.gitter.im/x-ream/x-ream.svg)](https://gitter.im/x-ream/community)
    
   [WIKI](https://github.com/x-ream/sqli/wiki)
    
    sqli/sqli-QB
    sqli/sqli-core
    sqli/sqli-dialect
    sqli/sqli-repo
        
## sqli/sqli-core 

### API
    BaseRepository
    ResultMapRepository
    TemporaryRepository
    CacheFilter
    
### SPI
    JdbcHelper                  //io.xream.x7/x7/x7-repo/x7-jdbc-template-plus
    IdGenerator                 //io.xream.x7/x7/x7-repo/x7-id-generator
    L2CacheStorage              //io.xream.x7/x7/x7-repo/x7-redis-integration
    L2CacheResolver             //io.xream.sqli/sqli/sqli-core
    L2CacheConsistency          //Optional SPI, do it by deleyed queue, or kafka
