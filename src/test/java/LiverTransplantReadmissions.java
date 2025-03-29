import java.io.File;
import java.io.PrintStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;
import java.util.Scanner;
import java.util.TreeMap;

public class LiverTransplantReadmissions {

	/**
	* ****************************************************************************
	* *                                                                          *
	* *  ____ _       _           _  __     __         _       _     _           *
	* * / ___| | ___ | |__   __ _| | \ \   / /_ _ _ __(_) __ _| |__ | | ___  ___ *
	* *| |  _| |/ _ \| '_ \ / _` | |  \ \ / / _` | '__| |/ _` | '_ \| |/ _ \/ __|*
	* *| |_| | | (_) | |_) | (_| | |   \ V / (_| | |  | | (_| | |_) | |  __/\__ \*
	* * \____|_|\___/|_.__/ \__,_|_|    \_/ \__,_|_|  |_|\__,_|_.__/|_|\___||___/*
	* *                                                                          *
	* ****************************************************************************
	*/
	
	public static TreeMap<Integer, STARDataPatient> patientDatabase = new TreeMap<Integer, STARDataPatient>();
    public static double[] BMICategoryBounds = {0, 18.5, 25, 30, 35, 40, 50};
    public static int[] functionalStatusCategories = {2010, 2020, 2030, 2040, 2050, 2060, 2070, 2080, 2090, 2100};
    public static double[] MELDCategoryBounds = {6, 15, 21, 28, 40.1};
    public static TreeMap<Integer, Integer> functionalStatusMap = new TreeMap<Integer, Integer>(){{ put(2010,0); put(2020,1); put(2030,2); put(2040,3); put(2050,4); put(2060,5); put(2070,6); put(2080,7); put(2090,8); put(2100,9); }};
    public static int timeInterval = 30;
    public static int yesDiabetesYesReadmitted, noDiabetesYesReadmitted, yesDiabetesNoReadmitted, noDiabetesNoReadmitted = 0;
    public static int yesHCVYesReadmitted, noHCVYesReadmitted, yesHCVNoReadmitted, noHCVNoReadmitted = 0;
    public static double[] functionalStatusYesReadmitted = new double[functionalStatusCategories.length], 
    						functionalStatusNoReadmitted = new double[functionalStatusCategories.length];
    public static double ageAverageYesReadmitted, agePopulationYesReadmitted, ageAverageNoReadmitted, agePopulationNoReadmitted = 0.0;
    public static ArrayList<Double> ageValuesYesReadmitted = new ArrayList<Double>(), 
    								ageValuesNoReadmitted = new ArrayList<Double>();
    public static double[] bmiAverageYesReadmitted = new double[BMICategoryBounds.length-1], 
    						bmiPopulationYesReadmitted = new double[BMICategoryBounds.length-1], 
    						bmiAverageNoReadmitted = new double[BMICategoryBounds.length-1], 
    						bmiPopulationNoReadmitted = new double[BMICategoryBounds.length-1];
    public static ArrayList<Double> bmiValuesYesReadmitted = new ArrayList<Double>(), 
    								bmiValuesNoReadmitted = new ArrayList<Double>();
    public static ArrayList<ArrayList<Double>> bmiSpecificYesReadmittedList = new ArrayList<ArrayList<Double>>(), 
    											bmiSpecificNoReadmittedList = new ArrayList<ArrayList<Double>>();
    public static double[] meldAverageYesReadmitted = new double[MELDCategoryBounds.length-1], 
    						meldPopulationYesReadmitted = new double[MELDCategoryBounds.length-1], 
    						meldAverageNoReadmitted = new double[MELDCategoryBounds.length-1], 
    						meldPopulationNoReadmitted = new double[MELDCategoryBounds.length-1];
    public static ArrayList<Double> meldValuesYesReadmitted = new ArrayList<Double>(), 
    								meldValuesNoReadmitted = new ArrayList<Double>();
    public static ArrayList<ArrayList<Double>> meldSpecificYesReadmittedList = new ArrayList<ArrayList<Double>>(), 
    											meldSpecificNoReadmittedList = new ArrayList<ArrayList<Double>>();
    
