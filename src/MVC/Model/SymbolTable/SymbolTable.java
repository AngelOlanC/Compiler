package MVC.Model.SymbolTable;

import java.util.ArrayList;
import java.util.HashMap;
import MVC.Model.Constants.*;
import MVC.Model.LiteralValue.LiteralValue;

public class SymbolTable {
  private HashMap<String, Integer> symbols;
  private ArrayList<HashMap<String, Integer>> identifiersByScope;
  private ArrayList<Integer> scopeParent;
  private ArrayList<Entry> entries;

  public SymbolTable() {
    entries = new ArrayList<>();
    scopeParent = new ArrayList<>();
    identifiersByScope = new ArrayList<>();
    identifiersByScope.add(new HashMap<>());
    initializeSymbolTable();
  }

  private void initializeSymbolTable() {
    for (ReservedWord rw : ReservedWord.values()) {
      addEntry(rw.name().toLowerCase(), TokenType.RW, null, null, null);
    }
    int sz = entries.size();
    addEntry("(", TokenType.LP, null, null, null);
    addEntry(")", TokenType.RP, null, null, null);
    addEntry("{", TokenType.ADD, null, null, null);
    addEntry("}", TokenType.SUB, null, null, null);
    addEntry("(", TokenType.LESS, null, null, null);
    addEntry("(", TokenType.LEQ, null, null, null);
    addEntry("(", TokenType.GREATER, null, null, null);
    addEntry("(", TokenType.GEQ, null, null, null);
    addEntry("(", TokenType.EQUALS, null, null, null);
    addEntry("(", TokenType.NEQ, null, null, null);
    addEntry("(", TokenType.ASSIGN, null, null, null);
    symbols = new HashMap<>();
    symbols.put("(", sz);
    symbols.put(")", sz + 1);
    symbols.put("{", sz + 2);
    symbols.put("}", sz + 3);
    symbols.put("+", sz + 4);
    symbols.put("-", sz + 5);
    symbols.put("<", sz + 6);
    symbols.put("<=", sz + 7);
    symbols.put(">", sz + 8);
    symbols.put(">=", sz + 9);
    symbols.put("==", sz + 10);
    symbols.put("!=", sz + 11);
    symbols.put("=", sz + 12);
  }

  public int getSymbolID(String symbol) {
    return symbols.get(symbol);
  }

  public int getRWID(String text) {
    for (ReservedWord rw : ReservedWord.values()) {
      if (text.equals(rw.name().toLowerCase())) {
        return rw.ordinal();
      }
    }
    return -1;
  }

  public void addScope(Integer parent) {
    identifiersByScope.add(new HashMap<>());
    scopeParent.add(parent);  
  }

  public int getScopeParent(int scopeID) {
    return scopeParent.get(scopeID);
  }

  public int getIdentifierID(String identifier, int scopeID) {
    Integer id = identifiersByScope.get(scopeID).get(identifier);
    if (id != null) return id;
    return scopeID == 0 ? -1 : getIdentifierID(identifier, getScopeParent(scopeID));
  }

  public int addEntry(String text, TokenType tokenType, DataType dataType, Integer scope, LiteralValue value) {
    entries.add(new Entry(text, tokenType, dataType, scope, value));
    int id = entries.size() - 1;
    if (tokenType == TokenType.ID) identifiersByScope.get(scope).put(text, id);
    return id;
  }

  public ArrayList<Entry> getEntries() {
    return entries;
  }

  public static class Entry {
    private final String text;
    private final TokenType tokenType;
    private final Integer scope;
    private DataType dataType;
    private boolean declared;
    private LiteralValue value;

    public Entry(String text, TokenType tokenType, DataType dataType, Integer scope, LiteralValue value) {
      this.text = text;
      this.tokenType = tokenType;
      this.dataType = dataType;
      this.scope = scope;
      this.value = value;
      declared = false;
    }

    public String getText() {
      return text;
    }
    
    public TokenType getTokenType() {
      return tokenType;
    }

    public DataType getDataType() {
      return dataType;
    }

    public void setDataType(DataType dataType) {
      this.dataType = dataType;
    }

    public Integer getScope() {
      return scope;
    }
    public boolean isDeclared() {
      return declared;
    }

    public void setDeclared(boolean declared) {
      this.declared = declared;
    }

    public LiteralValue getValue() {
      return value;
    }

    public void setValue(LiteralValue value) {
      this.value = value;
    }
  }
}