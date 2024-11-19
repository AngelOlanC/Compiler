package MVC.Model.Scanner;

import java.util.ArrayList;

import MVC.Model.Constants.DataType;
import MVC.Model.Constants.TokenType;
import MVC.Model.SymbolTable.SymbolTable;
import MVC.Model.SymbolTable.Entry.EntryID;
import MVC.Model.SymbolTable.Entry.EntryLiteral;

public class Scanner {
  private SymbolTable symbolTable;
  private ArrayList<Token> tokenStream;
  private String source;

  public Scanner(SymbolTable symbolTable, String source) {
    this.symbolTable = symbolTable;
    this.source = source;
    tokenStream = new ArrayList<>();
  }

  public boolean analyze() {
    tokenStream = new ArrayList<>();
    symbolTable.addScope(0);
    int i = 0, n = source.length(), scopeCnt = 0, scopeID = 0;
    while (i < n) {
      char c = source.charAt(i);
      if (c == ' ' || c == '\t' || c == '\n') { 
        ++i; 
        continue; 
      }
      if (TokenType.match("" + c) != null) {
        String txt = "" + c;
        TokenType tokenType = TokenType.match(txt);
        ++i;
        if (i != n && TokenType.match(txt + source.charAt(i)) != null) {
          txt += source.charAt(i);
          tokenType = TokenType.match(txt);
          ++i;
        }
        int idOnSymbolTable = symbolTable.getSymbolID(txt);
        tokenStream.add(new Token(tokenType, txt, idOnSymbolTable));
        if (txt.equals("{")) {
          symbolTable.addScope(scopeID);
          scopeID = ++scopeCnt;
        } else if (txt.equals("}")) {
          scopeID = symbolTable.getScopeParent(scopeID);
        }
        continue;
      }
      if (Character.isLowerCase(c)) {
        StringBuilder value = new StringBuilder();
        do {
          value.append(c);
          if (++i == n) break;
          c = source.charAt(i);
        } while (Character.isLowerCase(c) || Character.isDigit(c));
        String text = value.toString();
        int reservedWordID = symbolTable.getRWID(text);
        if (reservedWordID != -1) {
          int idOnSymbolTable = symbolTable.getRWID(text);
          tokenStream.add(new Token(TokenType.RW, text, idOnSymbolTable));
          continue;
        }
        int idOnSymbolTable = symbolTable.getIdentifierID(text, scopeID);
        if (idOnSymbolTable == -1) {
          idOnSymbolTable = symbolTable.addEntry(new EntryID(text,
                                                             TokenType.ID,
                                                             null,
                                                             scopeID));
        }
        tokenStream.add(new Token(TokenType.ID, text, idOnSymbolTable));
        continue;
      }
      if (c == '"') {
        StringBuilder value = new StringBuilder();
        do {
          value.append(c);
          if (++i == n) break;
          c = source.charAt(i);
        } while (Character.isLowerCase(c) || Character.isDigit(c) || c == ' ');
        if (i < n && c == '"') {
          value.append(c);
          String text = value.toString();
          int idOnSymbolTable = symbolTable.addEntry(
                                    new EntryLiteral(text, 
                                                     TokenType.LITERAL, 
                                                     DataType.STRING));
          tokenStream.add(new Token(TokenType.LITERAL, text, idOnSymbolTable));
          ++i;
          continue;
        }
        return false;
      }
      if (Character.isDigit(c)) {
        StringBuilder value = new StringBuilder();
        do {
          value.append(c);
          if (++i == n) break;
          c = source.charAt(i);
        } while (Character.isDigit(c));
        String text = value.toString();
        int idOnSymbolTable = symbolTable.addEntry(
                                    new EntryLiteral(text, 
                                                     TokenType.LITERAL, 
                                                     DataType.INT));
        tokenStream.add(new Token(TokenType.LITERAL, text, idOnSymbolTable));
        continue;
      }
      return false;
    }
    return true;
  }

  public ArrayList<Token> getTokenStream() {
    return tokenStream;
  }
}