package org.lightweight.json.components;

public abstract class JSONComponent {
	protected final StringBuilder builder;
	
	public JSONComponent(final StringBuilder builder) {
		this.builder = builder;
	}
	
	public JSONComponent(final CharSequence json) {
		this.builder = new StringBuilder(json);
	}
	
	public JSONComponent() {
		this.builder = new StringBuilder();
	}
	
	public StringBuilder getBuilder() {
		return this.builder;
	}
	
	public boolean isArray() {
		return this instanceof JSONArray;
	}
	
	public boolean isObject() {
		return this instanceof JSONObject;
	}
	
	public boolean isValue() {
		return this instanceof JSONValue;
	}
}