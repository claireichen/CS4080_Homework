package Chapter8;

import java.util.List;

public abstract class Stmt {
  public interface Visitor<R> {
    R visitBlockStmt(Block statement);
    R visitExpressionStmt(Expression statement);
    R visitPrintStmt(Print statement);
    R visitVarStmt(Var statement);
  }

  public abstract <R> R accept(Visitor<R> visitor);

  public static final class Block extends Stmt {
    public final List<Stmt> statements;

    public Block(List<Stmt> statements) {
      this.statements = statements;
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
      return visitor.visitBlockStmt(this);
    }
  }

  public static final class Expression extends Stmt {
    public final Expr expression;

    public Expression(Expr expression) {
      this.expression = expression;
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
      return visitor.visitExpressionStmt(this);
    }
  }

  public static final class Print extends Stmt {
    public final Expr expression;

    public Print(Expr expression) {
      this.expression = expression;
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
      return visitor.visitPrintStmt(this);
    }
  }

  public static final class Var extends Stmt {
    public final Token name;
    public final Expr initializer;

    public Var(Token name, Expr initializer) {
      this.name = name;
      this.initializer = initializer;
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
      return visitor.visitVarStmt(this);
    }
  }
}