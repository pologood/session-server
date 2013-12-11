package com.sogou.upd.passport.session.sdk.service.impl;

import com.sogou.upd.passport.session.sdk.service.EhcacheService;
import com.sogou.upd.passport.session.sdk.util.CoderUtil;
import com.sogou.upd.passport.session.sdk.util.CommonConfigUtil;
import com.sogou.upd.passport.session.sdk.util.StringUtil;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import net.sf.ehcache.config.CacheConfiguration;
import net.sf.ehcache.config.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Timer;

/**
 * Created with IntelliJ IDEA.
 * User: hujunfei
 * Date: 13-11-29
 * Time: 下午12:30
 * To change this template use File | Settings | File Templates.
 */
public class EhcacheServiceImpl implements EhcacheService {
    private static final Logger logger = LoggerFactory.getLogger(EhcacheServiceImpl.class);
    private static final String CACHE_PREFIX = "cache_";


    private CacheManager cacheManager = null;
    private CacheConfiguration cacheConfiguration = null;

    private int maxElements = 1000000;
    private int cacheInstanceSize = 10;
    private int cacheExpire = CommonConfigUtil.DEFAULT_EHCACHE_EXPIRE;


    public void init() {
        if (cacheConfiguration == null) {
            cacheConfiguration = new CacheConfiguration();

            int maxElementsPerCache = (maxElements + cacheInstanceSize-1) / cacheInstanceSize;  // 设置单个cache的缓存为ceil(平均数)

            cacheConfiguration.setEternal(false);   // 不永久存储
            cacheConfiguration.setDiskPersistent(false);    // 不存储磁盘
            cacheConfiguration.setOverflowToDisk(false);    // 超过内存限制不存磁盘
            cacheConfiguration.setTimeToLiveSeconds(cacheExpire);   // 默认过期时间
            cacheConfiguration.setMaxElementsInMemory(maxElementsPerCache);     // 测试发现，此数值为单个cache的最大记录数
        }
        Configuration configuration = new Configuration();
        configuration.setDefaultCacheConfiguration(cacheConfiguration);

        cacheManager = new CacheManager(configuration);

        /**
         * 启动监控代码，每分钟执行一次
         */
        Timer timer=new Timer();
        timer.schedule(new ShootingMonitor(this),60*1000l,60*1000l);
    }

    public void stop() {
        getCacheManagerInstance().shutdown();
    }

    @Override
    public String get(String key) {
        String cacheKey = getCacheKey(key);
        if (StringUtil.isEmpty(cacheKey)) {
            return null;
        }
        Cache cache = getCacheInstance(cacheKey);

        String value = null;
        if (cache != null) {
            Element element = cache.get(key);
            value = element == null ? null : (String) element.getObjectValue();
        }
        return value;
    }

    @Override
    public void set(String key, String value) {
        String cacheKey = getCacheKey(key);
        if (StringUtil.isEmpty(cacheKey)) {
            return;
        }
        Cache cache = getCacheInstance(cacheKey);

        if (cache != null) {
            cache.put(new Element(key, value));
        }
    }


/*    @Override
    public String queryCacheSize() {
        String[] strs = this.getCacheManagerInstance().getCacheNames();
        Map map = new HashMap();
        map.put("all", strs.length);
        for (int i=0; i<strs.length; i++) {
            map.put(strs[i], this.getCacheInstance(strs[i]).getMemoryStoreSize()+"|"+this.getCacheInstance(strs[i]).calculateInMemorySize()+"|"+this.getCacheInstance(strs[i]).getMaxElementsInMemory());
        }
        return map.toString();
    }*/


    /*
     * 获取cache实例
     */
    private Cache getCacheInstance(String cacheKey) {
        if (StringUtil.isEmpty(cacheKey)) {
            return null;
        }
        Cache cache = this.getCacheManagerInstance().getCache(cacheKey);
        if (cache == null) {
            synchronized (cacheManager) {
                cache = this.getCacheManagerInstance().getCache(cacheKey);
                if (cache == null) {
                    this.getCacheManagerInstance().addCache(cacheKey);
                    cache = this.getCacheManagerInstance().getCache(cacheKey);
                }
            }
        }
        return cache;
    }

    /*
     * 获取CacheManager实例
     */
    private CacheManager getCacheManagerInstance() {
        if (cacheManager == null) {
            synchronized (this) {
                init();
            }
        }
        return cacheManager;
    }

    /*
     * 根据key进行MD5，之后再转换为十进制取模
     */
    private String getCacheKey(String key) {
        try {
            return CACHE_PREFIX + Integer.parseInt(CoderUtil.encryptMD5(key).substring(28), 16) % getCacheInstanceSize();
        } catch (Exception e) {
            return null;
        }

    }

    public void setMaxElements(int maxElements) {
        this.maxElements = maxElements;
    }

    public void setCacheInstanceSize(int cacheInstanceSize) {
        this.cacheInstanceSize = cacheInstanceSize;
    }

    public int getCacheInstanceSize() {
        return cacheInstanceSize;
    }

    public void setCacheExpire(int cacheExpire) {
        this.cacheExpire = cacheExpire;
    }

    @Override
    public long getCacheHits() {
        long hits=0l;
        try{
            String[] cacheNames= this.getCacheManagerInstance().getCacheNames();
            for(String cacheName:cacheNames){
                Cache cache= this.getCacheManagerInstance().getCache(cacheName);
                hits+=cache.getStatistics().getCacheHits();
            }
        }catch (Exception e){
            logger.error("ehcache getHits error:",e);
        }

        return hits;
    }

    @Override
    public long getCacheMisses() {
        long misses=0l;
        try{
            String[] cacheNames= this.getCacheManagerInstance().getCacheNames();
            for(String cacheName:cacheNames){
                Cache cache= this.getCacheManagerInstance().getCache(cacheName);
                misses+=cache.getStatistics().getCacheMisses();
            }
        }catch (Exception e){
            logger.error("ehcache getHits error:",e);
        }

        return misses;
    }


    public void shooting(){

    }
}
