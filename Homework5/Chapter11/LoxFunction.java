package Chapter11;

import java.util.List;

public final class LoxFunction
    implements LoxCallable {

  private final String name;
  private final Expr.Function declaration;
  private final Environment closure;

  public LoxFunction(
      String name,
      Expr.Function declaration,
      Environment closure
  ) {
    this.name = name;
    this.declaration = declaration;
    this.closure = closure;
  }

  @Override
  public int arity() {
    return declaration.parameters.size();
  }

  @Override
  public Object call(
      Interpreter interpreter,
      List<Object> arguments
  ) {
    Environment environment =
        new Environment(closure);

    /*
     * Parameters are inserted in the same order
     * as the slots assigned by the resolver.
     */
    for (Object argument : arguments) {
      environment.define(argument);
    }

    try {
      interpreter.executeBlock(
          declaration.body,
          environment
      );
    } catch (Return returned) {
      return returned.value;
    }

    return null;
  }

  @Override
  public String toString() {
    if (name == null) {
      return "<fn>";
    }

    return "<fn " + name + ">";
  }
}