	public static void main(String[]args) throws Exception {
	
	    /**
	    * ****************************************************************************************************************************************
	    * *                                                                                                                                      *
	    * * ____        _          __  __                                                   _                     _   ____       _               *
	    * *|  _ \  __ _| |_ __ _  |  \/  | __ _ _ __   __ _  __ _  ___ _ __ ___   ___ _ __ | |_    __ _ _ __   __| | / ___|  ___| |_ _   _ _ __  *
	    * *| | | |/ _` | __/ _` | | |\/| |/ _` | '_ \ / _` |/ _` |/ _ \ '_ ` _ \ / _ \ '_ \| __|  / _` | '_ \ / _` | \___ \ / _ \ __| | | | '_ \ *
	    * *| |_| | (_| | || (_| | | |  | | (_| | | | | (_| | (_| |  __/ | | | | |  __/ | | | |_  | (_| | | | | (_| |  ___) |  __/ |_| |_| | |_) |*
	    * *|____/ \__,_|\__\__,_| |_|  |_|\__,_|_| |_|\__,_|\__, |\___|_| |_| |_|\___|_| |_|\__|  \__,_|_| |_|\__,_| |____/ \___|\__|\__,_| .__/ *
	    * *                                                 |___/                                                                         |_|    *
	    * *                                                                                                                                      *
	    * ****************************************************************************************************************************************
	    */
		
		for(int i = 0; i < BMICategoryBounds.length - 1; i++) { bmiSpecificYesReadmittedList.add(new ArrayList<Double>()); bmiSpecificNoReadmittedList.add(new ArrayList<Double>()); }
		for(int i = 0; i < MELDCategoryBounds.length - 1; i++) { meldSpecificYesReadmittedList.add(new ArrayList<Double>()); meldSpecificNoReadmittedList.add(new ArrayList<Double>()); }
				
//		File file = new File("Stata2CSV.py");
//		PrintStream ps = new PrintStream(file);
//		ps.println("import pandas as pd");
//		ps.println("import sys, getopt");
//		ps.println("data = pd.io.stata.read_stata(sys.argv[1])");
//		ps.println("data.to_csv(sys.argv[2])");
//		ps.close();
//
//		new ProcessBuilder("cmd.exe", "/c", "py " + file.getName() + " LIVER_DATA.DTA LIVER_DATA.csv").start().waitFor();

		Scanner sc = new Scanner(new File("LIVER_DATA.csv"));
		sc.nextLine();
		while(sc.hasNextLine()) {
			String line = sc.nextLine();
			ArrayList<String> fields = new ArrayList<String>(Arrays.asList(line.split(",")));
			String code = fields.get(43), TRR = fields.get(377), dischargeDate = fields.get(324), gender = fields.get(5),
					age = fields.get(345), bmicalc = fields.get(45), diab = fields.get(22), funcstattrr = fields.get(119), hcv = fields.get(139), 
					cr = fields.get(112), bili = fields.get(106), inr = fields.get(109), na = fields.get(113), alb = fields.get(93), dialysis = fields.get(107), MELD = "";
			
			boolean ptCodeIsValid = code.length() > 0 && TRR.length() > 0 && code.charAt(0) >= '0' && code.charAt(0) <= '9' && TRR.charAt(0) == 'A' && age.charAt(0) >= '0' && age.charAt(0) <= '9';	
            
            if (ptCodeIsValid) {
            	if(cr.length() > 0 && bili.length() > 0 && inr.length() > 0 && na.length() > 0 && alb.length() > 0 && dialysis.length() > 0 && gender.length() > 0 && cr.contains(".") && bili.contains(".") && inr.contains(".") && na.contains(".") && alb.contains(".")) {
        			double meld3_0 = 0.0, quantCr = Double.parseDouble(cr), quantBili = Double.parseDouble(bili), quantInr = Double.parseDouble(inr), quantNa = Double.parseDouble(na), quantAlb = Double.parseDouble(alb);
        			//MELD 3.0 = 1.33*(Female) + 4.56*ln(Serum bilirubin) + 0.82*(137 - Sodium) – 0.24*(137 - Sodium)*ln(Serum bilirubin) + 9.09*ln(INR) + 11.14*ln(Serum creatinine) + 1.85*(3.5 – Serum albumin) – 1.83*(3.5 – Serum albumin)*ln(Serum creatinine) + 6
        			if(gender.charAt(0) == 'F') { meld3_0 += 1.33; }
        			if(quantBili < 1) { quantBili = 1;}
        			if(quantInr < 1) { quantInr = 1;}
        			if(quantCr < 1) { quantCr = 1;}
        			if(quantNa < 125) { quantNa = 125; }
        			else if(quantNa > 137) { quantNa = 137; }
        			if(quantAlb < 1.5) { quantAlb = 1.5; }
        			else if(quantAlb > 3.5) { quantAlb = 3.5; }
        			if(quantCr > 3 || dialysis.equals("Y")) { quantCr = 3; }
        			
        			meld3_0 += 
        						4.56*Math.log(quantBili) 
        						+ 0.82*(137 - quantNa) 
        						- 0.24*(137 - quantNa)*Math.log(quantBili) 
        						+ 9.09*Math.log(quantInr) 
        						+ 11.14*Math.log(quantCr) 
        						+ 1.85*(3.5 - quantAlb) 
        						- 1.83*(3.5 - quantAlb)*Math.log(quantCr) 
        						+ 6;
        			if(meld3_0 >= 40) { meld3_0 = 40; }
        			MELD = String.valueOf(Math.round(meld3_0));
        		}
            	else { continue; }
            	
                Integer codeInt = Integer.valueOf(code.substring(0, code.indexOf('.')));
                Integer ageInt = Integer.valueOf(age.substring(0, age.indexOf('.')));
                
                if (ageInt >= 18 && ageInt <= 100 && dischargeDate.length() > 0) {
                    if (!patientDatabase.containsKey(codeInt)) {
                        STARDataPatient patient = new STARDataPatient(age, bmicalc, diab, dischargeDate, funcstattrr, hcv, MELD, code, TRR);
                        patientDatabase.put(codeInt, patient);
                    } else {
                        patientDatabase.get(codeInt).DISCHARGEDATE_DATA.add(dischargeDate);
                        if (bmicalc.length() > 0) { patientDatabase.get(codeInt).BMICALC_DATA.add(bmicalc); }
                        if (MELD.length() > 0) { patientDatabase.get(codeInt).MELD_DATA.add(MELD); }
                    }
                }
            }
		}
		sc.close();
		
		System.out.println("Population Size: " + patientDatabase.size());
		
		Object[] arr = patientDatabase.keySet().toArray();
		
		for (int i = 0; i < arr.length; i++) {
	    	STARDataPatient patient = patientDatabase.get((Integer) arr[i]);
	    	if (! (patient.DIAB_DATA.equals("1.0") || patient.DIAB_DATA.equals("2.0") || patient.DIAB_DATA.equals("3.0") || patient.DIAB_DATA.equals("4.0") || patient.DIAB_DATA.equals("5.0")) ) { patientDatabase.remove((Integer) arr[i]); }
	    	else if (! (patient.HCVSEROSTATUS_DATA.equals("P") || patient.HCVSEROSTATUS_DATA.equals("N"))  ) { patientDatabase.remove((Integer) arr[i]); }
	    	else if (patient.FUNCSTATTRR_DATA.length() != 6) { patientDatabase.remove((Integer) arr[i]); }
	    	else if(patient.BMICALC_DATA.isEmpty()) { patientDatabase.remove((Integer) arr[i]); }
	    	else if(patient.MELD_DATA.isEmpty()) { patientDatabase.remove((Integer) arr[i]); }
	    }
		
		System.out.println("Population Size: " + patientDatabase.size());
		
	    arr = patientDatabase.keySet().toArray();
	    for (int i = 0; i < arr.length; i++) {
	        STARDataPatient patient = patientDatabase.get((Integer) arr[i]);
	        ArrayList<Double> bmiDoubles = new ArrayList<Double>();
            for (int j = 0; j < patient.BMICALC_DATA.size(); j++) {
                bmiDoubles.add(Double.valueOf(patient.BMICALC_DATA.get(j)));
            }
            patient.averageBMI = Mean(bmiDoubles);
	        ArrayList<Double> meldDoubles = new ArrayList<Double>();
            for (int j = 0; j < patient.MELD_DATA.size(); j++) {
        		meldDoubles.add(Double.valueOf(patient.MELD_DATA.get(j)));
            }
            patient.averageMELD = Mean(meldDoubles);
	    }	    

//		new ProcessBuilder("cmd.exe", "/c", "py " + "Stata2CSV.py" + " LIVER_FOLLOWUP_DATA.DTA LIVER_FOLLOWUP_DATA.csv").start().waitFor();
		
		sc = new Scanner(new File("LIVER_FOLLOWUP_DATA.csv"));
		while(sc.hasNextLine()){
			String line = sc.nextLine();
			ArrayList<String> fields = new ArrayList<String>(Arrays.asList(line.split(",")));
			for(int i = 0; i < fields.size(); i++) {
				String code = fields.get(51), TRR = fields.get(56);
				boolean ptCodeIsValid = code.length() > 0 &&  TRR.length() > 0 &&  code.charAt(0) >= '0' &&  code.charAt(0) <= '9' && TRR.charAt(0) == 'A';
	            if (ptCodeIsValid) {
	                Integer codeInt = Integer.valueOf(code.substring(0, code.indexOf('.')));
	                String Hosp = fields.get(5), pxStatDate = fields.get(54);  //PX_STAT_DATE
	                boolean patientExists = patientDatabase.containsKey(codeInt), hospitalized = Hosp.length() == 1 && Hosp.charAt(0) == 'Y', pxStatDateIsValid = (pxStatDate.length() > 7 && pxStatDate.charAt(4) == '-' && pxStatDate.charAt(7) == '-');
	                if (patientExists && hospitalized && pxStatDateIsValid) { patientDatabase.get(codeInt).addFollowUp(Hosp, pxStatDate); }
	            }
			}
		}
		sc.close();

	    arr = patientDatabase.keySet().toArray();
	    for (int i = 0; i < arr.length; i++) {
	        STARDataPatient patient = patientDatabase.get((Integer) arr[i]);
	        for (int j = 0; j < patient.followUps.size(); j++) {
	            STARDataFollowUp followUp = patient.followUps.get(j);
	            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.ENGLISH);
	            LocalDate date2 = LocalDate.parse(followUp.PXSTATDATE_FOLLOWUP, formatter);
	            for (int k = 0; k < patient.DISCHARGEDATE_DATA.size(); k++) {
	                LocalDate date = LocalDate.parse(patient.DISCHARGEDATE_DATA.get(k), formatter);
	                int difference = (int) ChronoUnit.DAYS.between(date, date2);
	                patientDatabase.get((Integer) arr[i]).followUps.get(j).daysSinceDischarge.add(difference);
	                if (difference > 0 && difference <= timeInterval) {
                        patientDatabase.get((Integer) arr[i]).readmitted = true;
	                }
	            }
	        }
	    }    
	    
