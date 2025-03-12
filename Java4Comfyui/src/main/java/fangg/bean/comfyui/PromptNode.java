package com.fangg.bgm.api.bean.comfyui;

import java.util.HashMap;
import java.util.Map;

public class PromptNode {
    private String class_type; // 节点类型，如 "KSampler", "CLIPTextEncode"
    private Map<String, Object> inputs; // 节点的输入参数
    private Map<String, String> _meta; // 元数据，如 "title"
	private String nodeId;	// Node节点对应id

    public PromptNode(String classType) {
        this.class_type = classType;
        this.inputs = new HashMap<>();
        this._meta = new HashMap<>();
    }

    public String getNodeId() {
		return nodeId;
	}

	public void setNodeId(String nodeId) {
		this.nodeId = nodeId;
	}

	public String getClass_type() {
		return class_type;
	}

	public void setClass_type(String class_type) {
		this.class_type = class_type;
	}

	public Map<String, Object> getInputs() {
		return inputs;
	}

	public void setInputs(Map<String, Object> inputs) {
		this.inputs = inputs;
	}

	public Map<String, String> get_meta() {
		return _meta;
	}

	public void set_meta(Map<String, String> _meta) {
		this._meta = _meta;
	}

	public void addInput(String key, Object value) {
        inputs.put(key, value);
    }

    public void addMeta(String key, String value) {
    	_meta.put(key, value);
    }

	public Map<String, Object> toMap() {
        Map<String, Object> nodeMap = new HashMap<>();
        nodeMap.put("class_type", class_type);
        nodeMap.put("inputs", inputs);
        if (!_meta.isEmpty()) {
            nodeMap.put("_meta", _meta);
        }
        return nodeMap;
    }

    // 动态解析引用的 key
    public void linkInput(String inputKey, String referencedNodeId, int outputIndex) {
        inputs.put(inputKey, new Object[]{referencedNodeId, outputIndex});
    }
    
    // 动态解析引用的 key
    public boolean linkInput(String inputKey, PromptNode promptNode, int outputIndex) {
    	if (promptNode != null) {
        	inputs.put(inputKey, new Object[]{promptNode.getNodeId(), outputIndex});
        	return true;
		}
    	return false;
    }
    
}