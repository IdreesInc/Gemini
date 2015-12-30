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
import javax.swing.JOptionPane;

public class Gemini {

    private static String name;
    private static String twinName;
    private static Path twinPath;
    private static String waitingFor;
    private static int attemptsToTerminateTwin;
    private static final String TOKEN = "⬡";
    private static final String SEPERATOR = File.separator;
    private static final int MAX_DISTANCE = 5;
    private static final int MAX_ATTEMPTS = 10;
    private static final int UPDATE_INTERVAL = 500;
    private static final boolean AVOID_HIDDEN_FOLDERS = true;

    public static void main(String[] args) {
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
                sendClipboardMessage(name + " activated");
                waitingFor = "Path";
            }
            System.out.println(name + " activated");
        } else { //If this is the first instance of Gemini
            name = "Artemis";
            twinName = "Apollo";
            createTwin();
        }
        //Update loop
        Timer timer = new Timer();
        timer.schedule(new Update(), 0, UPDATE_INTERVAL);
    }

    static class Update extends TimerTask {

        @Override
        public void run() {
            String clipboardText = getClipboardText();
            //Check for communication
            if (clipboardText != null) {
                if (clipboardText.startsWith(TOKEN + twinName)) {
                    System.out.println(clipboardText);
                    if (waitingFor != null && clipboardText.contains(waitingFor)) {
                        String[] split = clipboardText.split("=");
                        switch (waitingFor) {
                            case "Path": //Generated twin's path has been recieved
                                twinPath = Paths.get(split[1]);
                                System.out.println("Twin's path: " + twinPath);
                                waitingFor = null;
                                setClipboardText("");
                                break;
                            case "activated": //Generated twin has been created
                                sendClipboardMessage("Path=" + Gemini.class.getProtectionDomain().getCodeSource().getLocation().toString().replace("file:", "").replace("%20", " "));
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
                            System.out.println("Attempt to terminate twin #" + attemptsToTerminateTwin);
                        }
                    }
                }
                //Check for user commands
                if (clipboardText.toUpperCase().startsWith("//")) {
                    if (clipboardText.toUpperCase().contains("TERMINATE")) {
                        terminate();
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
            System.out.println("Generated twin at " + twinPath);
        }
        sendClipboardMessage("Waiting for twin");
        waitingFor = "activated";
        try {
            Runtime.getRuntime().exec(" java -jar " + twinPath.toString());
        } catch (IOException ex) {
            System.err.println("Twin execution failed, terminating");
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
    private static void setClipboardText(String text) {
        StringSelection selection = new StringSelection(text);
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, selection);
        System.out.println(text);
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
        String path = Gemini.class.getProtectionDomain().getCodeSource().getLocation().toString().replace("file:", "").replace("%20", " ");
        Path duplicatedPath = null;
        File newLocation = randomFileLocation(path, MAX_DISTANCE);
        if (!newLocation.getPath().contains("Gemini.jar")) {
            newLocation = new File(newLocation.getAbsolutePath() + SEPERATOR + "Gemini.jar");
        }
        if (path.contains("jar")) {
            try {
                Files.copy(Paths.get(path), newLocation.toPath(), REPLACE_EXISTING);
                duplicatedPath = newLocation.toPath();
                System.out.println("Duplicated to " + newLocation.getAbsolutePath());
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
     * Creates a message dialog with the given text.
     *
     * @param message The message to output
     */
    private static void say(String message) {
        System.out.println(name + " said: " + message);
        JOptionPane.showMessageDialog(null, message, "Gemini", JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Terminates the application.
     */
    private static void terminate() {
        System.exit(0);
    }
}
