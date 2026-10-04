package com.cresora.utilities.compiler;

/**
 * A single lexical token.
 *
 * @param type        the token kind
 * @param lexeme      the raw text (for strings, the unquoted content)
 * @param line        1-based line number the token starts on
 * @param startOffset inclusive character offset into the source
 * @param endOffset   exclusive character offset into the source
 */
public record Token(TokenType type, String lexeme, int line, int startOffset, int endOffset) {
}
