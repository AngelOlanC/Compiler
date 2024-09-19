package MVC.Model.LiteralValue;

public class LiteralValueString extends LiteralValue {
  String value;
  public LiteralValueString(String value) {
    super();
    this.value = value;
  }
  public String getValue() {
    return value;
  }
  @Override
  public String toString() {
    return value;
  }
}