package br.com.davidbuzatto.cprl.ide.gui;

import javax.swing.text.Segment;
import org.fife.ui.rsyntaxtextarea.AbstractTokenMaker;
import org.fife.ui.rsyntaxtextarea.RSyntaxUtilities;
import org.fife.ui.rsyntaxtextarea.Token;
import org.fife.ui.rsyntaxtextarea.TokenMap;

/**
 * Token maker for the CPRL programming language.
 *
 * @author Prof. Dr. David Buzatto
 */
public class CPRLTokenMaker extends AbstractTokenMaker {

    private int currentTokenStart;
    private int currentTokenType;

    @Override
    public TokenMap getWordsToHighlight() {

        TokenMap tokenMap = new TokenMap();

        // Reserved words
        tokenMap.put( "and",       Token.RESERVED_WORD );
        tokenMap.put( "array",     Token.RESERVED_WORD );
        tokenMap.put( "begin",     Token.RESERVED_WORD );
        tokenMap.put( "const",     Token.RESERVED_WORD );
        tokenMap.put( "else",      Token.RESERVED_WORD );
        tokenMap.put( "elsif",     Token.RESERVED_WORD );
        tokenMap.put( "end",       Token.RESERVED_WORD );
        tokenMap.put( "exit",      Token.RESERVED_WORD );
        tokenMap.put( "function",  Token.RESERVED_WORD );
        tokenMap.put( "if",        Token.RESERVED_WORD );
        tokenMap.put( "is",        Token.RESERVED_WORD );
        tokenMap.put( "loop",      Token.RESERVED_WORD );
        tokenMap.put( "mod",       Token.RESERVED_WORD );
        tokenMap.put( "not",       Token.RESERVED_WORD );
        tokenMap.put( "of",        Token.RESERVED_WORD );
        tokenMap.put( "or",        Token.RESERVED_WORD );
        tokenMap.put( "procedure", Token.RESERVED_WORD );
        tokenMap.put( "read",      Token.RESERVED_WORD );
        tokenMap.put( "return",    Token.RESERVED_WORD );
        tokenMap.put( "then",      Token.RESERVED_WORD );
        tokenMap.put( "type",      Token.RESERVED_WORD );
        tokenMap.put( "var",       Token.RESERVED_WORD );
        tokenMap.put( "when",      Token.RESERVED_WORD );
        tokenMap.put( "while",     Token.RESERVED_WORD );
        tokenMap.put( "write",     Token.RESERVED_WORD );
        tokenMap.put( "writeln",   Token.RESERVED_WORD );
        
        // booleans
        tokenMap.put( "true",      Token.LITERAL_BOOLEAN );
        tokenMap.put( "false",     Token.LITERAL_BOOLEAN );

        // Built-in type names
        tokenMap.put( "Boolean", Token.DATA_TYPE );
        tokenMap.put( "Char",    Token.DATA_TYPE );
        tokenMap.put( "Integer", Token.DATA_TYPE );

        return tokenMap;

    }

