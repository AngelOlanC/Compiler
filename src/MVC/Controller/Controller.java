package MVC.Controller;

import java.awt.Font;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;

import MVC.Model.CodeGenerator.CodeGenerator;
import MVC.Model.Parser.Parser;
import MVC.Model.Scanner.Scanner;
import MVC.Model.Scanner.Token;
import MVC.Model.SemanticAnalysis.SemanticAnalyzer;
import MVC.Model.SymbolTable.SymbolTable;
import MVC.View.View;

public class Controller implements ActionListener, KeyListener {
  private View view;
  private SymbolTable symbolTable;
  private Scanner scanner;
  private Parser parser;
  private SemanticAnalyzer semanticAnalyzer;
  private CodeGenerator codeGenerator;

  public Controller(View view) {
    this.view = view;
    view.setListener(this);
    view.resetButtons();
    view.resetResultPanels();
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    if (e.getSource() == view.getStartScannerButton()) {
      symbolTable = new SymbolTable();
      String sourceCode = view.getCodingArea().getText(); 
      view.getStartSemanticButton().setEnabled(false);

      scanner = new Scanner(symbolTable, sourceCode);
      if (!scanner.analyze()) {
        view
          .getTokensPane()
          .setText("ERROR"); 
        view.getStartParserButton().setEnabled(false);
        view.getStartSemanticButton().setEnabled(false);
        return;
      }
      ArrayList<Token> tokenStream = scanner.getTokenStream();
      view.getTokensPane()
        .setText(
          tokenStream
            .stream()
            .reduce("", (partialString, token) -> partialString + token.toString() + "\n", String::concat)
        );
      view.getStartScannerButton().setEnabled(false);
      view.getStartParserButton().setEnabled(true);
      return;
    }

    if (e.getSource() == view.getStartParserButton()) {
      ArrayList<Token> tokenStream = scanner.getTokenStream();
      parser = new Parser(tokenStream);
      if (!parser.analyze()) {
        view
          .getParserPane()
          .setText("ERROR");
          view.getStartSemanticButton().setEnabled(false);
          return;
      }
      view
        .getParserPane()
        .setText("CORRECT"); 
      System.out.println(parser.getParseTree());
      view.getStartParserButton().setEnabled(false);
      view.getStartSemanticButton().setEnabled(true);
      return;
    }

    if (e.getSource() == view.getStartSemanticButton()) {
      semanticAnalyzer = new SemanticAnalyzer(symbolTable, parser.getParseTree());
      if (!semanticAnalyzer.analyze()) {
        view
          .getSemanticPane()
          .setText("ERROR");
          return;
      }
      view
      .getSemanticPane()
      .setText("CORRECT"); 
      view.getStartSemanticButton().setEnabled(false);
      view.getStartIntermediateCodeGenerationButton().setEnabled(true);
      return;
    }
    if (e.getSource() == view.getStartIntermediateCodeGenerationButton()) {
      codeGenerator = new CodeGenerator(symbolTable, parser.getParseTree());
      String intermediateAndMachineCode = codeGenerator.generate();
      view.getIntermediateAndMachineCodePane().setText(intermediateAndMachineCode);
      view.getStartIntermediateCodeGenerationButton().setEnabled(false);
      view.getCopyIntermediateCodeButton().setEnabled(true);
      return;
    }
    if (e.getSource() == view.getCopyIntermediateCodeButton()) {
      StringSelection stringSelection = new StringSelection(codeGenerator.getIntermediateCode());
      Clipboard clpbrd = Toolkit.getDefaultToolkit().getSystemClipboard();
      clpbrd.setContents (stringSelection, null);
      return;
    }
  }

  private boolean ctrlPressed = false;

  @Override
  public void keyPressed(KeyEvent e) {
    if (e.getKeyCode() == KeyEvent.VK_CONTROL) {
      ctrlPressed = true;
      return;
    }
    if (!ctrlPressed) {
      return;
    }
    int currentFontSize = view.getCodingArea().getFont().getSize(), newFontSize;
    if (e.getKeyCode() == KeyEvent.VK_PLUS) {
      newFontSize = currentFontSize + 1;
    } else if (e.getKeyCode() == KeyEvent.VK_MINUS) {
      newFontSize = currentFontSize - 1;
    } else if (e.getKeyCode() == KeyEvent.VK_0) {
      newFontSize = 16;
    } else {
      if (e.getKeyCode() == KeyEvent.VK_X || e.getKeyCode() == KeyEvent.VK_V) {
        resetView();
      }
      return;
    }
    Font newFont = new Font(Font.MONOSPACED, Font.PLAIN, newFontSize);
    view.getCodingArea().setFont(newFont);
  }
  @Override
  public void keyReleased(KeyEvent e) {
    if (e.getKeyCode() == KeyEvent.VK_CONTROL) {
      ctrlPressed = false;
      return;
    }
  }
  @Override
  public void keyTyped(KeyEvent e) {
    if (!ctrlPressed) {
      resetView();
    }
  }

  private void resetView() {
    view.resetButtons();
    view.resetResultPanels();
  }
}