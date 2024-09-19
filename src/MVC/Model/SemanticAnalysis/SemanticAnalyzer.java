package MVC.Model.SemanticAnalysis;

import MVC.Model.Constants.DataType;
import MVC.Model.Constants.Production;
import MVC.Model.Constants.ReservedWord;
import MVC.Model.Constants.TokenType;
import MVC.Model.LiteralValue.LiteralValue;
import MVC.Model.LiteralValue.LiteralValueInteger;
import MVC.Model.LiteralValue.LiteralValueString;
import MVC.Model.Parser.ParseTree;
import MVC.Model.Parser.ParseTree.Node;
import MVC.Model.Scanner.Token;
import MVC.Model.SymbolTable.SymbolTable;
import MVC.Model.SymbolTable.SymbolTable.Entry;

public class SemanticAnalyzer {
  private SymbolTable symbolTable;
  private ParseTree parseTree;
  private int loops = 0;

  public SemanticAnalyzer(SymbolTable symbolTable, ParseTree parseTree) {
    this.symbolTable = symbolTable;
    this.parseTree = parseTree;
  }

  public boolean analyze() {
    return analyze(parseTree.getRoot());
  }

  private boolean analyze(Node node) {
    return sentences(node.getChildren().get(1));
  }

  private boolean sentences(Node node) {
    for (Node child : node.getChildren()) if (!sentence(child)) return false;
    return true;
  }

  private boolean sentence(Node node) {
    if (node.getChildren().isEmpty()) return true;

    boolean isDeclaration = node.getChildren().getFirst().getProduction() == Production.DATA_TYPE;
    if (isDeclaration) {
      int identifierID = node.getChildren().get(1).getToken().getSymbolTableID();
      Entry entry = symbolTable.getEntries().get(identifierID);
      if (entry.isDeclared()) return false;
      entry.setDeclared(true);
      String tokenTypeDataType = node.getChildren().get(0).getChildren().get(0).getToken().getValue();
      entry.setDataType(tokenTypeDataType.equals("int") ? DataType.INT : DataType.STRING);
      if (entry.getDataType() == DataType.INT) entry.setValue(new LiteralValueInteger(0));
      else entry.setValue(new LiteralValueString(""));
      return true;
    }

    Token firstChildToken = node.getChildren().getFirst().getToken();
    boolean isAssignation = firstChildToken.getTokenType() == TokenType.ID;
    if (isAssignation) {
      int identifierID = firstChildToken.getSymbolTableID();
      if (!symbolTable.getEntries().get(identifierID).isDeclared()) return false;
      Entry entry = symbolTable.getEntries().get(identifierID);
      LiteralValue literalValueOperation = operation(node.getChildren().get(2));
      if (literalValueOperation == null ||
          (literalValueOperation instanceof LiteralValueInteger && entry.getDataType() == DataType.STRING) ||
          (literalValueOperation instanceof LiteralValueString && entry.getDataType() == DataType.INT))
            return false;
      entry.setValue(literalValueOperation);
      return true;
    }
    
    boolean isRead = firstChildToken.getSymbolTableID() == ReservedWord.READ.ordinal();
    if (isRead) {
      int identifierID = node.getChildren().get(1).getToken().getSymbolTableID();
      return symbolTable.getEntries().get(identifierID).isDeclared();
    }

    boolean isPrint = firstChildToken.getSymbolTableID() == ReservedWord.PRINT.ordinal();
    if (isPrint) {
      LiteralValue literalValueOperation = operation(node.getChildren().get(1));
      return literalValueOperation != null;
    }

    boolean isWhile = firstChildToken.getSymbolTableID() == ReservedWord.WHILE.ordinal();
    if (isWhile) {
      loops++;
      if (!condition(node.getChildren().get(1)) || !sentences(node.getChildren().get(3))) return false;
      loops--;
      return true;
    }

    boolean isIf = firstChildToken.getSymbolTableID() == ReservedWord.IF.ordinal();
    if (isIf) {
      if (!condition(node.getChildren().get(1)) || !sentences(node.getChildren().get(3))) return false;
      return node.getChildren().size() == 5 ? true : sentences(node.getChildren().get(7));
    }

    boolean isBreak = firstChildToken.getSymbolTableID() == ReservedWord.BREAK.ordinal();
    boolean isContinue = firstChildToken.getSymbolTableID() == ReservedWord.CONTINUE.ordinal();
    if (isBreak || isContinue) return loops > 0; 

    return true;
  }

  private LiteralValue operation(Node node) {
    Node firstChild = node.getChildren().get(0);
    if (firstChild.getProduction() == Production.DATA) {
      Token firstToken = firstChild.getChildren().getFirst().getToken();
      Entry entry = symbolTable.getEntries().get(firstToken.getSymbolTableID());
      if (firstToken.getTokenType() == TokenType.ID && !entry.isDeclared()) return null;
      return entry.getValue();
    }
    Token firstToken = node.getChildren().getFirst().getToken();
    if (firstToken.getTokenType() == TokenType.ID) {
      Entry entry = symbolTable.getEntries().get(firstToken.getSymbolTableID());
      return entry.isDeclared() ? entry.getValue() : null;
    }
    LiteralValue literalValueLeft = operation(node.getChildren().get(1)),
                 literalValueRight = operation(node.getChildren().get(3));
    if (literalValueLeft == null || literalValueRight == null) return null;
    boolean isSubstraction = node.getChildren().get(2).getToken().getTokenType() == TokenType.SUB;
    if (isSubstraction) {
      if (literalValueLeft instanceof LiteralValueString ||
          literalValueRight instanceof LiteralValueString) {
        return null;
      }
      LiteralValueInteger literalValueIntegerLeft = (LiteralValueInteger)literalValueLeft,
                          literalValueIntegerRight = (LiteralValueInteger)literalValueRight;
      return new LiteralValueInteger(literalValueIntegerLeft.getValue() - literalValueIntegerRight.getValue());
    }
    if (literalValueLeft instanceof LiteralValueString ||
      literalValueRight instanceof LiteralValueString) {
      String concat = "";
      if (literalValueLeft instanceof LiteralValueString) {
        concat += ((LiteralValueString)literalValueLeft).getValue();
      } else {
        concat += ((LiteralValueInteger)literalValueLeft).getValue();
      }
      if (literalValueRight instanceof LiteralValueString) {
        concat += ((LiteralValueString)literalValueRight).getValue();
      } else {
        concat += ((LiteralValueInteger)literalValueRight).getValue();
      }
      return new LiteralValueString(concat);
    }
    LiteralValueInteger literalValueIntegerLeft = (LiteralValueInteger)literalValueLeft,
                        literalValueIntegerRight = (LiteralValueInteger)literalValueRight;
    return new LiteralValueInteger(literalValueIntegerLeft.getValue() + literalValueIntegerRight.getValue());
  }

  private boolean condition(Node node) {
    LiteralValue literalValueLeft = operation(node.getChildren().get(0)),
                 literalValueRight = operation(node.getChildren().get(2));
    if ((literalValueLeft instanceof LiteralValueInteger && literalValueRight instanceof LiteralValueString) ||
        (literalValueLeft instanceof LiteralValueString && literalValueRight instanceof LiteralValueInteger))
        return false;
    TokenType tokenTypeOperator = node.getChildren().get(1).getToken().getTokenType();
    if (tokenTypeOperator == TokenType.EQUALS || tokenTypeOperator == TokenType.NEQ) return true;
    return literalValueLeft instanceof LiteralValueInteger;
  }
}