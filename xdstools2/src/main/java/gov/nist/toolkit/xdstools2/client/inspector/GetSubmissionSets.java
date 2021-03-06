package gov.nist.toolkit.xdstools2.client.inspector;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import gov.nist.toolkit.registrymetadata.client.AnyIds;
import gov.nist.toolkit.registrymetadata.client.ObjectRef;
import gov.nist.toolkit.registrymetadata.client.ObjectRefs;
import gov.nist.toolkit.results.client.Result;
import gov.nist.toolkit.results.client.TestInstance;
import gov.nist.toolkit.xdstools2.client.util.ClientUtils;

import java.util.List;


public class GetSubmissionSets implements ClickHandler {

	MetadataInspectorTab it;
	ObjectRefs ids;

	void run() {
		/*it.data.*/
		ClientUtils.INSTANCE.getToolkitServices().getSubmissionSets(null, new AnyIds(ids), queryCallback);
	}

	AsyncCallback<List<Result>> queryCallback = new AsyncCallback<List<Result>> () {

		public void onFailure(Throwable caught) {
			Result result = Result.RESULT(new TestInstance("GetSubmissionSets"));
			result.assertions.add(caught.getMessage());
			it.addToHistory(result);
		}

		public void onSuccess(List<Result> result) {
			it.addToHistory(result);
		}

	};

	public GetSubmissionSets(MetadataInspectorTab it, ObjectRefs ids) {
		this.it = it;
		this.ids = ids;
	}

	public GetSubmissionSets(MetadataInspectorTab it, ObjectRef id) {
		this.it = it;
		ids = new ObjectRefs();
		ids.objectRefs.add(id);
	}

	public void onClick(ClickEvent event) {
		run();
	}

}