	    System.out.println("Population Size: " + patientDatabase.size());
	    
	    /**
	    * *********************************************************************************************************************************
	    * * ____        _           ____      _ _           _   _                               _      _                _           _     *
	    * *|  _ \  __ _| |_ __ _   / ___|___ | | | ___  ___| |_(_) ___  _ __     __ _ _ __   __| |    / \   _ __   __ _| |_   _ ___(_)___ *
	    * *| | | |/ _` | __/ _` | | |   / _ \| | |/ _ \/ __| __| |/ _ \| '_ \   / _` | '_ \ / _` |   / _ \ | '_ \ / _` | | | | / __| / __|*
	    * *| |_| | (_| | || (_| | | |__| (_) | | |  __/ (__| |_| | (_) | | | | | (_| | | | | (_| |  / ___ \| | | | (_| | | |_| \__ \ \__ \*
	    * *|____/ \__,_|\__\__,_|  \____\___/|_|_|\___|\___|\__|_|\___/|_| |_|  \__,_|_| |_|\__,_| /_/   \_\_| |_|\__,_|_|\__, |___/_|___/*
	    * *                                                                                                               |___/           *
	    * *********************************************************************************************************************************
	    */
	    
	    
	    arr = patientDatabase.keySet().toArray();
	    for (int i = 0; i < arr.length; i++) {
	        STARDataPatient patient = patientDatabase.get((Integer) arr[i]);
	        
	        if (patient.DIAB_DATA.equals("1.0")) {
	        	if(patient.readmitted) { noDiabetesYesReadmitted++; }
	        	else { noDiabetesNoReadmitted++; }
	        } 
	        else if (patient.DIAB_DATA.equals("2.0") || patient.DIAB_DATA.equals("3.0") || patient.DIAB_DATA.equals("4.0") || patient.DIAB_DATA.equals("5.0")) {
	        	if(patient.readmitted) { yesDiabetesYesReadmitted++; }
	        	else { yesDiabetesNoReadmitted++; }
	        }
	        
	        
	        if (patient.HCVSEROSTATUS_DATA.equals("P")) {
                if (patient.readmitted) { yesHCVYesReadmitted++; }
                else { yesHCVNoReadmitted++; }
	        }
	        else if (patient.HCVSEROSTATUS_DATA.equals("N")) {
                if (patient.readmitted) { noHCVYesReadmitted++; }
                else { noHCVNoReadmitted++; }
	        }
	        
	        if (patient.FUNCSTATTRR_DATA.length() == 6) {
	            int funcstat = Double.valueOf(patient.FUNCSTATTRR_DATA).intValue();
                if (patient.readmitted) { functionalStatusYesReadmitted[functionalStatusMap.get(funcstat)]++; }
                else { functionalStatusNoReadmitted[functionalStatusMap.get(funcstat)]++; }
		    } 
	        
            if (patient.readmitted) { ageAverageYesReadmitted += Double.valueOf(patient.AGE_DATA); agePopulationYesReadmitted++; ageValuesYesReadmitted.add(Double.valueOf(patient.AGE_DATA)); } 
            else { ageAverageNoReadmitted += Double.valueOf(patient.AGE_DATA); agePopulationNoReadmitted++; ageValuesNoReadmitted.add(Double.valueOf(patient.AGE_DATA)); }

	        double b = patient.averageBMI;
            if (patient.readmitted) {
                for (int k = 0; k < BMICategoryBounds.length - 1; k++) {
                    if (b >= BMICategoryBounds[k] && b < BMICategoryBounds[k + 1]) { 
                    	bmiAverageYesReadmitted[k] += b; 
                    	bmiPopulationYesReadmitted[k]++; 
                    	bmiSpecificYesReadmittedList.get(k).add(b); 
                    }
                }
            	bmiValuesYesReadmitted.add(b); 
            }
            else {
                for (int k = 0; k < BMICategoryBounds.length - 1; k++) {
                    if (b >= BMICategoryBounds[k] && b < BMICategoryBounds[k + 1]) { 
                    	bmiAverageNoReadmitted[k] += b; 
                    	bmiPopulationNoReadmitted[k]++; 
                    	bmiSpecificNoReadmittedList.get(k).add(b); 
                    }
                }
            	bmiValuesNoReadmitted.add(b); 
            }
            
	        b = patient.averageMELD;
            if (patient.readmitted) {
                for (int k = 0; k < MELDCategoryBounds.length - 1; k++) {
                    if (b >= MELDCategoryBounds[k] && b < MELDCategoryBounds[k + 1]) { 
                    	meldAverageYesReadmitted[k] += b; 
                    	meldPopulationYesReadmitted[k]++; 
                    	meldSpecificYesReadmittedList.get(k).add(b);
                    }
                }
            	meldValuesYesReadmitted.add(b); 
            } 
            else {
                for (int k = 0; k < MELDCategoryBounds.length - 1; k++) {
                    if (b >= MELDCategoryBounds[k] && b < MELDCategoryBounds[k + 1]) { 
                    	meldAverageNoReadmitted[k] += b;
                    	meldPopulationNoReadmitted[k]++; 
                    	meldSpecificNoReadmittedList.get(k).add(b);
                    }
                }
            	meldValuesNoReadmitted.add(b); 
            }
	    }
	    
	    
	    
