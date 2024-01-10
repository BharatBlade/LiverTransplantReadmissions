import java.util.ArrayList;

/**
 * A utility class for storing and performing statistical calculations on healthcare data.
 */
public class FastStatistics {
    // Arrays to store counts for various intervals
    public int[] intervals = {30, 90, 365, 365*5};
    public int[] diab = new int[intervals.length+1];
    public int[] noDiab = new int[intervals.length+1];
    public int[] hcv = new int[intervals.length+1];
    public int[] nohcv = new int[intervals.length+1];
    
    // Arrays and lists for storing BMI-related data
    public double[] BMI = {0, 18.5, 25, 30, 35, 40, 50, 100};
    public double[][] bmiHosp = new double[intervals.length][BMI.length-1];
    public double[][] bmiHospCount = new double[intervals.length][BMI.length-1];
    public double[][] bmiNoHosp = new double[intervals.length][BMI.length-1];
    public double[][] bmiNoHospCount = new double[intervals.length][BMI.length-1];
    public ArrayList<ArrayList<Double>> bmiHospList = new ArrayList<ArrayList<Double>>();
    public ArrayList<ArrayList<Double>> bmiNoHospList = new ArrayList<ArrayList<Double>>();
    public ArrayList<ArrayList<ArrayList<Double>>> bmiSpecificHospList = new ArrayList<ArrayList<ArrayList<Double>>>();
    public ArrayList<ArrayList<ArrayList<Double>>> bmiSpecificNoHospList = new ArrayList<ArrayList<ArrayList<Double>>>();

    // Arrays and lists for storing MELD-related data
    public double[] MELD = {6, 15, 21, 28, 40};
    public double[][] meldHosp = new double[intervals.length][BMI.length-1];
    public double[][] meldHospCount = new double[intervals.length][BMI.length-1];
    public double[][] meldNoHosp = new double[intervals.length][BMI.length-1];
    public double[][] meldNoHospCount = new double[intervals.length][BMI.length-1];
    public ArrayList<ArrayList<Double>> meldHospList = new ArrayList<ArrayList<Double>>();
    public ArrayList<ArrayList<Double>> meldNoHospList = new ArrayList<ArrayList<Double>>();
    public ArrayList<ArrayList<ArrayList<Double>>> meldSpecificHospList = new ArrayList<ArrayList<ArrayList<Double>>>();
    public ArrayList<ArrayList<ArrayList<Double>>> meldSpecificNoHospList = new ArrayList<ArrayList<ArrayList<Double>>>();

    // Lists for storing MELD distribution
    ArrayList<ArrayList<Double>> MELDDistribution = new ArrayList<ArrayList<Double>>();

    // Arrays and lists for storing age-related data
    public double[] ageHosp = new double[intervals.length];
    public double[] ageHospCount = new double[intervals.length];
    public double[] ageNoHosp = new double[intervals.length];
    public double[] ageNoHospCount = new double[intervals.length];
    public ArrayList<ArrayList<Double>> ageHospList = new ArrayList<ArrayList<Double>>();
    public ArrayList<ArrayList<Double>> ageNoHospList = new ArrayList<ArrayList<Double>>();

    // Arrays for storing counts of malignancy
    public int[] malig = new int[intervals.length+1];
    public int[] noMalig = new int[intervals.length+1]; // Added noMalig array

    // Arrays and lists for storing functional status data
    public double[] funcStat = {2010, 2020, 2030, 2040, 2050, 2060, 2070, 2080, 2090, 2100};
    public double[][] funcStatHosp = new double[intervals.length][funcStat.length];
    public double[][] funcStatNoHosp = new double[intervals.length][funcStat.length];

