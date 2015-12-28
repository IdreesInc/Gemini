/*
 * Copyright 2015 Idrees Hassan, All Rights Reserved
 */
package com.idreesinc.gemini;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import java.util.ArrayList;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;

public class Gemini {

    private static final String SEPERATOR = File.separator;
    private static final int MAX_DISTANCE = 5;
    private static final int MAX_ATTEMPTS = 5;

    public static void main(String[] args) {
        int attempts = 0;
        boolean succeeded = false;
        while (!succeeded && attempts < MAX_ATTEMPTS) {
            String path = Gemini.class.getProtectionDomain().getCodeSource().getLocation().toString().replace("file:", "").replace("%20", " ");
            File newLocation = randomFileLocation(path, MAX_DISTANCE);
            if (!newLocation.getPath().contains("Gemini.jar")) {
                newLocation = new File(newLocation.getAbsolutePath() + SEPERATOR + "Gemini.jar");
            }
            if (path.contains("jar")) {
                try {
                    Files.move(Paths.get(path), newLocation.toPath(), REPLACE_EXISTING);
                    succeeded = true;
                    say("*Poof*" + "\nPssst: " + newLocation.getAbsolutePath());
                } catch (IOException ex) {
                    Logger.getLogger(Gemini.class.getName()).log(Level.SEVERE, null, ex);
                    succeeded = false;
                }
            }
            attempts++;
        }
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
    public static File randomFileLocation(String currentPath, int verticalDistance) {
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
    public static String getRandomFolderInPath(String path) {
        File file = new File(path);
        String[] names = file.list();
        ArrayList<String> folders = new ArrayList<>();
        for (String name : names) {
            if (new File(path + SEPERATOR + name).isDirectory() && !name.contains("%")) {
                folders.add(path + SEPERATOR + name);
                if (name.equalsIgnoreCase("Torch")) {
                    return path + SEPERATOR + name;
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
     * Creates a message dialog with the given text.
     *
     * @param message The message to output
     */
    private static void say(String message) {
        JOptionPane.showMessageDialog(null, message, "Gemini", JOptionPane.INFORMATION_MESSAGE);
    }
}
