package MVC.Model.IntermediateCodeGeneration;

import java.util.ArrayList;
import java.util.Stack;

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

public class IntermediateCodeGenerator {
  private SymbolTable symbolTable;
  private ParseTree parseTree;
  private StringBuilder intermediateCode;
  Stack<Integer> cycleIds;
  private int gid;
  
  public IntermediateCodeGenerator(SymbolTable symbolTable, ParseTree parseTree) {
    this.symbolTable = symbolTable;
    this.parseTree = parseTree;
  }

  public String generateIntermediateCode() {
    intermediateCode = new StringBuilder();
    gid = 0;
    cycleIds = new Stack<>();
    generateUpperPart();
    generateCode();
    generateLowerPart();
    return intermediateCode.toString();
  }

  private void generateUpperPart() {
    appendCode("", ".MODEL SMALL");
    appendCode("", ".486");
    appendCode("", ".STACK");
    appendCode("", ".DATA");
    for (Entry entry : symbolTable.getEntries()) {
      if (!(entry instanceof EntryID)) continue;
      EntryID entryID = (EntryID) entry;
      String value, type;
      if (entryID.getDataType() == DataType.INT) {
        type = "DW";
        value = "0";
      } else {
        type = "DB";
        value = "22 DUP('$')";
      }
      appendCode(getEntryAsmName(entryID), type, value);
    }
    appendCode("AUX_STRING", "DB", "22 DUP('$')");
    appendCode("", ".CODE");
    appendCode("MAIN", "PROC", "FAR");
    appendCode("", ".STARTUP");
  }

  private String getEntryAsmName(EntryID entryID) {
    return entryID.getText() + formatScopeID(entryID.getScopeID());
  }

  private String formatScopeID(int scopeID) {
    if (scopeID < 10 ) return "00" + scopeID;
    if (scopeID < 100) return "0"  + scopeID;
    return "" + scopeID;
  }

  private void generateCode() {
    generateSentences(parseTree.getRoot().getChildren().get(1));
  }

  private void generateSentences(Node node) {
    for (Node child : node.getChildren()) generateSentence(child);
  }

