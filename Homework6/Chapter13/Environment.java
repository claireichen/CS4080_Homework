package Chapter13;

import java.util.ArrayList;
import java.util.List;

public final class Environment {
  private final List<Object> values =
      new ArrayList<>();

  private final Environment enclosing;

  public Environment() {
    enclosing = null;
  }

  public Environment(Environment enclosing) {
    this.enclosing = enclosing;
  }

  public void define(Object value) {
    values.add(value);
  }

  public Object getAt(
      int distance,
      int slot
  ) {
    return ancestor(distance).values.get(slot);
  }

  public void assignAt(
      int distance,
      int slot,
      Object value
  ) {
    ancestor(distance).values.set(
        slot,
        value
    );
  }

  private Environment ancestor(int distance) {
    Environment environment = this;

    for (int i = 0; i < distance; i++) {
      environment = environment.enclosing;
    }

    return environment;
  }
}