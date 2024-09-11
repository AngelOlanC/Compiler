package MVC.Model;

public class Token {
  private final TokenType type;
  private final String value;

  public Token(TokenType type, String value) {
    this.type = type;
    this.value = value;
  }

  public TokenType getType() {
    return type;
  }

  @Override
  public String toString() {
    return type.toString().startsWith("RW")
            ? "<" + "RW, " + value + ">"
            : "<" + type.toString() + ", " + value + ">";
  }
}
