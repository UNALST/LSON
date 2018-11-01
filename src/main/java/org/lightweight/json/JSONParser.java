package org.lightweight.json;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import org.lightweight.json.components.JSONArray;
import org.lightweight.json.components.JSONObject;
import org.lightweight.json.exceptions.JSONParseException;
import org.lightweight.json.utils.JSONUtils;

public class JSONParser {
	/**
	 * Parses an object implementing {@link CharSequence} as either {@link JSONObject} or {@link JSONArray}.
	 * <p>This method is slower compared to calling either {@link JSONParser#parseObject(CharSequence)} or 
	 * {@link JSONParser#parseArray(CharSequence)} directly.</p>
	 * 
	 * @param json - the {@link CharSequence} that will be read.
	 * @param clazz - the class of the JSON type (either {@link JSONObject} or {@link JSONArray}).
	 * @return An instance of Object of type {@link T} that was parsed from {code json}.
	 * @throws JSONParseException if the text could not be parsed.
	 * 
	 * @see JSONParser#parseObject(CharSequence)
	 * @see JSONParser#parseArray(CharSequence)
	 */
	@SuppressWarnings("unchecked")
	public static <T> T parse(final CharSequence json, final Class<T> clazz) {
		return (T) (clazz == JSONObject.class ? JSONParser.parseObject(json) : JSONParser.parseArray(json));
	}
	