	    for (int i = 0; i < meldAverageYesReadmitted.length; i++) { 
	    	meldAverageYesReadmitted[i] /= meldPopulationYesReadmitted[i]; 
	    	meldAverageNoReadmitted[i] /= meldPopulationNoReadmitted[i]; 
	    }
	    
	    for (int i = 0; i < bmiAverageYesReadmitted.length; i++) { 
	    	bmiAverageYesReadmitted[i] /= bmiPopulationYesReadmitted[i]; 
	    	bmiAverageNoReadmitted[i] /= bmiPopulationNoReadmitted[i]; 
	    }
	    
    	ageAverageYesReadmitted /= agePopulationYesReadmitted;
    	ageAverageNoReadmitted /= agePopulationNoReadmitted;

	    /**
	    * ******************************************
	    *  ____  _        _   _     _   _          *
	    * / ___|| |_ __ _| |_(_)___| |_(_) ___ ___ *
	    * \___ \| __/ _` | __| / __| __| |/ __/ __|*
	    *  ___) | || (_| | |_| \__ \ |_| | (__\__ \*
	    * |____/ \__\__,_|\__|_|___/\__|_|\___|___/*
	    * ******************************************
	    */
	    
		System.out.println("\n------------------------------------------------------------------------------------");
		System.out.println("Diabetes");
		System.out.println("------------------------------------------------------------------------------------\n");	    
        System.out.println("Diabetics readmitted: " + yesDiabetesYesReadmitted + "\nNondiabetics readmitted: " + noDiabetesYesReadmitted + "\nDiabetics not readmitted: " + yesDiabetesNoReadmitted + "\nNondiabetics not readmitted: " + noDiabetesNoReadmitted);
        
