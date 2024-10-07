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

import MVC.Controller.Controller;

public class View extends JFrame {
  private JTextArea codingArea;
  private JTextPane tokensPane, parserPane, semanticPane, intermediateCodePane;
  private JButton startScannerButton, startParserButton, startSemanticButton, startIntermediateCodeGenerationButton;
  private int placedButtons;
  private final int FRAME_WIDTH, FRAME_HEIGHT;
  private final int X_CODING_AREA, X_TOKENS, X_PARSER_SEMANTIC, X_INTERMEDIATE_CODE, Y_UPPER_PANELS, Y_LOWER_PANELS, Y_UPPER_TEXT, Y_LOWER_TEXT, Y_BUTTONS;
  private final int BUTTONS, BUTTON_WIDTH, BUTTON_HEIGHT, MARGIN_LEFT_BUTTON;

  public View() {
    FRAME_WIDTH = 1325;
    FRAME_HEIGHT = 600;
    X_CODING_AREA = 50;
    X_TOKENS = 550;
    X_PARSER_SEMANTIC = 750;
    X_INTERMEDIATE_CODE = 950;
    Y_UPPER_PANELS = 50;
    Y_LOWER_PANELS = 300;
    Y_UPPER_TEXT = 10;
    Y_LOWER_TEXT = 250;
    Y_BUTTONS = 480;
    BUTTONS = 4;
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
    Font outerTextFont = new Font("New Courier", Font.PLAIN, 30);
    Font innerTextFont = new Font("New Courier", Font.PLAIN, 15);

    JLabel textCodingArea = new JLabel("CODING AREA");
    textCodingArea.setFont(outerTextFont);
    textCodingArea.setBounds(X_CODING_AREA, Y_UPPER_TEXT, 1000, 50);
    add(textCodingArea);

    codingArea = new JTextArea();
    codingArea.setBackground(Color.WHITE);
    codingArea.setOpaque(true);
    codingArea.setBorder(BorderFactory.createLineBorder(Color.BLACK));
    codingArea.setFont(innerTextFont);

    JScrollPane scrollCode = new JScrollPane(codingArea);
    scrollCode.setBounds(X_CODING_AREA, Y_UPPER_PANELS, 450, 400);
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
    scrollTokens.setBounds(X_TOKENS, Y_UPPER_PANELS, 150, 400);
    add(scrollTokens);

    JLabel textParserPane = new JLabel("PARSER");
    textParserPane.setFont(outerTextFont);
    
    textParserPane.setBounds(X_PARSER_SEMANTIC, Y_UPPER_TEXT, 1000, 50);
    add(textParserPane);

    parserPane = new JTextPane();
    parserPane.setEnabled(false);
    parserPane.setBackground(Color.WHITE);
    parserPane.setOpaque(true);
    parserPane.setBorder(BorderFactory.createLineBorder(Color.BLACK));
    parserPane.setDisabledTextColor(Color.BLACK);
    parserPane.setFont(innerTextFont);

    JScrollPane scrollParser = new JScrollPane(parserPane);
    scrollParser.setBounds(X_PARSER_SEMANTIC, Y_UPPER_PANELS, 150, 150);
    add(scrollParser);

    JLabel textSemanticPane = new JLabel("SEMANTIC");
    textSemanticPane.setFont(outerTextFont);
    textSemanticPane.setBounds(X_PARSER_SEMANTIC, Y_LOWER_TEXT, 1000, 50);
    add(textSemanticPane);

    semanticPane = new JTextPane();
    semanticPane.setEnabled(false);
    semanticPane.setBackground(Color.WHITE);
    semanticPane.setOpaque(true);
    semanticPane.setBorder(BorderFactory.createLineBorder(Color.BLACK));
    semanticPane.setDisabledTextColor(Color.BLACK);
    semanticPane.setFont(innerTextFont);

    JScrollPane scrollSemantic = new JScrollPane(semanticPane);
    scrollSemantic.setBounds(X_PARSER_SEMANTIC, Y_LOWER_PANELS, 150, 150);
    add(scrollSemantic);

    JLabel textIntermediateCodePane = new JLabel("INTER. CODE");
    textIntermediateCodePane.setFont(outerTextFont);
    textIntermediateCodePane.setBounds(X_INTERMEDIATE_CODE, Y_UPPER_TEXT, 1000, 50);
    add(textIntermediateCodePane);

    intermediateCodePane = new JTextPane();
    intermediateCodePane.setEnabled(false);
    intermediateCodePane.setBackground(Color.WHITE);
    intermediateCodePane.setOpaque(true);
    intermediateCodePane.setBorder(BorderFactory.createLineBorder(Color.BLACK));
    intermediateCodePane.setDisabledTextColor(Color.BLACK);
    intermediateCodePane.setFont(innerTextFont);

    JScrollPane scrollIntermediateCode = new JScrollPane(intermediateCodePane);
    scrollIntermediateCode.setBounds(X_INTERMEDIATE_CODE, Y_UPPER_PANELS, 300, 400);
    add(scrollIntermediateCode);

    placeButton(startScannerButton = new JButton(), "Scanner");
    placeButton(startParserButton = new JButton(), "Parser");
    placeButton(startSemanticButton = new JButton(), "Semantic");
    placeButton(startIntermediateCodeGenerationButton = new JButton(), "Intermediate Code");
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
    startScannerButton.addActionListener(controller);
    startParserButton.addActionListener(controller);
    startSemanticButton.addActionListener(controller);
    startIntermediateCodeGenerationButton.addActionListener(controller);
    codingArea.addKeyListener(controller);
  }

  public void resetButtons() {
    getStartScannerButton().setEnabled(true);
    getStartParserButton().setEnabled(false);
    getStartSemanticButton().setEnabled(false);
    getStartIntermediateCodeGenerationButton().setEnabled(false);
  }

  public void resetResultPanels() {
    tokensPane.setText("");
    parserPane.setText("");
    semanticPane.setText("");
    intermediateCodePane.setText("");
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

  public JTextPane getIntermediateCodePane() {
    return intermediateCodePane;
  }
}