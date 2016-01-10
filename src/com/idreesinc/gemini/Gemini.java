/*
 * Copyright 2015 Idrees Hassan, All Rights Reserved
 */
package com.idreesinc.gemini;

import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import java.util.ArrayList;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.WindowConstants;

public class Gemini {

    private static String name;
    private static String twinName;
    private static Path twinPath;
    private static String waitingFor;
    private static int attemptsToTerminateTwin;
    private static int updatesSinceLastPing;
    private static boolean showConsole;
    private static final Console console = new Console();
    private static final String TOKEN = "⬡";
    private static final String SEPERATOR = File.separator;
    private static final int MAX_DISTANCE = 5;
    private static final int MAX_ATTEMPTS = 10;
    private static final int UPDATE_INTERVAL = 250;
    private static final boolean AVOID_HIDDEN_FOLDERS = true;

    public static void main(String[] args) {
        if (args.length > 0) {
            showConsole = Boolean.parseBoolean(args[0]);
        }
        String clipboardText = getClipboardText();
        if (clipboardText != null && clipboardText.startsWith(TOKEN)) { //If an instance of Gemini is already running
            if (clipboardText.startsWith(TOKEN + "Apollo")) {
                name = "Artemis";
                twinName = "Apollo";
            } else if (clipboardText.startsWith(TOKEN + "Artemis")) {
                name = "Apollo";
                twinName = "Artemis";
            } else {
                System.err.println(clipboardText);
                terminate();
            }
            if (clipboardText.contains("Waiting for twin")) {
                waitingFor = "Path";
                sendClipboardMessage(name + " activated");
            }
            console.addConsoleText(name + " activated");
        } else { //If this is the first instance of Gemini
            name = "Artemis";
            twinName = "Apollo";
            createTwin();
        }
        //Update loop
        Timer timer = new Timer();
        timer.schedule(new Update(), 0, UPDATE_INTERVAL);

        JFrame frame = new JFrame();
        if (name == null) {
            name = "No Name";
        }
        frame.setTitle(name + "'s Console");
        frame.add(console);
        frame.setSize(console.getPreferredSize().width, console.getPreferredSize().height);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setVisible(showConsole);
    }

    static class Update extends TimerTask {

        @Override
        public void run() {
            String clipboardText = getClipboardText();
            //Check for communication
            if (clipboardText != null) {
                if (clipboardText.startsWith(TOKEN + twinName)) {
                    if (!clipboardText.contains("Marco") && !clipboardText.contains("Polo")) {
                        console.addConsoleText(clipboardText);
                    }
                    if (waitingFor != null && clipboardText.contains(waitingFor)) {
                        String[] split = clipboardText.split("=");
                        switch (waitingFor) {
                            case "Path": //Generated twin's path has been recieved
                                twinPath = Paths.get(split[1]);
                                waitingFor = null;
                                sendClipboardMessage("Path has been recieved");
                                break;
                            case "activated": //Generated twin has been created
                                sendClipboardMessage("Path=" + getPath());
                                waitingFor = "recieved";
                                break;
                            case "recieved":
                                waitingFor = null;
                                break;
                            case "Terminated":
                                attemptsToTerminateTwin = 0;
                                setClipboardText("");
                                createTwin();
                                break;
                        }
                    } else { //If not waiting on anything, yet recieved a message
                        String[] split = clipboardText.split(": ");
                        switch (split[1]) {
                            case "Marco":
                                if (name.equals("Apollo")) {
                                    updatesSinceLastPing = 0;
                                    sendClipboardMessage("Polo");
                                }
                                break;
                            case "Polo":
                                if (name.equals("Artemis")) {
                                    updatesSinceLastPing = 0;
                                }
                                break;
                            case "Terminate":
                                sendClipboardMessage("Terminated");
                                terminate();
                                break;
                        }
                    }
                } else {
                    //Check if termination command failed
                    if (waitingFor != null && waitingFor.equals("Terminated")) {
                        attemptsToTerminateTwin++;
                        if (attemptsToTerminateTwin == 5) {
                            waitingFor = null;
                            attemptsToTerminateTwin = 0;
                            setClipboardText("");
                            createTwin();
                            console.addConsoleText("Attempt to terminate twin #" + attemptsToTerminateTwin);
                        }
                    }
                }
                //Check for user commands
                if (clipboardText.toUpperCase().startsWith("//")) {
                    if (clipboardText.toUpperCase().contains("TERMINATE")) {
                        terminate();
                    } else if (clipboardText.toUpperCase().contains("DESTROY")) {
                        deleteApplication();
                        terminate();
                    } else if (clipboardText.toUpperCase().contains("WAITING")) {
                        if (waitingFor != null) {
                            console.addConsoleText(waitingFor.toUpperCase());
                        }
                    }
                }
            }
            //Check if twin's jar file exists
            if (twinPath != null && !doesFileExist(twinPath.toString())) {
                if (waitingFor == null || !waitingFor.equals("Terminated")) {
                    sendClipboardMessage("Terminate");
                    waitingFor = "Terminated";
                    attemptsToTerminateTwin = 0;
                }
            }
            //Check if twin has not played Marco Polo
            if (waitingFor == null) {
                //Play Marco Polo
                if (name.equals("Artemis")) {
                    sendClipboardMessage("Marco");
                }
                updatesSinceLastPing++;
                if (updatesSinceLastPing > 5) {
                    console.addConsoleText(name + ": Twin has not played Marco Polo, restarting twin");
                    sendClipboardMessage("Waiting for twin");
                    waitingFor = "activated";
                    try {
                        Runtime.getRuntime().exec(" java -jar " + twinPath.toString() + " " + showConsole);
                    } catch (IOException | NullPointerException ex) {
                        System.err.println("Twin execution failed, terminating");
                        terminate();
                    }
                    updatesSinceLastPing = 0;
                }
            }
        }
    }

