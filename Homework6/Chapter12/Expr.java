package Chapter12;

import java.util.List;

public abstract class Expr {
  public interface Visitor<R> {
    R visitAssignExpr(Assign expression);
    R visitBinaryExpr(Binary expression);
    R visitCallExpr(Call expression);
    R visitFunctionExpr(Function expression);
    R visitGetExpr(Get expression);
    R visitGroupingExpr(Grouping expression);
    R visitLiteralExpr(Literal expression);
    R visitLogicalExpr(Logical expression);
    R visitSetExpr(Set expression);
    R visitThisExpr(This expression);
    R visitUnaryExpr(Unary expression);
    R visitVariableExpr(Variable expression);
  }

  public abstract <R> R accept(Visitor<R> visitor);

  public static final class Assign extends Expr {
    public final Token name;
    public final Expr value;

    public Assign(Token name, Expr value) {
      this.name = name;
      this.value = value;
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
      return visitor.visitAssignExpr(this);
    }
  }

  public static final class Binary extends Expr {
    public final Expr left;
    public final Token operator;
    public final Expr right;

    public Binary(Expr left, Token operator, Expr right) {
      this.left = left;
      this.operator = operator;
      this.right = right;
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
      return visitor.visitBinaryExpr(this);
    }
  }

  public static final class Call extends Expr {
    public final Expr callee;
    public final Token paren;
    public final List<Expr> arguments;

    public Call(
        Expr callee,
        Token paren,
        List<Expr> arguments
    ) {
      this.callee = callee;
      this.paren = paren;
      this.arguments = arguments;
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
      return visitor.visitCallExpr(this);
    }
  }

  public static final class Function extends Expr {
    public final List<Token> parameters;
    public final List<Stmt> body;

    public Function(
        List<Token> parameters,
        List<Stmt> body
    ) {
      this.parameters = parameters;
      this.body = body;
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
      return visitor.visitFunctionExpr(this);
    }
  }

  public static final class Grouping extends Expr {
    public final Expr expression;

    public Grouping(Expr expression) {
      this.expression = expression;
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
      return visitor.visitGroupingExpr(this);
    }
  }

  public static final class Get extends Expr {
    public final Expr object;
    public final Token name;

    public Get(Expr object, Token name) {
      this.object = object;
      this.name = name;
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
      return visitor.visitGetExpr(this);
    }
  }

  public static final class Literal extends Expr {
    public final Object value;

    public Literal(Object value) {
      this.value = value;
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
      return visitor.visitLiteralExpr(this);
    }
  }

  public static final class Logical extends Expr {
    public final Expr left;
    public final Token operator;
    public final Expr right;

    public Logical(
        Expr left,
        Token operator,
        Expr right
    ) {
      this.left = left;
      this.operator = operator;
      this.right = right;
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
      return visitor.visitLogicalExpr(this);
    }
  }

  public static final class Unary extends Expr {
    public final Token operator;
    public final Expr right;

    public Unary(Token operator, Expr right) {
      this.operator = operator;
      this.right = right;
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
      return visitor.visitUnaryExpr(this);
    }
  }

  public static final class Set extends Expr {
    public final Expr object;
    public final Token name;
    public final Expr value;

    public Set(Expr object, Token name, Expr value) {
      this.object = object;
      this.name = name;
      this.value = value;
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
      return visitor.visitSetExpr(this);
    }
  }

  public static final class This extends Expr {
    public final Token keyword;

    public This(Token keyword) {
      this.keyword = keyword;
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
      return visitor.visitThisExpr(this);
    }
  }

  public static final class Variable extends Expr {
    public final Token name;

    public Variable(Token name) {
      this.name = name;
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
      return visitor.visitVariableExpr(this);
    }
  }
}
