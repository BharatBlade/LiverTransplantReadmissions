import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.avro.generic.GenericRecord;


public class LTRStatistics {
	
    // TreeMap to store patient data with patient ID as the key and STARDataPatient object as the value
    public static TreeMap<Integer, STARDataPatient> patientDatabase = new TreeMap<Integer, STARDataPatient>();

    // FastStatistics instance for statistical calculations
    public static FastStatistics statistics = new FastStatistics();

    // FastParquet instance for Parquet-related operations
    public static FastParquet parquet = new FastParquet();

	
    /**
     * The main method for processing liver transplant patient data and calculating statistics.
     */
    public static void main(String[] args) {
        // Set log level to error to reduce log output
        System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "error");

        // Set the delimiter for CSV parsing
        parquet.delimiter = ',';

        // Process liver transplant patient data
        getLiverTransplantPatients();

        // Remove inconsistent BMI data
        removeInconsistentBMIs();

        // Calculate average BMI for each patient
        calcAvgBMI();

        // Calculate average MELD for each patient
        calcAvgMELD();

        // Process liver transplant patient follow-up data
        getLiverTransplantPatientFollowUps();

        // Set readmission status for each patient
        setReadmissionStatus();

        // Print diabetes-related statistics
        System.out.println("DIABETES STATS---------------------------------------------------------------");
        DiabetesStats();

        // Print HCV-related statistics
        System.out.println("HCV STATS---------------------------------------------------------------");
        HCVStats();

        // Print malignancy-related statistics
        System.out.println("MALIGNANCY EVER STATS---------------------------------------------------------------");
        MalignancyStats();

        // Print age-related statistics
        System.out.println("AGE STATS---------------------------------------------------------------");
        AgeStats();

        // Print BMI-related statistics
        System.out.println("BMI STATS---------------------------------------------------------------");
        BMIStats();

        // Print MELD-related statistics
        System.out.println("MELD STATS---------------------------------------------------------------");
        MELDStats();

        // Print functional status-related statistics
        System.out.println("FUNCTIONAL STATUS STATS---------------------------------------------------------------");
        FuncStatusStats();

