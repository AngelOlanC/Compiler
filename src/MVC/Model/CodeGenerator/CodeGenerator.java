package MVC.Model.CodeGenerator;

import java.util.ArrayList;
import java.util.HashMap;
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

public class CodeGenerator {
  private SymbolTable symbolTable;
  private ParseTree parseTree;
  private HashMap<String, Integer> declarationDecimalOffset;
  private HashMap<String, String> declarationBinaryOffset;
  private HashMap<String, Integer> labelDecimalOffset;
  private ArrayList<String> intermediateCodeList, machineCodeList;
  private Stack<WaitingBreak> waitingBreaks;
  private String intermediateCode, machineCode, combined;
  private String segment;
  Stack<Integer> cycleIds;
  private int gid, currentByte;
  
  public CodeGenerator(SymbolTable symbolTable, ParseTree parseTree) {
    this.symbolTable = symbolTable;
    this.parseTree = parseTree;
  }

  public String generate() {
    intermediateCodeList = new ArrayList<String>();
    machineCodeList = new ArrayList<String>();
    cycleIds = new Stack<>();
    declarationBinaryOffset = new HashMap<>();
    declarationDecimalOffset = new HashMap<>();
    labelDecimalOffset = new HashMap<>();
    waitingBreaks = new Stack<>();
    
    currentByte = 0;
    segment = "0000";
    generateUpperPart();
    
    gid = 0;
    segment = "00A0";
    currentByte = 0;
    generateCode();

    generateLowerPart();
    
    buildIntermediateCode();
    buildMachineCode();
    buildCombinedCode();
    return combined;
  }

  private void generateUpperPart() {
    appendCode("", ".MODEL SMALL");
    appendCode("", ".486");
    appendCode("", ".STACK");
    appendCode("", ".DATA");
    appendBinary("");
    appendBinary("");
    appendBinary("");
    appendBinary("");
    for (Entry entry : symbolTable.getEntries()) {
      if (!(entry instanceof EntryID)) continue;
      EntryID entryID = (EntryID) entry;
      String value, type;
      if (entryID.getDataType() == DataType.INT) {
        type = "DW";
        value = "?";
      } else {
        type = "DB";
        value = "80 DUP('$')";
      }
      String asmName = getEntryAsmName(entryID);
      declarationDecimalOffset.put(asmName, currentByte);
      declarationBinaryOffset.put(asmName, int16ToBinary(currentByte));
      appendCode(asmName, type, value);
      appendBinary("0000 0000");
      if (type.equals("DB")) {
        currentByte += 79;
      }
    }
    declarationDecimalOffset.put("AUX_STRING", currentByte);
    declarationBinaryOffset.put("AUX_STRING", int16ToBinary(currentByte));
    appendCode("AUX_STRING", "DB", "80 DUP('$')");
    appendCode("", ".CODE");
    appendCode("MAIN", "PROC", "FAR");
    appendCode("", ".STARTUP");

    appendBinary("0000 0000");
    appendBinary("");
    appendBinary("");
    appendBinary("");
  }

  private void generateCode() {
    generateSentences(parseTree.getRoot().getChildren().get(1));
  }

  private void generateSentences(Node node) {
    for (Node child : node.getChildren()) {
      generateSentence(child);
    }
  }

