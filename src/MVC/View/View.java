package MVC.View;

import java.awt.Color;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import javax.swing.text.PlainDocument;

import MVC.Controller.Controller;

public class View extends JFrame {
  private JTextArea codingArea;
  private JTextPane tokensPane, parserPane, semanticPane, intermediateAndMachineCodePane;
  private JButton startScannerButton, startParserButton, startSemanticButton, startIntermediateCodeGenerationButton, copyIntermediateCodeButton;
  private int placedButtons;
  private final int FRAME_WIDTH, FRAME_HEIGHT, HORIZONTAL_SEPARATION;
  private final int WIDTH_CODING_AREA, WIDTH_TOKENS, WIDTH_INTERMEDIATE_CODE;
  private final int X_CODING_AREA, X_TOKENS, X_INTERMEDIATE_AND_MACHINE_CODE, Y_UPPER_PANELS, Y_UPPER_TEXT, Y_BUTTONS;
  private final int BUTTONS, BUTTON_WIDTH, BUTTON_HEIGHT, MARGIN_LEFT_BUTTON;

  public View() {
    FRAME_WIDTH = 1334;
    FRAME_HEIGHT = 600;
    HORIZONTAL_SEPARATION = 30;
    X_CODING_AREA = 50;
    WIDTH_CODING_AREA = 400;
    WIDTH_TOKENS = 140;
    WIDTH_INTERMEDIATE_CODE = 650;
    X_TOKENS = X_CODING_AREA + WIDTH_CODING_AREA + HORIZONTAL_SEPARATION;
    X_INTERMEDIATE_AND_MACHINE_CODE = X_TOKENS + WIDTH_TOKENS + HORIZONTAL_SEPARATION;
    Y_UPPER_PANELS = 50;
    Y_UPPER_TEXT = 10;
    Y_BUTTONS = 480;
    BUTTONS = 5;
    BUTTON_WIDTH = 150;
    BUTTON_HEIGHT = 50;
    MARGIN_LEFT_BUTTON = (FRAME_WIDTH - BUTTONS * BUTTON_WIDTH) / (BUTTONS + 1);
    placedButtons = 0;

    setSize(FRAME_WIDTH, FRAME_HEIGHT);
    setLocationRelativeTo(null);
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setLayout(null);
    setVisible(true);

    makeInterface();

    revalidate();
    repaint();
  }

