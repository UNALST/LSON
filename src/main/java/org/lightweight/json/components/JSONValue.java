package org.lightweight.json.components;

import java.util.UUID;

import org.lightweight.json.JSONParser;
import org.lightweight.json.utils.JSONUtils;

/**
 * A class representing a value of JSON.
 */
public class JSONValue extends JSONComponent {
	private final int[] indexes;
	
	public JSONValue(final StringBuilder json, final int[] indexes) {
		super(json);
		
		this.indexes = indexes;
	}
	
	public JSONArray getAsJSONArray() {
		return JSONParser.parseArray(this.builder, this.indexes[0], this.indexes[1]);
	}
	
	public JSONObject getAsJSONObject() {
		return JSONParser.parseObject(this.builder, this.indexes[0], this.indexes[1]);
	}
	
	public String getAsString() {
		final String string = this.toString();
		return JSONUtils.unescapeAsString(string.substring(string.charAt(0) == '"' ? 1 : 0, string.charAt(string.length() - 1) == '"' ? string.length() - 1 : string.length()));
	}
	
	public boolean getAsBoolean() {
		return Boolean.parseBoolean(this.toString());
	}
	
	public byte getAsByte() {
		return Byte.parseByte(this.toString());
	}
	
	public char getAsCharacter() {
		return this.toString().charAt(0);
	}
	
	public short getAsShort() {
		return Short.parseShort(this.toString());
	}
	
	public int getAsInt() {
		return Integer.parseInt(this.toString());
	}
	
	public float getAsFloat() {
		return Float.parseFloat(this.toString());
	}
	
	public long getAsLong() {
		return Long.parseLong(this.toString());
	}
	
	public double getAsDouble() {
		return Double.parseDouble(this.toString());
	}
	
	public UUID getAsUUID() {
		return UUID.fromString(this.toString());
	}
	
	@Override
	public String toString() {
		return this.builder.substring(this.indexes[0], this.indexes[1]);
	}
}