  private void generateSentence(Node node) {
    ArrayList<Node> children = node.getChildren();
    if (children.isEmpty()) {
      return;
    }

    Node firstChild = children.getFirst();
    boolean isDeclaration = firstChild instanceof NodeNonTerminal;
    if (isDeclaration) {
      return;
    }
    
    NodeTerminal firstChildTerminal = (NodeTerminal) firstChild;
    Token firstChildToken = firstChildTerminal.getToken();

    boolean isAssignment = firstChildToken.getTokenType() == TokenType.ID;
    if (isAssignment) {
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
      appendBinary("1000 1001 0001 1110 " + declarationBinaryOffset.get(asmName));
      
      appendEmptyLine();
      return;
    }
    
    boolean isRead = firstChildToken.getSymbolTableID() == ReservedWord.READ.ordinal();
    if (isRead) {
      int identifierID = ((NodeTerminal)children.get(1)).getToken().getSymbolTableID();
      EntryID entry = (EntryID) symbolTable.getEntries().get(identifierID);
      DataType variableDataType = entry.getDataType();
      if (variableDataType == DataType.STRING) {
        String asmName = getEntryAsmName(entry);
        appendCode("", "LEA", "DX, " + asmName);
        appendCode("", "MOV", "AH, 0AH");
        appendCode("", "INT", "21H");

        appendBinary("1000 1101 0001 0110 " + declarationBinaryOffset.get(asmName));
        appendBinary("1011 0100 " + int8ToBinary(10));
        appendBinary("1100 1101 0010 0001");

        appendEmptyLine();

        appendCode("", "LEA", "SI, " + asmName);
        appendCode("", "INC", "SI");
        appendCode("", "MOV", "AL, [SI]");
        appendCode("", "CBW");

        appendBinary("1000 1101 0011 0110 " + declarationBinaryOffset.get(asmName));
        appendBinary("0100 0110");
        appendBinary("1000 1010 0000 0100");
        appendBinary("1001 1000");
        
        appendEmptyLine();

        appendCode("", "ADD", "SI, AX");
        appendCode("", "INC", "SI");
        appendCode("","MOV", "AL, '$'");
        appendCode("", "MOV", "[SI], AL");

        appendBinary("0000 0001 1100 0110");
        appendBinary("0100 0110");
        appendBinary("1011 0000 " + int8ToBinary(36));
        appendBinary("1000 1000 0000 0100");

        ++gid;
        
        appendEmptyLine();

        appendEndOfLineImpresion();

        appendEmptyLine();
        return;
      }
      appendCode("", "MOV", "AH, 0AH");
      appendCode("", "LEA", "DX, AUX_STRING");
      appendCode("", "INT", "21H");

      appendBinary("1011 0100 " + int8ToBinary(10));
      appendBinary("1000 1101 0001 0110 " + declarationBinaryOffset.get("AUX_STRING"));
      appendBinary("1100 1101 0010 0001");
      
      appendEmptyLine();

      appendCode("", "MOV", "BX, 10");
      appendCode("", "XOR", "AX, AX");
      appendCode("", "LEA", "SI, AUX_STRING");
      appendCode("", "INC", "SI");
      appendCode("", "INC", "SI");

      appendBinary("1011 1011 " + int16ToBinary(10));
      appendBinary("0011 0001 1100 0000");
      appendBinary("1000 1101 0011 0110 " + declarationBinaryOffset.get("AUX_STRING"));
      appendBinary("0100 0110");
      appendBinary("0100 0110");

      appendEmptyLine();

      labelDecimalOffset.put("VUELVE_LECTURA" + gid, currentByte);
      appendCode("VUELVE_LECTURA" + gid + ":");
      appendCode("", "MOV", "DL, [SI]");
      appendCode("", "CMP", "DL, 0DH");
      appendCode("", "JE", "FIN_LECTURA" + gid);

      appendBinary("");
      appendBinary("1000 1010 0001 0100");
      appendBinary("1000 0000 1111 1010 " + int8ToBinary(13));
      appendBinary("0111 0100 " + signedInt8ToBinary(17));
      
      appendEmptyLine();

      appendCode("", "MUL", "BX");
      appendCode("", "MOV", "DX, AX");
      appendCode("", "MOV", "AL, [SI]");
      appendCode("", "SUB", "AL, '0'");
      appendCode("", "CBW");

      appendBinary("1111 0111 1110 0011");
      appendBinary("1000 1001 1100 0010");
      appendBinary("1000 1010 0000 0100");
      appendBinary("0010 1100 " + int8ToBinary(48));
      appendBinary("1001 1000");

      appendEmptyLine();

      appendCode("", "ADD", "DX, AX");
      appendCode("", "MOV", "AX, DX");
      appendCode("", "INC", "SI");
      appendCode("", "JMP", "VUELVE_LECTURA" + gid);

      appendBinary("0000 0001 1100 0010");
      appendBinary("1000 1001 1101 0000");
      appendBinary("0100 0110");
      appendBinary("1110 1011 " + signedInt8ToBinary(labelDecimalOffset.get("VUELVE_LECTURA" + gid) - currentByte));

      appendEmptyLine();

      String asmName = getEntryAsmName(entry);

      appendCode("FIN_LECTURA" + gid + ":");
      appendCode("", "MOV", asmName + ", AX");
      
      appendBinary("");
      appendBinary("1000 1001 0000 0110 " + declarationBinaryOffset.get(asmName));
      
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

        appendBinary("1011 0100 " + int8ToBinary(9));
        appendBinary("1000 1101 0001 0110 " + int16ToBinary(declarationDecimalOffset.get(asmName) + 2));
        appendBinary("1100 1101 0010 0001");
        
        appendEmptyLine();
        
        appendEndOfLineImpresion();
        
        appendEmptyLine();
        return;
      }
      appendCode("", "MOV", "AX, BX");
      appendCode("", "MOV", "BX, 10");
      appendCode("", "XOR", "CX, CX");

      appendBinary("1000 1001 1101 1000");
      appendBinary("1011 1011 " + int8ToBinary(10));
      appendBinary("0011 0001 1100 1001");

      appendEmptyLine();

      labelDecimalOffset.put("PUSHEAR" + gid, currentByte);
      appendCode("PUSHEAR" + gid + ":");
      appendCode("", "XOR", "DX, DX");
      appendCode("", "DIV", "BX");
      appendCode("", "PUSH", "DX");
      appendCode("", "INC", "CX");
      appendCode("", "TEST", "AX, AX");
      appendCode("", "JNZ", "PUSHEAR" + gid);

      appendBinary("");
      appendBinary("0011 0001 1101 0010");
      appendBinary("1111 0111 1111 0011");
      appendBinary("1111 1111 1111 0010");
      appendBinary("0100 0001");
      appendBinary("1000 0101 1100 0000");
      appendBinary("0111 0101 " + signedInt8ToBinary(-10));
      
      appendEmptyLine();

      labelDecimalOffset.put("POPEAR" + gid, currentByte);
      appendCode("POPEAR" + gid + ":");
      appendCode("", "POP", "DX");
      appendCode("", "ADD", "DL, '0'");
      appendCode("", "MOV", "AH, 02H");
      appendCode("", "INT", "21H");
      appendCode("", "LOOP", "POPEAR" + gid);

      appendBinary("");
      appendBinary("0101 1010");
      appendBinary("1000 0000 1100 0010 " + int8ToBinary(48));
      appendBinary("1011 0100 " + int8ToBinary(2));
      appendBinary("1100 1101 0010 0001");
      appendBinary("1110 0010 " + signedInt8ToBinary(-8));
      
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
      labelDecimalOffset.put("START_WHILE" + curr_id, currentByte);
      appendCode("START_WHILE" + curr_id + ":");
      appendBinary("");
      
      Node conditionNode = children.get(1);
      generateOperation(conditionNode.getChildren().get(0));
      appendCode("", "MOV", "AX, BX");
      appendBinary("1000 1001 1101 1000");
      
      generateOperation(conditionNode.getChildren().get(2));
      appendCode("", "MOV", "DX, BX");
      appendBinary("1000 1001 1101 1010");

      String comparationSymbol = ((NodeTerminal)conditionNode.getChildren().get(1).getChildren().get(0)).getToken().getValue();
      String inverseComparisonJump = getInverseComparisonJump(comparationSymbol);

      appendCode("", "CMP", "AX, DX");
      appendBinary("0011 1001 1100 0010");
      appendCode("", inverseComparisonJump, "END_WHILE" + curr_id);
      int idJump = machineCodeList.size(), byteJump = currentByte;
      appendBinary("0001 " + getTTTN(inverseComparisonJump) + " ");

      appendEmptyLine();

      cycleIds.push(curr_id);
      generateSentences(node.getChildren().get(3));

      appendCode("", "JMP", "START_WHILE" + curr_id);
      appendBinary("1110 1011 " + signedInt8ToBinary(labelDecimalOffset.get("START_WHILE" + curr_id) - currentByte));

      appendEmptyLine();
      
      machineCodeList.set(idJump, machineCodeList.get(idJump) + signedInt8ToBinary(currentByte - byteJump));
      while (!waitingBreaks.empty() && waitingBreaks.peek().getNameWhile().equals("END_WHILE" + curr_id)) {
        WaitingBreak wb = waitingBreaks.pop();
        machineCodeList.set(wb.getIdJump(), machineCodeList.get(wb.getIdJump()) + signedInt8ToBinary(currentByte - wb.getByteJump()));
      }
      appendCode("END_WHILE" + curr_id +":");
      appendBinary("");

      appendEmptyLine();

      cycleIds.pop();
      return;
    }

