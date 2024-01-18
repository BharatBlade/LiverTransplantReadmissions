import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
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
    
    public double chiSquareForYNVariables(double varYHosp, double varNHosp, double varYTotal, double varNTotal) {
    	double x2Y = chiSquare(varYHosp, varNHosp, varYTotal, varNTotal);
    	double x2N = chiSquare(varNHosp, varYHosp, varNTotal, varYTotal);
    	return x2Y + x2N;
    }

	//Lanczos's Approximation for the Gamma Function used in the
    //Chi-Square Probability Distribution Function
	public double gamma(double x) {
	   return Math.exp(
		   ((x - 0.5) * Math.log(x + 4.5) - (x + 4.5)) + 
		   
		   Math.log(
				   (  1.0000000001900148240 
				   + 76.180091729471463483        / (x + 0)   
				   - 86.505320329416767652        / (x + 1)
                   + 24.014098240830910490        / (x + 2)
                   -  1.2317395724501553875       / (x + 3)
                   +  0.0012086509738661785061    / (x + 4)
                   -  0.0000053952393849531283785 / (x + 5)
                   )
				   * Math.sqrt(2 * Math.PI)
		   )
	   ); 
	}
	
	public BigDecimal BIGgamma(double x) {
		BigDecimal total = new BigDecimal(1);
		while(x > 1) {
			total = total.multiply(new BigDecimal(x-1));
			x -= 1;
		}
		return total.multiply(new BigDecimal(gamma(x)));
	}
	
//	public BigDecimal BIGgamma(double x) {
//		BigDecimal constant1 = new BigDecimal(1.0000000001900148240);
//		BigDecimal constant2 = new BigDecimal(76.180091729471463483);
//		constant2 = constant2.divide(new BigDecimal(x+0));
//		BigDecimal constant3 = new BigDecimal(86.505320329416767652);
//		constant3 = constant3.divide(new BigDecimal(x+1));
//		BigDecimal constant4 = new BigDecimal(24.014098240830910490);
//		constant4 = constant4.divide(new BigDecimal(x+2));
//		BigDecimal constant5 = new BigDecimal(1.2317395724501553875);
//		constant5 = constant5.divide(new BigDecimal(x+3));
//		BigDecimal constant6 = new BigDecimal(0.0012086509738661785061);
//		constant6 = constant6.divide(new BigDecimal(x+4));
//		BigDecimal constant7 = new BigDecimal(0.0000053952393849531283785);
//		constant7 = constant7.divide(new BigDecimal(x+5));
//		
//		
//		BigDecimal log = constant1.add(constant2).subtract(constant3).add(constant4).subtract(constant5).add(constant6).subtract(constant7);
//		log = log.multiply(new BigDecimal(Math.sqrt(2 * Math.PI)));
//		BigDecimal temp = 
//		
//		BigDecimal BIG = new BigDecimal(
//		   Math.exp(
//			   ((x - 0.5) * Math.log(x + 4.5) - (x + 4.5)) + 
//			   
//			   Math.log(log)
//		   )
//		);
//		
//		return BIG;
//	}   
    
    public double pValueChiSquare(double x2, double df) {
    	double temp1 = df/2;
    	return (Math.pow(x2, temp1-1)*Math.exp(-x2/2)) / (gamma(temp1)*Math.pow(2, temp1));
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
    
    public BigDecimal pValueTTest(double t, double df) {
    	double temp1 = df/2;
    	BigDecimal temp2 = BIGgamma(temp1+(0.5));
    	BigDecimal temp3 = BIGgamma(temp1);
    	
    	System.out.println(t + ", " + df + ", " + temp1 + ", " + temp2.toString().substring(0, 10) + ", " + temp3.toString().substring(0, 10));
    	
    	temp2 = temp2.divide(temp3);
        BigDecimal pow = new BigDecimal(1 + (t * t / df));
        pow = pow.pow((int) temp1);  // Integer part of the exponent
        
        double dec = (temp1 + 0.5) - (int) (temp1 + 0.5);
        pow = pow.multiply(new BigDecimal(Math.pow(1 + t*t/df, dec)));
        
        temp2 = temp2.multiply(new BigDecimal(1).divide(pow, 100, RoundingMode.HALF_UP));
        
        BigDecimal sqrtPI = new BigDecimal(Math.sqrt(Math.PI));
        temp2 = temp2.divide(sqrtPI, 100, RoundingMode.HALF_UP);
        
        BigDecimal sqrtDf = new BigDecimal(Math.sqrt(df));
        temp2 = temp2.divide(sqrtDf, 10, RoundingMode.HALF_UP);

        
        
        return temp2;
    }
}
