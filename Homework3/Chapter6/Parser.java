package Chapter6;

import java.util.List;

import static Chapter6.TokenType.*;

public final class Parser {
  private static final class ParseError
      extends RuntimeException {
  }

  private final List<Token> tokens;
  private int current = 0;

  public Parser(List<Token> tokens) {
    this.tokens = tokens;
  }

  public Expr parse() {
    try {
      return expression();
    } catch (ParseError error) {
      return null;
    }
  }

  /*
   * expression -> comma
   *
   * Comma is the lowest-precedence expression.
   */
  private Expr expression() {
    return comma();
  }

  /*
   * comma -> equality ("," equality)*
   *
   * The loop makes comma left-associative.
   */
  private Expr comma() {
    Expr expression = conditional();

    while (match(COMMA)) {
      Token operator = previous();
      Expr right = conditional();

      expression = new Expr.Binary(
          expression,
          operator,
          right
      );
    }

      return expression;
  }

  private Expr conditional() {
  Expr expression = equality();

  if (match(QUESTION)) {
    Expr thenBranch = expression();

    consume(
        COLON,
        "Expect ':' after then branch."
    );

    Expr elseBranch = conditional();

    expression = new Expr.Conditional(
        expression,
        thenBranch,
        elseBranch
    );
  }

  return expression;
}


  private Expr equality() {
    Expr expression = comparison();

    while (match(BANG_EQUAL, EQUAL_EQUAL)) {
      Token operator = previous();
      Expr right = comparison();

      expression = new Expr.Binary(
          expression,
          operator,
          right
      );
    }

    return expression;
  }

  private Expr comparison() {
    Expr expression = term();

    while (match(
        GREATER,
        GREATER_EQUAL,
        LESS,
        LESS_EQUAL
    )) {
      Token operator = previous();
      Expr right = term();

      expression = new Expr.Binary(
          expression,
          operator,
          right
      );
    }

    return expression;
  }

  private Expr term() {
    Expr expression = factor();

    while (match(MINUS, PLUS)) {
      Token operator = previous();
      Expr right = factor();

      expression = new Expr.Binary(
          expression,
          operator,
          right
      );
    }

    return expression;
  }

  private Expr factor() {
    Expr expression = unary();

    while (match(SLASH, STAR)) {
      Token operator = previous();
      Expr right = unary();

      expression = new Expr.Binary(
          expression,
          operator,
          right
      );
    }

    return expression;
  }

  private Expr unary() {
    if (match(BANG, MINUS)) {
      Token operator = previous();
      Expr right = unary();

      return new Expr.Unary(operator, right);
    }

    return primary();
  }

  private Expr primary() {
    if (match(FALSE)) {
      return new Expr.Literal(false);
    }

    if (match(TRUE)) {
      return new Expr.Literal(true);
    }

    if (match(NIL)) {
      return new Expr.Literal(null);
    }

    if (match(NUMBER, STRING)) {
      return new Expr.Literal(previous().literal);
    }

    if (match(LEFT_PAREN)) {
      Expr expression = expression();

      consume(
          RIGHT_PAREN,
          "Expect ')' after expression."
      );

      return new Expr.Grouping(expression);
    }

    // Equality operators without a left operand.
  if (match(BANG_EQUAL, EQUAL_EQUAL)) {
    error(
        previous(),
        "Missing left-hand operand."
    );

    equality();
    return null;
  }

  // Comparison operators without a left operand.
  if (match(
      GREATER,
      GREATER_EQUAL,
      LESS,
      LESS_EQUAL
  )) {
    error(
        previous(),
        "Missing left-hand operand."
    );

    comparison();
    return null;
  }

  // Addition without a left operand.
  if (match(PLUS)) {
    error(
        previous(),
        "Missing left-hand operand."
    );

    term();
    return null;
  }

  // Multiplication or division without a left operand.
  if (match(SLASH, STAR)) {
    error(
        previous(),
        "Missing left-hand operand."
    );

    factor();
    return null;
  }

    throw error(
        peek(),
        "Expect expression."
    );
  }

  private boolean match(TokenType... types) {
    for (TokenType type : types) {
      if (check(type)) {
        advance();
        return true;
      }
    }

    return false;
  }

  private Token consume(
      TokenType type,
      String message
  ) {
    if (check(type)) {
      return advance();
    }

    throw error(peek(), message);
  }

  private boolean check(TokenType type) {
    if (isAtEnd()) {
      return type == EOF;
    }

    return peek().type == type;
  }

  private Token advance() {
    if (!isAtEnd()) {
      current++;
    }

    return previous();
  }

  private boolean isAtEnd() {
    return peek().type == EOF;
  }

  private Token peek() {
    return tokens.get(current);
  }

  private Token previous() {
    return tokens.get(current - 1);
  }

  private ParseError error(
      Token token,
      String message
  ) {
    System.err.println(
        "[line " + token.line + "] Error at '"
            + token.lexeme + "': " + message
    );

    return new ParseError();
  }
}