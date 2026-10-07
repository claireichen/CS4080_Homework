package Chapter12;

import java.util.List;
import java.util.Map;

public final class LoxClass
    extends LoxInstance
    implements LoxCallable {

  public final String name;
  private final Map<String, LoxFunction> methods;

  public LoxClass(
      LoxClass metaclass,
      String name,
      Map<String, LoxFunction> methods
  ) {
    super(metaclass);
    this.name = name;
    this.methods = methods;
  }

  public LoxFunction findMethod(String name) {
    return methods.get(name);
  }

  @Override
  public int arity() {
    LoxFunction initializer = findMethod("init");
    return initializer == null ? 0 : initializer.arity();
  }

  @Override
  public Object call(
      Interpreter interpreter,
      List<Object> arguments
  ) {
    LoxInstance instance = new LoxInstance(this);
    LoxFunction initializer = findMethod("init");

    if (initializer != null) {
      initializer.bind(instance).call(interpreter, arguments);
    }

    return instance;
  }

  @Override
  public String toString() {
    return name;
  }
}
