import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class Scanner {
  private static final Map<String, TokenType> KEYWORDS =
      new HashMap<>();

  static {
    KEYWORDS.put("and", TokenType.AND);
    KEYWORDS.put("class", TokenType.CLASS);
    KEYWORDS.put("else", TokenType.ELSE);
    KEYWORDS.put("false", TokenType.FALSE);
    KEYWORDS.put("for", TokenType.FOR);
    KEYWORDS.put("fun", TokenType.FUN);
    KEYWORDS.put("if", TokenType.IF);
    KEYWORDS.put("nil", TokenType.NIL);
    KEYWORDS.put("or", TokenType.OR);
    KEYWORDS.put("print", TokenType.PRINT);
    KEYWORDS.put("return", TokenType.RETURN);
    KEYWORDS.put("super", TokenType.SUPER);
    KEYWORDS.put("this", TokenType.THIS);
    KEYWORDS.put("true", TokenType.TRUE);
    KEYWORDS.put("var", TokenType.VAR);
    KEYWORDS.put("while", TokenType.WHILE);
  }

  private final String source;
  private final List<Token> tokens = new ArrayList<>();

  private int start = 0;
  private int current = 0;
  private int line = 1;

  public Scanner(String source) {
    this.source = source;
  }

  public List<Token> scanTokens() {
    while (!isAtEnd()) {
      start = current;
      scanToken();
    }

    tokens.add(new Token(TokenType.EOF, "", null, line));
    return tokens;
  }

  private void scanToken() {
    char c = advance();

    switch (c) {
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
          // Ignore characters until the end of the line.
          while (peek() != '\n' && !isAtEnd()) {
            advance();
          }
        } else if (match('*')) {
          // The opening "/*" has already been consumed.
          blockComment();
        } else {
          addToken(TokenType.SLASH);
        }
      }

      case ' ', '\r', '\t' -> {
        // Ignore ordinary whitespace.
      }

      case '\n' -> line++;

      case '"' -> string();

      default -> {
        if (isDigit(c)) {
          number();
        } else if (isIdentifierStart(c)) {
          identifier();
        } else {
          Lox.error(line, "Unexpected character: " + c);
        }
      }
    }
  }

  /*
   * Scans a C-style block comment.
   *
   * The nesting counter starts at one because scanToken() already
   * consumed the first opening delimiter.
   */
  private void blockComment() {
    int nesting = 1;

    while (nesting > 0 && !isAtEnd()) {
      if (peek() == '/' && peekNext() == '*') {
        advance();
        advance();
        nesting++;
      } else if (peek() == '*' && peekNext() == '/') {
        advance();
        advance();
        nesting--;
      } else {
        if (peek() == '\n') {
          line++;
        }

        advance();
      }
    }

    if (nesting > 0) {
      Lox.error(line, "Unterminated block comment.");
    }
  }

  private void identifier() {
    while (isIdentifierPart(peek())) {
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

    // Look for a decimal portion.
    if (peek() == '.' && isDigit(peekNext())) {
      advance();

      while (isDigit(peek())) {
        advance();
      }
    }

    String numberText = source.substring(start, current);
    double value = Double.parseDouble(numberText);

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

    // Consume the closing quotation mark.
    advance();

    String value = source.substring(start + 1, current - 1);
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
    if (isAtEnd()) {
      return '\0';
    }

    return source.charAt(current);
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

  private static boolean isDigit(char c) {
    return c >= '0' && c <= '9';
  }

  private static boolean isIdentifierStart(char c) {
    return c == '_' || Character.isLetter(c);
  }

  private static boolean isIdentifierPart(char c) {
    return isIdentifierStart(c) || isDigit(c);
  }

  private void addToken(TokenType type) {
    addToken(type, null);
  }

  private void addToken(TokenType type, Object literal) {
    String lexeme = source.substring(start, current);

    tokens.add(new Token(
        type,
        lexeme,
        literal,
        line
    ));
  }
}