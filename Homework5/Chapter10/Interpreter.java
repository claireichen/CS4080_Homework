package Chapter10;

import java.util.ArrayList;
import java.util.List;

public final class Interpreter
    implements Expr.Visitor<Object>,
               Stmt.Visitor<Void> {

  private static final Object UNINITIALIZED =
      new Object();

  private static final class BreakException
      extends RuntimeException {
    BreakException() {
      super(null, null, false, false);
    }
  }

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
  public Object visitCallExpr(
      Expr.Call expression
  ) {
    Object callee = evaluate(expression.callee);
    List<Object> arguments = new ArrayList<>();

    for (Expr argument : expression.arguments) {
      arguments.add(evaluate(argument));
    }

    if (!(callee instanceof LoxCallable function)) {
      throw new RuntimeError(
          expression.paren,
          "Can only call functions."
      );
    }

    if (arguments.size() != function.arity()) {
      throw new RuntimeError(
          expression.paren,
          "Expected " + function.arity()
              + " arguments but got "
              + arguments.size() + "."
      );
    }

    return function.call(this, arguments);
  }

  @Override
  public Object visitFunctionExpr(
      Expr.Function expression
  ) {
    return new LoxFunction(
        null,
        expression,
        environment
    );
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
  public Object visitLogicalExpr(
      Expr.Logical expression
  ) {
    Object left = evaluate(expression.left);

    if (expression.operator.type == TokenType.OR) {
      if (isTruthy(left)) {
        return left;
      }
    } else if (!isTruthy(left)) {
      return left;
    }

    return evaluate(expression.right);
  }

  @Override
  public Object visitVariableExpr(
      Expr.Variable expression
  ) {
    Object value =
        environment.get(expression.name);

    if (value == UNINITIALIZED) {
      throw new RuntimeError(
          expression.name,
          "Variable must be initialized before use."
      );
    }

    return value;
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

      case BANG_EQUAL ->
          !isEqual(left, right);

      case EQUAL_EQUAL ->
          isEqual(left, right);

      default -> throw new RuntimeError(
          expression.operator,
          "Unknown binary operator."
      );
    };
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

  @Override
  public Void visitBreakStmt(
      Stmt.Break statement
  ) {
    throw new BreakException();
  }

  @Override
  public Void visitExpressionStmt(
      Stmt.Expression statement
  ) {
    evaluate(statement.expression);
    return null;
  }

  @Override
  public Void visitFunctionStmt(
      Stmt.Function statement
  ) {
    LoxFunction function = new LoxFunction(
        statement.name.lexeme,
        statement.function,
        environment
    );

    environment.define(
        statement.name.lexeme,
        function
    );

    return null;
  }

  @Override
  public Void visitIfStmt(
      Stmt.If statement
  ) {
    if (isTruthy(evaluate(statement.condition))) {
      execute(statement.thenBranch);
    } else if (statement.elseBranch != null) {
      execute(statement.elseBranch);
    }

    return null;
  }

  @Override
  public Void visitPrintStmt(
      Stmt.Print statement
  ) {
    System.out.println(
        stringify(evaluate(statement.expression))
    );

    return null;
  }

  @Override
  public Void visitReturnStmt(
      Stmt.Return statement
  ) {
    Object value = null;

    if (statement.value != null) {
      value = evaluate(statement.value);
    }

    throw new Return(value);
  }

  @Override
  public Void visitVarStmt(
      Stmt.Var statement
  ) {
    Object value = UNINITIALIZED;

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
  public Void visitWhileStmt(
      Stmt.While statement
  ) {
    try {
      while (isTruthy(
          evaluate(statement.condition)
      )) {
        execute(statement.body);
      }
    } catch (BreakException ignored) {
      // Continue after the nearest loop.
    }

    return null;
  }

  void executeBlock(
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