package org.lightweight.json.utils;

public class JSONUtils {
	// Check if a character is a whitespace character.
	public static boolean isWhitespace(final char character) {
		return character == ' ' || character == '\n' || character == '\r' || character == '\t';
	}
	
	// Check if the character read is not the opening/closing character of a string in JSON ('"'),
	// if it is, append it to the name and return true, otherwise return false.
	public static boolean append(final StringBuilder name, final char read, final CharSequence json, int i) {
		if (read == '"') {
			boolean escaped = false;
			
			while (json.charAt(--i) == '\\') {
				escaped = !escaped;
			}
			if (!escaped) {
				return false;
			}
		}
		name.append(read);
		return true;
	}
	
	// Check if the character read is not the opening/closing character of a string in JSON ('"'),
	// if it is, append it to the name and return true, otherwise return false.
	public static boolean append(final StringBuilder name, final char read, final char[] json, int i) {
		if (read == '"') {
			boolean escaped = false;
			
			while (json[--i] == '\\') {
				escaped = !escaped;
			}
			if (!escaped) {
				return false;
			}
		}
		name.append(read);
		return true;
	}
	
	// Check if a quote is escaped while parsing a string to a JSON type.
	public static boolean shouldSkip(final char previous, final char current, final CharSequence json, int i) {
		if (current != '"') {
			return true;
		}
		boolean escaped = false;
		
		while (json.charAt(--i) == '\\') {
			escaped = !escaped;
		}
		return escaped;
	}
	
	// Check if a quote is escaped while parsing a string to a JSON type.
	public static boolean shouldSkip(final char previous, final char current, final char[] json, int i) {
		if (current != '"') {
			return true;
		}
		boolean escaped = false;
		
		while (json[--i] == '\\') {
			escaped = !escaped;
		}
		return escaped;
	}
	
	// Check if a brace or bracket is escaped while parsing a string to a JSON type.
	public static boolean shouldRead(final char previous, final char current, final char opening, final char closing) {
		return previous != '\\' && (current == opening || current == closing);
	}
	
	/**
	 * Escapes all of the special characters found in {@code string}.
	 * 
	 * @param string - the {@link String} that will be escaped.
	 * @return a {@link StringBuilder} object with all special characters escaped.
	 * 
	 * @see JSONUtils#escapeAsString(String)
	 */
	public static StringBuilder escape(final CharSequence text) {
		final int length = text.length();
		final StringBuilder escaped = new StringBuilder(length);
		
		for (int i = 0; i < length; ++i) {
			final char read = text.charAt(i);
			
			switch (read) {
				case '"': {
	            	escaped.append("\\\"");
	                break;
	            }
	            case '\\': {
	            	escaped.append("\\\\");
	                break;
	            }
	            case '/': {
	            	escaped.append("\\/");
	            	break;
	            }
	            case '\b': {
	            	escaped.append("\\b");
	                break;
	            }
	            case '\f': {
	            	escaped.append("\\f");
	                break;
	            }
	            case '\n': {
	            	escaped.append("\\n");
	                break;
	            }
	            case '\r': {
	            	escaped.append("\\r");
	                break;
	            }
	            case '\t': {
	            	escaped.append("\\t");
	                break;
	            }
				default: {
					escaped.append(read);
				}
			}
		}
		return escaped;
	}
	
	/**
	 * Escapes all of the special characters found in {@code string}.
	 * 
	 * @param string - the {@link String} that will be escaped.
	 * @return {@link JSONUtils#escape(String)}{@code .toString()}.
	 * 
	 * @see JSONUtils#escape(String)
	 */
	public static String escapeAsString(final CharSequence text) {
		return JSONUtils.escape(text).toString();
	}
	
	/**
	 * Unescapes all of the escaped special characters found in {@code string}.
	 * 
	 * @param string - the {@link String} that will be unescaped.
	 * @return a {@link StringBuilder} object with all special characters unescaped.
	 * 
	 * @see JSONUtils#unescapeAsString(String)
	 */
	public static StringBuilder unescape(final CharSequence text) {
		final int length = text.length(), readable = length - 1;
        final StringBuilder unescaped = new StringBuilder(length);
        
        for (int i = 0; i < length; ++i) {
            char read = text.charAt(i);
            
            if (read == '\\' && i != readable) {
                switch (text.charAt(i + 1)) {
	                case '"': {
	                	read = '"';
	                	++i;
	                    break;
	                }
	                case '\\': {
	                	read = '\\';
	                	++i;
	                    break;
	                }
	                case '/': {
	                	read = '/';
	                	++i;
	                	break;
	                }
	                case 'b': {
	                	read = '\b';
	                	++i;
	                    break;
	                }
	                case 'f': {
	                	read = '\f';
	                	++i;
	                    break;
	                }
	                case 'n': {
	                	read = '\n';
	                	++i;
	                    break;
	                }
	                case 'r': {
	                	read = '\r';
	                	++i;
	                    break;
	                }
	                case 't': {
	                	read = '\t';
	                	++i;
	                    break;
	                }
	                case 'u': {
	                    if (i > readable - 5) {
	                    	read = 'u';
	                        break;
	                    }
	                    unescaped.append(Character.toChars(Integer.parseInt(text.subSequence(i += 2, i += 3).toString(), 16)));
	                    continue;
	                }
	                default: {
	                	break;
	                }
	            }
            }
            unescaped.append(read);
        }
        return unescaped;
    }
	
	/**
	 * Unescapes all of the escaped special characters found in {@code string}.
	 * 
	 * @param string - the {@link String} that will be unescaped.
	 * @return {@link JSONUtils#unescape(String)}{@code .toString()}.
	 * 
	 * @see JSONUtils#unescape(String)
	 */
	public static String unescapeAsString(final CharSequence parsed) {
		return JSONUtils.unescape(parsed).toString();
	}
	
	public static void appendEscaped(final CharSequence text, final StringBuilder string) {
		final int length = text.length();
		
		for (int i = 0; i < length; ++i) {
			final char read = text.charAt(i);
			
			switch (read) {
				case '"': {
					string.append('\\').append('"');
	                break;
	            }
	            case '\\': {
	            	string.append('\\').append('\\');
	                break;
	            }
	            case '/': {
	            	string.append('\\').append('/');
	            	break;
	            }
	            case '\b': {
	            	string.append('\\').append('b');
	                break;
	            }
	            case '\f': {
	            	string.append('\\').append('f');
	                break;
	            }
	            case '\n': {
	            	string.append('\\').append('n');
	                break;
	            }
	            case '\r': {
	            	string.append('\\').append('r');
	                break;
	            }
	            case '\t': {
	            	string.append('\\').append('t');
	                break;
	            }
				default: {
					string.append(read);
					break;
				}
			}
		}
	}
}