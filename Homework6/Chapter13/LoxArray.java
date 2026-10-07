package Chapter13;

import java.util.List;

public final class LoxArray extends LoxInstance {
  private final Object[] elements;

  public LoxArray(int size) {
    super(null);
    elements = new Object[size];
  }

  @Override
  public Object get(Token name) {
    return switch (name.lexeme) {
      case "length" ->
          (double) elements.length;

      case "get" ->
          getter(name);

      case "set" ->
          setter(name);

      default -> throw new RuntimeError(
          name,
          "Undefined array property '"
              + name.lexeme
              + "'."
      );
    };
  }

  @Override
  public void set(
      Token name,
      Object value
  ) {
    throw new RuntimeError(
        name,
        "Can't add fields to arrays."
    );
  }

  private LoxCallable getter(Token token) {
    return new LoxCallable() {
      @Override
      public int arity() {
        return 1;
      }

      @Override
      public Object call(
          Interpreter interpreter,
          List<Object> arguments
      ) {
        int index = toIndex(
            arguments.get(0),
            token
        );

        return elements[index];
      }

      @Override
      public String toString() {
        return "<native array get>";
      }
    };
  }

  private LoxCallable setter(Token token) {
    return new LoxCallable() {
      @Override
      public int arity() {
        return 2;
      }

      @Override
      public Object call(
          Interpreter interpreter,
          List<Object> arguments
      ) {
        int index = toIndex(
            arguments.get(0),
            token
        );

        Object value = arguments.get(1);
        elements[index] = value;

        return value;
      }

      @Override
      public String toString() {
        return "<native array set>";
      }
    };
  }

  private int toIndex(
      Object value,
      Token token
  ) {
    if (!(value instanceof Double number)
        || number != Math.rint(number)) {
      throw new RuntimeError(
          token,
          "Array index must be an integer."
      );
    }

    int index = number.intValue();

    if (index < 0 || index >= elements.length) {
      throw new RuntimeError(
          token,
          "Array index "
              + index
              + " is out of bounds."
      );
    }

    return index;
  }

  @Override
  public String toString() {
    StringBuilder result =
        new StringBuilder("[");

    for (int i = 0; i < elements.length; i++) {
      if (i > 0) {
        result.append(", ");
      }

      result.append(stringify(elements[i]));
    }

    return result.append("]").toString();
  }

  private String stringify(Object value) {
    if (value == null) {
      return "nil";
    }

    if (value instanceof Double number) {
      String text = number.toString();

      if (text.endsWith(".0")) {
        return text.substring(
            0,
            text.length() - 2
        );
      }

      return text;
    }

    return value.toString();
  }
}