	/**
	 * Parses an object implementing {@link CharSequence} from bounds {@code lower} to {@code upper} as a {@link JSONObject}.
	 * 
	 * @param json - the sequence of characters that will be read.
	 * @param lower - the lower bound of {@code json}.
	 * @param upper - the upper bound of {@code json}.
	 * @return {@link JSONObject} that was parsed from {@code json}.
	 * @throws JSONParseException if the text could not be parsed.
	 * 
	 * @see JSONParser#parseObject(CharSequence)
	 */
	public static JSONObject parseObject(final CharSequence json, final int lower, final int upper) {
		final JSONObject object = new JSONObject(json);
		
		final int readable = upper - 1; // The maximum index that can be read from.
		boolean open = false; // The JSON object has yet to be opened.
		
		for (int i = lower; i < upper; ++i) {
			char read = json.charAt(i);
			
			switch (read) {
				// Encountered the start of a JSON object.
				case '{': {
					open = true; // Set open to true to indicate that the JSON object has been opened.
					break;
				}
				// Encountered the end of a JSON object.
				case '}': {
					// Check if there is a JSON object to close, if there isn't, the input json is not a valid JSON object.
					if (!open) {
						throw new JSONParseException("Curly braces ('{', '}') are not balanced!");
					}
					open = false; // Close the JSON object.
					
					break;
				}
				// Encountered the start of the name of a JSON value.
				case '"': {
					final StringBuilder name = new StringBuilder();
					
					// Read and append to name until a quote that closes the name is encountered.
					do {
						// If this operation has led i to be equal to readable, the input json is not a valid JSON object.
						if (i == readable) {
							throw new JSONParseException("Quotes ('\"') are not balanced!");
						}
					} while (JSONUtils.append(name, json.charAt(++i), json, i));
					
					++i; // Skip the colon (':') encountered after reading the name of the JSON value.
					
					// Skip all of the unnecessary whitespace characters.
					do {
						// If this operation has led i to be equal to readable, the input json is not a valid JSON object.
						if (i == readable) {
							throw new JSONParseException("Quotes ('\"') are not balanced!");
						}
					} while (JSONUtils.isWhitespace(json.charAt(++i)));
					
					// The bounds of the value associated with the name: [lower, upper + 1].
					// The one is added because CharSequence#subSequence(int, int) takes the upper bound and subtracts one from it .
					final int[] value = new int[2];
					
					// The value is a JSON array or JSON object.
					if ((read = json.charAt(i)) == '[' || read == '{') {
						value[0] = i; // The value begins at index i.
						
						// The difference of the integer values of a JSON structure's closing and opening characters is two.
						final char opening = read, closing = (char) (opening + 2);
						// Whatever is between the opening and closing characters does not matter right now, only the bounds of the value is needed.
						// The value is initially one because of the character that was just read.
						int balanced = 1;
						
						// Check if a name is being read so the opening or closing characters inside the name are not read.
						boolean quoteOpen = false;
						
						// Read until a character closes the structure completely (will happen once balanced equals 0).
						do {
							// Check if a name is being read so the opening or closing characters inside the name are not read.
							if ((read = json.charAt(++i)) == '"') {
								int j = 0;
								
								do {
									quoteOpen = !quoteOpen;
								} while (json.charAt(i - ++j) == '\\');
							}
							// Check if the character read is either the opening or closing character for the given JSON structure.
							if ((read == closing || read == opening) && !quoteOpen) {
								// balanced = balanced - 1 + closing - read
								// = balanced - 1 + (2 or 0, depends whether read is equal to closing or opening)
								// = balanced - (±1)
								balanced = balanced - 1 + closing - read;
							}
							// If this operation has led i to be equal to readable, the input json is not a valid JSON array.
							if (i == readable) {
								throw new JSONParseException("Curly braces ('{', '}') or brackets ('[', ']') are not balanced!");
							}
						} while (balanced != 0);
						
						value[1] = i + 1; // The value ends at index i, but index i + 1 is stored because of CharSequence#subSequence(int, int).
					// The value is a string.
					} else if (read == '"') {
						value[0] = i; // The value begins at index i.
						
						// Read until a quote that closes the value is encountered.
						while (JSONUtils.shouldSkip(read, read = json.charAt(++i), json, i)) {
							// If this operation has led i to be equal to readable, the input json is not a valid JSON object.
							if (i == readable) {
								throw new JSONParseException("Quotes ('\"') are not balanced!");
							}
						}
						value[1] = i + 1; // The value ends at index i, but index i + 1 is stored because of CharSequence#subSequence(int, int).
					// The value type is not specified.
					} else {
						value[0] = i; // The value begins at index i.
						
						// Read until a comma (end of value, value coming after it) or a closing curly brace (end of value, end of object).
						while ((read = json.charAt(++i)) != ',' && read != '}') {
							// Check if a whitespace comes after the colon (for JSON files that are supposed to be easily readable for humans).
							if (JSONUtils.isWhitespace(read)) {
								// Skip all of the unnecessary whitespace characters.
								do {
									// If this operation has led i to be equal to readable, the input json is not a valid JSON object.
									if (i == readable) {
										throw new JSONParseException("Curly braces ('{', '}') are not balanced!");
									}
								} while (JSONUtils.isWhitespace(json.charAt(++i)));
								
								// Check if the object was closed and break if it was.
								if ((read = json.charAt(i)) == '}') {
									break;
								}
							}
							// If this operation has led i to be equal to readable, the input json is not a valid JSON object.
							if (i == readable) {
								throw new JSONParseException("Curly braces ('{', '}') are not balanced!");
							}
						}
						// Check if the object was closed.
						if (read == '}') {
							// Check if the JSON object is supposed to close.
							if (open) {
								open = false; // Close the JSON object.
							} else {
								throw new JSONParseException("Curly braces ('{', '}') are not balanced!"); // The input json is not a valid JSON object.
							}
						}
						// The value ends at index i - 1, but index i is stored because of CharSequence#subSequence(int, int).
						// Currently, either ',' or '}' is at the index i.
						value[1] = i;
					}
					object.place(name.toString(), value); // set the value bounds corresponding to the given name in the object.
					break;
				}
				// The character read was a whitespace character, do nothing.
				default: {
					break;
				}
			}
		}
		// If the JSON object was never closed, the input json is not a valid JSON object.
		if (open) {
			throw new JSONParseException("Curly braces ('{', '}') are not balanced!");
		}
		return object;
	}
	
	/**
	 * Parses an object implementing {@link CharSequence} as {@link JSONObject}.
	 * 
	 * @param json - the sequence of characters that will be read.
	 * @return {@link JSONObject} that was parsed from {@code json}.
	 * @throws JSONParseException if the text could not be parsed.
	 * 
	 * @see JSONParser#parseObject(CharSequence, int, int)
	 */
	public static JSONObject parseObject(final CharSequence json) {
		return JSONParser.parseObject(json, 0, json.length());
	}
	
