package fangg.bean.comfyui;

import java.util.Map;

import fangg.util.UuidUtil;

public class SendParam {

	private String client_id;	// 接口调用id
	private Map<String, PromptNode> prompt;

	public String getClient_id() {
		if (client_id == null) {
			client_id = UuidUtil.genUuid_0(32);
		}
		return client_id;
	}

	public void setClient_id(String client_id) {
		this.client_id = client_id;
	}

	public Map<String, PromptNode> getPrompt() {
		return prompt;
	}

	public void setPrompt(Map<String, PromptNode> prompt) {
		this.prompt = prompt;
	}
	
}
