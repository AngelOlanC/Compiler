package MVC.Model.SymbolTable.Entry;

import MVC.Model.Constants.TokenType;

public class Entry {
  private final String text;
  private final TokenType tokenType;

  public Entry(String text, TokenType tokenType) {
    this.text = text;
    this.tokenType = tokenType;
  }

  public String getText() {
    return text;
  }
  
  public TokenType getTokenType() {
    return tokenType;
  }
}