  private void generateSentence(Node node) {
    ArrayList<Node> children = node.getChildren();
    if (children.isEmpty()) return;

    Node firstChild = children.getFirst();
    boolean isDeclaration = firstChild instanceof NodeNonTerminal;
    if (isDeclaration) {
      return;
    }
    
    NodeTerminal firstChildTerminal = (NodeTerminal) firstChild;
    Token firstChildToken = firstChildTerminal.getToken();

    boolean isAssignation = firstChildToken.getTokenType() == TokenType.ID;
    if (isAssignation) {
      int identifierID = firstChildToken.getSymbolTableID();
      EntryID entry = (EntryID) symbolTable.getEntries().get(identifierID);
      DataType variableDataType = entry.getDataType();
      Node operation = children.get(2);
      String asmName = getEntryAsmName(entry);
      if (variableDataType == DataType.STRING) {
        String literal = ((NodeTerminal)operation.getChildren().get(0).getChildren().get(0)).getToken().getValue();
        literal = literal.substring(1, literal.length() - 1);
        appendStringLiteralAssignation(asmName, literal);
        appendEmptyLine();
        return;
      }
      generateOperation(operation);

      appendCode("", "MOV", asmName + ", BX");
      
      appendEmptyLine();
      return;
    }
    
    boolean isRead = firstChildToken.getSymbolTableID() == ReservedWord.READ.ordinal();
    if (isRead) {
      int identifierID = ((NodeTerminal)children.get(1)).getToken().getSymbolTableID();
      EntryID entry = (EntryID) symbolTable.getEntries().get(identifierID);
      DataType variableDataType = entry.getDataType();
      if (variableDataType == DataType.STRING) {
        appendCode("", "LEA", "DX, " + getEntryAsmName(entry));
        appendCode("", "MOV", "AH, 0AH");
        appendCode("", "INT", "21H");

        appendEmptyLine();

        appendCode("", "LEA", "SI, " + getEntryAsmName(entry));
        appendCode("", "INC", "SI");
        appendCode("", "MOV", "AL, [SI]");
        appendCode("", "CBW");
        
        appendEmptyLine();

        appendCode("", "ADD", "SI, AX");
        appendCode("", "INC", "SI");
        appendCode("","MOV", "AL, '$'");
        appendCode("", "MOV", "[SI], AL");
        ++gid;
        
        appendEmptyLine();

        appendEndOfLineImpresion();

        appendEmptyLine();
        return;
      }
      appendCode("", "MOV", "AH, 0AH");
      appendCode("", "LEA", "DX, AUX_STRING");
      appendCode("", "INT", "21H");
      
      appendEmptyLine();

      appendCode("", "MOV", "BX, 10");
      appendCode("", "XOR", "AX, AX");
      appendCode("", "LEA", "SI, AUX_STRING");
      appendCode("", "INC", "SI");
      appendCode("", "INC", "SI");

      appendEmptyLine();

      appendCode("VUELVE_LECTURA" + gid + ":");
      appendCode("", "MOV", "DL, [SI]");
      appendCode("", "CMP", "DL, 0DH");
      appendCode("", "JE", "FIN_LECTURA" + gid);
      
      appendEmptyLine();

      appendCode("", "MUL", "BX");
      appendCode("", "MOV", "DX, AX");
      appendCode("", "MOV", "AL, [SI]");
      appendCode("", "SUB", "AL, '0'");
      appendCode("", "CBW");

      appendEmptyLine();

      appendCode("", "ADD", "DX, AX");
      appendCode("", "MOV", "AX, DX");
      appendCode("", "INC", "SI");
      appendCode("", "JMP", "VUELVE_LECTURA" + gid);
        
      appendEmptyLine();

      appendCode("FIN_LECTURA" + gid + ":");
      appendCode("", "MOV", getEntryAsmName(entry) + ", AX");
      
      appendEmptyLine();

      appendEndOfLineImpresion();

      appendEmptyLine();
      ++gid;
      return;
    }

    boolean isPrint = firstChildToken.getSymbolTableID() == ReservedWord.PRINT.ordinal();
    if (isPrint) {
      if (!generateOperation(children.get(1))) {
        NodeTerminal terminalNode = (NodeTerminal) children.get(1).getChildren().get(0).getChildren().get(0);
        int identifierID = terminalNode.getToken().getSymbolTableID();
        String asmName;
        if (terminalNode.getToken().getTokenType() == TokenType.ID) {
          EntryID entry = (EntryID) symbolTable.getEntries().get(identifierID);
          asmName = getEntryAsmName(entry);
        } else {
          String literal = terminalNode.getToken().getValue();
          literal = literal.substring(1, literal.length() - 1);
          asmName = "AUX_STRING";
          appendStringLiteralAssignation("AUX_STRING", literal);
          appendEmptyLine();
        }
        appendCode("", "MOV", "AH, 09H");
        appendCode("", "LEA", "DX, [" + asmName + " + 2]");
        appendCode("", "INT", "21H");
        
        appendEmptyLine();
        
        appendEndOfLineImpresion();
        
        appendEmptyLine();
        return;
      }
      appendCode("", "MOV", "AX, BX");
      appendCode("", "MOV", "BX, 10");
      appendCode("", "XOR", "CX, CX");

      appendEmptyLine();

      appendCode("PUSHEAR" + gid + ":");
      appendCode("", "XOR", "DX, DX");
      appendCode("", "DIV", "BX");
      appendCode("", "PUSH", "DX");
      appendCode("", "INC", "CX");
      appendCode("", "TEST", "AX, AX");
      appendCode("", "JNZ", "PUSHEAR" + gid);
      
      appendEmptyLine();

      appendCode("POPEAR" + gid + ":");
      appendCode("", "POP", "DX");
      appendCode("", "ADD", "DL, '0'");
      appendCode("", "MOV", "AH, 02H");
      appendCode("", "INT", "21H");
      appendCode("", "LOOP", "POPEAR" + gid);
      
      appendEmptyLine();

      appendEndOfLineImpresion();

      appendEmptyLine();

      ++gid;
      return;
    }

    boolean isWhile = firstChildToken.getSymbolTableID() == ReservedWord.WHILE.ordinal();
    if (isWhile) {
      int curr_id = gid;
      ++gid;
      appendCode("START_WHILE" + curr_id + ":");
      
      Node conditionNode = children.get(1);
      generateOperation(conditionNode.getChildren().get(0));
      appendCode("", "MOV", "AX, BX");
      
      generateOperation(conditionNode.getChildren().get(2));
      appendCode("", "MOV", "DX, BX");

      String comparationSymbol = ((NodeTerminal)conditionNode.getChildren().get(1).getChildren().get(0)).getToken().getValue();
      String inverseComparisonJump = getInverseComparisonJump(comparationSymbol);

      appendCode("", "CMP", "AX, DX");
      appendCode("", inverseComparisonJump, "END_WHILE" + curr_id);

      appendEmptyLine();

      cycleIds.push(curr_id);
      generateSentences(node.getChildren().get(3));

      appendCode("", "JMP", "START_WHILE" + curr_id);

      appendEmptyLine();
      
      appendCode("END_WHILE" + curr_id +":");

      appendEmptyLine();

      cycleIds.pop();
      return;
    }

    boolean isIf = firstChildToken.getSymbolTableID() == ReservedWord.IF.ordinal();
    if (isIf) {
      int curr_id = gid;
      ++gid;
      appendCode("START_IF" + curr_id + ":");
      
      Node conditionNode = children.get(1);
      generateOperation(conditionNode.getChildren().get(0));
      appendCode("", "MOV", "AX, BX");
      
      generateOperation(conditionNode.getChildren().get(2));
      appendCode("", "MOV", "DX, BX");

      String comparationSymbol = ((NodeTerminal)conditionNode.getChildren().get(1).getChildren().get(0)).getToken().getValue();
      String inverseComparisonJump = getInverseComparisonJump(comparationSymbol);

      appendCode("", "CMP", "AX, DX");
      if (node.getChildren().size() > 7) {
        appendCode("", inverseComparisonJump, "ELSE_PART" + curr_id);
      } else {
        appendCode("", inverseComparisonJump, "END_IF" + curr_id);
      }

      appendEmptyLine();

      generateSentences(node.getChildren().get(3));

      appendCode("", "JMP", "END_IF" + curr_id);

      appendEmptyLine();

      if (node.getChildren().size() > 7) {
        appendCode("ELSE_PART" + curr_id + ":");
        appendEmptyLine();
        generateSentences(node.getChildren().get(7));
      }

      appendCode("END_IF" + curr_id + ":");

      appendEmptyLine();
      return;
    }

    boolean isBreak = firstChildToken.getSymbolTableID() == ReservedWord.BREAK.ordinal();
    if (isBreak) {
      appendCode("", "JMP", "END_WHILE" + cycleIds.peek());
      appendEmptyLine();
      return;
    }

    boolean isContinue = firstChildToken.getSymbolTableID() == ReservedWord.CONTINUE.ordinal();
    if (isContinue) {
      appendCode("", "JMP", "START_WHILE" + cycleIds.peek());
      appendEmptyLine();
      return;
    }

    boolean isExit = firstChildToken.getSymbolTableID() == ReservedWord.EXIT.ordinal();
    if (isExit) {
      appendCode("", "MOV", "AH, 4CH");
      appendCode("", "MOV", "AL, 01");
      appendCode("", "INT", "21H");
      appendEmptyLine();
      return;
    }
  }

