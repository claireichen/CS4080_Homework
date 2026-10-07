package Chapter13;

import java.util.List;

public final class LoxFunction
    implements LoxCallable {

  private final String name;
  private final Expr.Function declaration;
  private final Environment closure;
  private final boolean isInitializer;

  public LoxFunction(
      String name,
      Expr.Function declaration,
      Environment closure,
      boolean isInitializer
  ) {
    this.name = name;
    this.declaration = declaration;
    this.closure = closure;
    this.isInitializer = isInitializer;
  }

  public LoxFunction bind(
      LoxInstance instance,
      LoxCallable inner
  ) {
    Environment environment = new Environment(closure);
    environment.define(instance);
    environment.define(inner);
    return new LoxFunction(
        name,
        declaration,
        environment,
        isInitializer
    );
  }

  @Override
  public int arity() {
    if (declaration.parameters == null) {
      return 0;
    }

    return declaration.parameters.size();
  }

  public boolean isGetter() {
    return declaration.parameters == null;
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
    if (declaration.parameters != null) {
      for (Object argument : arguments) {
        environment.define(argument);
      }
    }

    try {
      interpreter.executeBlock(
          declaration.body,
          environment
      );
    } catch (Return returned) {
      if (isInitializer) {
        return closure.getAt(0, 0);
      }
      return returned.value;
    }

    if (isInitializer) {
      return closure.getAt(0, 0);
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
