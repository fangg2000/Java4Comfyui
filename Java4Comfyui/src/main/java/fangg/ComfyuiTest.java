package fangg;

import java.io.File;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson2.JSONObject;

import fangg.bean.comfyui.Prompt;
import fangg.bean.comfyui.PromptNode;
import fangg.bean.comfyui.SendParam;
import fangg.util.FileUtil;
import fangg.util.HttpUtil;

public class ComfyuiTest {
	private static final Logger logger = LoggerFactory.getLogger(ComfyuiTest.class);
	
	private static String BOOK_PATH = "/user/other/book";

	public static void main(String[] args) throws Exception {
		System.out.println("开始comfyui封装测试...");
		// 
		test();
		
		System.out.println("...结束comfyui封装测试");
	}
	
	/**
	 * 循环读取文件侠下的图片进行高清修复
	 */
	private static void test() throws Exception {
		System.out.println("开始comfyui封装测试...");
		
		String promptStr = null;
		long time = System.currentTimeMillis();
		Random random = new Random();
        long random15DigitInt = random.longs(1, 1000000000000000L).findFirst().getAsLong();
        // 发送对象
		SendParam sendParam = new SendParam();
		
		// 调用comfyui websocket
		WebSocketClient wsc = null;
		
		try {
			// 读取本地工作流文件
			promptStr = FileUtil.fileRead(String.format("%s/init_data/comfyui/i2i_hd_repair_param_v1.json", BOOK_PATH));
			
			// 创建 Prompt 对象
			Prompt prompt = new Prompt(promptStr);
			
			wsc = null;
			Map<String, String> backMap = null;
			backMap = new HashMap<>();
			backMap.put("isConnected", "1");
			wsc = webSocketOpen("ws://127.0.0.1:8188/ws?clientId="+sendParam.getClient_id(), backMap, sendParam.getClient_id());
			
			// 等待建立图生视频websocket链接结果
			Thread.sleep(1000);
			
			if (wsc == null || Integer.valueOf(backMap.get("isConnected")) != 1) {
				logger.warn("建立websocket链接失败...");
				return;
			}
			
			File dirFile = new File("/user/下载/海洋冒险记/001");
			String saveDirFile = "/user/other/tts/MV-ComfyUI/output";
			String cacheStr;
			int num = 10000;
					
			for (File imgDirFile : dirFile.listFiles()) {
				if (imgDirFile.isDirectory()) {
					//cacheStr = saveDirFile+"/"+imgDirFile.getName();
					//newDirFile(cacheStr);
					
					for (File imgFile : imgDirFile.listFiles()) {
						if (imgFile.getName().endsWith(".png")) {
						    PromptNode loadImage = prompt.getPromptNode("LoadImage");
						    loadImage.addInput("image", imgFile.getPath());
						    
						    PromptNode saveImage = prompt.getPromptNode("SaveImage");
						    saveImage.addInput("filename_prefix", String.format("%s/%s", imgDirFile.getName(), num));
						    num++;
						    
						    backMap.put("over", "0");

						    sendParam.setPrompt(prompt.getNodes());
							String resultStr = HttpUtil.sendPostByJson("http://127.0.0.1:8188/api/prompt", JSONObject.toJSONString(sendParam));
							System.out.println("comfyui websocket调用返回："+resultStr);
							
							// 指定最大处理时间
							for (int i = 0; i < 180; i++) {
								if ("1".equals(backMap.get("over"))) {
									System.out.println(imgFile.getPath()+"推理结束");
									break;
								} else {
									Thread.sleep(1000);
								}
							}
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (wsc != null) {
				wsc.close();
			}
		}
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
                    	boolean inferFlag = true;
                    	
                    	// 判断推理是否结束
                    	if (msgJson != null && msgJson.containsKey("data")) {
                    		JSONObject dataJson = msgJson.getJSONObject("data");
							if (dataJson.containsKey("status")) {
								JSONObject statusJson = dataJson.getJSONObject("status");
								if (statusJson.containsKey("exec_info")) {
									JSONObject execJson = statusJson.getJSONObject("exec_info");
									if (execJson.containsKey("queue_remaining") && execJson.getIntValue("queue_remaining") == 0) {
										inferFlag = false;
									}
								}
							}
						}
                    	
                    	if (inferFlag) {
                    		// comfyui 执行中/失败
							return;
						}
                    	
                    	// comfyui 执行结束后其它处理
                    	
                    	backMap.put("over", "1");
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

	
	/**
	 * 新目录
	 */
	public static File newDirFile(String path) {
		File newDir = new File(path);
		if (newDir.isDirectory() == false) {
			newDir.mkdir();
		}
		return newDir;
	}

}
