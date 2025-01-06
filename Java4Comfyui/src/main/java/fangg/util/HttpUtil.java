package fangg.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpUtil {
	private static final Logger logger = LoggerFactory.getLogger(HttpUtil.class);

    /**
     * 发送post请求
     * @param url 发送请求的 URL
     * @param param 请求参数
     * @return 所代表远程资源的响应结果
     * @throws Exception 
     */
    public static String sendPostByJson(String urlStr, String jsonParamStr) throws Exception {
    	URL url = new URL(urlStr);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("POST");
        con.setRequestProperty("Content-Type", "application/json");
        con.setRequestProperty("Accept", "application/json");
        con.setDoOutput(true);
        
        //String jsonInputString = "{\"history\":[],\"prompt\":\"你好呀\"}";
        try (OutputStream os = con.getOutputStream()) {
            byte[] input = jsonParamStr.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        } catch (Exception e) {
			logger.error("{}请求异常：", urlStr, e.getMessage());
			return null;
		}
        

        StringBuilder response = new StringBuilder();
        
        try (BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream(), StandardCharsets.UTF_8))) {
            String responseLine = null;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }
            //System.out.println(con.getResponseCode() + " " + response);
        } catch (Exception e) {
        	logger.error("请求处理异常：", e.getMessage());
		}
        
        return response.toString();
    }
    
}
