package fangg;

import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.util.StringUtils;

import com.alibaba.fastjson2.JSONObject;

import fangg.bean.comfyui.Prompt;
import fangg.bean.comfyui.PromptNode;
import fangg.bean.comfyui.SendParam;
import fangg.util.FileUtil;
import fangg.util.HttpUtil;

public class MainTest {
	private static final Logger logger = LoggerFactory.getLogger(MainTest.class);

	// 文生图线程池
	static ThreadPoolTaskExecutor taskExecutor;
	
	static {
		taskExecutor = new ThreadPoolTaskExecutor();
        // 核心线程数1：线程池创建时候初始化的线程数
		taskExecutor.setCorePoolSize(1);
        // 最大线程数5：线程池最大的线程数，只有在缓冲队列满了之后才会申请超过核心线程数的线程
		taskExecutor.setMaxPoolSize(1);
        // 缓冲队列100：用来缓冲执行任务的队列
		taskExecutor.setQueueCapacity(100);
        // 允许线程的空闲时间60秒：当超过了核心线程出之外的线程在空闲时间到达之后会被销毁
		taskExecutor.setKeepAliveSeconds(30);
        // 线程池名的前缀：设置好了之后可以方便我们定位处理任务所在的线程池
		taskExecutor.setThreadNamePrefix("ThreadPool-Txt2Img-");
		taskExecutor.initialize();
	}

	public static void main(String[] args) throws Exception {
		// 通过java代码进行封装，实现对"prompt"对象可自由添加
		// 对kSampler.addInput("model", new String[]{"167", "0"});里面的"167"，可通过prompt.addNode("160", clipTextEncode);的key进行关联
		System.out.println("开始comfyui封装测试...");
		
		long time = System.currentTimeMillis();
		Random random = new Random();
		SendParam sendParam = new SendParam();
		
        String fileName = "s3d_prompt.json";  // 要取得的文件名
        InputStream inputStream = MainTest.class.getResourceAsStream("/" + fileName);  // 获取文件输入流
        String promptStr = FileUtil.fileRead(inputStream);
        //System.out.println(promptStr);
		
		// 创建 Prompt 对象
	    Prompt prompt = new Prompt(promptStr);
	
	    // 创建 CLIPTextEncode 节点
	    //PromptNode clipTextEncode = new PromptNode("CLIPTextEncode");
	    PromptNode clipTextEncode = prompt.getPromptNode("CLIPTextEncode", "4");
	    clipTextEncode.addInput("text", "A little girl goes home from school with her schoolbag on her back, cartoon style");
	
	    // 添加 CLIPTextEncode 节点到 Prompt
	    //prompt.addNode(clipTextEncode);
	
	    // 创建 KSampler 节点
	    //PromptNode kSampler = new PromptNode("KSampler");
	    PromptNode kSampler = prompt.getPromptNode("KSampler");
	    kSampler.addInput("seed", random.longs(1, 1000000000000000L).findFirst().getAsLong());
		/*kSampler.addInput("steps", 8);
		kSampler.addInput("cfg", 3);
		kSampler.addInput("sampler_name", "euler");
		kSampler.addInput("scheduler", "normal");
		kSampler.addInput("denoise", 1);*/
	
	    // 动态关联 CLIPTextEncode 节点的 key
		/*kSampler.linkInput("model", clipTextEncode, 0); // 引用 "160" 节点的输出
		kSampler.linkInput("positive", "160", 0); // 引用 "160" 节点的输出
		kSampler.linkInput("negative", "160", 1); // 引用 "160" 节点的输出
		kSampler.linkInput("latent_image", "160", 2); // 引用 "160" 节点的输出
		kSampler.addMeta("title", "KSampler");*/
	
	    // 添加 KSampler 节点到 Prompt
	    prompt.addNode(kSampler);
	    
	    // 清除显存
	    PromptNode purgeVRAM = JSONObject.parseObject("{\"inputs\":{\"purge_cache\":true,\"purge_models\":true,\"anything\":[\"52\",0]},\"class_type\":\"LayerUtility: PurgeVRAM\",\"_meta\":{\"title\":\"LayerUtility: Purge VRAM\"}}", PromptNode.class);
	    purgeVRAM.linkInput("anything", prompt.getPromptNode("VAEDecode", null), 0);
	    
	    prompt.addNode(purgeVRAM);
	    
	    // 生成最终的 JSON 结构
	    //Map<String, Object> promptMap = prompt.toMap();
	
	    // 使用 Gson 转换为 JSON 字符串
	    //String json = JSONObject.toJSONString(promptMap);
	    //System.out.println(json);
	    
	    sendParam.setPrompt(prompt.getNodes());
	    System.out.println(JSONObject.toJSONString(sendParam));

	    // 调用comfyui websocket
	    WebSocketClient wsc = null;
	    Map<String, String> backMap = null;
		backMap = new HashMap<>();
		backMap.put("isConnected", "1");
		wsc = webSocketOpen("ws://127.0.0.1:8188/ws?clientId="+sendParam.getClient_id(), backMap, sendParam.getClient_id());
		
		// 等待建立图生视频websocket链接结果
		Thread.sleep(500);
		
		if (wsc == null || Integer.valueOf(backMap.get("isConnected")) != 1) {
			logger.warn("建立websocket链接失败...");
			return;
		}
		
		String resultStr = HttpUtil.sendPostByJson("http://127.0.0.1:8188/api/prompt", JSONObject.toJSONString(sendParam));
		System.out.println("comfyui websocket调用返回："+resultStr);
		
		if (StringUtils.isEmpty(resultStr)) {
			wsc.close();
			return;
		} else {
			Thread.sleep(45000);
			wsc.close();
		}
		
		
		System.out.println("用时："+ checkWaitTime(System.currentTimeMillis()-time));
		System.out.println("...comfyui封装测试结束");
	}
	
