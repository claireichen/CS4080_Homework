package Chapter13;

import java.util.HashMap;
import java.util.Map;

public class LoxInstance {
  private final LoxClass klass;
  private final Map<String, Object> fields = new HashMap<>();

  public LoxInstance(LoxClass klass) {
    this.klass = klass;
  }

  public Object get(Token name) {
    if (fields.containsKey(name.lexeme)) {
      return fields.get(name.lexeme);
    }

    if (klass != null) {
      LoxFunction method = klass.findMethod(this, name.lexeme);
      if (method != null) return method;
    }

    throw new RuntimeError(
        name,
        "Undefined property '" + name.lexeme + "'."
    );
  }

  public void set(Token name, Object value) {
    fields.put(name.lexeme, value);
  }

  @Override
  public String toString() {
    return klass == null
        ? "<class object>"
        : klass.name + " instance";
  }
}
