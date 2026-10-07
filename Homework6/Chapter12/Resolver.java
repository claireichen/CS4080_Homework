package Chapter12;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

public final class Resolver
    implements Expr.Visitor<Void>,
               Stmt.Visitor<Void> {

  private enum FunctionType {
    NONE,
    FUNCTION,
    INITIALIZER,
    METHOD
  }

  private enum ClassType {
    NONE,
    CLASS
  }

  private enum VariableState {
    DECLARED,
    DEFINED,
    READ
  }

  private static final class Variable {
    final Token name;
    final int slot;
    VariableState state;

    Variable(
        Token name,
        int slot,
        VariableState state
    ) {
      this.name = name;
      this.slot = slot;
      this.state = state;
    }
  }

  private final Interpreter interpreter;

  private final Stack<Map<String, Variable>>
      scopes = new Stack<>();

  private FunctionType currentFunction =
      FunctionType.NONE;

  private ClassType currentClass =
      ClassType.NONE;

  public Resolver(Interpreter interpreter) {
    this.interpreter = interpreter;
  }

  public void resolve(List<Stmt> statements) {
    for (Stmt statement : statements) {
      if (statement != null) {
        resolve(statement);
      }
    }
  }

  private void resolve(Stmt statement) {
    statement.accept(this);
  }

  public void resolve(Expr expression) {
    expression.accept(this);
  }

  @Override
  public Void visitBlockStmt(
      Stmt.Block statement
  ) {
    beginScope();
    resolve(statement.statements);
    endScope();

    return null;
  }

  @Override
  public Void visitBreakStmt(
      Stmt.Break statement
  ) {
    return null;
  }

  @Override
  public Void visitClassStmt(
      Stmt.Class statement
  ) {
    ClassType enclosingClass = currentClass;
    currentClass = ClassType.CLASS;

    declare(statement.name);
    define(statement.name);

    beginScope();
    scopes.peek().put(
        "this",
        new Variable(
            new Token(
                TokenType.THIS,
                "this",
                null,
                statement.name.line
            ),
            0,
            VariableState.READ
        )
    );

    for (Stmt.Function method : statement.methods) {
      FunctionType type =
          method.name.lexeme.equals("init")
              ? FunctionType.INITIALIZER
              : FunctionType.METHOD;

      resolveFunction(method.function, type);
    }

    for (Stmt.Function method : statement.classMethods) {
      resolveFunction(
          method.function,
          FunctionType.METHOD
      );
    }

    endScope();
    currentClass = enclosingClass;
    return null;
  }

  @Override
  public Void visitExpressionStmt(
      Stmt.Expression statement
  ) {
    resolve(statement.expression);
    return null;
  }

  @Override
  public Void visitFunctionStmt(
      Stmt.Function statement
  ) {
    declare(statement.name);
    define(statement.name);

    resolveFunction(
        statement.function,
        FunctionType.FUNCTION
    );

    return null;
  }

  @Override
  public Void visitIfStmt(
      Stmt.If statement
  ) {
    resolve(statement.condition);
    resolve(statement.thenBranch);

    if (statement.elseBranch != null) {
      resolve(statement.elseBranch);
    }

    return null;
  }

  @Override
  public Void visitPrintStmt(
      Stmt.Print statement
  ) {
    resolve(statement.expression);
    return null;
  }

  @Override
  public Void visitReturnStmt(
      Stmt.Return statement
  ) {
    if (currentFunction == FunctionType.NONE) {
      Lox.error(
          statement.keyword,
          "Can't return from top-level code."
      );
    }

    if (statement.value != null) {
      if (currentFunction
          == FunctionType.INITIALIZER) {
        Lox.error(
            statement.keyword,
            "Can't return a value from an initializer."
        );
      }

      resolve(statement.value);
    }

    return null;
  }

  @Override
  public Void visitVarStmt(
      Stmt.Var statement
  ) {
    declare(statement.name);

    if (statement.initializer != null) {
      resolve(statement.initializer);
    }

    define(statement.name);
    return null;
  }

  @Override
  public Void visitWhileStmt(
      Stmt.While statement
  ) {
    resolve(statement.condition);
    resolve(statement.body);

    return null;
  }

  @Override
  public Void visitAssignExpr(
      Expr.Assign expression
  ) {
    resolve(expression.value);

    /*
     * Assignment is only a write, so it does
     * not count as using the variable.
     */
    resolveLocal(
        expression,
        expression.name,
        false
    );

    return null;
  }

  @Override
  public Void visitBinaryExpr(
      Expr.Binary expression
  ) {
    resolve(expression.left);
    resolve(expression.right);

    return null;
  }

  @Override
  public Void visitCallExpr(
      Expr.Call expression
  ) {
    resolve(expression.callee);

    for (Expr argument : expression.arguments) {
      resolve(argument);
    }

    return null;
  }

  @Override
  public Void visitFunctionExpr(
      Expr.Function expression
  ) {
    resolveFunction(
        expression,
        FunctionType.FUNCTION
    );
    return null;
  }

  @Override
  public Void visitGetExpr(
      Expr.Get expression
  ) {
    resolve(expression.object);
    return null;
  }

  @Override
  public Void visitGroupingExpr(
      Expr.Grouping expression
  ) {
    resolve(expression.expression);
    return null;
  }

  @Override
  public Void visitLiteralExpr(
      Expr.Literal expression
  ) {
    return null;
  }

  @Override
  public Void visitLogicalExpr(
      Expr.Logical expression
  ) {
    resolve(expression.left);
    resolve(expression.right);

    return null;
  }

  @Override
  public Void visitSetExpr(
      Expr.Set expression
  ) {
    resolve(expression.value);
    resolve(expression.object);
    return null;
  }

  @Override
  public Void visitThisExpr(
      Expr.This expression
  ) {
    if (currentClass == ClassType.NONE) {
      Lox.error(
          expression.keyword,
          "Can't use 'this' outside of a class."
      );
      return null;
    }

    resolveLocal(
        expression,
        expression.keyword,
        true
    );
    return null;
  }

  @Override
  public Void visitUnaryExpr(
      Expr.Unary expression
  ) {
    resolve(expression.right);
    return null;
  }

  @Override
  public Void visitVariableExpr(
      Expr.Variable expression
  ) {
    if (!scopes.isEmpty()) {
      Variable variable =
          scopes.peek().get(
              expression.name.lexeme
          );

      if (variable != null
          && variable.state
              == VariableState.DECLARED) {
        Lox.error(
            expression.name,
            "Can't read local variable "
                + "in its own initializer."
        );
      }
    }

    resolveLocal(
        expression,
        expression.name,
        true
    );

    return null;
  }

  private void resolveFunction(
      Expr.Function function,
      FunctionType type
  ) {
    FunctionType enclosingFunction =
        currentFunction;

    currentFunction = type;

    beginScope();

    if (function.parameters != null) {
      for (Token parameter : function.parameters) {
        declare(parameter);
        define(parameter);
      }
    }

    resolve(function.body);
    endScope();

    currentFunction = enclosingFunction;
  }

  private void resolveLocal(
      Expr expression,
      Token name,
      boolean isRead
  ) {
    for (int i = scopes.size() - 1;
         i >= 0;
         i--) {
      Variable variable =
          scopes.get(i).get(name.lexeme);

      if (variable != null) {
        int distance =
            scopes.size() - 1 - i;

        interpreter.resolve(
            expression,
            distance,
            variable.slot
        );

        if (isRead) {
          variable.state = VariableState.READ;
        }

        return;
      }
    }

    // Not found locally, so it is global.
  }

  private void beginScope() {
    scopes.push(new HashMap<>());
  }

  private void endScope() {
    Map<String, Variable> scope =
        scopes.pop();

    for (Variable variable : scope.values()) {
      if (variable.state
          == VariableState.DEFINED) {
        Lox.error(
            variable.name,
            "Local variable is not used."
        );
      }
    }
  }

  private void declare(Token name) {
    if (scopes.isEmpty()) {
      return;
    }

    Map<String, Variable> scope =
        scopes.peek();

    if (scope.containsKey(name.lexeme)) {
      Lox.error(
          name,
          "Already a variable with this "
              + "name in this scope."
      );
    }

    int slot = scope.size();

    scope.put(
        name.lexeme,
        new Variable(
            name,
            slot,
            VariableState.DECLARED
        )
    );
  }

  private void define(Token name) {
    if (scopes.isEmpty()) {
      return;
    }

    scopes.peek()
        .get(name.lexeme)
        .state = VariableState.DEFINED;
  }
}
