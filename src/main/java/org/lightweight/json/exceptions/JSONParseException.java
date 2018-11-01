package org.lightweight.json.exceptions;

/**
 * The exception thrown when there is an issue parsing any data to a JSON data structure
 */
public class JSONParseException extends RuntimeException {
	private static final long serialVersionUID = -4901560226298892448L;
	
	/**
	 * Creates exception with the message {@code message}.
	 * 
	 * @param message - the message describing the cause of this exception.
	 */
	public JSONParseException(final String message) {
        super(message);
    }
	
	/**
	 * Creates exception with the message {@code message} and cause {@code cause}.
	 * 
	 * @param message - the message describing the cause of this exception.
	 * @param cause - the cause of this exception.
	 */
	public JSONParseException(final String message, final Throwable cause) {
        super(message, cause);
    }
	
	/**
	 * Creates exception with the cause {@code cause}.
	 * 
	 * @param cause - the cause of this exception.
	 */
	public JSONParseException(final Throwable cause) {
        super(cause);
    }
}