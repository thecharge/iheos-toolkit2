package gov.nist.toolkit.xdstools2.client.command.command;

import gov.nist.toolkit.xdstools2.client.util.ClientUtils;
import gov.nist.toolkit.xdstools2.shared.command.CommandContext;

/**
 * Created by onh2 on 10/18/16.
 */
public abstract class AutoInitConformanceTestingCommand extends GenericCommand<CommandContext,Boolean>{
    @Override
    public void run(CommandContext var1) {
        ClientUtils.INSTANCE.getToolkitServices().getAutoInitConformanceTesting(this);
    }
}