	/**
	 * Parses a {@link File} as {@link JSONObject}.
	 * 
	 * @param file - the file that will be read.
	 * @return {@link JSONObject} that was parsed from {@code file}.
	 * @throws JSONParseException if the file was not found or could not be parsed.
	 */
	public static JSONObject parseObject(final File file) {
		final char[] buffer = new char[(int) file.length()];
		final int size, readable;
		
		try {
			final BufferedReader reader = new BufferedReader(new FileReader(file));
			readable = (size = reader.read(buffer)) - 1;
			
			reader.close();
		} catch (IOException e) {
			throw new JSONParseException("The file could not be parsed!", e);
		}
		final JSONObject object = new JSONObject(new String(buffer));
		boolean open = false;
		
		for (int i = 0; i < size; ++i) {
			char read = buffer[i];

			switch (read) {
				case '{': {
					open = true;
					break;
				}
				case '}': {
					if (open) {
						open = false;
					} else {
						throw new JSONParseException("Curly braces ('{', '}') are not balanced!");
					}
					break;
				}
				case '"': {
					final StringBuilder name = new StringBuilder();
					
					do {
						if (i == readable) {
							throw new JSONParseException("Quotes ('\"') are not balanced!");
						}
					} while (JSONUtils.append(name, buffer[++i], buffer, i));
					
					++i;
					
					do {
						if (i == readable) {
							throw new JSONParseException("Quotes ('\"') are not balanced!");
						}
					} while (JSONUtils.isWhitespace(buffer[++i]));
					
					final int[] value = new int[2];
					read = buffer[i];
					
					if (read == '[' || read == '{') {
						final char opening = read, closing = (char) (opening + 2);
						value[0] = i;
						
						int balanced = 1;
						
						do {
							if ((read = buffer[++i]) == opening || read == closing) {
								balanced = balanced - 1 + closing - read;
							}
							if (i == readable) {
								throw new JSONParseException("Curly braces ('{', '}') or brackets ('[', ']') are not balanced!");
							}
						} while (balanced != 0);
						
						value[1] = i + 1;
					} else if (read == '"') {
						value[0] = i;
						
						while (JSONUtils.shouldSkip(read, read = buffer[++i], buffer, i)) {
							if (i == readable) {
								throw new JSONParseException("Quotes ('\"') are not balanced!");
							}
						}
						value[1] = i + 1;
					} else {
						value[0] = i;
						
						while ((read = buffer[++i]) != ',' && read != '}') {
							if (JSONUtils.isWhitespace(read)) {
								do {
									if (i == readable) {
										throw new JSONParseException("Curly braces ('{', '}') are not balanced!");
									}
								} while (JSONUtils.isWhitespace(buffer[++i]));
	
								if ((read = buffer[i]) == '}') {
									break;
								}
							}
							if (i == readable) {
								throw new JSONParseException("Curly braces ('{', '}') are not balanced!");
							}
						}
						if (read == '}') {
							if (open) {
								open = false;
							} else {
								throw new JSONParseException("Curly braces ('{', '}') are not balanced!");
							}
						}
						value[1] = i;
					}
					object.place(name.toString(), value);
					break;
				}
				default: {
					break;
				}
			}
		}
		if (open) {
			throw new JSONParseException("Curly braces ('{', '}') or brackets ('[', ']') are not balanced!");
		}
		return object;
	}
	