        // Print the number of patients in the database
        System.out.println("NUMBER OF PATIENTS---------------------------------------------------------------");
        System.out.println(patientDatabase.size());
    }

	
	/**
	 * Retrieves liver transplant patients from a Parquet file and populates the patient database.
	 */
	public static void getLiverTransplantPatients() {
	    // Path to the Parquet file containing liver data
	    String dirLiverData = "LIVER_DATA.parquet";

	    // Initialize ParquetReader for reading data from the Parquet file
	    parquet.parquetReader(dirLiverData);

	    GenericRecord nextRecord;

	    try {
	        // Read the next record from the Parquet file
	        nextRecord = parquet.reader.read();

	        // Process each record until the end of the file is reached
	        while (nextRecord != null) {
	            // Extract relevant fields from the record
	            String code = nextRecord.get("PTCODE").toString();
	            String TRR = nextRecord.get("TRRIDCODE").toString();
	            String age = nextRecord.get("AGE").toString();

	            // Validate patient code, TRR, and age
	            boolean ptCodeIsValid = code.length() > 0 && 
	                                    TRR.length() > 0 && 
	                                    code.charAt(0) >= '0' && 
	                                    code.charAt(0) <= '9' &&
	                                    TRR.charAt(0) == 'A' &&
	                                    age.charAt(0) >= '0' && 
	                                    age.charAt(0) <= '9';

	            // Process valid patient data
	            if (ptCodeIsValid) {
	                Integer codeInt = Integer.valueOf(code.substring(0, code.indexOf('.')));
	                Integer ageInt = Integer.valueOf(age.substring(0, age.indexOf('.')));
	                String dischargeDate = nextRecord.get("DISCHARGEDATE").toString();

	                // Check age and discharge date criteria
	                if (ageInt >= 18 && ageInt <= 100 && dischargeDate.length() > 0) {
	                    // Check if patient is not already in the database
	                    if (!patientDatabase.containsKey(codeInt)) {
	                        STARDataPatient patient = new STARDataPatient(nextRecord);
	                        patientDatabase.put(codeInt, patient);
	                    } else {
	                        // Update existing patient with additional data
	                        patientDatabase.get(codeInt).DISCHARGEDATE_DATA.add(dischargeDate);
	                        if (nextRecord.get("BMICALC").toString().length() > 0)
	                            patientDatabase.get(codeInt).BMICALC_DATA.add(nextRecord.get("BMICALC").toString());
	                        if (nextRecord.get("MELDPELDLABSCORE").toString().length() > 0)
	                            patientDatabase.get(codeInt).MELDPELDLABSCORE_DATA.add(nextRecord.get("MELDPELDLABSCORE").toString());
	                    }
	                }
	            }

	            // Read the next record
	            nextRecord = parquet.reader.read();
	        }

	        // Close the ParquetReader
	        parquet.reader.close();
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	}

	
	/**
	 * Retrieves follow-up data for liver transplant patients and updates the patient database.
	 */
	public static void getLiverTransplantPatientFollowUps() {
	    // Path to the Parquet file containing liver follow-up data
	    String dirLiverFollowUpData = "LIVER_FOLLOWUP_DATA.parquet";

	    // Initialize ParquetReader for reading data from the follow-up Parquet file
	    parquet.parquetReader(dirLiverFollowUpData);

	    try {
	        // Read the next record from the Parquet file
	        GenericRecord nextRecord = parquet.reader.read();

	        // Process each record until the end of the file is reached
	        while (nextRecord != null) {
	            // Extract relevant fields from the record
	            String code = nextRecord.get("PTCODE").toString();
	            String TRR = nextRecord.get("TRRIDCODE").toString();

	            // Validate patient code and TRR
	            boolean ptCodeIsValid = code.length() > 0 && 
	                                    TRR.length() > 0 && 
	                                    code.charAt(0) >= '0' && 
	                                    code.charAt(0) <= '9' &&
	                                    TRR.charAt(0) == 'A';

	            // Process valid patient data
	            if (ptCodeIsValid) {
	                Integer codeInt = Integer.valueOf(code.substring(0, code.indexOf('.')));
	                String Hosp = nextRecord.get("HOSP").toString();
	                String pxStatDate = nextRecord.get("PXSTATDATE").toString();
	                boolean patientExists = patientDatabase.containsKey(codeInt);
	                boolean hospitalized = Hosp.length() == 1 && Hosp.charAt(0) == 'Y';
	                boolean pxStatDateIsValid = (pxStatDate.length() > 7 && pxStatDate.charAt(4) == '-' && pxStatDate.charAt(7) == '-');

	                // Check conditions for valid follow-up data
	                if (patientExists && hospitalized && pxStatDateIsValid) {
	                    patientDatabase.get(codeInt).addFollowUp(nextRecord);
	                }
	            }

	            // Read the next record
	            nextRecord = parquet.reader.read();
	        }

	        // Close the ParquetReader
	        parquet.reader.close();
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	}

	
	/**
	 * Sets the readmission status for liver transplant patients based on follow-up data and discharge dates.
	 */
	public static void setReadmissionStatus() {
	    // Convert patient keys to an array for iteration
	    Object[] arr = patientDatabase.keySet().toArray();

	    // Iterate through each patient in the database
	    for (int i = 0; i < arr.length; i++) {
	        STARDataPatient patient = patientDatabase.get((Integer) arr[i]);

	        // Iterate through each follow-up for the patient
	        for (int j = 0; j < patient.followUps.size(); j++) {
	            STARDataFollowUp followUp = patient.followUps.get(j);

	            // Parse follow-up date using DateTimeFormatter
	            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.ENGLISH);
	            LocalDate date2 = LocalDate.parse(followUp.PXSTATDATE_FOLLOWUP, formatter);

	            // Iterate through each discharge date for the patient
	            for (int k = 0; k < patient.DISCHARGEDATE_DATA.size(); k++) {
	                LocalDate date = LocalDate.parse(patient.DISCHARGEDATE_DATA.get(k), formatter);

	                // Calculate the difference in days between discharge date and follow-up date
	                int difference = (int) ChronoUnit.DAYS.between(date, date2);

	                // Update patient's follow-up data with days since discharge
	                patientDatabase.get((Integer) arr[i]).followUps.get(j).daysSinceDischarge.add(difference);

	                // Check if the patient had a readmission within specified intervals
	                if (difference > 0) {
	                    for (int l = 0; l < statistics.timeIntervals.length; l++) {
	                        if (difference <= statistics.timeIntervals[l]) {
	                            // Set readmission status for the corresponding interval
	                            patientDatabase.get((Integer) arr[i]).readmissions[l] = true;
	                        }
	                    }
	                }
	            }
	        }
	    }
	}

	
	/**
	 * Calculates the average BMI (Body Mass Index) for each liver transplant patient.
	 */
	public static void calcAvgBMI() {
	    // Convert patient keys to an array for iteration
	    Object[] arr = patientDatabase.keySet().toArray();

	    // Iterate through each patient in the database
	    for (int i = 0; i < arr.length; i++) {
	        STARDataPatient patient = patientDatabase.get((Integer) arr[i]);

	        // ArrayList to store BMI values as doubles
	        ArrayList<Double> bmiDoubles = new ArrayList<Double>();

	        // Check if BMI data is available for the patient
	        if (patient.BMICALC_DATA.size() > 0) {
	            // Convert BMI data to doubles and add to the ArrayList
	            for (int j = 0; j < patient.BMICALC_DATA.size(); j++) {
	                bmiDoubles.add(Double.valueOf(patient.BMICALC_DATA.get(j)));
	            }

	            // Calculate the average BMI for the patient using the Mean method from FastStatistics
	            patient.averageBMI = statistics.Mean(bmiDoubles);
	        }
	    }
	}

	
	/**
	 * Calculates the average MELD (Model for End-Stage Liver Disease) score for each liver transplant patient.
	 */
	public static void calcAvgMELD() {
	    // Convert patient keys to an array for iteration
	    Object[] arr = patientDatabase.keySet().toArray();

	    // Iterate through each patient in the database
	    for (int i = 0; i < arr.length; i++) {
	        STARDataPatient patient = patientDatabase.get((Integer) arr[i]);

	        // ArrayList to store MELD scores as doubles
	        ArrayList<Double> meldDoubles = new ArrayList<Double>();

	        // Check if MELD score data is available for the patient
	        if (patient.MELDPELDLABSCORE_DATA.size() > 0) {
	            // Convert MELD score data to doubles and add to the ArrayList
	            for (int j = 0; j < patient.MELDPELDLABSCORE_DATA.size(); j++) {
	                meldDoubles.add(Double.valueOf(patient.MELDPELDLABSCORE_DATA.get(j)));
	            }

	            // Calculate the average MELD score for the patient using the Mean method from FastStatistics
	            patient.averageMELD = statistics.Mean(meldDoubles);
	        }
	    }
	}
	
	/**
	 * Removes patients with inconsistent BMI (Body Mass Index) data from the patient database.
	 */
	public static void removeInconsistentBMIs() {
	    // Convert patient keys to an array for iteration
	    Object[] arr = patientDatabase.keySet().toArray();

	    // Iterate through each patient in the database
	    for (int i = 0; i < arr.length; i++) {
	        STARDataPatient patient = patientDatabase.get((Integer) arr[i]);

	        // TreeSet to store BMI values as doubles for consistency check
	        TreeSet<Double> bmiDoubles = new TreeSet<Double>();

	        // Flag to track inconsistency
	        boolean failure = false;

	        // Check if BMI data is available for the patient
	        if (patient.BMICALC_DATA.size() > 0) {
	            // Convert BMI data to doubles and add to the TreeSet
	            for (int j = 0; j < patient.BMICALC_DATA.size(); j++) {
	                bmiDoubles.add(Double.valueOf(patient.BMICALC_DATA.get(j)));
	            }

	            // Find the lowest and highest BMI values
	            double lowestBMI = bmiDoubles.first();
	            double highestBMI = bmiDoubles.last();

	            // Check consistency of BMI values within specified intervals
	            for (int j = 0; j < statistics.BMICategoryBounds.length - 1; j++) {
	                boolean one = (lowestBMI > statistics.BMICategoryBounds[j] && lowestBMI <= statistics.BMICategoryBounds[j + 1]);
	                boolean two = (highestBMI > statistics.BMICategoryBounds[j] && highestBMI <= statistics.BMICategoryBounds[j + 1]);

	                // Set the failure flag if inconsistency is detected
	                if (one != two) {
	                    failure = true;
	                    break;
	                }
	            }
	        } else {
	            // Set the failure flag if no BMI data is available
	            failure = true;
	        }

	        // Remove inconsistent patients from the database
	        if (failure) {
	            patientDatabase.remove((Integer) arr[i]);
	        }
	    }
	}

	/**
	 * Calculates and prints statistics related to diabetes and readmissions for liver transplant patients.
	 */
	public static void DiabetesStats() {
	    // Convert patient keys to an array for iteration
	    Object[] arr = patientDatabase.keySet().toArray();

	    // Iterate through each patient in the database
	    for (int i = 0; i < arr.length; i++) {
	        STARDataPatient patient = patientDatabase.get((Integer) arr[i]);
	        String strDiab = patient.DIAB_DATA;

	        // Check diabetes status and update statistics accordingly
	        if (strDiab.equals("1.0")) {
	            // Update statistics for patients without diabetes
	        	statistics.noDiabetes[statistics.noDiabetes.length - 1]++;
	            for (int j = 0; j < patient.readmissions.length; j++) {
	                if (patient.readmissions[j]) {
	                	statistics.noDiabetes[j]++;
	                }
	            }
	        } else if (strDiab.equals("2.0") || strDiab.equals("3.0") || strDiab.equals("4.0") || strDiab.equals("5.0")) {
	            // Update statistics for patients with diabetes
	        	statistics.yesDiabetes[statistics.yesDiabetes.length - 1]++;
	            for (int j = 0; j < patient.readmissions.length; j++) {
	                if (patient.readmissions[j]) {
	                	statistics.yesDiabetes[j]++;
	                }
	            }
	        }
	    }

	    // Print chi-square statistics for each time interval
	    for (int i = 0; i < statistics.yesDiabetes.length - 1; i++) {
	        double x = statistics.chiSquareForYNVariables(statistics.yesDiabetes[i], statistics.noDiabetes[i], statistics.yesDiabetes[statistics.yesDiabetes.length - 1], statistics.noDiabetes[statistics.noDiabetes.length - 1]);
	        System.out.println(x);
	    }

	    // Print the total number of readmissions for patients with and without diabetes
	    System.out.println(Arrays.toString(statistics.yesDiabetes));
	    System.out.println(Arrays.toString(statistics.noDiabetes));
	}

	/**
	 * Calculates and prints statistics related to Hepatitis C Virus (HCV) serostatus and readmissions for liver transplant patients.
	 */
	public static void HCVStats() {
	    // Convert patient keys to an array for iteration
	    Object[] arr = patientDatabase.keySet().toArray();

	    // Iterate through each patient in the database
	    for (int i = 0; i < arr.length; i++) {
	        STARDataPatient patient = patientDatabase.get((Integer) arr[i]);
	        String sero = patient.HCVSEROSTATUS_DATA;

	        // Check HCV serostatus and update statistics accordingly
	        if (sero.equals("P")) {
	            // Update statistics for patients with positive HCV serostatus
	        	statistics.yesHCV[statistics.yesHCV.length - 1]++;
	            for (int j = 0; j < patient.readmissions.length; j++) {
	                if (patient.readmissions[j]) {
	                	statistics.yesHCV[j]++;
	                }
	            }
	        } else if (sero.equals("N")) {
	            // Update statistics for patients with negative HCV serostatus
	        	statistics.noHCV[statistics.noHCV.length - 1]++;
	            for (int j = 0; j < patient.readmissions.length; j++) {
	                if (patient.readmissions[j]) {
	                	statistics.noHCV[j]++;
	                }
	            }
	        }
	    }

	    // Print chi-square statistics for each time interval
	    for (int i = 0; i < statistics.yesHCV.length - 1; i++) {
	        double x = statistics.chiSquareForYNVariables(statistics.yesHCV[i], statistics.noHCV[i], statistics.yesHCV[statistics.yesHCV.length - 1], statistics.noHCV[statistics.noHCV.length - 1]);
	        System.out.println(x);
	    }

	    // Print the total number of readmissions for patients with positive and negative HCV serostatus
	    System.out.println(Arrays.toString(statistics.yesHCV));
	    System.out.println(Arrays.toString(statistics.noHCV));
	}
	
	/**
	 * Calculates and prints statistics related to malignancy status and readmissions for liver transplant patients.
	 */
	public static void MalignancyStats() {
	    // Convert patient keys to an array for iteration
	    Object[] arr = patientDatabase.keySet().toArray();

	    // Iterate through each patient in the database
	    for (int i = 0; i < arr.length; i++) {
	        STARDataPatient patient = patientDatabase.get((Integer) arr[i]);
	        String malig = patient.MALIG_DATA;

	        // Check malignancy status and update statistics accordingly
	        if (malig.equals("Y")) {
	            // Update statistics for patients with malignancy
	        	statistics.malignancyPopulationofHospitalized[statistics.malignancyPopulationofHospitalized.length - 1]++;
	            for (int j = 0; j < patient.readmissions.length; j++) {
	                if (patient.readmissions[j]) {
	                	statistics.malignancyPopulationofHospitalized[j]++;
	                }
	            }
	        } else if (malig.equals("N")) {
	            // Update statistics for patients without malignancy
	        	statistics.malignancyPopulationOfNotHospitalized[statistics.malignancyPopulationOfNotHospitalized.length - 1]++;
	            for (int j = 0; j < patient.readmissions.length; j++) {
	                if (patient.readmissions[j]) {
	                	statistics.malignancyPopulationOfNotHospitalized[j]++;
	                }
	            }
	        }
	    }

	    // Print chi-square statistics for each time interval
	    for (int i = 0; i < statistics.malignancyPopulationofHospitalized.length - 1; i++) {
	        double x = statistics.chiSquareForYNVariables(statistics.malignancyPopulationofHospitalized[i], statistics.malignancyPopulationOfNotHospitalized[i], statistics.malignancyPopulationofHospitalized[statistics.malignancyPopulationofHospitalized.length - 1], statistics.malignancyPopulationOfNotHospitalized[statistics.malignancyPopulationOfNotHospitalized.length - 1]);
	        System.out.println(x);
	    }

	    // Print the total number of readmissions for patients with and without malignancy
	    System.out.println(Arrays.toString(statistics.malignancyPopulationofHospitalized));
	    System.out.println(Arrays.toString(statistics.malignancyPopulationOfNotHospitalized));
	}

	/**
	 * Calculates and prints statistics related to patient age and readmissions for liver transplant patients.
	 */
	public static void AgeStats() {
	    // Convert patient keys to an array for iteration
	    Object[] arr = patientDatabase.keySet().toArray();

	    // Iterate through each patient in the database
	    for (int i = 0; i < arr.length; i++) {
	        STARDataPatient patient = patientDatabase.get((Integer) arr[i]);
	        double age = Double.valueOf(patient.AGE_DATA);

	        // Update statistics based on readmission status
	        for (int j = 0; j < patient.readmissions.length; j++) {
	            if (patient.readmissions[j]) {
	                // Update statistics for hospitalized patients
	            	statistics.ageAverageOfHospitalized[j] += age;
	            	statistics.agePopulationofHospitalized[j]++;
	            	statistics.ageValuesOfHospitalized.get(j).add(age);
	            } else {
	                // Update statistics for non-hospitalized patients
	            	statistics.ageAverageOfNotHospitalized[j] += age;
	            	statistics.agePopulationOfNotHospitalized[j]++;
	            	statistics.ageValuesOfNotHospitalized.get(j).add(age);
	            }
	        }
	    }

	    // Calculate average age for hospitalized and non-hospitalized patients
	    for (int j = 0; j < statistics.ageAverageOfHospitalized.length; j++) {
	    	statistics.ageAverageOfHospitalized[j] = statistics.ageAverageOfHospitalized[j] / statistics.agePopulationofHospitalized[j];
	    	statistics.ageAverageOfNotHospitalized[j] = statistics.ageAverageOfNotHospitalized[j] / statistics.agePopulationOfNotHospitalized[j];
	    }

	    // Print statistics related to patient age and readmissions
	    System.out.println(Arrays.toString(statistics.ageAverageOfHospitalized));
	    System.out.println(Arrays.toString(statistics.agePopulationofHospitalized));
	    System.out.println(Arrays.toString(statistics.ageAverageOfNotHospitalized));
	    System.out.println(Arrays.toString(statistics.agePopulationOfNotHospitalized));

	    // Print t-test results for each time interval
	    for (int i = 0; i < statistics.ageValuesOfHospitalized.size(); i++) {
	        double t = statistics.tTest(statistics.ageValuesOfHospitalized.get(i), statistics.ageValuesOfNotHospitalized.get(i));
	        t = Math.abs(t);
	        System.out.println(t);
	    }
	}
	
	/**
	 * Calculates and prints statistics related to patient BMI and readmissions for liver transplant patients.
	 */
	public static void BMIStats() {
	    // Convert patient keys to an array for iteration
	    Object[] arr = patientDatabase.keySet().toArray();

	    // Iterate through each patient in the database
	    for (int i = 0; i < arr.length; i++) {
	        STARDataPatient patient = patientDatabase.get((Integer) arr[i]);
	        double b = patient.averageBMI;

	        // Update statistics based on readmission status and BMI ranges
	        for (int j = 0; j < patient.readmissions.length; j++) {
	            if (patient.readmissions[j]) {
	                // Update statistics for hospitalized patients
	                for (int k = 0; k < statistics.BMICategoryBounds.length - 1; k++) {
	                    if (b > statistics.BMICategoryBounds[k] && b <= statistics.BMICategoryBounds[k + 1]) {
	                    	statistics.bmiAverageOfHospitalized[j][k] += b;
	                    	statistics.bmiPopulationOfHospitalized[j][k]++;
	                    	statistics.bmiValuesOfHospitalized.get(j).add(b);
	                    	statistics.bmiSpecificHospList.get(j).get(k).add(b);
	                    }
	                }
	            } else {
	                // Update statistics for non-hospitalized patients
	                for (int k = 0; k < statistics.BMICategoryBounds.length - 1; k++) {
	                    if (b > statistics.BMICategoryBounds[k] && b <= statistics.BMICategoryBounds[k + 1]) {
	                    	statistics.bmiAverageOfNotHospitalized[j][k] += b;
	                    	statistics.bmiPopulationOfNotHospitalized[j][k]++;
	                    	statistics.bmiValuesOfNotHospitalized.get(j).add(b);
	                    	statistics.bmiSpecificNoHospList.get(j).get(k).add(b);
	                    }
	                }
	            }
	        }
	    }

	    // Calculate average BMI for hospitalized and non-hospitalized patients
	    for (int i = 0; i < statistics.bmiAverageOfHospitalized.length; i++) {
	        for (int j = 0; j < statistics.bmiAverageOfHospitalized[i].length; j++) {
	        	statistics.bmiAverageOfHospitalized[i][j] = statistics.bmiAverageOfHospitalized[i][j] / statistics.bmiPopulationOfHospitalized[i][j];
	        	statistics.bmiAverageOfNotHospitalized[i][j] = statistics.bmiAverageOfNotHospitalized[i][j] / statistics.bmiPopulationOfNotHospitalized[i][j];
	        }
	    }

	    // Print statistics related to patient BMI and readmissions
	    for (int i = 0; i < statistics.bmiAverageOfHospitalized.length; i++) {
	        System.out.println(Arrays.toString(statistics.bmiAverageOfHospitalized[i]));
	    }
	    for (int i = 0; i < statistics.bmiPopulationOfHospitalized.length; i++) {
	        System.out.println(Arrays.toString(statistics.bmiPopulationOfHospitalized[i]));
	    }
	    for (int i = 0; i < statistics.bmiAverageOfNotHospitalized.length; i++) {
	        System.out.println(Arrays.toString(statistics.bmiAverageOfNotHospitalized[i]));
	    }
	    for (int i = 0; i < statistics.bmiPopulationOfNotHospitalized.length; i++) {
	        System.out.println(Arrays.toString(statistics.bmiPopulationOfNotHospitalized[i]));
	    }
	    System.out.println();

	    // Print chi-square test results for BMI ranges
	    for (int i = 0; i < statistics.bmiAverageOfHospitalized.length; i++) {
	        for (int j = 0; j < statistics.bmiAverageOfHospitalized[i].length; j++) {
	            System.out.print(statistics.chiSquare(statistics.bmiPopulationOfHospitalized[i][j], statistics.bmiPopulationOfNotHospitalized[i][j],
	            		statistics.sum(statistics.bmiPopulationOfHospitalized[i]), statistics.sum(statistics.bmiPopulationOfNotHospitalized[i])) + ", ");
	        }
	        System.out.println();
	    }
	    System.out.println();

	    // Print t-test results for BMI averages
	    for (int i = 0; i < statistics.bmiValuesOfHospitalized.size(); i++) {
	        System.out.println(statistics.tTest(statistics.bmiValuesOfHospitalized.get(i), statistics.bmiValuesOfNotHospitalized.get(i)));
	    }
	    System.out.println();
	    System.out.println();

	    // Print t-test results for specific BMI ranges
	    for (int i = 0; i < statistics.bmiSpecificHospList.size(); i++) {
	        for (int j = 0; j < statistics.bmiSpecificHospList.get(i).size(); j++) {
	            System.out.print(statistics.tTest(statistics.bmiSpecificHospList.get(i).get(j), statistics.bmiSpecificNoHospList.get(i).get(j)) + ", ");
	        }
	        System.out.println();
	    }
	}

		
	/**
	 * Calculates and prints statistics related to patient MELD scores and readmissions for liver transplant patients.
	 */
	public static void MELDStats() {
	    // Convert patient keys to an array for iteration
	    Object[] arr = patientDatabase.keySet().toArray();

	    // Iterate through each patient in the database
	    for (int i = 0; i < arr.length; i++) {
	        STARDataPatient patient = patientDatabase.get((Integer) arr[i]);
	        double b = patient.averageMELD;

	        // Update statistics based on readmission status and MELD score ranges
	        for (int j = 0; j < patient.readmissions.length; j++) {
	            if (patient.readmissions[j]) {
	                // Update statistics for hospitalized patients
	                for (int k = 0; k < statistics.MELDCategoryBounds.length - 1; k++) {
	                    if (b > statistics.MELDCategoryBounds[k] && b <= statistics.MELDCategoryBounds[k + 1]) {
	                    	statistics.meldAverageOfHospitalized[j][k] += b;
	                    	statistics.meldPopulationofHospitalized[j][k]++;
	                    	statistics.meldValuesOfHospitalized.get(j).add(b);
	                    	statistics.meldSpecificHospList.get(j).get(k).add(b);
	                    }
	                }
	            } else {
	                // Update statistics for non-hospitalized patients
	                for (int k = 0; k < statistics.MELDCategoryBounds.length - 1; k++) {
	                    if (b > statistics.MELDCategoryBounds[k] && b <= statistics.MELDCategoryBounds[k + 1]) {
	                    	statistics.meldAverageOfNotHospitalized[j][k] += b;
	                    	statistics.meldPopulationOfNotHospitalized[j][k]++;
	                    	statistics.meldValuesOfNotHospitalized.get(j).add(b);
	                    	statistics.meldSpecificNoHospList.get(j).get(k).add(b);
	                    }
	                }
	            }
	        }
	    }

	    // Calculate average MELD scores for hospitalized and non-hospitalized patients
	    for (int i = 0; i < statistics.meldAverageOfHospitalized.length; i++) {
	        for (int j = 0; j < statistics.meldAverageOfHospitalized[i].length; j++) {
	        	statistics.meldAverageOfHospitalized[i][j] = statistics.meldAverageOfHospitalized[i][j] / statistics.meldPopulationofHospitalized[i][j];
	        	statistics.meldAverageOfNotHospitalized[i][j] = statistics.meldAverageOfNotHospitalized[i][j] / statistics.meldPopulationOfNotHospitalized[i][j];
	        }
	    }

	    // Print statistics related to patient MELD scores and readmissions
	    for (int i = 0; i < statistics.meldAverageOfHospitalized.length; i++) {
	        System.out.println(Arrays.toString(statistics.meldAverageOfHospitalized[i]));
	    }
	    for (int i = 0; i < statistics.meldPopulationofHospitalized.length; i++) {
	        System.out.println(Arrays.toString(statistics.meldPopulationofHospitalized[i]));
	    }
	    for (int i = 0; i < statistics.meldAverageOfNotHospitalized.length; i++) {
	        System.out.println(Arrays.toString(statistics.meldAverageOfNotHospitalized[i]));
	    }
	    for (int i = 0; i < statistics.meldPopulationOfNotHospitalized.length; i++) {
	        System.out.println(Arrays.toString(statistics.meldPopulationOfNotHospitalized[i]));
	    }

	    // Print chi-square test results for MELD score ranges
	    for (int i = 0; i < statistics.meldAverageOfHospitalized.length; i++) {
	        for (int j = 0; j < statistics.meldAverageOfHospitalized[i].length; j++) {
	            System.out.print(statistics.chiSquare(statistics.meldPopulationofHospitalized[i][j], statistics.meldPopulationOfNotHospitalized[i][j],
	            		statistics.sum(statistics.meldPopulationofHospitalized[i]), statistics.sum(statistics.meldPopulationOfNotHospitalized[i])) + ", ");
	        }
	        System.out.println();
	    }
	    System.out.println();

	    // Print t-test results for MELD score averages
	    for (int i = 0; i < statistics.meldValuesOfHospitalized.size(); i++) {
	        System.out.println(statistics.tTest(statistics.meldValuesOfHospitalized.get(i), statistics.meldValuesOfNotHospitalized.get(i)));
	    }
	    System.out.println();
	    System.out.println();

	    // Print t-test results for specific MELD score ranges
	    for (int i = 0; i < statistics.meldSpecificHospList.size(); i++) {
	        for (int j = 0; j < statistics.meldSpecificHospList.get(i).size(); j++) {
	            System.out.print(statistics.tTest(statistics.meldSpecificHospList.get(i).get(j), statistics.meldSpecificNoHospList.get(i).get(j)) + ", ");
	        }
	        System.out.println();
	    }
	}


	/**
	 * Calculates and prints statistics related to patient functional status (FUNCSTATTRR) and readmissions for liver transplant patients.
	 */
	public static void FuncStatusStats() {
	    // Convert patient keys to an array for iteration
	    Object[] arr = patientDatabase.keySet().toArray();

	    // Iterate through each patient in the database
	    for (int i = 0; i < arr.length; i++) {
	        STARDataPatient patient = patientDatabase.get((Integer) arr[i]);

	        // Check if FUNCSTATTRR_DATA has a valid length
	        if (patient.FUNCSTATTRR_DATA.length() == 6) {
	            double funcstat = Double.valueOf(patient.FUNCSTATTRR_DATA);

	            // Update statistics based on readmission status and functional status intervals
	            for (int j = 0; j < patient.readmissions.length; j++) {
	                if (patient.readmissions[j]) {
	                    // Update statistics for hospitalized patients
	                    for (int k = 0; k < statistics.functionalStatusCategories.length; k++) {
	                        if (funcstat == statistics.functionalStatusCategories[k]) {
	                        	statistics.functionalStatusofHospitalized[j][k]++;
	                            break;
	                        }
	                    }
	                } else {
	                    // Update statistics for non-hospitalized patients
	                    for (int k = 0; k < statistics.functionalStatusCategories.length; k++) {
	                        if (funcstat == statistics.functionalStatusCategories[k]) {
	                        	statistics.functionalStatusofNotHospitalized[j][k]++;
	                            break;
	                        }
	                    }
	                }
	            }
	        }
	    }

	    // Print functional status statistics for hospitalized and non-hospitalized patients
	    for (int i = 0; i < statistics.functionalStatusofHospitalized.length; i++) {
	        System.out.println(statistics.timeIntervals[i]);
	        System.out.println(Arrays.toString(statistics.functionalStatusofHospitalized[i]));
	        System.out.println(Arrays.toString(statistics.functionalStatusofNotHospitalized[i]));
	    }
	    System.out.println();

	    // Print chi-square test results for functional status intervals
	    for (int i = 0; i < statistics.functionalStatusofHospitalized.length; i++) {
	        for (int j = 0; j < statistics.functionalStatusofHospitalized[i].length; j++) {
	            System.out.print(statistics.chiSquare(statistics.functionalStatusofHospitalized[i][j], statistics.functionalStatusofNotHospitalized[i][j],
	            		statistics.sum(statistics.functionalStatusofHospitalized[i]), statistics.sum(statistics.functionalStatusofNotHospitalized[i])) + ", ");
	        }
	        System.out.println();
	    }
	    System.out.println();
	}

}