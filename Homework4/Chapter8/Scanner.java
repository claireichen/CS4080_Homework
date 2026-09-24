package Chapter8;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class Scanner {
  private static final Map<String, TokenType> KEYWORDS =
      new HashMap<>();

  static {
    KEYWORDS.put("false", TokenType.FALSE);
    KEYWORDS.put("nil", TokenType.NIL);
    KEYWORDS.put("print", TokenType.PRINT);
    KEYWORDS.put("true", TokenType.TRUE);
    KEYWORDS.put("var", TokenType.VAR);
  }

  private final String source;
  private final List<Token> tokens =
      new ArrayList<>();

  private int start;
  private int current;
  private int line = 1;

  public Scanner(String source) {
    this.source = source;
  }

  public List<Token> scanTokens() {
    while (!isAtEnd()) {
      start = current;
      scanToken();
    }

    tokens.add(
        new Token(TokenType.EOF, "", null, line)
    );

    return tokens;
  }

  private void scanToken() {
    char character = advance();

    switch (character) {
      case '(' -> addToken(TokenType.LEFT_PAREN);
      case ')' -> addToken(TokenType.RIGHT_PAREN);
      case '{' -> addToken(TokenType.LEFT_BRACE);
      case '}' -> addToken(TokenType.RIGHT_BRACE);
      case ',' -> addToken(TokenType.COMMA);
      case '.' -> addToken(TokenType.DOT);
      case '-' -> addToken(TokenType.MINUS);
      case '+' -> addToken(TokenType.PLUS);
      case ';' -> addToken(TokenType.SEMICOLON);
      case '*' -> addToken(TokenType.STAR);

      case '!' -> addToken(
          match('=')
              ? TokenType.BANG_EQUAL
              : TokenType.BANG
      );

      case '=' -> addToken(
          match('=')
              ? TokenType.EQUAL_EQUAL
              : TokenType.EQUAL
      );

      case '<' -> addToken(
          match('=')
              ? TokenType.LESS_EQUAL
              : TokenType.LESS
      );

      case '>' -> addToken(
          match('=')
              ? TokenType.GREATER_EQUAL
              : TokenType.GREATER
      );

      case '/' -> {
        if (match('/')) {
          while (peek() != '\n' && !isAtEnd()) {
            advance();
          }
        } else {
          addToken(TokenType.SLASH);
        }
      }

      case ' ', '\r', '\t' -> {
        // Ignore whitespace.
      }

      case '\n' -> line++;
      case '"' -> string();

      default -> {
        if (isDigit(character)) {
          number();
        } else if (isAlpha(character)) {
          identifier();
        } else {
          Lox.error(line, "Unexpected character.");
        }
      }
    }
  }

  private void identifier() {
    while (isAlphaNumeric(peek())) {
      advance();
    }

    String text = source.substring(start, current);

    TokenType type = KEYWORDS.getOrDefault(
        text,
        TokenType.IDENTIFIER
    );

    addToken(type);
  }

  private void number() {
    while (isDigit(peek())) {
      advance();
    }

    if (peek() == '.' && isDigit(peekNext())) {
      advance();

      while (isDigit(peek())) {
        advance();
      }
    }

    double value = Double.parseDouble(
        source.substring(start, current)
    );

    addToken(TokenType.NUMBER, value);
  }

  private void string() {
    while (peek() != '"' && !isAtEnd()) {
      if (peek() == '\n') {
        line++;
      }

      advance();
    }

    if (isAtEnd()) {
      Lox.error(line, "Unterminated string.");
      return;
    }

    advance();

    String value =
        source.substring(start + 1, current - 1);

    addToken(TokenType.STRING, value);
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

  private char peek() {
    return isAtEnd()
        ? '\0'
        : source.charAt(current);
  }

  private char peekNext() {
    if (current + 1 >= source.length()) {
      return '\0';
    }

    return source.charAt(current + 1);
  }

  private char advance() {
    return source.charAt(current++);
  }

  private boolean isAtEnd() {
    return current >= source.length();
  }

  private boolean isDigit(char character) {
    return character >= '0' && character <= '9';
  }

  private boolean isAlpha(char character) {
    return character >= 'a' && character <= 'z'
        || character >= 'A' && character <= 'Z'
        || character == '_';
  }

  private boolean isAlphaNumeric(char character) {
    return isAlpha(character) || isDigit(character);
  }

  private void addToken(TokenType type) {
    addToken(type, null);
  }

  private void addToken(
      TokenType type,
      Object literal
  ) {
    String lexeme =
        source.substring(start, current);

    tokens.add(
        new Token(type, lexeme, literal, line)
    );
  }
}