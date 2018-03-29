/**
 *
 */
package com.ftb.app.net;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author mac
 */
public class JSONRequest {

    public static final String SERVER_FTB_URI = "https://feitebi.com";

    public static final String POST = "POST";

    public static final String GET = "GET";

    public static String post(String uri, Map<String, String> params) {
        return send(SERVER_FTB_URI, uri, POST, params);
    }

    public static String get(String uri) {
        return send("", uri, GET, new HashMap<String, String>(0));
    }

    /**
     * POST请求操作
     */
    public static String send(String server, String uri, String method, Map<String, String> params) {
        try {

            // 请求的地址
            String spec = server + uri;
            // 根据地址创建URL对象
            URL url = new URL(spec);
            // 根据URL对象打开链接
            HttpURLConnection urlConnection = (HttpURLConnection) url
                    .openConnection();
            // 设置请求的方式
            urlConnection.setRequestMethod(method);
            // 设置请求的超时时间
            urlConnection.setReadTimeout(5000);
            urlConnection.setConnectTimeout(5000);

            // 设置请求的头
            urlConnection.setRequestProperty("Connection", "keep-alive");
            // 设置请求的头
            urlConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

            // 设置请求的头
            urlConnection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.3; WOW64; rv:27.0) Gecko/20100101 Firefox/27.0");

            int repCode = 200;

            if (method.equalsIgnoreCase("post")) {
                String data = "android=1";

                // 传递的数据
                if (params != null && !params.isEmpty()) {
                    Set<String> keys = params.keySet();
                    for (String key : keys) {
                        data += "&" + key + "=" + URLEncoder.encode(params.get(key), "UTF-8");
                    }
                }

                // 设置请求的头
                urlConnection.setRequestProperty("Content-Length", String.valueOf(data.getBytes().length));

                urlConnection.setDoOutput(true); // 发送POST请求必须设置允许输出
                urlConnection.setDoInput(true); // 发送POST请求必须设置允许输入

                //setDoInput的默认值就是true
                //获取输出流
                OutputStream os = urlConnection.getOutputStream();
                os.write(data.getBytes());
                os.flush();

                repCode = urlConnection.getResponseCode();
            }

            if (repCode == 200) {
                // 获取响应的输入流对象
                InputStream is = urlConnection.getInputStream();
                // 创建字节输出流对象
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                // 定义读取的长度
                int len = 0;
                // 定义缓冲区
                byte buffer[] = new byte[2048];
                // 按照缓冲区的大小，循环读取
                while ((len = is.read(buffer)) != -1) {
                    // 根据读取的长度写入到os对象中
                    baos.write(buffer, 0, len);
                }
                // 释放资源
                is.close();
                baos.close();
                // 返回字符串
                final String result = new String(baos.toByteArray());

                return result;
            } else {
                return "";
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }
}
