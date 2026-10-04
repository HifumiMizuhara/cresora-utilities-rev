package com.cresora.utilities.compiler;

/**
 * Token kinds produced by {@link Lexer}. Only the weapon-related keywords are kept;
 * the artifact/movement keywords of the original DSL are intentionally dropped.
 */
public enum TokenType {
	IDENTIFIER, NUMBER, STRING,
	LEFT_BRACE, RIGHT_BRACE, COLON, COMMA, LEFT_PAREN, RIGHT_PAREN,
	LEFT_BRACKET, RIGHT_BRACKET,
	DOT, OPERATOR, SEMICOLON,
	KEYWORD_WEAPON, KEYWORD_STATS, KEYWORD_SKILL, KEYWORD_BUFF, KEYWORD_TRANSLATIONS,
	KEYWORD_SUB_SKILL, KEYWORD_ICON, KEYWORD_DICTIONARY, KEYWORD_TEXTURE,
	EOF
}
