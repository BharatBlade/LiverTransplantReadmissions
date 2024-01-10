import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * A utility class for writing to files quickly using BufferedWriter.
 */
public class FastWriter {
    // Variables to store file-related information
    public String fileName;
    public File file;
    public FileWriter fw;
    public BufferedWriter bw;

    /**
     * Constructor that initializes the FastWriter with a file name.
     * Creates a new file for writing using FileWriter and BufferedWriter.
     * Handles IOException by setting FileWriter and BufferedWriter to null and printing the stack trace.
     *
     * @param str The name of the file to be written.
     */
    public FastWriter(String str) {
        try {
            fileName = str;
            file = new File(fileName);
            fw = new FileWriter(file);
            bw = new BufferedWriter(fw);
        } catch (IOException e) {
            fw = null;
            bw = null;
            e.printStackTrace();
        }
    }

    /**
     * Writes the specified string to the file without appending a newline character.
     *
     * @param str The string to be written to the file.
     */
    public void print(String str) {
        try {
            bw.write(str);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Writes the specified string to the file and appends a newline character.
     *
     * @param str The string to be written to the file.
     */
    public void println(String str) {
        try {
            bw.write(str + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Flushes and closes the BufferedWriter and FileWriter.
     * Handles IOException by printing the stack trace.
     */
    public void close() {
        try {
            bw.flush();
            fw.flush();
            bw.close();
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
