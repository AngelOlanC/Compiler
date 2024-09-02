package MVC.Model;

public class Lexeme {
  private Token type;
  private String value;

  public Lexeme(Token type, String value) {
    this.type = type;
    this.value = value;
  }

  public Token getType() {
    return type;
  }

  public String getValue() {
    return value;
  }
}
