package org.lightweight.json.components;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.lightweight.json.utils.JSONUtils;

/**
 * A class representing the object data structure of JSON.
 */
public class JSONObject extends JSONComponent {
	private final Map<String, int[]> components = new LinkedHashMap<>();
	
	public JSONObject(final CharSequence json) {
		super(json);
	}
	
	public JSONObject() {
		super();
	}
	
	public void place(final String name, final int[] value) {
		this.components.put(name, value);
	}
	
	public JSONObject add(final String name, final Object value) {
		this.builder.append('"').append(name).append("\":");
		
		final int[] indexes = new int[2];
		
		indexes[0] = this.builder.length();
		this.builder.append(JSONUtils.escape(value.toString()));
		indexes[1] = this.builder.length();
		
		this.components.put(name, indexes);
		
		return this;
	}
	
	public JSONObject add(final String name, final CharSequence value) {
		this.builder.append('"').append(name).append("\":");
		
		final int[] indexes = new int[2];
		
		indexes[0] = this.builder.length();
		this.builder.append('"').append(JSONUtils.escape(value)).append('"');
		indexes[1] = this.builder.length();
		
		this.components.put(name, indexes);
		
		return this;
	}
	
	public JSONObject add(final String name, final Character value) {
		return this.add(name, value.toString());
	}
	
	public Map<String, int[]> get() {
		return this.components;
	}
	
	public JSONValue get(final String name) {
		final int[] indexes = this.components.get(name);
		return indexes == null ? null : new JSONValue(this.builder, indexes);
	}
	
	@Override
	public String toString() {
		final StringBuilder string = new StringBuilder(this.builder.length() + 3 * this.components.size());
		string.append('{');
		
		boolean first = true;
		
		for (final Entry<String, int[]> entry : this.components.entrySet()) {
			if (!first) {
				string.append(',');
			} else {
				first = false;
			}
			final int[] value = entry.getValue();
			string.append('"').append(entry.getKey()).append("\":").append(this.builder.subSequence(value[0], value[1]));
		}
		string.append('}');
		
		return string.toString();
	}
}