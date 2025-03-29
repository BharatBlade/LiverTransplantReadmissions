import java.util.ArrayList;

public class STARDataPatient {
	
	public String AGE_DATA;
	public ArrayList<String>  BMICALC_DATA = new ArrayList<String>();
	public String DIAB_DATA;
	public ArrayList<String> DISCHARGEDATE_DATA = new ArrayList<String>();
	public String FUNCSTATTRR_DATA;
	public String HCVSEROSTATUS_DATA;
	public ArrayList<String> MELD_DATA = new ArrayList<String>();
	public String PTCODE_DATA;
	public String TRRIDCODE_DATA;

	public ArrayList<STARDataFollowUp> followUps = new ArrayList<STARDataFollowUp>();
	public boolean readmitted = false;
	public double averageBMI;
	public double averageMELD;
	
	public STARDataPatient(String age, 
							String bmicalc, 
							String diab, 
							String dischargeDate, 
							String funcstattrr, 
							String hcv,
							String meld,
							String code,
							String TRR) {
		AGE_DATA = age;
		if(bmicalc.length() > 0)
			BMICALC_DATA.add(bmicalc);

		DIAB_DATA = diab;
		DISCHARGEDATE_DATA.add(dischargeDate);
		
		FUNCSTATTRR_DATA = funcstattrr;
		HCVSEROSTATUS_DATA = hcv;
		
		
		if(meld.length() > 0)
			MELD_DATA.add(meld);
		PTCODE_DATA = code;
		TRRIDCODE_DATA = TRR;
	}
	
	
	public void addFollowUp(String hosp, String pxstatdate) {
		followUps.add(new STARDataFollowUp(hosp, pxstatdate));
	}

	
}