package cn.csservice.dataexchange.tools;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;

import net.sf.json.JSONObject;

public class HttpUtils {
	private static Logger logger = Logger.getLogger(HttpUtils.class);

	/**
	 * post方式提交表单（模拟用户登录请求）
	 * 
	 * @param sUrl
	 * @param params
	 * @return
	 */
	public static String post(String sUrl, Map<String, String> params) {
		// 创建默认的httpClient实例.
		CloseableHttpClient httpclient = HttpClients.createDefault();
		// 创建httppost
		HttpPost httppost = new HttpPost(sUrl);
		// 创建参数队列
		List<NameValuePair> formparams = new ArrayList<NameValuePair>();
		Iterator<Map.Entry<String, String>> it = params.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<String, String> entry = it.next();
			formparams.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
		}
		UrlEncodedFormEntity uefEntity;
		String sResult = "";
		try {
			uefEntity = new UrlEncodedFormEntity(formparams, "UTF-8");
			httppost.setEntity(uefEntity);
			System.out.println("executing request " + httppost.getURI());
			CloseableHttpResponse response = httpclient.execute(httppost);
			try {
				HttpEntity entity = response.getEntity();
				if (entity != null) {
					System.out.println("--------------------------------------");
					sResult = EntityUtils.toString(entity, "UTF-8");
					System.out.println("Response content: " + sResult);
					System.out.println("--------------------------------------");
				}
			} finally {
				response.close();
			}
		} catch (ClientProtocolException e) {
			logger.error("发送http请求失败", e);
		} catch (UnsupportedEncodingException e) {
			logger.error("编码不支持", e);
		} catch (IOException e) {
			logger.error("io失败", e);
		} finally {
			// 关闭连接,释放资源
			try {
				httpclient.close();
			} catch (IOException e) {
				logger.error("关闭http连接失败", e);
			}
		}
		return sResult;
	}

	public static String postJson(String sUrl, Map<String, String> params) {
		String sResult = "";
		// 创建默认的httpClient实例.
		CloseableHttpClient httpclient = HttpClients.createDefault();
		// 创建httppost
		HttpPost httppost = new HttpPost(sUrl);
		// 接收参数json列表
		JSONObject jsonParam = new JSONObject();
		Iterator<Map.Entry<String, String>> it = params.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<String, String> entry = it.next();
			jsonParam.put(entry.getKey(), entry.getValue());
		}
		try {
			StringEntity postEntity = new StringEntity(jsonParam.toString(), "utf-8");// 解决中文乱码问题
			postEntity.setContentEncoding("UTF-8");
			postEntity.setContentType("application/json");
			httppost.setEntity(postEntity);

			CloseableHttpResponse response = httpclient.execute(httppost);
			HttpEntity entity = response.getEntity();
			if (entity != null) {
				System.out.println("--------------------------------------");
				sResult = EntityUtils.toString(entity, "UTF-8");
				System.out.println("Response content: " + sResult);
				System.out.println("--------------------------------------");
			}
		} catch (ClientProtocolException e) {
			logger.error("发送http请求失败", e);
		} catch (UnsupportedEncodingException e) {
			logger.error("编码不支持", e);
		} catch (IOException e) {
			logger.error("io失败", e);
		} finally {
			// 关闭连接,释放资源
			try {
				httpclient.close();
			} catch (IOException e) {
				logger.error("关闭http连接失败", e);
			}
		}
		return sResult;
	}
}
