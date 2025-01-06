package fangg.bean.comfyui;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.alibaba.fastjson.JSONObject;

public class Prompt {
    private Map<String, PromptNode> nodes; 	// 存储所有节点
    private Map<String, Integer> idMap;		// 缓存id顺序

    public Prompt() {
        nodes = new LinkedHashMap<>(); // 保持插入顺序
    }

	public Prompt(String str) {
		nodes = new LinkedHashMap<>(); // 保持插入顺序
		// 
		JSONObject promptJson = JSONObject.parseObject(str);
		PromptNode promptNode;
		int maxNodeId = 0;
		
		for (Entry<String, Object> entry : promptJson.entrySet()) {
			promptNode = JSONObject.parseObject(JSONObject.toJSONString(entry.getValue()), PromptNode.class);
			addNode(entry.getKey(), promptNode);
			
			if (Integer.valueOf(entry.getKey()) > maxNodeId) {
				maxNodeId = Integer.valueOf(entry.getKey());
			}
		}
		
		// 最大序号
		if (idMap == null) {
			idMap = new HashMap<>();
		}
		idMap.put("nodeId", maxNodeId);
	}

	public Map<String, PromptNode> getNodes() {
		return nodes;
	}

	public boolean containsKey(String key) {
		return getNodes().containsKey(key);
	}

	// 添加一个节点
    public void addNode(String nodeId, PromptNode node) {
    	node.setNodeId(nodeId);
        nodes.put(nodeId, node);
    }
    
    // 添加一个节点
    public void addNode(PromptNode node) {
    	String nodeId = nextNodeId();
    	node.setNodeId(nodeId);
    	nodes.put(nodeId, node);
    }

    // 生成最终的 JSON 结构
    public Map<String, Object> toMap() {
        Map<String, Object> promptMap = new LinkedHashMap<>();
        for (Map.Entry<String, PromptNode> entry : nodes.entrySet()) {
            promptMap.put(entry.getKey(), entry.getValue().toMap());
        }
        return promptMap;
    }
    
    public String nextNodeId() {
    	if (idMap == null) {
			idMap = new HashMap<>();
			idMap.put("nodeId", 1);
		} else {
			idMap.put("nodeId", idMap.get("nodeId")+1);
		}
    	
    	return String.valueOf(idMap.get("nodeId"));
    }
    
    public PromptNode getPromptNode(String classType) {
    	return getPromptNode(classType, null);
    }
    
    public PromptNode getPromptNode(String classType, String nodeId) {
    	if (nodes != null) {
    		for (Entry<String, PromptNode> entry : nodes.entrySet()) {
    			if (entry.getValue().getClass_type().equals(classType) && ((nodeId!=null)?entry.getKey().equals(nodeId):true)) {
    				//System.out.println("存在匹配的类："+classType);
    				return entry.getValue();
    			}
    		}
    	}
    	
    	return null;
    }
    
}
