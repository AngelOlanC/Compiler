package MVC.Model.LiteralValue;

public class LiteralValueInteger extends LiteralValue {
  Integer value;
  public LiteralValueInteger(Integer value) {
    super();
    this.value = value;
  }
  public Integer getValue() {
    return value;
  }
  @Override
  public String toString() {
    return value + "";
  }
}