	/**
	 * Parses an object implementing {@link CharSequence} from bounds {@code lower} to {@code upper} as {@link JSONArray}.
	 * 
	 * @param json - the sequence of characters that will be read.
	 * @param lower - the lower bound of {@code json}.
	 * @param upper - the upper bound of {@code json}.
	 * @return {@link JSONArray} that was parsed from {@code json}.
	 * @throws JSONParseException if the text could not be parsed.
	 * 
	 * @see JSONParser#parseArray(CharSequence)
	 */
	public static JSONArray parseArray(final CharSequence json, final int lower, final int upper) {
		final int readable = upper - 1; // The maximum index that can be read from.
		
		int i = lower;
		
		// Skip all of the unnecessary whitespace characters.
		while (JSONUtils.isWhitespace(json.charAt(i++))) {
			// If this operation has led i to be equal to readable, the input json is not a valid JSON array.
			if (i == upper) {
				throw new JSONParseException("There was no JSON array found in the input json from bounds " + lower + " to " + upper + '!');
			}
		}
		// Check if the first non-whitespace character is an opening bracket, if it isn't, the input json is not a valid JSON array.
		if (json.charAt(i - 1) != '[') {
			throw new JSONParseException("There was no JSON array found in the input json from bounds " + lower + " to " + upper + '!');
		}
		final JSONArray array = new JSONArray(json);
		boolean open = true; // The JSON array was successfully opened.
		
		for (; i < upper; ++i) {
			char read = json.charAt(i);
			
			// The character read was a whitespace character or a comma, do nothing.
			if (JSONUtils.isWhitespace(read) || read == ',') {
				continue;
			}
			switch (read) {
				// Encountered the start of a JSON structure.
				case '[':
				case '{': {
					// The bounds of the element: [lower, upper + 1].
					// The one is added because CharSequence#subSequence(int, int) takes the upper bound and subtracts one from it .
					final int[] element = new int[2];
					element[0] = i; // The element begins at index i.
					
					// The difference of the integer values of a JSON structure's closing and opening characters is two.
					final char opening = read, closing = (char) (read + 2);
					// Whatever is between the opening and closing characters does not matter right now, only the bounds of the value is needed.
					// The value is initially one because of the character that was just read.
					int balanced = 1;
					
					// Check if a name is being read so the opening or closing characters inside the name are not read.
					boolean quoteOpen = false;
					
					// Read until a character closes the structure completely (will happen once balanced equals 0).
					do {
						// If this operation has led i to be equal to readable, the input json is not a valid JSON array.
						if (i == readable) {
							throw new JSONParseException("Curly braces ('{', '}') or brackets ('[', ']') are not balanced!");
						}
						// Check if a name is being read so the opening or closing characters inside the name are not read.
						if (read == '"') {
							int j = 0;
							
							do {
								quoteOpen = !quoteOpen;
							} while (json.charAt(i - ++j) == '\\');
						}
						// Check if the character read is either the opening or closing character for the given JSON structure.
						if (((read = json.charAt(++i)) == closing || read == opening) && !quoteOpen) {
							// balanced = balanced - 1 + closing - read
							// = balanced - 1 + (2 or 0, depends whether read is equal to closing or opening)
							// = balanced - (±1)
							balanced = balanced - 1 + closing - read;
						}
					} while (balanced != 0);
					
					element[1] = i + 1; // The element ends at index i, but index i + 1 is stored because of CharSequence#subSequence(int, int).
					
					array.place(element); // Set the bounds in the array corresponding the element.
					break;
				}
				// Encountered the end of a JSON array.
				case ']': {
					// Check if the JSON object is supposed to close.
					if (open) {
						open = false; // Close the JSON array.
					} else {
						throw new JSONParseException("Brackets ('[', ']') are not balanced!"); // The input json is not a valid JSON array.
					}
					break;
				}
				case '"': {
					// The bounds of the element: [lower, upper + 1].
					// The one is added because CharSequence#subSequence(int, int) takes the upper bound and subtracts one from it .
					final int[] element = new int[2];
					element[0] = i;  // The element begins at index i.
					
					while (JSONUtils.shouldSkip(read, read = json.charAt(++i), json, i)) {
						// If this operation has led i to be equal to readable, the input json is not a valid JSON array.
						if (i == readable) {
							throw new JSONParseException("Quotes ('\"') are not balanced!");
						}
					}
					element[1] = i + 1; // The element ends at index i, but index i + 1 is stored because of CharSequence#subSequence(int, int).
					
					array.place(element); // Set the bounds in the array corresponding the element.
					break;
				}
				default: {
					// The bounds of the element: [lower, upper + 1].
					// The one is added because CharSequence#subSequence(int, int) takes the upper bound and subtracts one from it .
					final int[] element = new int[2];
					element[0] = i; // The element begins at index i.
					
					// Read until a comma (end of element, element coming after it) or a closing bracket (end of element, end of array).
					do {
						// If this operation has led i to be equal to readable, the input json is not a valid JSON array.
						if (i == readable) {
							throw new JSONParseException("Brackets ('[', ']') are not balanced!");
						}
					} while (!JSONUtils.isWhitespace(read = json.charAt(++i)) && read != ',' && read != ']');
					
					if (read == ']') {
						// Check if the JSON object is supposed to close.
						if (open) {
							open = false; // Close the JSON array.
						} else {
							throw new JSONParseException("Brackets ('[', ']') are not balanced!"); // The input json is not a valid JSON array.
						}
					}
					// The element ends at index i - 1, but index i is stored because of CharSequence#subSequence(int, int).
					// Currently, either ',' or ']' is at the index i.
					element[1] = i;
					
					array.place(element); // Set the bounds in the array corresponding the element.
					break;
				}
			}
		}
		// If the JSON object was never closed, the input json is not a valid JSON array.
		if (open) {
			throw new JSONParseException("Brackets ('[', ']') are not balanced!");
		}
		return array;
	}
	
