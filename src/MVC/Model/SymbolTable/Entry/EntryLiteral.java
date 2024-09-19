package MVC.Model.SymbolTable.Entry;

import MVC.Model.Constants.DataType;
import MVC.Model.Constants.TokenType;
import MVC.Model.LiteralValue.LiteralValue;

public class EntryLiteral extends Entry {
  private DataType dataType;
  private LiteralValue value;

  public EntryLiteral(String text, TokenType tokenType, DataType dataType, LiteralValue value) {
    super(text, tokenType);
    this.dataType = dataType;
    this.value = value;
  }

  public DataType getDataType() {
    return dataType;
  }

  public LiteralValue getValue() {
    return value;
  }
}