    boolean isIf = firstChildToken.getSymbolTableID() == ReservedWord.IF.ordinal();
    if (isIf) {
      int curr_id = gid;
      ++gid;
      labelDecimalOffset.put("START_IF" + curr_id, currentByte);
      appendCode("START_IF" + curr_id + ":");
      appendBinary("");
      
      Node conditionNode = children.get(1);
      generateOperation(conditionNode.getChildren().get(0));
      appendCode("", "MOV", "AX, BX");
      appendBinary("1000 1001 1101 1000");
      
      generateOperation(conditionNode.getChildren().get(2));
      appendCode("", "MOV", "DX, BX");
      appendBinary("1000 1001 1101 1010");

      String comparationSymbol = ((NodeTerminal)conditionNode.getChildren().get(1).getChildren().get(0)).getToken().getValue();
      String inverseComparisonJump = getInverseComparisonJump(comparationSymbol);

      appendCode("", "CMP", "AX, DX");
      appendBinary("0011 1001 1100 0010");
      
      int idElseJump = -1, byteElseJump = -1, idEndJump = -1, byteEndJump = -1;
      if (node.getChildren().size() > 7) {
        idElseJump = machineCodeList.size();
        byteElseJump = currentByte;
        appendCode("", inverseComparisonJump, "ELSE_PART" + curr_id);
        appendBinary("0111 " + getTTTN(inverseComparisonJump) + " ");
      } else {
        idEndJump = machineCodeList.size();
        byteEndJump = currentByte;
        appendCode("", inverseComparisonJump, "END_IF" + curr_id);
        appendBinary("0111" + getTTTN(inverseComparisonJump) + " ");
      }

      appendEmptyLine();

      generateSentences(node.getChildren().get(3));

      int idEndJump2 = machineCodeList.size(), byteEndJump2 = machineCodeList.size();
      appendCode("", "JMP", "END_IF" + curr_id);
      appendBinary("1110 1011 ");

      appendEmptyLine();

      if (node.getChildren().size() > 7) {
        machineCodeList.set(idElseJump, machineCodeList.get(idElseJump) + signedInt8ToBinary(currentByte - byteElseJump));
        appendCode("ELSE_PART" + curr_id + ":");
        appendBinary("");
        appendEmptyLine();
        generateSentences(node.getChildren().get(7));
      } else {
        machineCodeList.set(idEndJump, machineCodeList.get(idEndJump) + signedInt8ToBinary(currentByte - byteEndJump));
      }
      machineCodeList.set(idEndJump2, machineCodeList.get(idEndJump2) + signedInt8ToBinary(currentByte - byteEndJump2));
      appendCode("END_IF" + curr_id + ":");
      appendBinary("");
      appendEmptyLine();
      return;
    }

