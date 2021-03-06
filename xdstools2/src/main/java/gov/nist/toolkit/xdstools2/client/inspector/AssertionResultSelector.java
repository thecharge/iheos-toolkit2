package gov.nist.toolkit.xdstools2.client.inspector;


import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import gov.nist.toolkit.results.client.AssertionResults;

public class AssertionResultSelector implements ClickHandler {
	MetadataInspectorTab it;
	AssertionResults ar;
	
	AssertionResultSelector(MetadataInspectorTab it, AssertionResults ar) {
		this.it = it;
		this.ar = ar;
	}
	
	public void onClick(ClickEvent event) {
		it.detailPanel.clear();
		it.structPanel.clear();
		
		new DetailDisplay(it).displayDetail(ar);
	}

}