		System.out.println("\n------------------------------------------------------------------------------------");
		System.out.println("HCV");
		System.out.println("------------------------------------------------------------------------------------\n");
        System.out.println("HCV readmitted: " + yesHCVYesReadmitted + "\nNonHCV readmitted: " + noHCVYesReadmitted + "\nHCV not readmitted: " + yesHCVNoReadmitted + "\nNonHCV not readmitted: " + noHCVNoReadmitted);
        
	    System.out.println("\n------------------------------------------------------------------------------------");
		System.out.println("FUNCTIONAL STATUS");
		System.out.println("------------------------------------------------------------------------------------\n");
	    for (int i = 0; i < functionalStatusYesReadmitted.length; i++) {
	    	System.out.println(functionalStatusCategories[i] + "\t" + functionalStatusYesReadmitted[i] + "\t" + functionalStatusNoReadmitted[i]);
	    }
	    
		System.out.println("\n------------------------------------------------------------------------------------");
		System.out.println("AGE");
		System.out.println("------------------------------------------------------------------------------------\n");

	    System.out.println("Age of readmitted " + ageAverageYesReadmitted);
	    System.out.println("Number of readmitted " + agePopulationYesReadmitted);
	    System.out.println("Age of not readmitted " + ageAverageNoReadmitted);
	    System.out.println("Number of not readmitted " + agePopulationNoReadmitted);
        System.out.println("T Test: " + tTest(ageValuesYesReadmitted, ageValuesNoReadmitted));
	    
	    
	    System.out.println("\n------------------------------------------------------------------------------------");
		System.out.println("BMI");
		System.out.println("------------------------------------------------------------------------------------\n");
		
