package MVC.Model.SymbolTable;

import java.util.ArrayList;
import java.util.HashMap;
import MVC.Model.Constants.*;
import MVC.Model.SymbolTable.Entry.Entry;
import MVC.Model.SymbolTable.Entry.EntryID;

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
      addEntry(new Entry(rw.name().toLowerCase(), TokenType.RW));
    }
    symbols = new HashMap<>();
    for (TokenType tokenType : TokenType.values()) {
      if (tokenType.getValue() != null) {
        symbols.put(tokenType.getValue(), entries.size());
        addEntry(new Entry(tokenType.getValue(), tokenType));
      }
    }
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

  public int addEntry(Entry entry) {
    entries.add(entry);
    int id = entries.size() - 1;
    if (entry instanceof EntryID) {
      EntryID entryID = (EntryID) entry;
      identifiersByScope.get(entryID.getScopeID()).put(entry.getText(), id);
    }
    return id;
  }

  public ArrayList<Entry> getEntries() {
    return entries;
  }
}