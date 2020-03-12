package com.cyq.rpgmvtranshelper.utils;

import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;

/**
 * @author cheng
 */
public class HttpCallUtil {
	private HttpCallUtil(){}

	/**
	 * @param url 
	 * @param requestConfig 可空
	 * @return
	 * @throws IOException
	 */
	public static String httpGet(String url, RequestConfig requestConfig) throws IOException {
		CloseableHttpClient httpClient = HttpClients.createDefault();
		HttpGet httpget = new HttpGet(url);
		httpget.addHeader("User-Agent", "Mozilla/5.0");
		httpget.addHeader("Accept", "*/*");
		httpget.addHeader("Cache-Control", "no-cache");
		httpget.addHeader("Host", "translate.googleapis.com");
		httpget.addHeader("Accept-Encoding", "gzip, deflate, br");
		httpget.addHeader("Connection", "keep-alive");
		//设置请求和传输超时时间
		if (requestConfig != null) {
			httpget.setConfig(requestConfig);
		}

		try (CloseableHttpResponse response = httpClient.execute(httpget)) {
			HttpEntity entity = response.getEntity();
			return EntityUtils.toString(entity);
		}
	}
}
