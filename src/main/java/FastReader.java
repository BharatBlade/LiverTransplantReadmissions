import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

/**
 * A utility class for reading files quickly using BufferedReader.
 */
public class FastReader {
    // Variables to store file-related information
    public String fileName;
    public String[] fileNames;
    public File file;
    public File[] files;
    public FileReader fr;
    public BufferedReader br;

    /**
     * Constructor that initializes the FastReader with a file name.
     * Opens the file for reading using FileReader and BufferedReader.
     * Handles IOException by setting FileReader and BufferedReader to null and printing the stack trace.
     *
     * @param str The name of the file to be read.
     */
    public FastReader(String str) {
        try {
            fileName = str;
            file = new File(fileName);
            fr = new FileReader(file);
            br = new BufferedReader(fr);
        } catch (IOException e) {
            fr = null;
            br = null;
            e.printStackTrace();
        }
    }

    /**
     * Reads the next line from the file using BufferedReader.
     *
     * @return The next line from the file, or null if an IOException occurs.
     */
    public String nextLine() {
        try {
            return br.readLine();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Closes the BufferedReader and FileReader.
     * Handles IOException by printing the stack trace.
     */
    public void close() {
        try {
            br.close();
            fr.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Counts the number of columns in a CSV line using a specified delimiter.
     *
     * @param line      The CSV line to count columns from.
     * @param delimiter The character used to separate values in the CSV line.
     * @return The number of columns in the CSV line.
     */
    public int countColumns(String line, String delimiter) {
        return ((line.length() - line.replace(delimiter, "").length()) / delimiter.length() + 1);
    }

    /**
     * Converts a CSV (Comma-Separated Values) line to an array of strings using the specified delimiter.
     * Assumes that the input CSV line is well-formed.
     *
     * @param s          The CSV line to be converted.
     * @param delimiter  The character used to separate values in the CSV line.
     * @return           An array of strings containing individual values from the CSV line.
     */
    public String[] csvLineToArray(String s, char delimiter) {
        String[] t = new String[countColumns(s, String.valueOf(delimiter))];
        for (int i = 0; i < t.length - 1; i++) {
            int p = s.indexOf(delimiter);
            t[i] = s.substring(0, p);
            s = s.substring(p + 1);
        }
        t[t.length - 1] = s;
        return t;
    }
}
