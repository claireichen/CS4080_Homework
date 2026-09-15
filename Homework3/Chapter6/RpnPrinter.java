package Chapter6;

public final class RpnPrinter
    implements Expr.Visitor<String> {

  public String print(Expr expression) {
    return expression.accept(this);
  }

  @Override
  public String visitBinaryExpr(Expr.Binary expression) {
    return expression.left.accept(this)
        + " "
        + expression.right.accept(this)
        + " "
        + expression.operator.lexeme;
  }

  @Override
  public String visitConditionalExpr(
      Expr.Conditional expression
  ) {
    return expression.condition.accept(this)
        + " "
        + expression.thenBranch.accept(this)
        + " "
        + expression.elseBranch.accept(this)
        + " ?:";
  }

  @Override
  public String visitGroupingExpr(Expr.Grouping expression) {
    return expression.expression.accept(this);
  }

  @Override
  public String visitLiteralExpr(Expr.Literal expression) {
    if (expression.value == null) {
      return "nil";
    }

    if (expression.value instanceof Double number
        && number == Math.rint(number)) {
      return Long.toString(number.longValue());
    }

    return expression.value.toString();
  }

  @Override
  public String visitUnaryExpr(Expr.Unary expression) {
    String operator = expression.operator.lexeme;

    // Unary and binary minus need different RPN symbols.
    if (expression.operator.type == TokenType.MINUS) {
      operator = "neg";
    }

    return expression.right.accept(this)
        + " "
        + operator;
  }

  public static void main(String[] args) {
    Expr left = new Expr.Binary(
        new Expr.Literal(1.0),
        new Token(TokenType.PLUS, "+", null, 1),
        new Expr.Literal(2.0)
    );

    Expr right = new Expr.Binary(
        new Expr.Literal(4.0),
        new Token(TokenType.MINUS, "-", null, 1),
        new Expr.Literal(3.0)
    );

    Expr expression = new Expr.Binary(
        new Expr.Grouping(left),
        new Token(TokenType.STAR, "*", null, 1),
        new Expr.Grouping(right)
    );

    System.out.println(
        new RpnPrinter().print(expression)
    );
  }
}