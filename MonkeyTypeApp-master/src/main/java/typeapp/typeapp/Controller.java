package typeapp.typeapp;


import javafx.animation.*;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.util.Duration;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class Controller {

    private static final double DELAY  = 0.001;
    private  HBox hBox;
    private  VBox vBox;
    private ChoiceBox<String> languageChoiceBox;
    private Random random;
    private static TextFlow textFlow;
    int currentIndex;
    private TextField textField;
    private Text caret = new Text("|");
    private static final double JUMP_HEIGHT = 10;
    private static final Duration JUMP_DURATION = Duration.millis(100);
    private int totalWordsTyped = 0;
    private long startTime = 0;
    Text wordCountText = new Text();
    List<String> randomWords;
    private List<Integer> listOfLettersOfWords;
    private MonkeytypeApp monkeytypeApp;
    private boolean shortcutEnabled = false;
    public boolean isShortcutEnabled() {
        return shortcutEnabled;
    }

    public void setShortcutEnabled(boolean shortcutEnabled) {
        this.shortcutEnabled = shortcutEnabled;
    }

    ChoiceBox<Integer> timeChoiceBox;

    public Controller(ChoiceBox<Integer> timeChoiceBox, ChoiceBox<String> languageChoiceBox, VBox vBox, HBox hBox, MonkeytypeApp monkeytypeApp) {
        this.timeChoiceBox = timeChoiceBox;
        this.languageChoiceBox = languageChoiceBox;
        this.textFlow = new TextFlow();
        this.random = new Random();
        this.vBox = vBox;
        this.hBox = hBox;
        this.textField = new TextField();
        this.listOfLettersOfWords = new ArrayList<>();
        this.monkeytypeApp = monkeytypeApp;
    }

    private void createTextFlow() {
        textFlow = new TextFlow();
        for (Node node : textFlow.getChildren()) {
            if (node instanceof Text) {
                ((Text) node).setOnKeyPressed(null);
            }
        }
    }

    public void displaySelectedTextFromFile() {
        // Recreate the TextFlow
        createTextFlow();
        // Clear the children of VBox
        vBox.getChildren().clear();
        textFlow.getChildren().clear();
        startTime = System.currentTimeMillis();
        String selectedLanguage = languageChoiceBox.getValue();
        String fileName = selectedLanguage + ".txt";
        File selectedFile = new File("dictionary/" + fileName);

        try (BufferedReader reader = new BufferedReader(new FileReader(selectedFile))) {
            List<String> words = new ArrayList<>();
            String line;
            while ((line = reader.readLine()) != null) {
                words.add(line);
            }
            int nuberOfLettersInTheWord = 0;
            int indexOfnuberOfListOfLettersInTheWord = 0;
            Collections.shuffle(words, random);
            int numWordsToShow = Math.min(words.size(), 30);
            this.randomWords = words.subList(0, numWordsToShow);
            System.out.println("randomWords" + randomWords);
            for (String word : randomWords) {
                for (char c : word.toCharArray()) {
                    Text textNode = new Text(String.valueOf(c));
                    textNode.setFill(Color.GRAY);
                    textNode.setFont(Font.font(20));
                    textFlow.getChildren().add(textNode);
                    nuberOfLettersInTheWord++;
                    textNode.setOnKeyPressed(this::handleKeyPress);
                }
                textFlow.getChildren().add(new Text(" ")); // Add space between words
                this.listOfLettersOfWords.add(indexOfnuberOfListOfLettersInTheWord,nuberOfLettersInTheWord);
                indexOfnuberOfListOfLettersInTheWord++;
                nuberOfLettersInTheWord= 0;
            }
            System.out.println("listOfLettersOfWords  = " + listOfLettersOfWords.toString());

            vBox.getChildren().add(textFlow);
            wordCountText.setText("Words typed: " + totalWordsTyped);
            if (!hBox.getChildren().contains(wordCountText)) {
                hBox.getChildren().add(wordCountText);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }}


    public void handleKeyPress(KeyEvent event) {

        if (!MonkeytypeApp.editingEnabled) {
            return; // Don't process key events if editing is not enabled
        }


        KeyCode keyCode = event.getCode();
        System.out.println("Current index is: " + currentIndex);

        Text textNode = (Text) textFlow.getChildren().get(currentIndex);
        String character = textNode.getText();
        System.out.println("Current letter in the textFlow is: " + character);
        Text textNodeOfNextCharacter = (Text) textFlow.getChildren().get(currentIndex + 1);
        String nextCharacter = textNodeOfNextCharacter.getText();

        String inputCharToString = event.getText();

        if (character.equals(" ")) {
            if (keyCode.equals(KeyCode.ENTER)) {
                currentIndex++;
                return;
            } else if (keyCode.isLetterKey()) {
                Text redundantLetter = new Text(inputCharToString);
                redundantLetter.setFill(Color.ORANGE);
                redundantLetter.setFont(Font.font(20));
                textFlow.getChildren().add(currentIndex, redundantLetter);
                currentIndex++; // Aktualizacja currentIndex
                return;
            }
        }

        // Check if it's the last letter of the last word
        if (isLastLetterOfLastWord()) {
            displaySelectedTextFromFile();
            currentIndex = 0; // Reset the currentIndex
            return;
        }

            if (keyCode == KeyCode.BACK_SPACE && currentIndex > 1) {
                if (character.equals(" ")) {
                    currentIndex--;
                    return;
                } else if (textNode.getFill().equals(Color.ORANGE)) {
                    textFlow.getChildren().remove(currentIndex);
                    currentIndex--;
                    return;
                } else {
                    textNode.setFill(Color.GRAY);
                    currentIndex--;
                }
                ;

            } else if (keyCode.isLetterKey()) {
                currentIndex++;
            } else {
                return;
            }

            if (inputCharToString.equals(character) && !textNode.getFill().equals(Color.ORANGE)) {
                textNode.setFill(Color.BLUE);
            } else if (inputCharToString.equals(nextCharacter) && !textNode.getFill().equals(Color.ORANGE)) {
                textNode.setFill(Color.BLACK);
                textNodeOfNextCharacter.setFill(Color.BLUE);
            } else if (keyCode.isLetterKey() && !inputCharToString.equals(character) && !textNode.getFill().equals(Color.ORANGE)) {
                textNode.setFill(Color.RED);
            } else {
                return;
            }

            vBox.getChildren().clear();
            vBox.getChildren().add(textFlow);
            totalWordsTyped = countTypedWords();
            wordCountText.setText("Words typed: " + totalWordsTyped);
            hBox.getChildren().set((hBox.getChildren().size() - 1), wordCountText);
        }

    public void handleShortcut(KeyEvent event) {
        if (!shortcutEnabled || !MonkeytypeApp.editingEnabled) {
            return; // Shortcut is not enabled or editing is not enabled
        }

        if (event.getCode() == KeyCode.TAB && event.isShiftDown()) {
            // TAB + Shift was pressed, restart the countdown and clear text changes
            // tab + shift bo enter zajety przez przerwe miedzy slowami
            displaySelectedTextFromFile();
            currentIndex = 0; // Reset the currentIndex

            // Restart the countdown
            int selectedTime = timeChoiceBox.getValue();
            startCountdown(selectedTime); // Replace '60' with your desired countdown duration
        } else if (event.getCode().equals(KeyCode.P) && event.isControlDown() && event.isShiftDown()) {

            // ten shortcut nie dziala (ctrl+shift+p)

            MonkeytypeApp.countdownTimeline.pause();
            disableTextFlowEditing();
            shortcutEnabled = false;
        } else if (event.getCode() == KeyCode.ESCAPE) {
            // Close the application
            // tez nie dziala
            Platform.exit();
            System.exit(0);
        }
    }

    public static void disableTextFlowEditing() {
        for (Node node : textFlow.getChildren()) {
            node.setMouseTransparent(true); // Disable mouse interaction with nodes
            node.setFocusTraversable(false); // Disable keyboard focus on nodes
        }
    }

    private boolean isLastLetterOfLastWord() {
        return currentIndex >= textFlow.getChildren().size() - 2;
    }

public int countTypedWords() {
    int wordCount = 0;
    int wordLengthIndex = 0;
    int lettersCount = 0;

    for (Node node : textFlow.getChildren()) {
        if (node instanceof Text) {
            Text textNode = (Text) node;
            String character = textNode.getText();

            if (!character.equals(" ")) {
                if (textNode.getFill() != Color.ORANGE && textNode.getFill() != Color.GRAY) {
                    lettersCount++;
                    System.out.println("Letter - lettersCount is now " + lettersCount + "with wordLengthIndex = " + wordLengthIndex);
                }
            } else {
                if (wordLengthIndex < listOfLettersOfWords.size()) {
                    int wordLength = listOfLettersOfWords.get(wordLengthIndex);
                    if (lettersCount == wordLength) {
                        wordCount++;
                    }
                    wordLengthIndex++;
                    lettersCount = 0;
                }
            }
        }
    }

    return wordCount;
}

    public static void startCountdown(int seconds) {
        final int[] remainingSeconds = {seconds};
        MonkeytypeApp.countdownLabel.setText(Integer.toString(remainingSeconds[0]));

        if (MonkeytypeApp.countdownTimeline != null && MonkeytypeApp.countdownTimeline.getStatus() == Animation.Status.RUNNING) {
            MonkeytypeApp.countdownTimeline.stop(); // Stop the previous timeline if it's running
        }

        MonkeytypeApp.countdownTimeline = new Timeline();
        MonkeytypeApp.countdownTimeline.setCycleCount(Timeline.INDEFINITE);
        KeyFrame keyFrame = new KeyFrame(Duration.seconds(1), event -> {
            remainingSeconds[0]--;
            MonkeytypeApp.countdownLabel.setText(Integer.toString(remainingSeconds[0]));
            if (remainingSeconds[0] <= 0) {
                MonkeytypeApp.countdownTimeline.stop();
                MonkeytypeApp.countdownRunning = false; // Reset the flag to indicate countdown has finished
                MonkeytypeApp.editingEnabled = false;
            }
        });
        MonkeytypeApp.countdownTimeline.getKeyFrames().add(keyFrame);
        MonkeytypeApp.countdownTimeline.play();

        MonkeytypeApp.countdownTimeline.setOnFinished(event -> {
            MonkeytypeApp.countdownRunning = false; // Reset the flag to indicate countdown has finished
            MonkeytypeApp.editingEnabled = false;
            disableTextFlowEditing();
        });

        MonkeytypeApp.countdownTimeline.play();
        MonkeytypeApp.editingEnabled = true; // Enable editing when the countdown starts
    }

    void jumpText() {
        SequentialTransition sequentialTransition = new SequentialTransition();

        for (int i = 0; i < this.textFlow.getChildren().size(); i++) {
            Text text = (Text) this.textFlow.getChildren().get(i);
            animateJump(text, i, sequentialTransition);
        }

        sequentialTransition.play();
    }

    private void animateJump(Text text, int index, SequentialTransition sequentialTransition) {
        TranslateTransition jumpTransition = new TranslateTransition(JUMP_DURATION, text);
        jumpTransition.setByY(-JUMP_HEIGHT);
        jumpTransition.setInterpolator(Interpolator.EASE_OUT);

        TranslateTransition fallTransition = new TranslateTransition(JUMP_DURATION, text);
        fallTransition.setByY(JUMP_HEIGHT);
        fallTransition.setInterpolator(Interpolator.EASE_IN);

        TranslateTransition initialPositionTransition = new TranslateTransition(Duration.ZERO, text);
        initialPositionTransition.setToY(0);

        SequentialTransition letterTransition = new SequentialTransition(
                new PauseTransition(Duration.seconds( DELAY)),
                jumpTransition,
                fallTransition
        );

        sequentialTransition.getChildren().add(letterTransition);
    }
}