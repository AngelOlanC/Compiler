package MVC.Model.IntermediateCodeGeneration;

import MVC.Model.Constants.DataType;
import MVC.Model.SymbolTable.SymbolTable;
import MVC.Model.SymbolTable.Entry.Entry;
import MVC.Model.SymbolTable.Entry.EntryID;

public class IntermediateCodeGenerator {
  private SymbolTable symbolTable;
  
  public IntermediateCodeGenerator(SymbolTable symbolTable) {
    this.symbolTable = symbolTable;
  }

  public String generateIntermediateCode() {
    StringBuilder text = new StringBuilder("\t.DATA\n");
    for (Entry entry : symbolTable.getEntries()) {
      if (!(entry instanceof EntryID)) continue;
      EntryID entryID = (EntryID) entry;
      String type, value;
      if (entryID.getDataType() == DataType.INT) {
        type = "DD";
        value = "?";
      } else {
        type = "DB";
        value = "50 dup('$')";
      }
      String name = entryID.getText() + formatScopeID(entryID.getScopeID());
      text.append(name + "\t" + type + "\t" + value + "\n");
    }
    text.append("\t.CODE\n");
    return text.toString();
  }

  private String formatScopeID(int scopeID) {
    if (scopeID < 10 ) return "00" + scopeID;
    if (scopeID < 100) return "0"  + scopeID;
    return "" + scopeID;
  }
}