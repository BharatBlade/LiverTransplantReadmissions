import java.util.ArrayList;

import org.apache.avro.generic.GenericRecord;

public class STARDataFollowUp {
	public String HOSP_FOLLOWUP;
	public String PXSTATDATE_FOLLOWUP;	
	public ArrayList<Integer> daysSinceDischarge = new ArrayList<Integer>();
	
	public STARDataFollowUp(GenericRecord nextRecord) {
		HOSP_FOLLOWUP = nextRecord.get("HOSP").toString();
		PXSTATDATE_FOLLOWUP = nextRecord.get("PXSTATDATE").toString();
	}
	
}
