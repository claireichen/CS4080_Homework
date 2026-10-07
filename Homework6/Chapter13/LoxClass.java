package Chapter13;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;

public final class LoxClass
    extends LoxInstance
    implements LoxCallable {

  public final String name;
  private final LoxClass metaclass;
  private final LoxClass superclass;
  private final Map<String, LoxFunction> methods;

  private static final LoxCallable NO_OP_INNER =
      new LoxCallable() {
        @Override
        public int arity() {
          return 0;
        }

        @Override
        public Object call(
            Interpreter interpreter,
            List<Object> arguments
        ) {
          return null;
        }

        @Override
        public String toString() {
          return "<no inner method>";
        }
      };

  public LoxClass(
      LoxClass metaclass,
      String name,
      LoxClass superclass,
      Map<String, LoxFunction> methods
  ) {
    super(metaclass);
    this.metaclass = metaclass;
    this.name = name;
    this.superclass = superclass;
    this.methods = methods;
  }

  public LoxClass metaclass() {
    return metaclass;
  }

  public LoxFunction findMethod(
      LoxInstance instance,
      String name
  ) {
    List<LoxFunction> matches = new ArrayList<>();

    for (LoxClass klass = this;
         klass != null;
         klass = klass.superclass) {
      LoxFunction method = klass.methods.get(name);
      if (method != null) matches.add(method);
    }

    if (matches.isEmpty()) return null;

    LoxCallable inner = NO_OP_INNER;
    LoxFunction bound = null;

    for (LoxFunction method : matches) {
      bound = method.bind(instance, inner);
      inner = bound;
    }

    return bound;
  }

  private LoxFunction findHighestMethod(String name) {
    LoxFunction result = null;

    for (LoxClass klass = this;
         klass != null;
         klass = klass.superclass) {
      LoxFunction method = klass.methods.get(name);
      if (method != null) result = method;
    }

    return result;
  }

  @Override
  public int arity() {
    LoxFunction initializer = findHighestMethod("init");
    return initializer == null ? 0 : initializer.arity();
  }

  @Override
  public Object call(
      Interpreter interpreter,
      List<Object> arguments
  ) {
    LoxInstance instance = new LoxInstance(this);
    LoxFunction initializer = findMethod(instance, "init");

    if (initializer != null) {
      initializer.call(interpreter, arguments);
    }

    return instance;
  }

  @Override
  public String toString() {
    return name;
  }
}
