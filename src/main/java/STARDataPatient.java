import java.util.ArrayList;

import org.apache.avro.generic.GenericRecord;

public class STARDataPatient {
	
	public String AGE_DATA;
	public ArrayList<String>  BMICALC_DATA = new ArrayList<String>();
	public String DIAB_DATA;
	public ArrayList<String> DISCHARGEDATE_DATA = new ArrayList<String>();
	public String FUNCSTATTCR_DATA;
	public String FUNCSTATTRF_DATA;
	public String FUNCSTATTRR_DATA;
	public String HCVSEROSTATUS_DATA;
	public ArrayList<String> MELDPELDLABSCORE_DATA = new ArrayList<String>();
	public String MALIG_DATA;
	public String PTCODE_DATA;
	public String TRRIDCODE_DATA;

	public ArrayList<STARDataFollowUp> followUps = new ArrayList<STARDataFollowUp>();
	public boolean [] readmissions = new boolean[4];
	public double averageBMI;
	public double averageMELD;

	public STARDataPatient(GenericRecord nextRecord) {
		for(int i = 0; i < readmissions.length; i++) {
			readmissions[i] = false;
		}
		AGE_DATA = nextRecord.get("AGE").toString();
		if(nextRecord.get("BMICALC").toString().length() > 0)
			BMICALC_DATA.add(nextRecord.get("BMICALC").toString());

		DIAB_DATA = nextRecord.get("DIAB").toString();
		DISCHARGEDATE_DATA.add(nextRecord.get("DISCHARGEDATE").toString());
		
		FUNCSTATTCR_DATA = nextRecord.get("FUNCSTATTCR").toString();
		FUNCSTATTRF_DATA = nextRecord.get("FUNCSTATTRF").toString();
		FUNCSTATTRR_DATA = nextRecord.get("FUNCSTATTRR").toString();
		HCVSEROSTATUS_DATA = nextRecord.get("HCVSEROSTATUS").toString();
		if(nextRecord.get("MELDPELDLABSCORE").toString().length() > 0)
			MELDPELDLABSCORE_DATA.add(nextRecord.get("MELDPELDLABSCORE").toString());
		MALIG_DATA = nextRecord.get("MALIG").toString();
		PTCODE_DATA = nextRecord.get("PTCODE").toString();
		TRRIDCODE_DATA = nextRecord.get("TRRIDCODE").toString();
	}
	
	public void addFollowUp(GenericRecord nextRecord) {
		followUps.add(new STARDataFollowUp(nextRecord));
	}
	
}