    /**
     * Constructor that initializes lists and arrays for statistical data.
     */
    public FastStatistics() {
        // Initialize lists for BMI-related data
        for (int i = 0; i < intervals.length; i++) {
            bmiHospList.add(new ArrayList<Double>());
            bmiNoHospList.add(new ArrayList<Double>());
            meldHospList.add(new ArrayList<Double>());
            meldNoHospList.add(new ArrayList<Double>());
            ageHospList.add(new ArrayList<Double>());
            ageNoHospList.add(new ArrayList<Double>());
        }

        // Initialize lists and sublists for specific BMI and MELD data
        for (int i = 0; i < intervals.length; i++) {
            bmiSpecificHospList.add(new ArrayList<ArrayList<Double>>());
            bmiSpecificNoHospList.add(new ArrayList<ArrayList<Double>>());
            meldSpecificHospList.add(new ArrayList<ArrayList<Double>>());
            meldSpecificNoHospList.add(new ArrayList<ArrayList<Double>>());
            for (int j = 0; j < BMI.length-1; j++) {
                bmiSpecificHospList.get(i).add(new ArrayList<Double>());
                bmiSpecificNoHospList.get(i).add(new ArrayList<Double>());
            }
            for (int j = 0; j < MELD.length-1; j++) {
                meldSpecificHospList.get(i).add(new ArrayList<Double>());
                meldSpecificNoHospList.get(i).add(new ArrayList<Double>());                
            }
        }
    }

    /**
     * Performs chi-square test for a contingency table.
     *
     * @param subY  Count for subset Y.
     * @param subN  Count for subset N.
     * @param popY  Count for population Y.
     * @param popN  Count for population N.
     * @return      The chi-square score.
     */
    public double chiSquare(double subY, double subN, double popY, double popN) {
        double expected = (subY + subN) * (popY / (popY + popN));
        double observed = subY;
        double score = Math.pow(observed - expected, 2) / expected;
        return score;
    }

    /**
     * Calculates the sum of an array of doubles.
     *
     * @param arr   The array of doubles.
     * @return      The sum of the array.
     */
    public double sum(double[] arr) {
        double total = 0;
        for (int i = 0; i < arr.length; i++) {
            total += arr[i];
        }
        return total;
    }

    /**
     * Calculates the mean of an ArrayList of doubles.
     *
     * @param arr   The ArrayList of doubles.
     * @return      The mean of the ArrayList.
     */
    public double Mean(ArrayList<Double> arr) {
        double sum = 0;
        for (int i = 0; i < arr.size(); i++) {
            sum = sum + arr.get(i);
        }
        return sum / arr.size();
    }

    /**
     * Calculates the mean of an array of doubles.
     *
     * @param arr   The array of doubles.
     * @return      The mean of the array.
     */
    public double Mean(double[] arr) {
        double sum = 0;
        for (int i = 0; i < arr.length; i++) {
            sum = sum + arr[i];
        }
        return sum / arr.length;
    }

    /**
     * Calculates the standard deviation of an ArrayList of doubles.
     *
     * @param arr   The ArrayList of doubles.
     * @param mean  The mean of the ArrayList.
     * @return      The standard deviation of the ArrayList.
     */
    public double standardDeviation(ArrayList<Double> arr, double mean) {
        double sum = 0;
        for (int i = 0; i < arr.size(); i++) {
            sum = sum + (arr.get(i) - mean) * (arr.get(i) - mean);
        }
        return Math.sqrt(sum / (arr.size() - 1));
    }

    /**
     * Performs a t-test for two ArrayLists of doubles.
     *
     * @param arr1  The first ArrayList.
     * @param arr2  The second ArrayList.
     * @return      The t-test value.
     */
    public double tTest(ArrayList<Double> arr1, ArrayList<Double> arr2) {
        double mean1 = Mean(arr1);
        double mean2 = Mean(arr2);
        double sd1 = standardDeviation(arr1, mean1);
        double sd2 = standardDeviation(arr2, mean2);
        double t_test = (mean1 - mean2) / Math.sqrt((sd1 * sd1) / arr1.size() + (sd2 * sd2) / arr2.size());
        return t_test;
    }
}
