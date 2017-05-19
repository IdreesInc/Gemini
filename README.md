# Gemini
A Java program that refuses to be deleted. By running two instances at once, the program constantly affirms that the other process and .jar file are still available. If one of the instances is terminated, the other twin instance restarts it.

## How it Works
Gemini works by creating two instances everytime the program is executed. These two twins proceed to continually watch over the .jar file that contains their source, continually communicating with one another to avoid collisions. If the source file is deleted, the file is instantly copied to another directory in the filesystem. Should a twin be killed (whether via Task Manager or some other method) the other twin makes sure to restart it so that there is always two twins running at the same time.

The two instances communicate via the clipboard, constantly sharing data to one another. You can send commands to the program by copying text containing commands, such as "//terminate". To destroy the twins, you only need to terminate both instances at the same time using the Task Manager.
