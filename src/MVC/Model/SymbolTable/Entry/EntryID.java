package MVC.Model.SymbolTable.Entry;

import MVC.Model.Constants.DataType;
import MVC.Model.Constants.TokenType;
import MVC.Model.LiteralValue.LiteralValue;

public class EntryID extends Entry {
  private final Integer scopeID;
  private DataType dataType;
  private boolean declared;
  private LiteralValue value;

  public EntryID(String text, TokenType tokenType, DataType dataType, Integer scopeID) {
    super(text, tokenType);
    this.dataType = dataType;
    this.scopeID = scopeID;
    declared = false;
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
    return declared;
  }

  public void declare() {
    declared = true;
  }

  public LiteralValue getValue() {
    return value;
  }
  
}
