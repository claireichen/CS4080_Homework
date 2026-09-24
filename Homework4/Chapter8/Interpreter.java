package Chapter8;

import java.util.List;

public final class Interpreter
    implements Expr.Visitor<Object>,
               Stmt.Visitor<Void> {
  private Environment environment =
      new Environment();

  public void interpret(List<Stmt> statements) {
    try {
      for (Stmt statement : statements) {
        if (statement != null) {
          execute(statement);
        }
      }
    } catch (RuntimeError error) {
      Lox.runtimeError(error);
    }
  }

  /*
   * This overload is used for a bare REPL expression.
   */
  public String interpret(Expr expression) {
    try {
      return stringify(evaluate(expression));
    } catch (RuntimeError error) {
      Lox.runtimeError(error);
      return null;
    }
  }

  private Object evaluate(Expr expression) {
    return expression.accept(this);
  }

  private void execute(Stmt statement) {
    statement.accept(this);
  }

  @Override
  public Object visitAssignExpr(
      Expr.Assign expression
  ) {
    Object value = evaluate(expression.value);

    environment.assign(
        expression.name,
        value
    );

    return value;
  }

  @Override
  public Object visitGroupingExpr(
      Expr.Grouping expression
  ) {
    return evaluate(expression.expression);
  }

  @Override
  public Object visitLiteralExpr(
      Expr.Literal expression
  ) {
    return expression.value;
  }

  @Override
  public Object visitVariableExpr(
      Expr.Variable expression
  ) {
    return environment.get(expression.name);
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
      case MINUS -> {
        checkNumbers(
            expression.operator,
            left,
            right
        );

        yield (double) left - (double) right;
      }

      case SLASH -> {
        checkNumbers(
            expression.operator,
            left,
            right
        );

        if ((double) right == 0.0) {
          throw new RuntimeError(
              expression.operator,
              "Cannot divide by zero."
          );
        }

        yield (double) left / (double) right;
      }

      case STAR -> {
        checkNumbers(
            expression.operator,
            left,
            right
        );

        yield (double) left * (double) right;
      }

      case PLUS -> {
        if (left instanceof String
            || right instanceof String) {
          yield stringify(left) + stringify(right);
        }

        checkNumbers(
            expression.operator,
            left,
            right
        );

        yield (double) left + (double) right;
      }

      case GREATER ->
          compare(
              expression.operator,
              left,
              right
          ) > 0;

      case GREATER_EQUAL ->
          compare(
              expression.operator,
              left,
              right
          ) >= 0;

      case LESS ->
          compare(
              expression.operator,
              left,
              right
          ) < 0;

      case LESS_EQUAL ->
          compare(
              expression.operator,
              left,
              right
          ) <= 0;

      case BANG_EQUAL -> !isEqual(left, right);
      case EQUAL_EQUAL -> isEqual(left, right);

      default -> throw new RuntimeError(
          expression.operator,
          "Unknown binary operator."
      );
    };
  }

  @Override
  public Void visitExpressionStmt(
      Stmt.Expression statement
  ) {
    evaluate(statement.expression);
    return null;
  }

  @Override
  public Void visitPrintStmt(
      Stmt.Print statement
  ) {
    Object value = evaluate(statement.expression);
    System.out.println(stringify(value));

    return null;
  }

  @Override
  public Void visitVarStmt(Stmt.Var statement) {
    Object value = null;

    if (statement.initializer != null) {
      value = evaluate(statement.initializer);
    }

    environment.define(
        statement.name.lexeme,
        value
    );

    return null;
  }

  @Override
  public Void visitBlockStmt(
      Stmt.Block statement
  ) {
    executeBlock(
        statement.statements,
        new Environment(environment)
    );

    return null;
  }

  private void executeBlock(
      List<Stmt> statements,
      Environment blockEnvironment
  ) {
    Environment previous = environment;

    try {
      environment = blockEnvironment;

      for (Stmt statement : statements) {
        if (statement != null) {
          execute(statement);
        }
      }
    } finally {
      environment = previous;
    }
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
    if (left == null) {
      return right == null;
    }

    return left.equals(right);
  }

  private void checkNumberOperand(
      Token operator,
      Object value
  ) {
    if (!(value instanceof Double)) {
      throw new RuntimeError(
          operator,
          "Operand must be a number."
      );
    }
  }

  private void checkNumbers(
      Token operator,
      Object left,
      Object right
  ) {
    if (!(left instanceof Double
        && right instanceof Double)) {
      throw new RuntimeError(
          operator,
          "Operands must be numbers."
      );
    }
  }

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

  private String stringify(Object value) {
    if (value == null) {
      return "nil";
    }

    if (value instanceof Double number) {
      String text = number.toString();

      if (text.endsWith(".0")) {
        return text.substring(
            0,
            text.length() - 2
        );
      }

      return text;
    }

    return value.toString();
  }
}