  private boolean generateOperation(Node node) {
    ArrayList<Node> children = node.getChildren();

    if (children.getFirst() instanceof NodeNonTerminal) {
      NodeNonTerminal firstChild = (NodeNonTerminal) children.getFirst();
      boolean isData = firstChild.getProduction() == Production.DATA;
      if (isData) {
        NodeTerminal dataNode = (NodeTerminal) firstChild.getChildren().getFirst();
        Token dataToken = dataNode.getToken();
        Entry entry = symbolTable.getEntries().get(dataToken.getSymbolTableID());
        boolean isID = entry instanceof EntryID;
        if (isID) {
          EntryID entryID = (EntryID) entry;
          boolean isString = entryID.getDataType() == DataType.STRING;
          if (isString) {
            return false;
          }
          appendCode("", "MOV", "BX, " + getEntryAsmName(entryID));
          return true;
        }
        String literal = ((EntryLiteral) entry).getText();
        if (Character.isDigit(literal.charAt(0))) {
          appendCode("", "MOV", "BX, " + literal);
          return true;
        }
        return false;
      }
    }

    appendCode("", "PUSH", "AX");
    appendCode("", "PUSH", "DX");

    appendEmptyLine();

    generateOperation(children.get(1));

    appendEmptyLine();

    appendCode("", "MOV", "AX, BX");

    appendEmptyLine();

    generateOperation(children.get(3));

    appendEmptyLine();

    appendCode("", "MOV", "DX, BX");
    char operator = ((NodeTerminal)children.get(2).getChildren().get(0)).getToken().getValue().charAt(0);
    switch (operator) {
      case '+':
        appendCode("","ADD", "AX, DX");
        break;
      case '-':
        appendCode("","SUB", "AX, DX");
        break;
      case '*':
        appendCode("","MUL", "DX");
        break;
    }
    appendCode("", "MOV", "BX, AX");
    appendCode("", "POP", "AX");
    appendCode("", "POP", "DX");
    appendEmptyLine();
    return true;
  }

