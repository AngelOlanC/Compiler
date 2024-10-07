package MVC.Model.SymbolTable.Entry;

import MVC.Model.Constants.DataType;
import MVC.Model.Constants.TokenType;
import MVC.Model.LiteralValue.LiteralValue;

public class EntryID extends Entry {
  private final Integer scopeID;
  private DataType dataType;
  private LiteralValue value;

  public EntryID(String text, TokenType tokenType, DataType dataType, Integer scopeID) {
    super(text, tokenType);
    this.dataType = dataType;
    this.scopeID = scopeID;
  }

  public DataType getDataType() {
    return dataType;
  }

  public void setDataType(DataType dataType) {
    this.dataType = dataType;
  }

  public Integer getScopeID() {
    return scopeID;
  }

  public boolean isDeclared() {
    return dataType != null;
  }

  public LiteralValue getValue() {
    return value;
  }
}