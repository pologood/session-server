package com.sogou.upd.passport.session.sdk.service;

/**
 * Created with IntelliJ IDEA.
 * User: hujunfei
 * Date: 13-11-29
 * Time: 下午12:28
 * To change this template use File | Settings | File Templates.
 */
public interface EhcacheService {

    public String get(String key);

    public void set(String key, String value);

    public void setMaxElements(int maxElements);

    public void setCacheInstanceSize(int cacheInstanceSize);

    public void setCacheExpire(int expire);

    public long getCacheHits();

    public long getCacheMisses();

/*    public String queryCacheSize();*/
}