  private void generateLowerPart() {
    appendCode("", ".EXIT");
    appendCode("MAIN", "ENDP");
    appendCode("END");
  }

  private String getInverseComparisonJump(String comparationSymbol) {
    String inverseComparisonJump = "";
    switch (comparationSymbol) {
      case "<":
        inverseComparisonJump = "JNL";
        break;
      case "<=":
        inverseComparisonJump = "JNLE";
        break;
      case "==":
        inverseComparisonJump = "JNE";
        break;
      case "!=":
        inverseComparisonJump = "JEQ";
        break;
      case ">":
        inverseComparisonJump = "JNG";
        break;
      case ">=":
        inverseComparisonJump = "JNGE";
        break;
    }
    return inverseComparisonJump;
  }

  private void appendStringLiteralAssignation(String asmName, String literal) {
    for (int i = 0; i < literal.length(); ++i) {
      appendCode("", "MOV", "[" + asmName + " + " + (i + 2) + "], '" + literal.charAt(i)+ "'");
    }
    appendCode("", "MOV", "[" + asmName + " + " + (literal.length() + 2) + "], '$'");
  }

  private void appendEmptyLine() {
    appendCode("", "", "");
  }

  private void appendEndOfLineImpresion() {
    appendCode("", "MOV", "AH, 02H");
    appendCode("", "MOV", "DL, 0AH");
    appendCode("", "INT", "21H");
  }

  private void appendCode(String p1) {
    intermediateCode.append(p1 + "\n");
  }

  private void appendCode(String p1, String p2) {
    intermediateCode.append(p1 + "\t" + p2 + "\n");
  }

  private void appendCode(String p1, String p2, String p3) {
    intermediateCode.append(p1 + "\t" + p2 + "\t" + p3 + "\n");
  }
}