package org.lightweight.json.components;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.lightweight.json.utils.JSONUtils;

/**
 * A class representing the array data structure of JSON.
 */
public class JSONArray extends JSONComponent implements Iterable<JSONValue> {
	private final List<int[]> elements = new LinkedList<>();
	
	public JSONArray(final CharSequence json) {
		super(json);
	}
	
	public JSONArray() {
		super();
	}
	
	public void place(final int[] element) {
		this.elements.add(element);
	}
	
	public JSONArray add(final Object element) {
		final int[] indexes = new int[2];
		
		indexes[0] = this.builder.length();
		this.builder.append(JSONUtils.escape(element.toString()));
		indexes[1] = this.builder.length();
		
		this.elements.add(indexes);
		
		return this;
	}
	
	public JSONArray add(final String element) {
		final int[] indexes = new int[2];
		
		indexes[0] = this.builder.length();
		this.builder.append('"').append(JSONUtils.escape(element)).append('"');
		indexes[1] = this.builder.length();
		
		this.elements.add(indexes);
		
		return this;
	}
	
	public JSONArray add(final Character value) {
		return this.add(value.toString());
	}
	
	public List<int[]> getIndexes() {
		return this.elements;
	}
	
	public Stream<JSONValue> getStream() {
		return this.elements.stream().map(index -> new JSONValue(this.builder, index));
	}
	
	public Stream<JSONValue> getParallelStream() {
		return this.elements.parallelStream().map(index -> new JSONValue(this.builder, index));
	}
	
	public List<JSONValue> get() {
		return this.getStream().collect(Collectors.toList());
	}
	
	public JSONValue get(final int index) {
		return new JSONValue(this.builder, this.elements.get(index));
	}
	
	@Override
	public String toString() {
		final StringBuilder string = new StringBuilder();
		string.append('[');
		
		boolean first = true;
		
		for (final int[] element : this.elements) {
			if (!first) {
				string.append(',');
			} else {
				first = false;
			}
			string.append(this.builder.subSequence(element[0], element[1]));
		}
		string.append(']');
		
		return string.toString();	
	}
	
	@Override
	public Iterator<JSONValue> iterator() {
		return this.get().iterator();
	}
}