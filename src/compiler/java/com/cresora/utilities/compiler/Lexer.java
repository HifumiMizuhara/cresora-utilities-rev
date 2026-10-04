package com.cresora.utilities.compiler;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** Turns Cresora DSL source into a flat token list. Java port of the original {@code Lexer.kt}. */
public final class Lexer {
	private static final Map<String, TokenType> KEYWORDS = Map.ofEntries(
			Map.entry("weapon", TokenType.KEYWORD_WEAPON),
			Map.entry("stats", TokenType.KEYWORD_STATS),
			Map.entry("skill", TokenType.KEYWORD_SKILL),
			Map.entry("buff", TokenType.KEYWORD_BUFF),
			Map.entry("translations", TokenType.KEYWORD_TRANSLATIONS),
			Map.entry("sub_skill", TokenType.KEYWORD_SUB_SKILL),
			Map.entry("icon", TokenType.KEYWORD_ICON),
			Map.entry("dictionary", TokenType.KEYWORD_DICTIONARY),
			Map.entry("texture", TokenType.KEYWORD_TEXTURE)
	);

	private final String source;
	private final List<Token> tokens = new ArrayList<>();
	private int start = 0;
	private int current = 0;
	private int line = 1;

	public Lexer(String source) {
		this.source = source;
	}

	public List<Token> scanTokens() {
		while (!isAtEnd()) {
			start = current;
			scanToken();
		}
		tokens.add(new Token(TokenType.EOF, "", line, current, current));
		return tokens;
	}

	private void scanToken() {
		char c = advance();
		switch (c) {
			case '{' -> addToken(TokenType.LEFT_BRACE);
			case '}' -> addToken(TokenType.RIGHT_BRACE);
			case ',' -> addToken(TokenType.COMMA);
			case '(' -> addToken(TokenType.LEFT_PAREN);
			case ')' -> addToken(TokenType.RIGHT_PAREN);
			case '[' -> addToken(TokenType.LEFT_BRACKET);
			case ']' -> addToken(TokenType.RIGHT_BRACKET);
			case '.' -> addToken(TokenType.DOT);
			case ';' -> addToken(TokenType.SEMICOLON);
			case '?' -> {
				if (match(':')) {
					addToken(TokenType.OPERATOR, "?:");
				} else if (match('.')) {
					addToken(TokenType.OPERATOR, "?.");
				} else {
					addToken(TokenType.OPERATOR);
				}
			}
			case '-' -> addToken(TokenType.OPERATOR, match('>') ? "->" : "-");
			case ':' -> {
				if (match(':')) {
					addToken(TokenType.OPERATOR, "::");
				} else {
					addToken(TokenType.COLON);
				}
			}
			case '!' -> {
				if (match('=')) {
					addToken(TokenType.OPERATOR, "!=");
				} else if (match('!')) {
					addToken(TokenType.OPERATOR, "!!");
				} else {
					addToken(TokenType.OPERATOR);
				}
			}
			case '@' -> addToken(TokenType.OPERATOR);
			case '<' -> addToken(TokenType.OPERATOR, match('=') ? "<=" : "<");
			case '>' -> addToken(TokenType.OPERATOR, match('=') ? ">=" : ">");
			case '=' -> addToken(TokenType.OPERATOR, match('=') ? "==" : "=");
			case '&' -> addToken(TokenType.OPERATOR, match('&') ? "&&" : "&");
			case '|' -> addToken(TokenType.OPERATOR, match('|') ? "||" : "|");
			case '/' -> {
				if (match('/')) {
					while (peek() != '\n' && !isAtEnd()) {
						advance();
					}
				} else if (match('*')) {
					scanBlockComment();
				} else {
					addToken(TokenType.OPERATOR);
				}
			}
			case '+', '*', '%' -> addToken(TokenType.OPERATOR);
			case ' ', '\r', '\t' -> {
			}
			case '\n' -> line++;
			case '"' -> string();
			default -> {
				if (Character.isDigit(c)) {
					number();
				} else if (Character.isLetter(c) || c == '_') {
					identifier();
				} else {
					throw new ParseException("Unexpected character '" + c + "' at line " + line);
				}
			}
		}
	}

	private boolean match(char expected) {
		if (isAtEnd()) {
			return false;
		}
		if (source.charAt(current) != expected) {
			return false;
		}
		current++;
		return true;
	}

	private void identifier() {
		while (Character.isLetterOrDigit(peek()) || peek() == '_') {
			advance();
		}
		String text = source.substring(start, current);
		addToken(KEYWORDS.getOrDefault(text, TokenType.IDENTIFIER));
	}

	private void number() {
		while (Character.isDigit(peek())) {
			advance();
		}
		if (peek() == '.' && Character.isDigit(peekNext())) {
			advance();
			while (Character.isDigit(peek())) {
				advance();
			}
		}
		char suffix = peek();
		if (suffix == 'L' || suffix == 'f' || suffix == 'd' || suffix == 'F' || suffix == 'D') {
			advance();
		}
		addToken(TokenType.NUMBER);
	}

	private void string() {
		while (peek() != '"' && !isAtEnd()) {
			if (peek() == '\n') {
				line++;
			}
			advance();
		}
		if (isAtEnd()) {
			throw new ParseException("Unterminated string at line " + line);
		}
		advance();
		String value = source.substring(start + 1, current - 1);
		addToken(TokenType.STRING, value);
	}

	private void scanBlockComment() {
		while (!isAtEnd()) {
			if (peek() == '*' && peekNext() == '/') {
				advance();
				advance();
				return;
			}
			if (peek() == '\n') {
				line++;
			}
			advance();
		}
	}

	private boolean isAtEnd() {
		return current >= source.length();
	}

	private char advance() {
		return source.charAt(current++);
	}

	private char peek() {
		return isAtEnd() ? '\u0000' : source.charAt(current);
	}

	private char peekNext() {
		return current + 1 >= source.length() ? '\u0000' : source.charAt(current + 1);
	}

	private void addToken(TokenType type) {
		addToken(type, source.substring(start, current));
	}

	private void addToken(TokenType type, String lexeme) {
		tokens.add(new Token(type, lexeme, line, start, current));
	}
}
