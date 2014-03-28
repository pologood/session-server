package com.sogou.upd.passport.session.sdk.util;

import com.sogou.upd.passport.session.sdk.model.RequestModel;
import com.sogou.upd.passport.session.sdk.param.HttpMethodEnum;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.*;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.io.InputStream;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

/**
 * Created with IntelliJ IDEA.
 * User: hujunfei
 * Date: 13-11-29
 * Time: 上午11:43
 * To change this template use File | Settings | File Templates.
 */
public class HttpClientUtil {
    protected static final HttpClient httpClient;
    /**
     * 最大连接数
     */
    protected final static int MAX_TOTAL_CONNECTIONS = 500;
    /**
     * 获取连接的最大等待时间
     */
    protected final static int WAIT_TIMEOUT = 5000;
    /**
     * 每个路由最大连接数
     */
    protected final static int MAX_ROUTE_CONNECTIONS = 200;
    /**
     * 读取超时时间
     */
    protected final static int READ_TIMEOUT = 5000;

    /**
     * http返回成功的code
     */
    protected final static int RESPONSE_SUCCESS_CODE = 200;

    /**
     * 超过500ms的请求定义为慢请求
     */
    protected final static int SLOW_TIME = 500;

    static final Logger logger = LoggerFactory.getLogger(HttpClientUtil.class);

    static {
        HttpParams params = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(params, WAIT_TIMEOUT);
        HttpConnectionParams.setSoTimeout(params, READ_TIMEOUT);
        httpClient = WebClientDevWrapper.wrapClient(new DefaultHttpClient());
    }


    /**
     * 执行请求操作，返回服务器返回内容
     *
     * @param requestModel
     * @return
     */
    public static String executeStr(RequestModel requestModel) {
        HttpEntity httpEntity = execute(requestModel);
        try {
            String charset = EntityUtils.getContentCharSet(httpEntity);
            if (StringUtil.isBlank(charset)) {
                charset = CommonConfigUtil.DEFAULT_CONTENT_CHARSET;
            }
            String value = EntityUtils.toString(httpEntity, charset);
            if (!StringUtil.isBlank(value)) {
                value = value.trim();
            }
            return value;
        } catch (Exception e) {
            throw new RuntimeException("http request error ", e);
        }
    }

    /**
     * 对外提供的执行请求的方法，主要添加了性能log
     *
     * @param requestModel
     * @return
     */
    public static HttpEntity execute(RequestModel requestModel) {
        //性能分析
        try {
            HttpEntity httpEntity = executePrivate(requestModel);
            return httpEntity;
        } catch (Exception e) {
            throw new RuntimeException("http request error ", e);
        }
    }

    public static Header[] executeHeaders(RequestModel requestModel) {
        if (requestModel == null) {
            throw new NullPointerException("requestModel 不能为空");
        }
        HttpRequestBase httpRequest = getHttpRequest(requestModel);
        try {
            HttpParams params = httpClient.getParams();
            params.setParameter(ClientPNames.HANDLE_REDIRECTS, false);
            HttpResponse httpResponse = httpClient.execute(httpRequest);
            Header[] headers = httpResponse.getAllHeaders();
            return headers;
        } catch (IOException e) {
            throw new RuntimeException("http request error ", e);
        }
    }

    /**
     * 执行请求并返回请求结果
     *
     * @param requestModel
     * @return
     */
    private static HttpEntity executePrivate(RequestModel requestModel) {
        if (requestModel == null) {
            throw new NullPointerException("requestModel 不能为空");
        }
        HttpRequestBase httpRequest = getHttpRequest(requestModel);
        InputStream in=null;
        try {
            HttpResponse httpResponse = httpClient.execute(httpRequest);
            in=httpResponse.getEntity().getContent();
            int responseCode = httpResponse.getStatusLine().getStatusCode();
            //302如何处理
            if (responseCode == RESPONSE_SUCCESS_CODE) {
                return httpResponse.getEntity();
            }
            String params = EntityUtils.toString(requestModel.getRequestEntity(), CommonConfigUtil.DEFAULT_CONTENT_CHARSET);
            String result= EntityUtils.toString(httpResponse.getEntity(),CommonConfigUtil.DEFAULT_CONTENT_CHARSET);
            throw new RuntimeException("http response error code: " + responseCode + " url:" + requestModel.getUrl() + " params:" + params + "  result:"+result);
        } catch (Exception e) {
            if(in!=null){
                try{
                    in.close();
                }catch(IOException ioe){
                }
            }
            throw new RuntimeException("http request error ", e);
        }
    }

    /**
     * 根据请求的参数构造HttpRequestBase
     *
     * @param requestModel
     * @return
     */
    private static HttpRequestBase getHttpRequest(RequestModel requestModel) {
        HttpRequestBase httpRequest = null;
        HttpMethodEnum method = requestModel.getHttpMethodEnum();
        switch (method) {
            case GET:
                httpRequest = new HttpGet(requestModel.getUrlWithParam());
                break;
            case POST:
                HttpPost httpPost = new HttpPost(requestModel.getUrl());
                httpPost.setEntity(requestModel.getRequestEntity());
                httpRequest = httpPost;
                break;
            case PUT:
                HttpPut httpPut = new HttpPut(requestModel.getUrl());
                httpPut.setEntity(requestModel.getRequestEntity());
                httpRequest = httpPut;
                break;
            case DELETE:
                httpRequest = new HttpDelete(requestModel.getUrl());
                break;
        }
        httpRequest.setHeaders(requestModel.getHeaders());
        return httpRequest;
    }

    /*
     * 避免HttpClient的”SSLPeerUnverifiedException: peer not authenticated”异常
     * 不用导入SSL证书
     */
    public static class WebClientDevWrapper {

        public static org.apache.http.client.HttpClient wrapClient(org.apache.http.client.HttpClient base) {
            try {
                SSLContext ctx = SSLContext.getInstance("TLS");
                X509TrustManager tm = new X509TrustManager() {
                    public X509Certificate[] getAcceptedIssuers() {
                        return null;
                    }

                    public void checkClientTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
                    }

                    public void checkServerTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
                    }
                };
                ctx.init(null, new TrustManager[]{tm}, null);
                SSLSocketFactory ssf = new SSLSocketFactory(ctx, SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
                SchemeRegistry registry = new SchemeRegistry();
                registry.register(new Scheme("http", 80, PlainSocketFactory.getSocketFactory()));
                registry.register(new Scheme("https", 443, ssf));
                ThreadSafeClientConnManager mgr = new ThreadSafeClientConnManager(registry);


                HttpParams params = base.getParams();
                mgr.setMaxTotal(MAX_TOTAL_CONNECTIONS);
                mgr.setDefaultMaxPerRoute(MAX_ROUTE_CONNECTIONS);
                HttpConnectionParams.setConnectionTimeout(params, WAIT_TIMEOUT);
                HttpConnectionParams.setSoTimeout(params, READ_TIMEOUT);

                return new DefaultHttpClient(mgr, params);
            } catch (Exception ex) {
                ex.printStackTrace();
                return null;
            }
        }
    }

}
