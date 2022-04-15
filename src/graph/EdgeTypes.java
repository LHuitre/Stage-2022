package graph;

public enum EdgeTypes implements Types{
	// types personnalisés
	R_SUCC,
	R_ASSOC,
	R_ISIN,
	R_HEAD,
	
	// types JdM
	R_POS,
	R_LEMMA,
	R_RAFF_SEM,
	
	R_CARAC,
	R_ISA,
	
	R_AGENT,
	R_AGENT_1,
	
	R_PATIENT,
	R_PATIENT_1
	;
	
	public String toString() {
		return this.name().toLowerCase();		
	}
}
