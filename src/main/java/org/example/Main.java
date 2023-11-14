package org.example;


import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.*;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class Main extends JFrame {

    WebDriver driver;
    static ArrayList<String> wordleList;
    String[] correctLetters;
    Set<String> absentLetters;
    HashMap<Integer, HashSet<String>> presentLettersMap;
    Set<String> presentLettersSet;
    int currentRow;

    /**
     * First the program will
     * setUp() - clicks the play button, removes the popup, and sets the webdriver and variables
     * playWordle() - plays the wordle game until it guesses the correct word or fails after 6 tries
     * clickShareButton() - clicks on the share button, copies the results from the clipboard, and then returns it to result
     * postResults(result, CHANNEL_ID, usernameOrEmail, password) - send the results to the discord server with the given CHANNEL_ID
     *
     * @throws Exception
     */
    public Main() throws Exception {
        DiscordUserInfo discordUserInfo = new DiscordUserInfo();
        discordUserInfo.pack();
        discordUserInfo.setVisible(true);
        String username = discordUserInfo.getUsername().strip();
        String password = discordUserInfo.getPassword().strip();
        String channelID = discordUserInfo.getChannelID().strip();
        setUp();
        playWordle();
        String result = clickShareButton();
        Thread.sleep(3000);
        driver.close();
        if (!(username.isEmpty() || password.isEmpty() || channelID.isEmpty())) {
            postResults(result, channelID, username, password);
        }
        System.exit(0);
    }

    /**
     * Sets the Webdriver and variables used to solve the wordle
     *
     * @throws FileNotFoundException
     * @throws InterruptedException
     */
    public void setUp() throws FileNotFoundException, InterruptedException {
        getAllWords();
        correctLetters = new String[5];
        presentLettersMap = new HashMap<Integer, HashSet<String>>();
        presentLettersSet = new HashSet<>();
        absentLetters = new HashSet<>();
        currentRow = 0;
        driver = new ChromeDriver();
        driver.get("https://www.nytimes.com/games/wordle/index.html");
        driver.manage().timeouts().implicitlyWait(Duration.ofMillis(10000));
        driver.findElement(By.xpath("//button[contains(.,'Play')]")).click();
        driver.findElement(By.cssSelector("[aria-label='Close']")).click();
    }


    /**
     * Function that starts playing the wordle game
     *
     * @throws Exception
     */
    public void playWordle() throws Exception {
        int cnt = 0;
        for (String word : wordleList) {
            if (cnt >= 6) {
                return;
            }
            if (isValidWordAbsent(word)) {
                if (isValidWordCorrect(word)) {
                    if (isValidWordPresent(word)) {
                        enterWord(word);
                        cnt++;
                    }
                }
            }
        }
    }

    /**
     * Clicks the share button and then returns the results as a String
     *
     * @return The results of the wordle
     * @throws IOException
     * @throws UnsupportedFlavorException
     */
    public String clickShareButton() throws IOException, UnsupportedFlavorException {
        WebElement shareButton = driver.findElement(By.xpath("//span[contains(.,'Share')]"));
        shareButton.click();
        return (String) Toolkit.getDefaultToolkit().getSystemClipboard().getData(DataFlavor.stringFlavor);
    }

    /**
     * Returns the row element at the specified index
     *
     * @param index The row index
     * @return
     */
    public WebElement getGameRow(int index) {
        List<WebElement> gameRows = driver.findElements(By.cssSelector(".Row-module_row__pwpBq"));
        return gameRows.get(index);
    }

    /**
     * Returns the tile element at the specified row index and tile index
     *
     * @param gameRowIndex The row index
     * @param tileIndex    The tile index
     * @return
     */
    public WebElement getGameTile(int gameRowIndex, int tileIndex) {
        WebElement gameRow = getGameRow(gameRowIndex);
        List<WebElement> gameTiles = gameRow.findElements(By.cssSelector(".Tile-module_tile__UWEHN"));
        return gameTiles.get(tileIndex);
    }


    /**
     * Enters a single word into the Wordle
     *
     * @param word The word to be entered
     * @throws Exception
     */
    public void enterWord(String word) throws Exception {
        for (int i = 0; i < word.length(); i++) {
            String letter = word.substring(i, i + 1);
            clickLetter(letter.toUpperCase());
        }
        clickLetter("ENTER");
        Thread.sleep(2000);
        for (int i = 0; i < 5; i++) {
            WebElement curTile = getGameTile(currentRow, i);
            String letter = curTile.getText();
            String dataState = curTile.getAttribute("data-state");
            if (dataState.equals("correct")) {
                correctLetters[i] = letter;
                continue;
            }
            if (dataState.equals("present")) {
                presentLettersSet.add(letter);
                if (presentLettersMap.get(i) == null) {
                    presentLettersMap.put(i, new HashSet<>(Arrays.asList(letter)));
                } else {
                    presentLettersMap.get(i).add(letter);
                }
            }
        }
        currentRow++;
        findAbsentLetters();
    }

    /**
     * Checks if there are any absent letters in the word
     *
     * @param word The word to be checked
     * @return true if there are no absent letters in the word and false otherwise
     */
    public boolean isValidWordAbsent(String word) {
        StringBuilder absentLetterPattern = new StringBuilder("[^");
        for (String s : absentLetters) {
            absentLetterPattern.append(s);
        }
        absentLetterPattern.append("]+");
        boolean validWord;
        try {
            validWord = Pattern.matches(absentLetterPattern.toString(), word.toUpperCase());
        } catch (PatternSyntaxException e) {
            validWord = true;
        }
        return validWord;
    }

    /**
     * Checks if the words aligns with the positions of the correct letters
     *
     * @param word The word to be checked
     * @return true if the word contains the correct letters at the correct position and false otherwise
     */
    public boolean isValidWordCorrect(String word) {
        StringBuilder correctLetterPattern = new StringBuilder();
        for (String s : correctLetters) {
            correctLetterPattern.append(Objects.requireNonNullElse(s, "."));
        }
        return Pattern.matches(correctLetterPattern.toString(), word.toUpperCase());
    }

    /**
     * Checks if the word contains any of the present letters
     *
     * @param word The word to be checked
     * @return true if the word contains a present letter and that present letter is not in same position when found,
     * false otherwise
     */
    public boolean isValidWordPresent(String word) {
        for (String s : presentLettersSet) {
            String presentLetterPattern = "\\w*[" + s + "]\\w*";
            if (!Pattern.matches(presentLetterPattern, word.toUpperCase())) {
                return false;
            }
        }
        for (int index : presentLettersMap.keySet()) {
            for (String s : presentLettersMap.get(index)) {
                StringBuilder presentLetterPattern = new StringBuilder(".....");
                presentLetterPattern.replace(index, index + 1, "[^" + s + "]");
                if (!Pattern.matches(presentLetterPattern.toString(), word.toUpperCase())) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Loops through the keyboard element and adds any absent letters to the absentLetters variable
     */
    public void findAbsentLetters() {
        List<WebElement> rows = driver.findElements(By.cssSelector(".Keyboard-module_row__ilOKU"));
        for (WebElement row : rows) {
            List<WebElement> letters = row.findElements(By.cssSelector("button"));
            for (WebElement curLetter : letters) {
                if (curLetter.getAttribute("data-state") != null && curLetter.getAttribute("data-state").equals("absent")) {
                    this.absentLetters.add(curLetter.getText());
                }
            }
        }
    }

    /**
     * Scans through all the possible wordle guesses and adds them to wordleList variable
     *
     * @throws FileNotFoundException if file is not found
     */
    public static void getAllWords() throws FileNotFoundException {
        wordleList = new ArrayList<>();
        Scanner sc = new Scanner(new File("WordleWordList/wordle-allowed-guesses"));
        while (sc.hasNext()) {
            String word = sc.nextLine();
            wordleList.add(word);
        }
    }


    /**
     * clicks on a letter in wordle
     *
     * @param letter is the letter to be click on
     * @throws Exception if the letter is not found
     */
    public void clickLetter(String letter) throws Exception {
        List<WebElement> rows = driver.findElements(By.cssSelector(".Keyboard-module_row__ilOKU"));
        for (WebElement row : rows) {
            List<WebElement> letters = row.findElements(By.cssSelector("button"));
            for (WebElement curLetter : letters) {
                if (curLetter.getText().equals(letter)) {
                    curLetter.click();
                    return;
                }
            }
        }
        throw new Exception("Letter not found");
    }

    /**
     * posts the results to discord
     *
     * @param results         is the results of the wordle game
     * @param CHANNEL_ID      is the channel the results will be posted to
     * @param usernameOrEmail is the username of the discord account
     * @param password        is the password of the discord account
     * @throws URISyntaxException
     * @throws IOException
     * @throws InterruptedException
     */
    public void postResults(String results, final String CHANNEL_ID, String usernameOrEmail, String password) throws URISyntaxException, IOException, InterruptedException {
        Gson gson = new Gson();
        HttpClient httpClient = HttpClient.newHttpClient();

        Login login = new Login(usernameOrEmail, password, false, null, null, null);
        String loginJSON = gson.toJson(login);
        HttpRequest loginRequest = HttpRequest.newBuilder()
                .uri(new URI("https://discord.com/api/v9/auth/login"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(loginJSON))
                .build();

        HttpResponse<String> postResponse = httpClient.send(loginRequest, HttpResponse.BodyHandlers.ofString());
        JsonObject jsonObject = JsonParser.parseString(postResponse.body()).getAsJsonObject();
        String token = jsonObject.get("token").getAsString();

        PostMessage postMessage = new PostMessage(results, Integer.toString((int) (Math.random() * 10000000)), false, 0);
        String postMessageJSON = gson.toJson(postMessage);
        HttpRequest messageRequest = HttpRequest.newBuilder()
                .uri(new URI("https://discord.com/api/v9/channels/" + CHANNEL_ID + "/messages"))
                .header("Authorization", token)
                .header("Content-Type", "application/json")
                .header("Accept", "*/*")
                .POST(HttpRequest.BodyPublishers.ofString(postMessageJSON))
                .build();
        httpClient.send(messageRequest, HttpResponse.BodyHandlers.ofString());
    }

    public static void main(String[] args) throws Exception {
        new Main();
    }
}
