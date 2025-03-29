import java.util.ArrayList;

public class STARDataFollowUp {
	public String HOSP_FOLLOWUP;
	public String PXSTATDATE_FOLLOWUP;	
	public ArrayList<Integer> daysSinceDischarge = new ArrayList<Integer>();
	
	public STARDataFollowUp(String hosp, String pxstatdate) {
		HOSP_FOLLOWUP = hosp;
		PXSTATDATE_FOLLOWUP = pxstatdate;
	}
	
}
