package com.haoxy.common.util;

import org.apache.commons.collections.CollectionUtils;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * @author beiley
 * @since 20-11-19 上午9:38
 */
public class MyCookieStore extends BasicCookieStore {
    public static final Logger logger = LoggerFactory.getLogger(MyCookieStore.class);

    @Override
    public List<Cookie> getCookies() {
        List<Cookie> cookies = super.getCookies();
        if (CollectionUtils.isNotEmpty(cookies)) {
            decode(cookies);
        }
        return cookies;
    }

    private void decode(List<Cookie> cookies) {
        for (Cookie cookie : cookies) {
            try {
                if("loginName".equalsIgnoreCase(cookie.getName())) {
                    ((BasicClientCookie) cookie).setValue(new String(cookie.getValue().getBytes("iso-8859-1")));
                }
            }catch (Exception e){
                logger.error("decode cookie[{}] exception", cookie.getName(), e);
            }
        }
    }
}