    @Override
    public Token getTokenList( Segment text, int startTokenType, int startOffset ) {

        resetTokenList();

        char[] array = text.array;
        int offset = text.offset;
        int count = text.count;
        int end = offset + count;

        int newStartOffset = startOffset - offset;

        currentTokenStart = offset;
        currentTokenType = startTokenType;

        for ( int i = offset; i < end; i++ ) {

            char c = array[i];

            switch ( currentTokenType ) {

                case Token.NULL:

                    currentTokenStart = i;

                    switch ( c ) {

                        case ' ':
                        case '\t':
                            currentTokenType = Token.WHITESPACE;
                            break;

                        case '"':
                            currentTokenType = Token.LITERAL_STRING_DOUBLE_QUOTE;
                            break;

                        case '\'':
                            currentTokenType = Token.LITERAL_CHAR;
                            break;

                        case '/':
                            if ( i + 1 < end && array[i + 1] == '/' ) {
                                currentTokenType = Token.COMMENT_EOL;
                            } else {
                                addToken( text, i, i, Token.OPERATOR, newStartOffset + i );
                                currentTokenType = Token.NULL;
                            }
                            break;

                        // Separators — emitted immediately as single-char tokens
                        case '(':
                        case ')':
                        case '[':
                        case ']':
                        case ',':
                        case ';':
                        case '.':
                            addToken( text, i, i, Token.SEPARATOR, newStartOffset + i );
                            currentTokenType = Token.NULL;
                            break;

                        // Single-char operators
                        case '+':
                        case '-':
                        case '*':
                        case '=':
                            addToken( text, i, i, Token.OPERATOR, newStartOffset + i );
                            currentTokenType = Token.NULL;
                            break;

                        // Potentially two-char operators: < <=
                        case '<':
                            if ( i + 1 < end && array[i + 1] == '=' ) {
                                addToken( text, i, i + 1, Token.OPERATOR, newStartOffset + i );
                                i++;
                            } else {
                                addToken( text, i, i, Token.OPERATOR, newStartOffset + i );
                            }
                            currentTokenType = Token.NULL;
                            break;

                        // Potentially two-char operators: > >=
                        case '>':
                            if ( i + 1 < end && array[i + 1] == '=' ) {
                                addToken( text, i, i + 1, Token.OPERATOR, newStartOffset + i );
                                i++;
                            } else {
                                addToken( text, i, i, Token.OPERATOR, newStartOffset + i );
                            }
                            currentTokenType = Token.NULL;
                            break;

                        // Two-char operator: !=
                        case '!':
                            if ( i + 1 < end && array[i + 1] == '=' ) {
                                addToken( text, i, i + 1, Token.OPERATOR, newStartOffset + i );
                                i++;
                            } else {
                                addToken( text, i, i, Token.IDENTIFIER, newStartOffset + i );
                            }
                            currentTokenType = Token.NULL;
                            break;

                        // Single-char operator ':' or two-char ':='
                        case ':':
                            if ( i + 1 < end && array[i + 1] == '=' ) {
                                addToken( text, i, i + 1, Token.OPERATOR, newStartOffset + i );
                                i++;
                            } else {
                                addToken( text, i, i, Token.OPERATOR, newStartOffset + i );
                            }
                            currentTokenType = Token.NULL;
                            break;

                        default:
                            if ( RSyntaxUtilities.isDigit( c ) ) {
                                currentTokenType = Token.LITERAL_NUMBER_DECIMAL_INT;
                            } else if ( RSyntaxUtilities.isLetter( c ) ) {
                                currentTokenType = Token.IDENTIFIER;
                            } else {
                                // Unknown character — emit as single-char identifier
                                addToken( text, i, i, Token.IDENTIFIER, newStartOffset + i );
                                currentTokenType = Token.NULL;
                            }
                            break;

                    } // End of switch (c)

                    break;

                case Token.WHITESPACE:

                    switch ( c ) {

                        case ' ':
                        case '\t':
                            break;

                        default:
                            addToken( text, currentTokenStart, i - 1, Token.WHITESPACE, newStartOffset + currentTokenStart );
                            i--;
                            currentTokenType = Token.NULL;
                            break;

                    }

                    break;

                case Token.IDENTIFIER:

                    // CPRL identifiers: letter ( letter | digit )*
                    if ( !RSyntaxUtilities.isLetterOrDigit( c ) ) {
                        int ttype = wordsToHighlight.get( text.array, currentTokenStart, i - 1 );
                        addToken( text, currentTokenStart, i - 1,
                                  ttype != -1 ? ttype : Token.IDENTIFIER,
                                  newStartOffset + currentTokenStart );
                        i--;
                        currentTokenType = Token.NULL;
                    }

                    break;

                case Token.LITERAL_NUMBER_DECIMAL_INT:

                    if ( !RSyntaxUtilities.isDigit( c ) ) {
                        addToken( text, currentTokenStart, i - 1, Token.LITERAL_NUMBER_DECIMAL_INT, newStartOffset + currentTokenStart );
                        i--;
                        currentTokenType = Token.NULL;
                    }

                    break;

                case Token.LITERAL_CHAR:

                    if ( c == '\\' ) {
                        i++; // skip the escaped char (e.g. \', \\, \n, \t ...)
                    } else if ( c == '\'' ) {
                        addToken( text, currentTokenStart, i, Token.LITERAL_CHAR, newStartOffset + currentTokenStart );
                        currentTokenType = Token.NULL;
                    }

                    break;

                case Token.LITERAL_STRING_DOUBLE_QUOTE:

                    if ( c == '\\' ) {
                        i++; // skip the escaped char (e.g. \", \\, \n, \t ...)
                    } else if ( c == '"' ) {
                        addToken( text, currentTokenStart, i, Token.LITERAL_STRING_DOUBLE_QUOTE, newStartOffset + currentTokenStart );
                        currentTokenType = Token.NULL;
                    }

                    break;

                case Token.COMMENT_EOL:
                    i = end - 1;
                    addToken( text, currentTokenStart, i, Token.COMMENT_EOL, newStartOffset + currentTokenStart );
                    currentTokenType = Token.NULL;
                    break;

            } // End of switch (currentTokenType)

        } // End of for (int i = offset; i < end; i++)

        // Handle tokens that reach the end of the line
        switch ( currentTokenType ) {

            // Unclosed string/char literals carry over to the next line
            case Token.LITERAL_STRING_DOUBLE_QUOTE:
            case Token.LITERAL_CHAR:
                addToken( text, currentTokenStart, end - 1, currentTokenType, newStartOffset + currentTokenStart );
                break;

            case Token.NULL:
                addNullToken();
                break;

            default:
                int finalType = currentTokenType;
                if ( currentTokenType == Token.IDENTIFIER ) {
                    int ttype = wordsToHighlight.get( text.array, currentTokenStart, end - 1 );
                    if ( ttype != -1 ) {
                        finalType = ttype;
                    }
                }
                addToken( text, currentTokenStart, end - 1, finalType, newStartOffset + currentTokenStart );
                addNullToken();
                break;

        }

        return firstToken;

    }

}
