package com.cresora.utilities.compiler;

/** Thrown when the DSL source cannot be lexed or parsed. */
public class ParseException extends RuntimeException {
	public ParseException(String message) {
		super(message);
	}
}