    boolean isBreak = firstChildToken.getSymbolTableID() == ReservedWord.BREAK.ordinal();
    if (isBreak) {
      waitingBreaks.push(new WaitingBreak("END_WHILE" + cycleIds.peek(), machineCodeList.size(), currentByte));
      appendCode("", "JMP", "END_WHILE" + cycleIds.peek());
      appendBinary("1110 1011 ");
      appendEmptyLine();
      return;
    }

    boolean isContinue = firstChildToken.getSymbolTableID() == ReservedWord.CONTINUE.ordinal();
    if (isContinue) {
      appendCode("", "JMP", "START_WHILE" + cycleIds.peek());
      appendBinary("1110 1011 " + signedInt8ToBinary(labelDecimalOffset.get("START_WHILE" + cycleIds.peek()) - currentByte));
      appendEmptyLine();
      return;
    }

    boolean isExit = firstChildToken.getSymbolTableID() == ReservedWord.EXIT.ordinal();
    if (isExit) {
      appendCode("", "MOV", "AH, 4CH");
      appendCode("", "MOV", "AL, 01");
      appendCode("", "INT", "21H");

      appendBinary("1011 0100 " + int8ToBinary(76));
      appendBinary("1011 0000 " + int8ToBinary(1));
      appendBinary("1100 1101 0010 0001");

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
          String asmName = getEntryAsmName(entryID);
          appendCode("", "MOV", "BX, " + asmName);
          appendBinary("1000 1011 0001 1110 " + declarationBinaryOffset.get(asmName));
          return true;
        }
        String literal = ((EntryLiteral) entry).getText();
        if (Character.isDigit(literal.charAt(0))) {
          appendCode("", "MOV", "BX, " + literal);
          appendBinary("1011 1011 " + int8ToBinary(Integer.parseInt(literal)));
          return true;
        }
        return false;
      }
    }

    appendCode("", "PUSH", "AX");
    appendCode("", "PUSH", "DX");

    appendBinary("1111 1111 1111 0000");
    appendBinary("1111 1111 1111 0010");

    appendEmptyLine();

    generateOperation(children.get(1));

    appendEmptyLine();

    appendCode("", "MOV", "AX, BX");
    appendBinary("1000 1001 1101 1000");

    appendEmptyLine();

    generateOperation(children.get(3));

    appendEmptyLine();

    TokenType operator = ((NodeTerminal)children.get(2).getChildren().get(0)).getToken().getTokenType();
    switch (operator) {
      case TokenType.ADD:
        appendCode("","ADD", "AX, BX");
        appendBinary("0000 0001 1101 1000");
        break;
      case TokenType.SUB:
        appendCode("","SUB", "AX, BX");
        appendBinary("0010 1001 1101 1000");
        break;
      case TokenType.MUL:
        appendCode("","MUL", "BX");
        appendBinary("1111 0111 1110 0011");
        break;
      case TokenType.DIV:
        appendCode("","XOR", "DX, DX");
        appendCode("","DIV", "BX");

        appendBinary("0011 0001 1101 0010");
        appendBinary("1111 0111 1111 0011");
        break;
      case TokenType.REM:
        appendCode("","XOR", "DX, DX");
        appendCode("","DIV", "BX");
        appendCode("" , "MOV", "AX, DX");

        appendBinary("0011 0001 1101 0010");
        appendBinary("1111 0111 1111 0011");
        appendBinary("1000 1001 1101 0000");
        break;
      default:
        break;
    }
    appendCode("", "MOV", "BX, AX");
    appendCode("", "POP", "DX");
    appendCode("", "POP", "AX");

    appendBinary("1000 1001 1100 0011");
    appendBinary("0101 1010");
    appendBinary("0101 1000");

    appendEmptyLine();
    return true;
  }

  private void generateLowerPart() {
    appendCode("", ".EXIT");
    appendCode("MAIN", "ENDP");
    appendCode("END");

    appendBinary("");
    appendBinary("");
    appendBinary("");
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
        inverseComparisonJump = "JE";
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

  private String getTTTN(String inverseComparisonJump) {
    return switch(inverseComparisonJump) {
      case "JNL" -> "1101";
      case "JNLE" -> "1111";
      case "JNE" -> "0101";
      case "JE" -> "0100";
      case "JNG" -> "1110";
      case "JNGE" -> "1100";
      default -> "xxxx";
    };
  }

  private void appendStringLiteralAssignation(String asmName, String literal) {
    literal += '$';
    for (int i = 0; i < literal.length(); ++i) {
      appendCode("", "MOV", "[" + asmName + " + " + (i + 2) + "], '" + literal.charAt(i)+ "'");

      appendBinary("1100 0110 0000 0110 " + int8ToBinary(declarationDecimalOffset.get(asmName) + i + 2) + " " + int8ToBinary(literal.charAt(i)));
    }
  }

  private void appendEmptyLine() {
    appendCode("", "", "");

    appendBinary("");
  }

  private void appendEndOfLineImpresion() {
    appendCode("", "MOV", "AH, 02H");
    appendCode("", "MOV", "DL, 0AH");
    appendCode("", "INT", "21H");

    appendBinary("1011 0100 " + int8ToBinary(2));
    appendBinary("1011 0010 " + int8ToBinary(10));
    appendBinary("1100 1101 0010 0001");
  }

  private void appendCode(String p1) {
    appendCode(p1, "", "");
  }

  private void appendCode(String p1, String p2) {
    appendCode(p1, p2, "");
  }

  private void appendCode(String p1, String p2, String p3) {
    intermediateCodeList.add(String.format("%-10s %-4s %-22s", p1, p2, p3));
  }

  private void appendBinary(String nibbles) {
    String str = segment + ":" + intToHex(currentByte) + " " + nibbles;
    if (nibbles.equals("")) {
      str = "";
    }
    int nibbles_cnt = nibbles.split(" ").length;
    currentByte += nibbles_cnt / 2;
    machineCodeList.add(str + "\n");
  }
  
  private void buildIntermediateCode() {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < intermediateCodeList.size(); ++i) {
      sb.append(intermediateCodeList.get(i) + "\n");
    }
    intermediateCode = sb.toString();
  }

  private void buildMachineCode() {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < machineCodeList.size(); ++i) {
      sb.append(machineCodeList.get(i) + "\n");
    }
    combined = sb.toString();
  }

  private void buildCombinedCode() {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < machineCodeList.size(); ++i) {
      sb.append(intermediateCodeList.get(i) + " " + machineCodeList.get(i) + "\n");
    }
    combined = sb.toString();
  }

  private String int16ToBinary(int x) {
    int pot = 1;
    for (int exp = 1; exp < 16; ++exp) {
      pot *= 2;
    }
    String ret = "";
    for (int exp = 15; exp >= 0; --exp) {
      if (x >= pot) {
        ret += "1";
        x %= pot;
      } else {
        ret += "0";
      }
      pot /= 2;
      if (exp == 12 || exp == 8 || exp == 4) {
        ret += " ";
      }
    }
    return ret;
  }

  private String signedInt8ToBinary(int x) {
    String normal = int8ToBinary(Math.abs(x));
    System.out.println(x + " " + normal);
    if (x >= 0) {
      return normal;
    }
    String ret = "";
    for (int i = 0; i < normal.length(); ++i) {
      if (normal.charAt(i) == '0') {
        ret += '1';
      } else if (normal.charAt(i) == '1') {
        ret += '0';
      }
    }
    for (int i = ret.length() - 1; i >= 0; --i) {
      if (ret.charAt(i) == '0') {
        ret = ret.substring(0, i) + '1' + "0".repeat(ret.length() - i - 1);
        break;
      }
    }
    return ret.substring(0, 4) + " " + ret.substring(4, 8);
  }

  private String int8ToBinary(int x) {
    int pot = 1;
    for (int exp = 1; exp < 8; ++exp) {
      pot *= 2;
    }
    String ret = "";
    for (int exp = 7; exp >= 0; --exp) {
      if (x >= pot) {
        ret += "1";
        x %= pot;
      } else {
        ret += "0";
      }
      pot /= 2;
      if (exp == 4) {
        ret += " ";
      }
    }
    return ret;
  }

  private String intToHex(int x) {
    int pot = 16 * 16 * 16;
    String ret = "";
    for (int exp = 3; exp >= 0; --exp) {
      ret += getHexCharacter(x / pot);
      x %= pot;
      pot /= 16;
    }
    return ret;
  }

  private char getHexCharacter(int x) {
    if (x < 10) {
      return Character.toString((char)(48 + x)).charAt(0) ;
    }
    return Character.toString((char)(55 + x)).charAt(0);
  }

  private String getEntryAsmName(EntryID entryID) {
    return entryID.getText() + formatScopeID(entryID.getScopeID());
  }

  private String formatScopeID(int scopeID) {
    if (scopeID < 10 ) return "00" + scopeID;
    if (scopeID < 100) return "0"  + scopeID;
    return "" + scopeID;
  }
  
  public String getIntermediateCode() {
    return intermediateCode;
  }

  public String getMachineCode() {
    return machineCode;
  }

  private class WaitingBreak {
    private String nameWhile;
    int idJump, byteJump;
    public WaitingBreak(String nameWhile, int idJump, int byteJump) {
      this.nameWhile = nameWhile;
      this.idJump = idJump;
      this.byteJump = byteJump;
    }
    public String getNameWhile() {
      return nameWhile;
    }
    public int getIdJump() {
      return idJump;
    }
    public int getByteJump() {
      return byteJump;
    }
  }
}