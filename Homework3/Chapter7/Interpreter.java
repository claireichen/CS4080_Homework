package Chapter7;

public final class Interpreter
    implements Expr.Visitor<Object> {

  public Object evaluate(Expr expression) {
    return expression.accept(this);
  }

  @Override
  public Object visitLiteralExpr(
      Expr.Literal expression
  ) {
    return expression.value;
  }

  @Override
  public Object visitGroupingExpr(
      Expr.Grouping expression
  ) {
    return evaluate(expression.expression);
  }

  @Override
  public Object visitUnaryExpr(
      Expr.Unary expression
  ) {
    Object right = evaluate(expression.right);

    return switch (expression.operator.type) {
      case MINUS -> {
        checkNumberOperand(
            expression.operator,
            right
        );

        yield -(double) right;
      }

      case BANG -> !isTruthy(right);

      default -> throw new RuntimeError(
          expression.operator,
          "Unknown unary operator."
      );
    };
  }

  @Override
  public Object visitBinaryExpr(
      Expr.Binary expression
  ) {
    Object left = evaluate(expression.left);
    Object right = evaluate(expression.right);

    return switch (expression.operator.type) {
      case GREATER ->
          compare(expression.operator, left, right) > 0;

      case GREATER_EQUAL ->
          compare(expression.operator, left, right) >= 0;

      case LESS ->
          compare(expression.operator, left, right) < 0;

      case LESS_EQUAL ->
          compare(expression.operator, left, right) <= 0;

      case MINUS -> {
        checkNumberOperands(
            expression.operator,
            left,
            right
        );

        yield (double) left - (double) right;
      }

      case SLASH -> {
        checkNumberOperands(
            expression.operator,
            left,
            right
        );

        yield (double) left / (double) right;
      }

      case STAR -> {
        checkNumberOperands(
            expression.operator,
            left,
            right
        );

        yield (double) left * (double) right;
      }

      case PLUS -> {
        if (left instanceof Double
            && right instanceof Double) {
          yield (double) left + (double) right;
        }

        if (left instanceof String
            && right instanceof String) {
          yield (String) left + (String) right;
        }

        throw new RuntimeError(
            expression.operator,
            "Operands must be two numbers or two strings."
        );
      }

      case BANG_EQUAL -> !isEqual(left, right);
      case EQUAL_EQUAL -> isEqual(left, right);

      default -> throw new RuntimeError(
          expression.operator,
          "Unknown binary operator."
      );
    };
  }

  @Override
  public Object visitConditionalExpr(
      Expr.Conditional expression
  ) {
    Object condition = evaluate(expression.condition);

    if (isTruthy(condition)) {
      return evaluate(expression.thenBranch);
    }

    return evaluate(expression.elseBranch);
  }

  /*
   * Numbers are compared numerically.
   * Strings are compared lexicographically.
   * Mixed types are rejected.
   */
  private int compare(
      Token operator,
      Object left,
      Object right
  ) {
    if (left instanceof Double leftNumber
        && right instanceof Double rightNumber) {
      return Double.compare(
          leftNumber,
          rightNumber
      );
    }

    if (left instanceof String leftString
        && right instanceof String rightString) {
      return leftString.compareTo(rightString);
    }

    throw new RuntimeError(
        operator,
        "Operands must be two numbers or two strings."
    );
  }

  private void checkNumberOperand(
      Token operator,
      Object operand
  ) {
    if (operand instanceof Double) {
      return;
    }

    throw new RuntimeError(
        operator,
        "Operand must be a number."
    );
  }

  private void checkNumberOperands(
      Token operator,
      Object left,
      Object right
  ) {
    if (left instanceof Double
        && right instanceof Double) {
      return;
    }

    throw new RuntimeError(
        operator,
        "Operands must be numbers."
    );
  }

  private boolean isTruthy(Object value) {
    if (value == null) {
      return false;
    }

    if (value instanceof Boolean booleanValue) {
      return booleanValue;
    }

    return true;
  }

  private boolean isEqual(
      Object left,
      Object right
  ) {
    if (left == null && right == null) {
      return true;
    }

    if (left == null) {
      return false;
    }

    return left.equals(right);
  }
}