  private void makeInterface() {
    Font outerTextFont = new Font("New Courier", Font.PLAIN, 26);
    Font innerTextFont = new Font(Font.MONOSPACED, Font.PLAIN, 14);

    JLabel textCodingArea = new JLabel("CODING AREA");
    textCodingArea.setFont(outerTextFont);
    textCodingArea.setBounds(X_CODING_AREA, Y_UPPER_TEXT, 1000, 50);
    add(textCodingArea);

    codingArea = new JTextArea();
    codingArea.setBackground(Color.WHITE);
    codingArea.setOpaque(true);
    codingArea.setBorder(BorderFactory.createLineBorder(Color.BLACK));
    codingArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 16));

    ((PlainDocument) codingArea.getDocument()).setDocumentFilter(new ChangeTabToSpacesFilter(4));

    JScrollPane scrollCode = new JScrollPane(codingArea);
    scrollCode.setBounds(X_CODING_AREA, Y_UPPER_PANELS, WIDTH_CODING_AREA, 400);
    add(scrollCode);  

    JLabel textTokensPane = new JLabel("TOKENS");
    textTokensPane.setFont(outerTextFont);
    textTokensPane.setBounds(X_TOKENS, Y_UPPER_TEXT, 1000, 50);
    add(textTokensPane);

    tokensPane = new JTextPane();
    tokensPane.setEnabled(false);
    tokensPane.setBackground(Color.WHITE);
    tokensPane.setOpaque(true);
    tokensPane.setBorder(BorderFactory.createLineBorder(Color.BLACK));
    tokensPane.setDisabledTextColor(Color.BLACK);
    tokensPane.setFont(innerTextFont);

    JScrollPane scrollTokens = new JScrollPane(tokensPane);
    scrollTokens.setBounds(X_TOKENS, Y_UPPER_PANELS, WIDTH_TOKENS, 200);
    add(scrollTokens);

    JLabel textParserPane = new JLabel("PARSER");
    textParserPane.setFont(outerTextFont);
    
    textParserPane.setBounds(X_TOKENS, 250, 1000, 50);
    add(textParserPane);

    parserPane = new JTextPane();
    parserPane.setEnabled(false);
    parserPane.setBackground(Color.WHITE);
    parserPane.setOpaque(true);
    parserPane.setBorder(BorderFactory.createLineBorder(Color.BLACK));
    parserPane.setDisabledTextColor(Color.BLACK);
    parserPane.setFont(innerTextFont);

    JScrollPane scrollParser = new JScrollPane(parserPane);
    scrollParser.setBounds(X_TOKENS, 300, WIDTH_TOKENS, 50);
    add(scrollParser);

    JLabel textSemanticPane = new JLabel("SEMANTIC");
    textSemanticPane.setFont(outerTextFont);
    textSemanticPane.setBounds(X_TOKENS, 350, 1000, 50);
    add(textSemanticPane);

    semanticPane = new JTextPane();
    semanticPane.setEnabled(false);
    semanticPane.setBackground(Color.WHITE);
    semanticPane.setOpaque(true);
    semanticPane.setBorder(BorderFactory.createLineBorder(Color.BLACK));
    semanticPane.setDisabledTextColor(Color.BLACK);
    semanticPane.setFont(innerTextFont);

    JScrollPane scrollSemantic = new JScrollPane(semanticPane);
    scrollSemantic.setBounds(X_TOKENS, 400, WIDTH_TOKENS, 50);
    add(scrollSemantic);

    JLabel textIntermediateAndMachineCodePane = new JLabel("INTERMEDIATE AND MACHINE CODE");
    textIntermediateAndMachineCodePane.setFont(outerTextFont);
    textIntermediateAndMachineCodePane.setBounds(X_INTERMEDIATE_AND_MACHINE_CODE, Y_UPPER_TEXT, 1000, 50);
    add(textIntermediateAndMachineCodePane);

    intermediateAndMachineCodePane = new JTextPane();
    intermediateAndMachineCodePane.setEnabled(false);
    intermediateAndMachineCodePane.setBackground(Color.WHITE);
    intermediateAndMachineCodePane.setOpaque(true);
    intermediateAndMachineCodePane.setBorder(BorderFactory.createLineBorder(Color.BLACK));
    intermediateAndMachineCodePane.setDisabledTextColor(Color.BLACK);
    intermediateAndMachineCodePane.setFont(innerTextFont);

    JScrollPane scrollIntermediateAndMachineCode = new JScrollPane(intermediateAndMachineCodePane);
    scrollIntermediateAndMachineCode.setBounds(X_INTERMEDIATE_AND_MACHINE_CODE, Y_UPPER_PANELS, WIDTH_INTERMEDIATE_CODE, 400);
    add(scrollIntermediateAndMachineCode);

    placeButton(startScannerButton = new JButton(), "Scanner");
    placeButton(startParserButton = new JButton(), "Parser");
    placeButton(startSemanticButton = new JButton(), "Semantic");
    placeButton(startIntermediateCodeGenerationButton = new JButton(), "Intermediate Code");
    placeButton(copyIntermediateCodeButton = new JButton(), "Copy inter. code");
  }

  private void placeButton(JButton button, String text) {
    add(button);
    button.setBounds(BUTTON_WIDTH * placedButtons + MARGIN_LEFT_BUTTON * (placedButtons + 1),
                     Y_BUTTONS,
                     BUTTON_WIDTH,
                     BUTTON_HEIGHT);
    button.setText(text);
    placedButtons++;
  }

  public void setListener(Controller controller) {
    codingArea.addKeyListener(controller);
    startScannerButton.addActionListener(controller);
    startParserButton.addActionListener(controller);
    startSemanticButton.addActionListener(controller);
    startIntermediateCodeGenerationButton.addActionListener(controller);
    copyIntermediateCodeButton.addActionListener(controller);
  }

  public void resetButtons() {
    startScannerButton.setEnabled(true);
    startParserButton.setEnabled(false);
    startSemanticButton.setEnabled(false);
    startIntermediateCodeGenerationButton.setEnabled(false);
    copyIntermediateCodeButton.setEnabled(false);
  }

  public void resetResultPanels() {
    tokensPane.setText("");
    parserPane.setText("");
    semanticPane.setText("");
    intermediateAndMachineCodePane.setText("");
  }

  public JTextArea getCodingArea() {
    return codingArea;
  }

  public JTextPane getTokensPane() {
    return tokensPane;
  }

  public JTextPane getParserPane() {
    return parserPane;
  }

  public JButton getStartScannerButton() {
    return startScannerButton;
  }

  public JTextPane getSemanticPane() {
    return semanticPane;
  }

  public JButton getStartParserButton() {
    return startParserButton;
  }

  public JButton getStartSemanticButton() {
    return startSemanticButton;
  }

  public JButton getStartIntermediateCodeGenerationButton() {
    return startIntermediateCodeGenerationButton;
  }

  public JTextPane getIntermediateAndMachineCodePane() {
    return intermediateAndMachineCodePane;
  }

  public JButton getCopyIntermediateCodeButton() {
    return copyIntermediateCodeButton;
  }

  private static class ChangeTabToSpacesFilter extends DocumentFilter {
    private String spaces = "";
    
    public ChangeTabToSpacesFilter(int spaceCount) {
      for (int i = 0; i < spaceCount; i++) {
        spaces += " ";
      }
    }

    @Override
    public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr)
            throws BadLocationException {
      string = string.replace("\t", spaces);
      super.insertString(fb, offset, string, attr);
    }
    
    @Override
    public void remove(FilterBypass fb, int offset, int length) throws BadLocationException {
      super.remove(fb, offset, length);
    }
    
    @Override
    public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs)
            throws BadLocationException {
      text = text.replace("\t", spaces);
      super.replace(fb, offset, length, text, attrs);
    }
    
  }
}