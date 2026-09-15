package Chapter7;

public abstract class Expr {
  public interface Visitor<R> {
    R visitBinaryExpr(Binary expression);

    R visitConditionalExpr(Conditional expression);

    R visitGroupingExpr(Grouping expression);

    R visitLiteralExpr(Literal expression);

    R visitUnaryExpr(Unary expression);
  }

  public abstract <R> R accept(Visitor<R> visitor);

  public static final class Binary extends Expr {
    public final Expr left;
    public final Token operator;
    public final Expr right;

    public Binary(
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
      return visitor.visitBinaryExpr(this);
    }
  }

  public static final class Conditional extends Expr {
  public final Expr condition;
  public final Expr thenBranch;
  public final Expr elseBranch;

  public Conditional(
      Expr condition,
      Expr thenBranch,
      Expr elseBranch
  ) {
    this.condition = condition;
    this.thenBranch = thenBranch;
    this.elseBranch = elseBranch;
  }

  @Override
  public <R> R accept(Visitor<R> visitor) {
    return visitor.visitConditionalExpr(this);
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

  public static final class Unary extends Expr {
    public final Token operator;
    public final Expr right;

    public Unary(
        Token operator,
        Expr right
    ) {
      this.operator = operator;
      this.right = right;
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
      return visitor.visitUnaryExpr(this);
    }
  }
}