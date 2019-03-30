package com.example.springbootdemo.common;

import com.alibaba.fastjson.JSON;
import org.apache.http.Consts;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.*;

/**
 * @author kai.zhang
 * @description TODO
 * @since 2018/9/28
 */
public class HttpClient4Utils {
    private static final Logger logger = LoggerFactory.getLogger(HttpClient4Utils.class);
    /**
     * 实例化HttpClient
     * @param maxTotal
     * @param maxPerRoute
     * @param socketTimeout 数据包数据传输时间 （服务端处理时间 + 网络传输时间）
     * @param connectTimeout 三次握手建立连接时间
     * @param connectionRequestTimeout 从连接池获取连接等待时间
     * @return
     */
    public static HttpClient createHttpClient(int maxTotal, int maxPerRoute, int socketTimeout, int connectTimeout,
                                              int connectionRequestTimeout) {
        RequestConfig defaultRequestConfig = RequestConfig.custom().setSocketTimeout(socketTimeout)
                .setConnectTimeout(connectTimeout).setConnectionRequestTimeout(connectionRequestTimeout).build();
        PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
        cm.setMaxTotal(maxTotal);
        cm.setDefaultMaxPerRoute(maxPerRoute);
        CloseableHttpClient httpClient = HttpClients.custom().setConnectionManager(cm)
                .setDefaultRequestConfig(defaultRequestConfig).build();
        return httpClient;
    }

    /**
     * 发送post请求
     * @param httpClient
     * @param url 请求地址
     * @param params 请求参数
     * @param encoding 编码
     * @return
     */
    public static String sendPost(HttpClient httpClient, String url, Map<String, String> params, Charset encoding, String contentType) {
        String resp = "";
        HttpPost httpPost = new HttpPost(url);
        if (params != null && params.size() > 0) {
            List<NameValuePair> formParams = new ArrayList<NameValuePair>();
            Iterator<Map.Entry <String, String>> itr = params.entrySet().iterator();
            while (itr.hasNext()) {
                Map.Entry <String, String> entry = itr.next();
                formParams.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
            }
            UrlEncodedFormEntity postEntity = new UrlEncodedFormEntity(formParams, encoding);
            httpPost.setEntity(postEntity);
            if (contentType != null) {
                //设置请求的报文头部的编码
                httpPost.setHeader(
                        new BasicHeader("Content-Type", contentType));
            }
        }
        CloseableHttpResponse response = null;
        try {
            response = (CloseableHttpResponse) httpClient.execute(httpPost);
            resp = EntityUtils.toString(response.getEntity(), encoding);
        } catch (Exception e) {
            logger.error("sendPost param " + params + " error ", e);
        } finally {
            if (response != null) {
                try {
                    response.close();
                } catch (IOException e) {
                    logger.error("sendPost error ", e);
                }
            }
        }
        return resp;
    }

    /**
     * 发送post请求
     * @param httpClient
     * @param url 请求地址
     * @param params 请求参数
     * @param encoding 编码
     * @return
     */
    public static String sendGet(HttpClient httpClient, String url, Map<String, String> params, Charset encoding, String contentType) {
        String resp = "";
        CloseableHttpResponse response = null;
        try {
            URIBuilder uriBuilder = new URIBuilder(url);
            if (params != null && params.size() > 0) {
                for (Map.Entry<String, String> entry : params.entrySet()) {
                    uriBuilder.addParameter(entry.getKey(), entry.getValue());
                }
            }
            HttpGet httpGet = new HttpGet(uriBuilder.build());
            if (contentType != null) {
                //设置请求的报文头部的编码
                httpGet.setHeader(
                        new BasicHeader("Content-Type", contentType));
            }
            response = (CloseableHttpResponse) httpClient.execute(httpGet);
            resp = EntityUtils.toString(response.getEntity(), encoding);
        } catch (Exception e) {
            logger.error("sendGet param " + params + " error ", e);
        } finally {
            if (response != null) {
                try {
                    response.close();
                } catch (IOException e) {
                    logger.error("sendGet error ", e);
                }
            }
        }
        return resp;
    }
    public static void main(String ...args){
        //application/x-www-form-urlencoded
        //application/json
        String contentType = "application/json";
        HttpClient httpClient = HttpClient4Utils.createHttpClient(200, 50, 30, 10, 30);
        Map<String, String> params = new HashMap<>();
        params.put("name", "zhangkai");
        String result = HttpClient4Utils.sendPost(httpClient, "http://10.177.34.132:9202/safe/config", params, Consts.UTF_8, contentType);
        System.out.println(result);
        Map<String, Object> secondParam = (Map<String, Object>) JSON.parse(result);
        Map<String, Object> data = (Map<String, Object>) secondParam.get("data");


        Map<String, String> couponParam = new HashMap<>();
        couponParam.put("distAntiCheatToken", (String) data.get("token"));
        couponParam.put("distAntiCheatActivityId", "bind_mobile_phone_activity");
        couponParam.put("recordId", "");
        couponParam.put("srcCouponId", "");
        String secondResult = HttpClient4Utils.sendPost(httpClient, "http://10.177.34.132:9202/coupon/takePresentCoupon", couponParam, Consts.UTF_8, contentType);
        System.out.println(secondResult);
    }
}
