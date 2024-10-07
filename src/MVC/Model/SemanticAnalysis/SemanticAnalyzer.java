package MVC.Model.SemanticAnalysis;

import java.util.ArrayList;

import MVC.Model.Constants.DataType;
import MVC.Model.Constants.Production;
import MVC.Model.Constants.ReservedWord;
import MVC.Model.Constants.TokenType;
import MVC.Model.Parser.ParseTree;
import MVC.Model.Parser.Node.Node;
import MVC.Model.Parser.Node.NodeNonTerminal;
import MVC.Model.Parser.Node.NodeTerminal;
import MVC.Model.Scanner.Token;
import MVC.Model.SymbolTable.SymbolTable;
import MVC.Model.SymbolTable.Entry.Entry;
import MVC.Model.SymbolTable.Entry.EntryID;
import MVC.Model.SymbolTable.Entry.EntryLiteral;

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
    ArrayList<Node> children = node.getChildren();
    if (children.isEmpty()) return true;

    Node firstChild = children.getFirst();

    if (firstChild instanceof NodeNonTerminal) {
      NodeTerminal dataTypeNode = (NodeTerminal) firstChild.getChildren().getFirst();
      Token dataTypeToken = dataTypeNode.getToken();

      boolean isIntDeclaration = dataTypeToken.getSymbolTableID() == ReservedWord.INT.ordinal();
      boolean isStringDeclaration = dataTypeToken.getSymbolTableID() == ReservedWord.STRING.ordinal();
      if (isIntDeclaration || isStringDeclaration) {
        NodeTerminal secondChild = (NodeTerminal) children.get(1);
        int identifierID = secondChild.getToken().getSymbolTableID();
        EntryID entry = (EntryID) symbolTable.getEntries().get(identifierID);
        if (entry.isDeclared()) return false;
        entry.setDataType(isIntDeclaration ? DataType.INT : DataType.STRING);
        return true;
      }
      return false;
    }
    
    NodeTerminal firstChildTerminal = (NodeTerminal) firstChild;
    Token firstChildToken = firstChildTerminal.getToken();

    boolean isAssignation = firstChildToken.getTokenType() == TokenType.ID;
    if (isAssignation) {
      int identifierID = firstChildToken.getSymbolTableID();
      EntryID entry = (EntryID) symbolTable.getEntries().get(identifierID);
      if (!entry.isDeclared()) return false;
      DataType variableDataType = entry.getDataType();
      DataType operationResultType = operation(node.getChildren().get(2));
      return operationResultType != null && variableDataType == operationResultType;
    }
    
    boolean isRead = firstChildToken.getSymbolTableID() == ReservedWord.READ.ordinal();
    if (isRead) {
      NodeTerminal nodeVariable = (NodeTerminal) children.get(1);
      int identifierID = nodeVariable.getToken().getSymbolTableID();
      EntryID entryVariable = (EntryID) symbolTable.getEntries().get(identifierID);
      return entryVariable.isDeclared();
    }

    boolean isPrint = firstChildToken.getSymbolTableID() == ReservedWord.PRINT.ordinal();
    if (isPrint) {
      DataType operationResultType = operation(children.get(1));
      return operationResultType != null;
    }

    boolean isWhile = firstChildToken.getSymbolTableID() == ReservedWord.WHILE.ordinal();
    if (isWhile) {
      loops++;
      if (!condition(children.get(1)) || !sentences(children.get(3))) return false;
      loops--;
      return true;
    }

    boolean isIf = firstChildToken.getSymbolTableID() == ReservedWord.IF.ordinal();
    if (isIf) {
      if (!condition(children.get(1)) || !sentences(children.get(3))) return false;
      return children.size() == 5 ? true : sentences(children.get(7));
    }

    boolean isBreak = firstChildToken.getSymbolTableID() == ReservedWord.BREAK.ordinal();
    boolean isContinue = firstChildToken.getSymbolTableID() == ReservedWord.CONTINUE.ordinal();
    if (isBreak || isContinue) return loops > 0; 

    return true;
  }

  private DataType operation(Node node) {
    ArrayList<Node> children = node.getChildren();

    if (children.getFirst() instanceof NodeNonTerminal) {
      NodeNonTerminal firstChild = (NodeNonTerminal) children.getFirst();
      if (firstChild.getProduction() == Production.DATA) {
        NodeTerminal dataNode = (NodeTerminal) firstChild.getChildren().getFirst();
        Token dataToken = dataNode.getToken();
        Entry entry = symbolTable.getEntries().get(dataToken.getSymbolTableID());
        if (entry instanceof EntryID) {
          EntryID entryID = (EntryID) entry;
          return entryID.isDeclared() ? entryID.getDataType() : null;
        }
        EntryLiteral entryLiteral = (EntryLiteral) entry;
        return entryLiteral.getDataType();
      }
    }

    DataType dataTypeLeftOperation = operation(children.get(1)),
             dataTypeRightOperation = operation(children.get(3));
    
    if (dataTypeLeftOperation == null || dataTypeRightOperation == null) {
      return null;
    }

    NodeTerminal nodeOperator = (NodeTerminal) children.get(2).getChildren().get(0);
    boolean isSubstraction = nodeOperator.getToken().getTokenType() == TokenType.SUB;
    if (isSubstraction) {
      if (dataTypeLeftOperation == DataType.STRING || dataTypeLeftOperation == DataType.STRING) {
        return null;
      }
      return DataType.INT;
    }
    if (dataTypeLeftOperation == DataType.STRING || dataTypeLeftOperation == DataType.STRING) {
      return DataType.STRING;
    }
    return DataType.INT;
  }

  private boolean condition(Node node) {
    ArrayList<Node> children = node.getChildren();

    DataType dataTypeLeftOperation = operation(children.get(0)),
             dataTypeRightOperation = operation(children.get(2));

    if (dataTypeLeftOperation == null || dataTypeRightOperation == null) {
      return false;
    }

    NodeTerminal nodeOperator = (NodeTerminal) children.get(1).getChildren().get(0);
    TokenType tokenTypeOperator = nodeOperator.getToken().getTokenType();
    if (tokenTypeOperator == TokenType.EQUALS || tokenTypeOperator == TokenType.NEQ) {
      return true;
    }
    return dataTypeLeftOperation == DataType.INT && dataTypeRightOperation == DataType.INT;
  }
}