		System.out.println("Average BMI of those Hospitalized: " + Mean(bmiValuesYesReadmitted) );
		System.out.println("Average BMI of those Not Hospitalized: " + Mean(bmiValuesNoReadmitted) );
		System.out.println("Sample Size: " + (bmiValuesYesReadmitted.size() +  bmiValuesNoReadmitted.size()) );
		
		System.out.println("T Test Score of entire sample: " + tTest(bmiValuesYesReadmitted, bmiValuesNoReadmitted) + "\n");
		
		for(int i = 0; i < BMICategoryBounds.length-1; i++) {
        	System.out.println("[" + BMICategoryBounds[i] + ", " + BMICategoryBounds[i+1] + ")"
        							+ "\n\tBMI of those readmitted: " + bmiAverageYesReadmitted[i] 
        							+ "\n\tBMI of those not readmitted: " + bmiAverageNoReadmitted[i]
        							+ "\n\tT Test Score: " + tTest(bmiSpecificYesReadmittedList.get(i), bmiSpecificNoReadmittedList.get(i))
        							+ "\n\tNumber of those readmitted: " + bmiPopulationYesReadmitted[i]
   									+ "\n\tNumber of those not readmitted: " + bmiPopulationNoReadmitted[i]
   									+ "\n\tPercentage of those readmitted: " + bmiPopulationYesReadmitted[i]*100/(bmiPopulationYesReadmitted[i]+bmiPopulationNoReadmitted[i])
        	);
        }
	    
	    
	    System.out.println("\n------------------------------------------------------------------------------------");
		System.out.println("MELD");
		System.out.println("------------------------------------------------------------------------------------\n");
		
