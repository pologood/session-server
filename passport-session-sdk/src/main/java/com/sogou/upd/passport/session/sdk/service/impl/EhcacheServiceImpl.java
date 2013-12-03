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

    private int maxElements = 10000000;
    private int cacheInstanceSize = 100;
    private int cacheExpire = CommonConfigUtil.DEFAULT_EHCACHE_EXPIRE;


    public void init() {
        if (cacheConfiguration == null) {
            cacheConfiguration = new CacheConfiguration();

            cacheConfiguration.setEternal(false);   // 不永久存储
            cacheConfiguration.setDiskPersistent(false);    // 不存储磁盘
            cacheConfiguration.setOverflowToDisk(false);    // 超过内存限制不存磁盘
            cacheConfiguration.setTimeToLiveSeconds(cacheExpire);   // 默认过期时间
            cacheConfiguration.setMaxElementsInMemory(maxElements);
        }
        Configuration configuration = new Configuration();
        configuration.setDefaultCacheConfiguration(cacheConfiguration);

        cacheManager = new CacheManager(configuration);
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
}
