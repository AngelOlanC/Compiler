package MVC.Model.Scanner;

import java.util.ArrayList;

import MVC.Model.Constants.DataType;
import MVC.Model.Constants.TokenType;
import MVC.Model.LiteralValue.LiteralValueInteger;
import MVC.Model.LiteralValue.LiteralValueString;
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
      if (c == ' ' || c == '\t' || c == '\n') { ++i; continue; }
      if (c == '{') {
        ++i;
        symbolTable.addScope(scopeID);
        scopeID = ++scopeCnt;
        int idOnSymbolTable = symbolTable.getSymbolID("{");
        tokenStream.add(new Token(TokenType.LCB, "{", idOnSymbolTable));
        continue;
      }
      if (c == '}') {
        ++i;
        scopeID = symbolTable.getScopeParent(scopeID);
        int idOnSymbolTable = symbolTable.getSymbolID("}");
        tokenStream.add(new Token(TokenType.RCB, "}", idOnSymbolTable));
        continue;
      }
      if (c == '+') {
        ++i;
        int idOnSymbolTable = symbolTable.getSymbolID(Character.toString(c));
        tokenStream.add(new Token(TokenType.ADD, "+", idOnSymbolTable));
        continue;
      }
      if (c == '-') {
        ++i;
        int idOnSymbolTable = symbolTable.getSymbolID("-");
        tokenStream.add(new Token(TokenType.SUB, "-", idOnSymbolTable));
        continue;
      }
      if (c == '(') {
        ++i;
        int idOnSymbolTable = symbolTable.getSymbolID("(");
        tokenStream.add(new Token(TokenType.LP, "(", idOnSymbolTable));
        continue;
      }
      if (c == ')') {
        ++i;
        int idOnSymbolTable = symbolTable.getSymbolID(")");
        tokenStream.add(new Token(TokenType.RP, ")", idOnSymbolTable));
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
                                                     DataType.STRING,
                                                     new LiteralValueString(text.substring(1, text.length() - 1))));
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
                                                     DataType.INT,
                                                     new LiteralValueInteger(Integer.parseInt(text))));
        tokenStream.add(new Token(TokenType.LITERAL, text, idOnSymbolTable));
        continue;
      }
      if (c == '<') {
        if (++i == n || source.charAt(i) != '=') {
          int idOnSymbolTable = symbolTable.getSymbolID("<");
          tokenStream.add(new Token(TokenType.LESS, "<", idOnSymbolTable));
          continue;
        }
        int idOnSymbolTable = symbolTable.getSymbolID("<=");
        tokenStream.add(new Token(TokenType.LEQ, "<=", idOnSymbolTable));
        ++i;
        continue;
      }
      if (c == '>') {
        if (++i == n || source.charAt(i) != '=') {
          int idOnSymbolTable = symbolTable.getSymbolID(">");
          tokenStream.add(new Token(TokenType.GREATER, ">", idOnSymbolTable));
          continue;
        }
        int idOnSymbolTable = symbolTable.getSymbolID(">=");
        tokenStream.add(new Token(TokenType.GEQ, ">=", idOnSymbolTable));
        ++i;
        continue;
      }
      if (c == '=') {
        if (++i == n || source.charAt(i) != '=') {
          int idOnSymbolTable = symbolTable.getSymbolID("=");
          tokenStream.add(new Token(TokenType.ASSIGN, "=", idOnSymbolTable));
          continue;
        }
        int idOnSymbolTable = symbolTable.getSymbolID("==");
        tokenStream.add(new Token(TokenType.EQUALS, "==", idOnSymbolTable));
        ++i;
        continue;
      }
      if (c == '!') {
        if (++i == n) return false;
        c = source.charAt(i);
        if (c != '=') return false;
        int idOnSymbolTable = symbolTable.getSymbolID("!=");
        tokenStream.add(new Token(TokenType.NEQ, "!=", idOnSymbolTable));
        ++i;
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