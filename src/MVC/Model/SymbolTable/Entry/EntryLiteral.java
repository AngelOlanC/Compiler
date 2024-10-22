package MVC.Model.SymbolTable.Entry;

import MVC.Model.Constants.DataType;
import MVC.Model.Constants.TokenType;

public class EntryLiteral extends Entry {
  private DataType dataType;

  public EntryLiteral(String text, TokenType tokenType, DataType dataType) {
    super(text, tokenType);
    this.dataType = dataType;
  }

  public DataType getDataType() {
    return dataType;
  }
}
