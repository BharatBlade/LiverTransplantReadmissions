import java.util.ArrayList;

/**
 * A utility class for storing and performing statistical calculations on healthcare data.
 */
public class FastStatistics {
    // Arrays to store counts for various intervals
    public int[] timeIntervals = {30, 90, 365, 365*5};
    public int[] yesDiabetes = new int[timeIntervals.length+1];
    public int[] noDiabetes = new int[timeIntervals.length+1];
    public int[] yesHCV = new int[timeIntervals.length+1];
    public int[] noHCV = new int[timeIntervals.length+1];
    
    // Arrays and lists for storing BMI-related data
    public double[] BMICategoryBounds = {0, 18.5, 25, 30, 35, 40, 50, 100};
    
    //These arrays are for fast calculations of average BMI of each timeInterval x BMICategory group
    public double[][] bmiAverageOfHospitalized = new double[timeIntervals.length][BMICategoryBounds.length-1];
    public double[][] bmiPopulationOfHospitalized = new double[timeIntervals.length][BMICategoryBounds.length-1];
    public double[][] bmiAverageOfNotHospitalized = new double[timeIntervals.length][BMICategoryBounds.length-1];
    public double[][] bmiPopulationOfNotHospitalized = new double[timeIntervals.length][BMICategoryBounds.length-1];
    
    //These lists are for statistical chi-square/t-tests
    
    //Lists separated by timeIntervals
    public ArrayList<ArrayList<Double>> bmiValuesOfHospitalized = new ArrayList<ArrayList<Double>>();
    public ArrayList<ArrayList<Double>> bmiValuesOfNotHospitalized = new ArrayList<ArrayList<Double>>();
    //2D Lists separated by timeIntervals and BMICategories
    public ArrayList<ArrayList<ArrayList<Double>>> bmiSpecificHospList = new ArrayList<ArrayList<ArrayList<Double>>>();
    public ArrayList<ArrayList<ArrayList<Double>>> bmiSpecificNoHospList = new ArrayList<ArrayList<ArrayList<Double>>>();

    // Arrays and lists for storing MELD-related data
    public double[] MELDCategoryBounds = {6, 15, 21, 28, 40};
    public double[][] meldAverageOfHospitalized = new double[timeIntervals.length][BMICategoryBounds.length-1];
    public double[][] meldPopulationofHospitalized = new double[timeIntervals.length][BMICategoryBounds.length-1];
    public double[][] meldAverageOfNotHospitalized = new double[timeIntervals.length][BMICategoryBounds.length-1];
    public double[][] meldPopulationOfNotHospitalized = new double[timeIntervals.length][BMICategoryBounds.length-1];
    public ArrayList<ArrayList<Double>> meldValuesOfHospitalized = new ArrayList<ArrayList<Double>>();
    public ArrayList<ArrayList<Double>> meldValuesOfNotHospitalized = new ArrayList<ArrayList<Double>>();
    public ArrayList<ArrayList<ArrayList<Double>>> meldSpecificHospList = new ArrayList<ArrayList<ArrayList<Double>>>();
    public ArrayList<ArrayList<ArrayList<Double>>> meldSpecificNoHospList = new ArrayList<ArrayList<ArrayList<Double>>>();

    // Lists for storing MELD distribution
    ArrayList<ArrayList<Double>> MELDDistribution = new ArrayList<ArrayList<Double>>();

    // Arrays and lists for storing age-related data
    public double[] ageAverageOfHospitalized = new double[timeIntervals.length];
    public double[] agePopulationofHospitalized = new double[timeIntervals.length];
    public double[] ageAverageOfNotHospitalized = new double[timeIntervals.length];
    public double[] agePopulationOfNotHospitalized = new double[timeIntervals.length];
    public ArrayList<ArrayList<Double>> ageValuesOfHospitalized = new ArrayList<ArrayList<Double>>();
    public ArrayList<ArrayList<Double>> ageValuesOfNotHospitalized = new ArrayList<ArrayList<Double>>();

    // Arrays for storing counts of malignancy
    public int[] malignancyPopulationofHospitalized = new int[timeIntervals.length+1];
    public int[] malignancyPopulationOfNotHospitalized = new int[timeIntervals.length+1]; // Added noMalig array

    // Arrays and lists for storing functional status data
    public double[] functionalStatusCategories = {2010, 2020, 2030, 2040, 2050, 2060, 2070, 2080, 2090, 2100};
    public double[][] functionalStatusofHospitalized = new double[timeIntervals.length][functionalStatusCategories.length];
    public double[][] functionalStatusofNotHospitalized = new double[timeIntervals.length][functionalStatusCategories.length];

    /**
     * Constructor that initializes lists and arrays for statistical data.
     */
    public FastStatistics() {
        // Initialize lists for BMI-related data
        for (int i = 0; i < timeIntervals.length; i++) {
        	bmiValuesOfHospitalized.add(new ArrayList<Double>());
        	bmiValuesOfNotHospitalized.add(new ArrayList<Double>());
        	meldValuesOfHospitalized.add(new ArrayList<Double>());
        	meldValuesOfNotHospitalized.add(new ArrayList<Double>());
        	ageValuesOfHospitalized.add(new ArrayList<Double>());
        	ageValuesOfNotHospitalized.add(new ArrayList<Double>());
        }

        // Initialize lists and sublists for specific BMI and MELD data
        for (int i = 0; i < timeIntervals.length; i++) {
            bmiSpecificHospList.add(new ArrayList<ArrayList<Double>>());
            bmiSpecificNoHospList.add(new ArrayList<ArrayList<Double>>());
            meldSpecificHospList.add(new ArrayList<ArrayList<Double>>());
            meldSpecificNoHospList.add(new ArrayList<ArrayList<Double>>());
            for (int j = 0; j < BMICategoryBounds.length-1; j++) {
                bmiSpecificHospList.get(i).add(new ArrayList<Double>());
                bmiSpecificNoHospList.get(i).add(new ArrayList<Double>());
            }
            for (int j = 0; j < MELDCategoryBounds.length-1; j++) {
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
    
    public double chiSquareForYNVariables(double varYHosp, double varNHosp, double varYTotal, double varNTotal) {
    	double x2Y = chiSquare(varYHosp, varNHosp, varYTotal, varNTotal);
    	double x2N = chiSquare(varNHosp, varYHosp, varNTotal, varYTotal);
    	return x2Y + x2N;
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