    /**
     * Attempts to create and execute a twin. In the event of failure, the
     * program is terminated.
     */
    private static void createTwin() {
        twinPath = null;
        int attempts = 0;
        while (twinPath == null && attempts < MAX_ATTEMPTS) {
            twinPath = duplicate();
            attempts++;
        }
        if (twinPath == null) {
            System.err.println("Unable to generate twin");
            terminate();
        } else {
            console.addConsoleText("Generated twin at " + twinPath);
        }
        sendClipboardMessage("Waiting for twin");
        waitingFor = "activated";
        try {
            Runtime.getRuntime().exec(" java -jar " + twinPath.toString() + " " + showConsole);
        } catch (IOException ex) {
            System.err.println("Twin execution failed, terminating");
            terminate();
        }
    }

    /**
     * Returns the text stored in the system's clipboard.
     *
     * @return The text stored in the clipboard
     */
    private static String getClipboardText() {
        String text = "";
        try {
            text = (String) Toolkit.getDefaultToolkit().getSystemClipboard().getData(DataFlavor.stringFlavor);
        } catch (UnsupportedFlavorException | IOException ex) {
        }
        return text;
    }

    /**
     * Stores the given text into the system's clipboard.
     *
     * @param text The text to store
     */
    public static void setClipboardText(String text) {
        StringSelection selection = new StringSelection(text);
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, selection);
        if (!text.contains("Marco") && !text.contains("Polo")) {
            console.addConsoleText(text);
        }
    }

    /**
     * Sets the clipboard's text as a combination of the globally specified
     * token, this twin's name, a colon with a space, and the message given (in
     * that order).
     *
     * @param message The message to add to the clipboard
     */
    private static void sendClipboardMessage(String message) {
        setClipboardText(TOKEN + name + ": " + message);
    }

    /**
     * Attempts to duplicate this jar to a random location.
     *
     * @return The path of the duplicated jar
     */
    private static Path duplicate() {
        String path = getPath();
        Path duplicatedPath = null;
        File newLocation = randomFileLocation(path, MAX_DISTANCE);
        if (!newLocation.getPath().contains("Gemini.jar")) {
            newLocation = new File(newLocation.getAbsolutePath() + SEPERATOR + "Gemini.jar");
        }
        if (path.contains("jar")) {
            try {
                Files.copy(Paths.get(path), newLocation.toPath(), REPLACE_EXISTING);
                duplicatedPath = newLocation.toPath();
            } catch (IOException ex) {
                Logger.getLogger(Gemini.class.getName()).log(Level.SEVERE, null, ex);
                duplicatedPath = null;
            }
        }
        return duplicatedPath;
    }

    /**
     * Returns a random file location that is within the vertical distance given
     * from the given path.
     *
     * @param currentPath The path to start the search from
     * @param verticalDistance The maximum number of folders higher or lower
     * than the original location that the random location should be located
     * within
     * @return A file with the calculated path
     */
    private static File randomFileLocation(String currentPath, int verticalDistance) {
        Random rand = new Random();
        int folderLevel = currentPath.length() - currentPath.replace(SEPERATOR, "").length();
        int newFolderLevel = folderLevel;
        File newLocation = new File(System.getProperty("user.dir"));
        for (int y = 0; y < verticalDistance; y++) {
            if (rand.nextBoolean() && newFolderLevel > 3) {
                if (newLocation.getParentFile() == null) {
                    newFolderLevel = 0;
                } else {
                    newLocation = newLocation.getParentFile();
                    newFolderLevel--;
                }
            } else {
                String subdirectory = getRandomFolderInPath(newLocation.getAbsolutePath());
                if (subdirectory != null) {
                    newFolderLevel++;
                    newLocation = new File(subdirectory);
                }
            }
        }
        return newLocation;
    }

    /**
     * Returns a random folder within the given folder's path. Should any folder
     * within be named "Torch", this method will return that folder.
     *
     * @param path The path containing the folders
     * @return A randomized folder
     */
    private static String getRandomFolderInPath(String path) {
        File file = new File(path);
        String[] names = file.list();
        ArrayList<String> folders = new ArrayList<>();
        if (names != null) {
            for (String nm : names) {
                if (new File(path + SEPERATOR + nm).isDirectory() && !nm.contains("%") && (!AVOID_HIDDEN_FOLDERS || !(nm.contains(".") || nm.contains("tmp")))) {
                    folders.add(path + SEPERATOR + nm);
                    if (nm.equalsIgnoreCase("Torch")) {
                        return path + SEPERATOR + nm;
                    }
                }
            }
        }
        if (!folders.isEmpty()) {
            Random rand = new Random();
            return folders.get(rand.nextInt(folders.size()));
        } else {
            return null;
        }
    }

    /**
     * Determines whether a file exists at the given path.
     *
     * @param path The path to check
     * @return Whether the file exists
     */
    private static boolean doesFileExist(String path) {
        File varTmpDir = new File(path);
        return varTmpDir.exists();
    }

    /**
     * Returns the file path of the jar file that this application is running
     * from.
     *
     * @return The file path of this application
     */
    private static String getPath() {
        return Gemini.class.getProtectionDomain().getCodeSource().getLocation().toString().replace("file:", "").replace("%20", " ");
    }

    /**
     * Creates a message dialog with the given text.
     *
     * @param message The message to output
     */
    private static void say(String message) {
        JOptionPane.showMessageDialog(null, message, "Gemini", JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Deletes the jar file that this program is running from.
     */
    private static void deleteApplication() {
        File file = new File(getPath());
        file.delete();
    }

    /**
     * Terminates the application.
     */
    private static void terminate() {
        System.exit(0);
    }
}
