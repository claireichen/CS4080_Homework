package Chapter10;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class Parser {
  private static final class ParseError
      extends RuntimeException {
  }

  private final List<Token> tokens;
  private int current;
  private int loopDepth;

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
      /*
       * A named function is a declaration.
       * If FUN is not followed by an identifier,
       * it will be parsed later as an anonymous
       * function expression.
       */
      if (check(TokenType.FUN)
          && checkNext(TokenType.IDENTIFIER)) {
        advance();
        return function("function");
      }

      if (match(TokenType.VAR)) {
        return varDeclaration();
      }

      return statement();
    } catch (ParseError error) {
      synchronize();
      return null;
    }
  }

  private Stmt.Function function(String kind) {
    Token name = consume(
        TokenType.IDENTIFIER,
        "Expect " + kind + " name."
    );

    return new Stmt.Function(
        name,
        functionBody(kind)
    );
  }

  private Expr.Function functionBody(String kind) {
    consume(
        TokenType.LEFT_PAREN,
        "Expect '(' after " + kind + "."
    );

    List<Token> parameters = new ArrayList<>();

    if (!check(TokenType.RIGHT_PAREN)) {
      do {
        if (parameters.size() >= 255) {
          error(
              peek(),
              "Can't have more than 255 parameters."
          );
        }

        parameters.add(
            consume(
                TokenType.IDENTIFIER,
                "Expect parameter name."
            )
        );
      } while (match(TokenType.COMMA));
    }

    consume(
        TokenType.RIGHT_PAREN,
        "Expect ')' after parameters."
    );

    consume(
        TokenType.LEFT_BRACE,
        "Expect '{' before " + kind + " body."
    );

    return new Expr.Function(
        parameters,
        block()
    );
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
    if (match(TokenType.BREAK)) {
      return breakStatement();
    }

    if (match(TokenType.FOR)) {
      return forStatement();
    }

    if (match(TokenType.IF)) {
      return ifStatement();
    }

    if (match(TokenType.PRINT)) {
      return printStatement();
    }

    if (match(TokenType.RETURN)) {
      return returnStatement();
    }

    if (match(TokenType.WHILE)) {
      return whileStatement();
    }

    if (match(TokenType.LEFT_BRACE)) {
      return new Stmt.Block(block());
    }

    return expressionStatement();
  }

  private Stmt returnStatement() {
    Token keyword = previous();
    Expr value = null;

    if (!check(TokenType.SEMICOLON)) {
      value = expression();
    }

    consume(
        TokenType.SEMICOLON,
        "Expect ';' after return value."
    );

    return new Stmt.Return(
        keyword,
        value
    );
  }

  private Stmt breakStatement() {
    Token keyword = previous();

    if (loopDepth == 0) {
      error(
          keyword,
          "Must be inside a loop to use 'break'."
      );
    }

    consume(
        TokenType.SEMICOLON,
        "Expect ';' after 'break'."
    );

    return new Stmt.Break();
  }

  private Stmt forStatement() {
    consume(
        TokenType.LEFT_PAREN,
        "Expect '(' after 'for'."
    );

    Stmt initializer;

    if (match(TokenType.SEMICOLON)) {
      initializer = null;
    } else if (match(TokenType.VAR)) {
      initializer = varDeclaration();
    } else {
      initializer = expressionStatement();
    }

    Expr condition = null;

    if (!check(TokenType.SEMICOLON)) {
      condition = expression();
    }

    consume(
        TokenType.SEMICOLON,
        "Expect ';' after loop condition."
    );

    Expr increment = null;

    if (!check(TokenType.RIGHT_PAREN)) {
      increment = expression();
    }

    consume(
        TokenType.RIGHT_PAREN,
        "Expect ')' after for clauses."
    );

    Stmt body;

    try {
      loopDepth++;
      body = statement();
    } finally {
      loopDepth--;
    }

    if (increment != null) {
      body = new Stmt.Block(
          Arrays.asList(
              body,
              new Stmt.Expression(increment)
          )
      );
    }

    if (condition == null) {
      condition = new Expr.Literal(true);
    }

    body = new Stmt.While(
        condition,
        body
    );

    if (initializer != null) {
      body = new Stmt.Block(
          Arrays.asList(
              initializer,
              body
          )
      );
    }

    return body;
  }

  private Stmt ifStatement() {
    consume(
        TokenType.LEFT_PAREN,
        "Expect '(' after 'if'."
    );

    Expr condition = expression();

    consume(
        TokenType.RIGHT_PAREN,
        "Expect ')' after if condition."
    );

    Stmt thenBranch = statement();
    Stmt elseBranch = null;

    if (match(TokenType.ELSE)) {
      elseBranch = statement();
    }

    return new Stmt.If(
        condition,
        thenBranch,
        elseBranch
    );
  }

  private Stmt whileStatement() {
    consume(
        TokenType.LEFT_PAREN,
        "Expect '(' after 'while'."
    );

    Expr condition = expression();

    consume(
        TokenType.RIGHT_PAREN,
        "Expect ')' after condition."
    );

    try {
      loopDepth++;
      Stmt body = statement();

      return new Stmt.While(
          condition,
          body
      );
    } finally {
      loopDepth--;
    }
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
    Expr expression = or();

    if (match(TokenType.EQUAL)) {
      Token equals = previous();
      Expr value = assignment();

      if (expression instanceof Expr.Variable variable) {
        return new Expr.Assign(
            variable.name,
            value
        );
      }

      error(
          equals,
          "Invalid assignment target."
      );
    }

    return expression;
  }

  private Expr or() {
    Expr expression = and();

    while (match(TokenType.OR)) {
      Token operator = previous();
      Expr right = and();

      expression = new Expr.Logical(
          expression,
          operator,
          right
      );
    }

    return expression;
  }

  private Expr and() {
    Expr expression = equality();

    while (match(TokenType.AND)) {
      Token operator = previous();
      Expr right = equality();

      expression = new Expr.Logical(
          expression,
          operator,
          right
      );
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

      expression = new Expr.Binary(
          expression,
          operator,
          comparison()
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

      expression = new Expr.Binary(
          expression,
          operator,
          term()
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

      expression = new Expr.Binary(
          expression,
          operator,
          factor()
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

      expression = new Expr.Binary(
          expression,
          operator,
          unary()
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

      return new Expr.Unary(
          operator,
          unary()
      );
    }

    return call();
  }

  private Expr call() {
    Expr expression = primary();

    while (match(TokenType.LEFT_PAREN)) {
      expression = finishCall(expression);
    }

    return expression;
  }

  private Expr finishCall(Expr callee) {
    List<Expr> arguments = new ArrayList<>();

    if (!check(TokenType.RIGHT_PAREN)) {
      do {
        if (arguments.size() >= 255) {
          error(
              peek(),
              "Can't have more than 255 arguments."
          );
        }

        arguments.add(assignment());
      } while (match(TokenType.COMMA));
    }

    Token paren = consume(
        TokenType.RIGHT_PAREN,
        "Expect ')' after arguments."
    );

    return new Expr.Call(
        callee,
        paren,
        arguments
    );
  }

  private Expr primary() {
    /*
     * If FUN appears where an expression is
     * expected, parse an anonymous function.
     */
    if (match(TokenType.FUN)) {
      return functionBody("function");
    }

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
      return new Expr.Literal(
          previous().literal
      );
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
      return type == TokenType.EOF;
    }

    return peek().type == type;
  }

  private boolean checkNext(TokenType type) {
    if (current + 1 >= tokens.size()) {
      return false;
    }

    return tokens.get(current + 1).type == type;
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
      if (previous().type
          == TokenType.SEMICOLON) {
        return;
      }

      switch (peek().type) {
        case BREAK:
        case FOR:
        case FUN:
        case IF:
        case PRINT:
        case RETURN:
        case VAR:
        case WHILE:
          return;

        default:
          advance();
      }
    }
  }
}