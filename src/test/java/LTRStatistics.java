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
    public static FastStatistics stats = new FastStatistics();

    // FastParquet instance for Parquet-related operations
    public static FastParquet p = new FastParquet();

	
    /**
     * The main method for processing liver transplant patient data and calculating statistics.
     */
    public static void main(String[] args) {
        // Set log level to error to reduce log output
        System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "error");

        // Set the delimiter for CSV parsing
        p.delimiter = ',';

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
	    p.parquetReader(dirLiverData);

	    GenericRecord nextRecord;

	    try {
	        // Read the next record from the Parquet file
	        nextRecord = p.reader.read();

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
	            nextRecord = p.reader.read();
	        }

	        // Close the ParquetReader
	        p.reader.close();
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
	    p.parquetReader(dirLiverFollowUpData);

	    try {
	        // Read the next record from the Parquet file
	        GenericRecord nextRecord = p.reader.read();

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
	            nextRecord = p.reader.read();
	        }

	        // Close the ParquetReader
	        p.reader.close();
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
	                    for (int l = 0; l < stats.intervals.length; l++) {
	                        if (difference <= stats.intervals[l]) {
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
	            patient.averageBMI = stats.Mean(bmiDoubles);
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
	            patient.averageMELD = stats.Mean(meldDoubles);
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
	            for (int j = 0; j < stats.BMI.length - 1; j++) {
	                boolean one = (lowestBMI > stats.BMI[j] && lowestBMI <= stats.BMI[j + 1]);
	                boolean two = (highestBMI > stats.BMI[j] && highestBMI <= stats.BMI[j + 1]);

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
	            stats.noDiab[stats.noDiab.length - 1]++;
	            for (int j = 0; j < patient.readmissions.length; j++) {
	                if (patient.readmissions[j]) {
	                    stats.noDiab[j]++;
	                }
	            }
	        } else if (strDiab.equals("2.0") || strDiab.equals("3.0") || strDiab.equals("4.0") || strDiab.equals("5.0")) {
	            // Update statistics for patients with diabetes
	            stats.diab[stats.diab.length - 1]++;
	            for (int j = 0; j < patient.readmissions.length; j++) {
	                if (patient.readmissions[j]) {
	                    stats.diab[j]++;
	                }
	            }
	        }
	    }

	    // Print chi-square statistics for each time interval
	    for (int i = 0; i < stats.diab.length - 1; i++) {
	        System.out.println(stats.chiSquare(stats.diab[i], stats.noDiab[i], stats.diab[stats.diab.length - 1], stats.noDiab[stats.noDiab.length - 1]));
	    }

	    // Print the total number of readmissions for patients with and without diabetes
	    System.out.println(Arrays.toString(stats.diab));
	    System.out.println(Arrays.toString(stats.noDiab));
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
	            stats.hcv[stats.hcv.length - 1]++;
	            for (int j = 0; j < patient.readmissions.length; j++) {
	                if (patient.readmissions[j]) {
	                    stats.hcv[j]++;
	                }
	            }
	        } else if (sero.equals("N")) {
	            // Update statistics for patients with negative HCV serostatus
	            stats.nohcv[stats.nohcv.length - 1]++;
	            for (int j = 0; j < patient.readmissions.length; j++) {
	                if (patient.readmissions[j]) {
	                    stats.nohcv[j]++;
	                }
	            }
	        }
	    }

	    // Print chi-square statistics for each time interval
	    for (int i = 0; i < stats.hcv.length - 1; i++) {
	        System.out.println(stats.chiSquare(stats.hcv[i], stats.nohcv[i], stats.hcv[stats.hcv.length - 1], stats.nohcv[stats.nohcv.length - 1]));
	    }

	    // Print the total number of readmissions for patients with positive and negative HCV serostatus
	    System.out.println(Arrays.toString(stats.hcv));
	    System.out.println(Arrays.toString(stats.nohcv));
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
	            stats.malig[stats.malig.length - 1]++;
	            for (int j = 0; j < patient.readmissions.length; j++) {
	                if (patient.readmissions[j]) {
	                    stats.malig[j]++;
	                }
	            }
	        } else if (malig.equals("N")) {
	            // Update statistics for patients without malignancy
	            stats.noMalig[stats.noMalig.length - 1]++;
	            for (int j = 0; j < patient.readmissions.length; j++) {
	                if (patient.readmissions[j]) {
	                    stats.noMalig[j]++;
	                }
	            }
	        }
	    }

	    // Print chi-square statistics for each time interval
	    for (int i = 0; i < stats.malig.length - 1; i++) {
	        System.out.println(stats.chiSquare(stats.malig[i], stats.noMalig[i], stats.malig[stats.malig.length - 1], stats.noMalig[stats.noMalig.length - 1]));
	    }

	    // Print the total number of readmissions for patients with and without malignancy
	    System.out.println(Arrays.toString(stats.malig));
	    System.out.println(Arrays.toString(stats.noMalig));
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
	                stats.ageHosp[j] += age;
	                stats.ageHospCount[j]++;
	                stats.ageHospList.get(j).add(age);
	            } else {
	                // Update statistics for non-hospitalized patients
	                stats.ageNoHosp[j] += age;
	                stats.ageNoHospCount[j]++;
	                stats.ageNoHospList.get(j).add(age);
	            }
	        }
	    }

	    // Calculate average age for hospitalized and non-hospitalized patients
	    for (int j = 0; j < stats.ageHosp.length; j++) {
	        stats.ageHosp[j] = stats.ageHosp[j] / stats.ageHospCount[j];
	        stats.ageNoHosp[j] = stats.ageNoHosp[j] / stats.ageNoHospCount[j];
	    }

	    // Print statistics related to patient age and readmissions
	    System.out.println(Arrays.toString(stats.ageHosp));
	    System.out.println(Arrays.toString(stats.ageHospCount));
	    System.out.println(Arrays.toString(stats.ageNoHosp));
	    System.out.println(Arrays.toString(stats.ageNoHospCount));

	    // Print t-test results for each time interval
	    for (int i = 0; i < stats.ageHospList.size(); i++) {
	        System.out.println(stats.tTest(stats.ageHospList.get(i), stats.ageNoHospList.get(i)));
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
	                for (int k = 0; k < stats.BMI.length - 1; k++) {
	                    if (b >= stats.BMI[k] && b < stats.BMI[k + 1]) {
	                        stats.bmiHosp[j][k] += b;
	                        stats.bmiHospCount[j][k]++;
	                        stats.bmiHospList.get(j).add(b);
	                        stats.bmiSpecificHospList.get(j).get(k).add(b);
	                    }
	                }
	            } else {
	                // Update statistics for non-hospitalized patients
	                for (int k = 0; k < stats.BMI.length - 1; k++) {
	                    if (b > stats.BMI[k] && b <= stats.BMI[k + 1]) {
	                        stats.bmiNoHosp[j][k] += b;
	                        stats.bmiNoHospCount[j][k]++;
	                        stats.bmiNoHospList.get(j).add(b);
	                        stats.bmiSpecificNoHospList.get(j).get(k).add(b);
	                    }
	                }
	            }
	        }
	    }

	    // Calculate average BMI for hospitalized and non-hospitalized patients
	    for (int i = 0; i < stats.bmiHosp.length; i++) {
	        for (int j = 0; j < stats.bmiHosp[i].length; j++) {
	            stats.bmiHosp[i][j] = stats.bmiHosp[i][j] / stats.bmiHospCount[i][j];
	            stats.bmiNoHosp[i][j] = stats.bmiNoHosp[i][j] / stats.bmiNoHospCount[i][j];
	        }
	    }

	    // Print statistics related to patient BMI and readmissions
	    for (int i = 0; i < stats.bmiHosp.length; i++) {
	        System.out.println(Arrays.toString(stats.bmiHosp[i]));
	    }
	    for (int i = 0; i < stats.bmiHospCount.length; i++) {
	        System.out.println(Arrays.toString(stats.bmiHospCount[i]));
	    }
	    for (int i = 0; i < stats.bmiNoHosp.length; i++) {
	        System.out.println(Arrays.toString(stats.bmiNoHosp[i]));
	    }
	    for (int i = 0; i < stats.bmiNoHospCount.length; i++) {
	        System.out.println(Arrays.toString(stats.bmiNoHospCount[i]));
	    }
	    System.out.println();

	    // Print chi-square test results for BMI ranges
	    for (int i = 0; i < stats.bmiHosp.length; i++) {
	        for (int j = 0; j < stats.bmiHosp[i].length; j++) {
	            System.out.print(stats.chiSquare(stats.bmiHospCount[i][j], stats.bmiNoHospCount[i][j],
	                    stats.sum(stats.bmiHospCount[i]), stats.sum(stats.bmiNoHospCount[i])) + ", ");
	        }
	        System.out.println();
	    }
	    System.out.println();

	    // Print t-test results for BMI averages
	    for (int i = 0; i < stats.bmiHospList.size(); i++) {
	        System.out.println(stats.tTest(stats.bmiHospList.get(i), stats.bmiNoHospList.get(i)));
	    }
	    System.out.println();
	    System.out.println();

	    // Print t-test results for specific BMI ranges
	    for (int i = 0; i < stats.bmiSpecificHospList.size(); i++) {
	        for (int j = 0; j < stats.bmiSpecificHospList.get(i).size(); j++) {
	            System.out.print(stats.tTest(stats.bmiSpecificHospList.get(i).get(j), stats.bmiSpecificNoHospList.get(i).get(j)) + ", ");
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
	                for (int k = 0; k < stats.MELD.length - 1; k++) {
	                    if (b > stats.MELD[k] && b <= stats.MELD[k + 1]) {
	                        stats.meldHosp[j][k] += b;
	                        stats.meldHospCount[j][k]++;
	                        stats.meldHospList.get(j).add(b);
	                        stats.meldSpecificHospList.get(j).get(k).add(b);
	                    }
	                }
	            } else {
	                // Update statistics for non-hospitalized patients
	                for (int k = 0; k < stats.MELD.length - 1; k++) {
	                    if (b > stats.MELD[k] && b <= stats.MELD[k + 1]) {
	                        stats.meldNoHosp[j][k] += b;
	                        stats.meldNoHospCount[j][k]++;
	                        stats.meldNoHospList.get(j).add(b);
	                        stats.meldSpecificNoHospList.get(j).get(k).add(b);
	                    }
	                }
	            }
	        }
	    }

	    // Calculate average MELD scores for hospitalized and non-hospitalized patients
	    for (int i = 0; i < stats.meldHosp.length; i++) {
	        for (int j = 0; j < stats.meldHosp[i].length; j++) {
	            stats.meldHosp[i][j] = stats.meldHosp[i][j] / stats.meldHospCount[i][j];
	            stats.meldNoHosp[i][j] = stats.meldNoHosp[i][j] / stats.meldNoHospCount[i][j];
	        }
	    }

	    // Print statistics related to patient MELD scores and readmissions
	    for (int i = 0; i < stats.meldHosp.length; i++) {
	        System.out.println(Arrays.toString(stats.meldHosp[i]));
	    }
	    for (int i = 0; i < stats.meldHospCount.length; i++) {
	        System.out.println(Arrays.toString(stats.meldHospCount[i]));
	    }
	    for (int i = 0; i < stats.meldNoHosp.length; i++) {
	        System.out.println(Arrays.toString(stats.meldNoHosp[i]));
	    }
	    for (int i = 0; i < stats.meldNoHospCount.length; i++) {
	        System.out.println(Arrays.toString(stats.meldNoHospCount[i]));
	    }

	    // Print chi-square test results for MELD score ranges
	    for (int i = 0; i < stats.meldHosp.length; i++) {
	        for (int j = 0; j < stats.meldHosp[i].length; j++) {
	            System.out.print(stats.chiSquare(stats.meldHospCount[i][j], stats.meldNoHospCount[i][j],
	                    stats.sum(stats.meldHospCount[i]), stats.sum(stats.meldNoHospCount[i])) + ", ");
	        }
	        System.out.println();
	    }
	    System.out.println();

	    // Print t-test results for MELD score averages
	    for (int i = 0; i < stats.meldHospList.size(); i++) {
	        System.out.println(stats.tTest(stats.meldHospList.get(i), stats.meldNoHospList.get(i)));
	    }
	    System.out.println();
	    System.out.println();

	    // Print t-test results for specific MELD score ranges
	    for (int i = 0; i < stats.meldSpecificHospList.size(); i++) {
	        for (int j = 0; j < stats.meldSpecificHospList.get(i).size(); j++) {
	            System.out.print(stats.tTest(stats.meldSpecificHospList.get(i).get(j), stats.meldSpecificNoHospList.get(i).get(j)) + ", ");
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
	                    for (int k = 0; k < stats.funcStat.length; k++) {
	                        if (funcstat == stats.funcStat[k]) {
	                            stats.funcStatHosp[j][k]++;
	                            break;
	                        }
	                    }
	                } else {
	                    // Update statistics for non-hospitalized patients
	                    for (int k = 0; k < stats.funcStat.length; k++) {
	                        if (funcstat == stats.funcStat[k]) {
	                            stats.funcStatNoHosp[j][k]++;
	                            break;
	                        }
	                    }
	                }
	            }
	        }
	    }

	    // Print functional status statistics for hospitalized and non-hospitalized patients
	    for (int i = 0; i < stats.funcStatHosp.length; i++) {
	        System.out.println(stats.intervals[i]);
	        System.out.println(Arrays.toString(stats.funcStatHosp[i]));
	        System.out.println(Arrays.toString(stats.funcStatNoHosp[i]));
	    }
	    System.out.println();

	    // Print chi-square test results for functional status intervals
	    for (int i = 0; i < stats.funcStatHosp.length; i++) {
	        for (int j = 0; j < stats.funcStatHosp[i].length; j++) {
	            System.out.print(stats.chiSquare(stats.funcStatHosp[i][j], stats.funcStatNoHosp[i][j],
	                    stats.sum(stats.funcStatHosp[i]), stats.sum(stats.funcStatNoHosp[i])) + ", ");
	        }
	        System.out.println();
	    }
	    System.out.println();
	}

}