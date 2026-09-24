package Chapter8;

import java.util.ArrayList;
import java.util.List;

public final class Parser {
  private static final class ParseError
      extends RuntimeException {
  }

  private final List<Token> tokens;
  private int current;

  private boolean allowExpression;
  private boolean foundExpression;

  public Parser(List<Token> tokens) {
    this.tokens = tokens;
  }

  public List<Stmt> parse() {
    List<Stmt> statements = new ArrayList<>();

    while (!isAtEnd()) {
      statements.add(declaration());
    }

    return statements;
  }

  /*
   * Parses one REPL line. It returns either:
   *
   * - Expr for a bare expression without a semicolon.
   * - List<Stmt> for one or more statements.
   */
  public Object parseRepl() {
    allowExpression = true;
    foundExpression = false;

    List<Stmt> statements = new ArrayList<>();

    while (!isAtEnd()) {
      statements.add(declaration());

      if (foundExpression) {
        Stmt last =
            statements.get(statements.size() - 1);

        return ((Stmt.Expression) last).expression;
      }

      allowExpression = false;
    }

    return statements;
  }

  private Stmt declaration() {
    try {
      if (match(TokenType.VAR)) {
        return varDeclaration();
      }

      return statement();
    } catch (ParseError error) {
      synchronize();
      return null;
    }
  }

  private Stmt varDeclaration() {
    Token name = consume(
        TokenType.IDENTIFIER,
        "Expect variable name."
    );

    Expr initializer = null;

    if (match(TokenType.EQUAL)) {
      initializer = expression();
    }

    consume(
        TokenType.SEMICOLON,
        "Expect ';' after variable declaration."
    );

    return new Stmt.Var(name, initializer);
  }

  private Stmt statement() {
    if (match(TokenType.PRINT)) {
      return printStatement();
    }

    if (match(TokenType.LEFT_BRACE)) {
      return new Stmt.Block(block());
    }

    return expressionStatement();
  }

  private Stmt printStatement() {
    Expr value = expression();

    consume(
        TokenType.SEMICOLON,
        "Expect ';' after value."
    );

    return new Stmt.Print(value);
  }

  private Stmt expressionStatement() {
    Expr expression = expression();

    if (allowExpression && isAtEnd()) {
      foundExpression = true;
    } else {
      consume(
          TokenType.SEMICOLON,
          "Expect ';' after expression."
      );
    }

    return new Stmt.Expression(expression);
  }

  private List<Stmt> block() {
    List<Stmt> statements = new ArrayList<>();

    while (!check(TokenType.RIGHT_BRACE)
        && !isAtEnd()) {
      statements.add(declaration());
    }

    consume(
        TokenType.RIGHT_BRACE,
        "Expect '}' after block."
    );

    return statements;
  }

  private Expr expression() {
    return assignment();
  }

  private Expr assignment() {
    Expr expression = equality();

    if (match(TokenType.EQUAL)) {
      Token equals = previous();
      Expr value = assignment();

      if (expression instanceof Expr.Variable variable) {
        return new Expr.Assign(
            variable.name,
            value
        );
      }

      error(equals, "Invalid assignment target.");
    }

    return expression;
  }

  private Expr equality() {
    Expr expression = comparison();

    while (match(
        TokenType.BANG_EQUAL,
        TokenType.EQUAL_EQUAL
    )) {
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
        TokenType.GREATER,
        TokenType.GREATER_EQUAL,
        TokenType.LESS,
        TokenType.LESS_EQUAL
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

    while (match(
        TokenType.MINUS,
        TokenType.PLUS
    )) {
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

    while (match(
        TokenType.SLASH,
        TokenType.STAR
    )) {
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
    if (match(
        TokenType.BANG,
        TokenType.MINUS
    )) {
      Token operator = previous();
      Expr right = unary();

      return new Expr.Unary(operator, right);
    }

    return primary();
  }

  private Expr primary() {
    if (match(TokenType.FALSE)) {
      return new Expr.Literal(false);
    }

    if (match(TokenType.TRUE)) {
      return new Expr.Literal(true);
    }

    if (match(TokenType.NIL)) {
      return new Expr.Literal(null);
    }

    if (match(
        TokenType.NUMBER,
        TokenType.STRING
    )) {
      return new Expr.Literal(previous().literal);
    }

    if (match(TokenType.IDENTIFIER)) {
      return new Expr.Variable(previous());
    }

    if (match(TokenType.LEFT_PAREN)) {
      Expr expression = expression();

      consume(
          TokenType.RIGHT_PAREN,
          "Expect ')' after expression."
      );

      return new Expr.Grouping(expression);
    }

    throw error(peek(), "Expect expression.");
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
      return type == TokenType.EOF;
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
    return peek().type == TokenType.EOF;
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
    Lox.error(token, message);
    return new ParseError();
  }

  private void synchronize() {
    advance();

    while (!isAtEnd()) {
      if (previous().type == TokenType.SEMICOLON) {
        return;
      }

      if (peek().type == TokenType.PRINT
          || peek().type == TokenType.VAR) {
        return;
      }

      advance();
    }
  }
}