		System.out.println("Average MELD of those Hospitalized: " + Mean(meldValuesYesReadmitted) );
		System.out.println("Average MELD of those Not Hospitalized: " + Mean(meldValuesNoReadmitted) );
		System.out.println("Sample Size: " + (meldValuesYesReadmitted.size() +  meldValuesNoReadmitted.size()) );
		System.out.println("Sample Size: " + (sum(meldPopulationYesReadmitted) + sum(meldPopulationNoReadmitted)) );
	    
	    System.out.println("T Test Score of entire sample: " + tTest(meldValuesYesReadmitted, meldValuesNoReadmitted) + "\n");
        
        for(int i = 0; i < MELDCategoryBounds.length-1; i++) {
        	System.out.println("[" + MELDCategoryBounds[i] + ", " + MELDCategoryBounds[i+1] + ")"
        							+ "\n\tMELD of those readmitted: " + meldAverageYesReadmitted[i] 
        							+ "\n\tMELD of those not readmitted: " + meldAverageNoReadmitted[i]
        							+ "\n\tT Test Score: " + tTest(meldSpecificYesReadmittedList.get(i), meldSpecificNoReadmittedList.get(i))
        							+ "\n\tNumber of those readmitted: " + meldPopulationYesReadmitted[i]
   									+ "\n\tNumber of those not readmitted: " + meldPopulationNoReadmitted[i]
   									+ "\n\tPercentage of those readmitted: " + meldPopulationYesReadmitted[i]*100/(meldPopulationYesReadmitted[i]+meldPopulationNoReadmitted[i])
        	);
        }
	    
	    System.out.println("\n\nPopulation Size: " + patientDatabase.size());
	}

    public static double sum(double[] arr) {
        double total = 0;
        for (int i = 0; i < arr.length; i++) { total += arr[i]; }
        return total;
    }

    public static double Mean(ArrayList<Double> arr) {
        double sum = 0;
        for (int i = 0; i < arr.size(); i++) { sum += arr.get(i); }
        return sum / arr.size();
    }

    public static double Mean(double[] arr) {
        double sum = 0;
        for (int i = 0; i < arr.length; i++) { sum += arr[i]; }
        return sum / arr.length;
    }

    public static double standardDeviation(ArrayList<Double> arr, double mean) {
        double sum = 0;
        for (int i = 0; i < arr.size(); i++) { sum += Math.pow(arr.get(i) - mean, 2); }
        return Math.sqrt(sum / (arr.size() - 1));
    }

    public static double tTest(ArrayList<Double> arr1, ArrayList<Double> arr2) {
        double mean1 = Mean(arr1), mean2 = Mean(arr2);
        double sd1 = standardDeviation(arr1, mean1), sd2 = standardDeviation(arr2, mean2);
        double t_test = (mean1 - mean2) / Math.sqrt((sd1 * sd1) / arr1.size() + (sd2 * sd2) / arr2.size());
        return Math.abs(t_test);
    }	
}