package com.haoxy.common.util;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.config.SocketConfig;
import org.apache.http.cookie.Cookie;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultRedirectStrategy;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 分发爬虫专用的HttpClient get/post请求默认同时开启自动重定向
 *
 * @author beiley
 * @since 20-9-2 下午4:05
 */
public class BrowserHttpClient {
    public static final Logger logger = LoggerFactory.getLogger(BrowserHttpClient.class);
    private static final int MAX_CONN_TOTAL = 50;
    private static final int MAX_CON_PER_ROUTE = 25;
    private static final int SOCKET_TIMEOUT = 10000;
    private static final int CONN_TIMEOUT = 10000;
    private static final int CONN_REQUEST_TIMEOUT = 30000;
    private static final String DEFAULT_CHART_SET = "UTF-8";
    private static final Header[] BASE_HEADER = new Header[]{
            new BasicHeader("Connection", "keep-alive"),
            new BasicHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8"),
            new BasicHeader("Upgrade-Insecure-Requests", "1"),
            new BasicHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:80.0) Gecko/20100101 Firefox/80.0"),
            new BasicHeader("Accept-Encoding", "gzip, deflate, sdch"),
            new BasicHeader("Accept-Language", "zh-CN,zh;q=0.8")
    };
    private CloseableHttpClient httpClient;
    private RequestConfig requestConfig;
    private MyCookieStore cookieStore;

    public static BrowserHttpClient localClient() {
        return new BrowserHttpClient();
    }

    private BrowserHttpClient() {
        init();
    }

    private void init() {
        this.cookieStore = new MyCookieStore();

        this.requestConfig = RequestConfig.custom()
                .setConnectionRequestTimeout(CONN_REQUEST_TIMEOUT)
                .setConnectTimeout(CONN_TIMEOUT)
                .setSocketTimeout(SOCKET_TIMEOUT)
                .setCookieSpec(CookieSpecs.STANDARD_STRICT)
                .build();

        SocketConfig socketConfig = SocketConfig.custom().setSoKeepAlive(true).setSoLinger(-1).
                setSoReuseAddress(false).setSoTimeout(SOCKET_TIMEOUT).setTcpNoDelay(true).build();

        this.httpClient = HttpClients.custom()
                .setMaxConnTotal(MAX_CONN_TOTAL)
                .setMaxConnPerRoute(MAX_CON_PER_ROUTE)
                .setDefaultCookieStore(cookieStore)
                .evictExpiredConnections()
                .evictIdleConnections(30, TimeUnit.SECONDS)
                .setDefaultSocketConfig(socketConfig)
                .setRedirectStrategy(new DefaultRedirectStrategy() {
                    private final String[] REDIRECT_METHODS = new String[]{
                            HttpGet.METHOD_NAME,
                            HttpPost.METHOD_NAME,
                            HttpHead.METHOD_NAME
                    };

                    @Override
                    protected boolean isRedirectable(final String method) {
                        for (final String m : REDIRECT_METHODS) {
                            if (m.equalsIgnoreCase(method)) {
                                return true;
                            }
                        }
                        return false;
                    }
                }).build();
    }

    public List<Cookie> getCookies() {
        return cookieStore.getCookies();
    }

    private static Header[] processNewHeads(Header[] newHeaders) {
        if (newHeaders != null && newHeaders.length > 0) {
            Header[] headers = new BasicHeader[BASE_HEADER.length + newHeaders.length];
            System.arraycopy(BASE_HEADER, 0, headers, 0, BASE_HEADER.length);
            System.arraycopy(newHeaders, 0, headers, BASE_HEADER.length, newHeaders.length);
            return headers;
        }
        return BASE_HEADER;
    }

    private void monitor(String url, long costTime) {
        try {
            if (StringUtils.contains(url, "?")) {
                url = StringUtils.substringBetween(url, "//", "?");
            } else {
                url = StringUtils.substringAfter(url, "//");
            }

            String domain = url.split("/")[0];
            String target = url.replaceAll("/", "_");
        } catch (Exception e) {
            logger.error("monitor exception:", e);
        }
    }

    public String get(String url) throws Exception {
        return get(url, null, null, null);
    }

    public String get(String url, String charset) throws Exception {
        return get(url, null, null, charset);
    }

    public String get(String url, Header[] newHeaders, String charset) throws Exception {
        return get(url, newHeaders, null, charset);
    }

    public String get(String url, Header[] newHeaders, Map<String, String> paramMap, String charset) throws Exception {
        if (StringUtils.isBlank(charset)) {
            charset = DEFAULT_CHART_SET;
        }
        StringBuilder sb = new StringBuilder(url);
        String paramString = buildParamString(paramMap, charset);
        if (!StringUtils.contains(url, "?")) {
            sb.append("?").append(paramString);
        } else {
            sb.append("&").append(paramString);
        }
        url = sb.toString();

        Header[] headers = processNewHeads(newHeaders);
        HttpGet httpGet = new HttpGet(url);
        httpGet.setHeaders(headers);

        httpGet.setConfig(requestConfig);
        long start = System.currentTimeMillis();
        try (CloseableHttpResponse httpResp = httpClient.execute(httpGet)) {
            String response = EntityUtils.toString(httpResp.getEntity(), charset);
            long time = System.currentTimeMillis() - start;
            int statusCode = httpResp.getStatusLine().getStatusCode();
            if (statusCode == HttpStatus.SC_OK) {
                monitor(url, time);
                return response;
            }
        }
        return null;
    }

    public String post(String url, Map<String, String> paramMap, Header[] newHeaders, String charset) throws Exception {
        HttpEntity httpEntity = mapToHttpEntity(paramMap, charset);
        return post(url, httpEntity, newHeaders, charset);
    }


    public String postJson(String url, String json, Header[] newHeaders, String charset) throws Exception {
        HttpEntity httpEntity = new StringEntity(json, ContentType.APPLICATION_JSON);
        return post(url, httpEntity, newHeaders, charset);
    }

    public String post(String url, HttpEntity httpEntity, Header[] newHeaders, String charset) throws Exception {
        if (StringUtils.isBlank(charset)) {
            charset = DEFAULT_CHART_SET;
        }
        Header[] headers = processNewHeads(newHeaders);
        HttpPost httpPost = new HttpPost(url);
        httpPost.setHeaders(headers);
        httpPost.setEntity(httpEntity);
        httpPost.setConfig(requestConfig);
        long start = System.currentTimeMillis();
        try (CloseableHttpResponse httpResp = httpClient.execute(httpPost)) {
            String response = EntityUtils.toString(httpResp.getEntity(), charset);
            long time = System.currentTimeMillis() - start;
            int statusCode = httpResp.getStatusLine().getStatusCode();
            if (statusCode == HttpStatus.SC_OK) {
                monitor(url, time);
                return response;
            }
        }
        return null;
    }

    /**
     * 拼接参数
     */
    public static String buildParamString(Map<String, String> paramMap, String encoding) {
        //添加时间戳参数，防止代理做缓存
        if (MapUtils.isNotEmpty(paramMap)) {
            if (paramMap.containsKey("_")) {
                paramMap.put("aaa", String.valueOf(System.currentTimeMillis()));
            } else {
                paramMap.put("_", String.valueOf(System.currentTimeMillis()));
            }
        } else {
            paramMap = Maps.newHashMap();
            paramMap.put("_", String.valueOf(System.currentTimeMillis()));
        }
        StringBuilder sb = new StringBuilder();
        try {
            for (Map.Entry<String, String> entry : paramMap.entrySet()) {
                String value = entry.getValue();
                sb.append("&").append(URLEncoder.encode(entry.getKey(), encoding)).append("=")
                        .append(URLEncoder.encode(value, encoding));
            }
        } catch (UnsupportedEncodingException ignore) {
        }
        return sb.toString().substring(1);
    }

    public static HttpEntity mapToHttpEntity(Map<String, String> params, String charset){
        if (params != null && !params.isEmpty()) {
            List<NameValuePair> nvPairs = Lists.newArrayList();
            for (String key : params.keySet()) {
                nvPairs.add(
                        new BasicNameValuePair(StringUtils.trimToEmpty(key), StringUtils.trimToEmpty(params.get(key))));
            }
            try {
                return new UrlEncodedFormEntity(nvPairs, charset);
            } catch (UnsupportedEncodingException ignore) {
            }
        }
        return null;
    }
}