	/**
	 * Parses an object implementing {@link CharSequence} as {@link JSONArray}.
	 * 
	 * @param json - the sequence of characters that will be read.
	 * @return {@link JSONArray} that was parsed from {@code json}.
	 * @throws JSONParseException if the text could not be parsed.
	 * 
	 * @see JSONParser#parseArray(CharSequence, int, int)
	 */
	public static JSONArray parseArray(final CharSequence json) {
		return JSONParser.parseArray(json, 0, json.length());
	}
	
	/**
	 * Parses a {@link File} as {@link JSONArray}.
	 * 
	 * @param file - the file that will be read.
	 * @return {@link JSONArray} that was parsed from {@code file}.
	 * @throws JSONParseException if the file was not found or could not be parsed.
	 */
	public static JSONArray parseArray(final File file) {
		final char[] buffer = new char[(int) file.length()];
		final int size, readable;
		
		try {
			final BufferedReader reader = new BufferedReader(new FileReader(file));
			readable = (size = reader.read(buffer)) - 1;
			
			reader.close();
		} catch (IOException e) {
			throw new JSONParseException("The file could not be parsed!", e);
		}
		int i = 0;
		
		while (JSONUtils.isWhitespace(buffer[i++])) {
			if (i == size) {
				throw new JSONParseException("There was no JSON array found in the file " + file.getName() + '!');
			}
		}
		if (buffer[i - 1] != '[') {
			throw new JSONParseException("There was no JSON array found in the file " + file.getName() + '!');
		}
		final JSONArray array = new JSONArray(new String(buffer));
		boolean open = true;
		
		for (; i < size; ++i) {
			char read = buffer[i];
			
			if (JSONUtils.isWhitespace(read) || read == ',') {
				continue;
			}
			switch (read) {
				case '[':
				case '{': {
					final int[] element = new int[2];
					element[0] = i;
					
					final char opening = read, closing = (char) (read + 2);
					int balanced = 1;
					
					boolean quoteOpen = false;
					
					do {
						if (i == readable) {
							throw new JSONParseException("Curly braces ('{', '}') or brackets ('[', ']') are not balanced!");
						}
						if (read == '"') {
							int j = 0;
							
							do {
								quoteOpen = !quoteOpen;
							} while (buffer[i - ++j] == '\\');
						}
						if (((read = buffer[++i]) == opening || read == closing) && !quoteOpen) {
							balanced = balanced - 1 + closing - read;
						}
					} while (balanced != 0);

					element[1] = i + 1;
					
					array.place(element);
					break;
				}
				case ']': {
					if (open) {
						open = false;
					} else {
						throw new JSONParseException("Brackets ('[', ']') are not balanced!");
					}
					break;
				}
				case '"': {
					final int[] element = new int[2];
					element[0] = i;
					
					while (JSONUtils.shouldSkip(read, read = buffer[++i], buffer, i)) {
						if (i == readable) {
							throw new JSONParseException("Quotes ('\"') are not balanced!");
						}
					}
					element[1] = i + 1;
					
					array.place(element);
					break;
				}
				default: {
					final int[] element = new int[2];
					element[0] = i;
					
					do {
						if (i == readable) {
							throw new JSONParseException("Brackets ('[', ']') are not balanced!");
						}
					} while (!JSONUtils.isWhitespace(read = buffer[++i]) && read != ',' && read != ']');
					
					if (read == ']') {
						if (open) {
							open = false;	
						} else {
							throw new JSONParseException("Brackets ('[', ']') are not balanced!");
						}
					}
					element[1] = i;
					
					array.place(element);
					break;
				}
			}
		}
		if (open) {
			throw new JSONParseException("Brackets ('[', ']') are not balanced!");
		}
		return array;
	}
}