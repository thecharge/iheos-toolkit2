package gov.nist.toolkit.xdstools2.client.tabs.GatewayTestsTabs;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;
import gov.nist.toolkit.actorfactory.client.SimulatorConfig;
import gov.nist.toolkit.configDatatypes.SimulatorProperties;
import gov.nist.toolkit.services.client.RSNAEdgeOrchestrationRequest;
import gov.nist.toolkit.services.client.RSNAEdgeOrchestrationResponse;
import gov.nist.toolkit.services.client.RawResponse;
import gov.nist.toolkit.xdstools2.client.tabs.genericQueryTab.GenericQueryTab;
import gov.nist.toolkit.xdstools2.client.widgets.PopupMessage;
import gov.nist.toolkit.xdstools2.client.widgets.buttons.AbstractOrchestrationButton;

/**
 * Handles "Build Test Environment" Button for IIG Test Orchestration
 */
class BuildRSNAEdgeTestOrchestrationButton extends AbstractOrchestrationButton {
   private RSNAEdgeTestTab testTab;
   boolean includeEdge;

   public BuildRSNAEdgeTestOrchestrationButton(RSNAEdgeTestTab testTab, Panel topPanel, String label, boolean includeEdge) {
      super(topPanel, label);
      this.testTab = testTab;
      this.includeEdge = includeEdge;
   }

   @SuppressWarnings("unused")
   @Override
   public void orchestrate() {
       if (GenericQueryTab.empty(testTab.getCurrentTestSession())) {
           new PopupMessage("Must select test session first");
           return;
       }
       RSNAEdgeOrchestrationRequest request = new RSNAEdgeOrchestrationRequest();
       request.setUserName(testTab.getCurrentTestSession());
       testTab.getToolkitServices().buildRSNAEdgeTestOrchestration(request, new AsyncCallback<RawResponse>() {

           @Override
           public void onFailure(Throwable throwable) {
               handleError(throwable);
           }

           @Override
           public void onSuccess(RawResponse rawResponse) {
               if (handleError(rawResponse, RSNAEdgeOrchestrationResponse.class)) return;
               RSNAEdgeOrchestrationResponse orchResponse = (RSNAEdgeOrchestrationResponse) rawResponse;

               testTab.config = orchResponse.getSimulatorConfig();

               panel().add(new HTML("<h2>Test Environment</h2>"));
               FlexTable table = new FlexTable();
               panel().add(table);
               int row = 0;

               Widget w;

               table.setWidget(row, 0, new HTML("<h3>Test data pattern</h3>"));

               SimulatorConfig config = testTab.config;

               table.setWidget(row++, 0, new HTML("<h4>Repository UID</h4>"));

               table.setWidget(row, 0, new HTML("repositoryUniqueId"));
               table.setWidget(row++, 1, new HTML(config.get(SimulatorProperties.repositoryUniqueId).asString()));


               table.setWidget(row++, 0, new HTML("Endpoints"));

               table.setWidget(row, 1, new HTML("Provide and Register"));
               table.setWidget(row++, 2, new HTML(config.getConfigEle(SimulatorProperties.pnrEndpoint).asString()));

               table.setWidget(row, 1, new HTML("Provide and Register TLS"));
               table.setWidget(row++, 2, new HTML(config.getConfigEle(SimulatorProperties.pnrTlsEndpoint).asString()));

               testTab.genericQueryTab.reloadTransactionOfferings();
           }

       });
   }

 }