	// 判断等待时长
	private static String checkWaitTime(long times) {
		// 每个等待按最大inferSize个推理算，一个推理估计不超过30秒
		long sumSeconds = times/1000;
		int minutes = new BigDecimal(sumSeconds / 60.0).setScale(1, RoundingMode.HALF_UP).intValue(); // 60秒为1分钟
		double hours = new BigDecimal(sumSeconds / 3600.0).setScale(2, RoundingMode.HALF_UP).doubleValue(); // 3600秒为1小时
        //System.out.println(sumSeconds + "秒等于" + minutes + "分钟");
        //System.out.println(sumSeconds + "秒等于" + hours + "小时");
		return hours>=1?(hours+"小时"):(minutes+"分钟");
	}
	
	// comfyui websocket
	private static WebSocketClient webSocketOpen(String wsUrl, Map<String, String> backMap, String clientId) {
		WebSocketClient wsc = null;
        try {
        	wsc = new WebSocketClient(new URI(wsUrl)) {
                @Override
                public void onOpen(ServerHandshake serverHandshake) {
                    logger.info(clientId+"与comfyui服务端建立连接");
                }

                @Override
                public void onMessage(String msgJsonStr) {
                	//logger.info("收到服务端的消息a：{}", msgJsonStr);
                	
                	if (backMap != null) {
                    	JSONObject msgJson = JSONObject.parseObject(msgJsonStr);
                    	
                    	if (msgJson != null && "progress".equals(msgJson.getString("type"))) {
                    		logger.info("{}收到comfyui服务端的消息：{}", clientId, msgJsonStr);
                    		
                    		JSONObject dataJson = msgJson.getJSONObject("data");
                    		
                    		// 过滤范围4~50
                    		if (dataJson.containsKey("max") 
                    				&& dataJson.containsKey("value") 
                    				&& dataJson.getIntValue("max") >= 4 
                    				&& dataJson.getIntValue("max") <= 50
                    				&& dataJson.getIntValue("max") == dataJson.getIntValue("value")) {
                    			logger.info("{}推理结束：{}", clientId, JSONObject.toJSONString(backMap));
                    			
    						}
    					}
					}
                }
                
                @Override
                public void onClose(int i, String s, boolean b) {
                	logger.info(clientId+"与comfyui服务端的连接断开 code:{} reason:{} {}", i, s, b);
                	backMap.put("isConnected", "-1");
                }

                @Override
                public void onError(Exception e) {
                	logger.info(clientId+"连接报错");
                	backMap.put("isConnected", "0");
                }
            };
            wsc.connect();
        } catch (Exception e) {
            backMap.put("isConnected", "-1");
        }
        
        return wsc;
	}

}
