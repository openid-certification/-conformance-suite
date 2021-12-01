package net.openid.conformance.openbanking_brasil.testmodules.channels;

import net.openid.conformance.condition.Condition;
import net.openid.conformance.openbanking_brasil.OBBProfile;
import net.openid.conformance.openbanking_brasil.channels.BankingAgentsChannelValidator;
import net.openid.conformance.openbanking_brasil.testmodules.AbstractNoAuthFunctionalTestModule;
import net.openid.conformance.openbanking_brasil.testmodules.support.DoNotStopOnFailure;
import net.openid.conformance.openbanking_brasil.testmodules.support.PrepareToGetProductsNChannelsApi;
import net.openid.conformance.testmodule.PublishTestModule;

@PublishTestModule(
	testName = "Channels Banking Agents Api test",
	displayName = "Validate structure of Channels Banking Agents Api resources",
	summary = "Validate structure of all Channels Banking Agents Api resources",
	profile = OBBProfile.OBB_PROFIlE_PHASE1,
	configurationFields = {
		"server.discoveryUrl",
		"resource.brazilCpf",
		"resource.resourceUrl",
		"resource.consentUrl"
	}
)
public class ChannelsBankingAgentsApiTestModule extends AbstractNoAuthFunctionalTestModule {

	@Override
	protected void runTests() {
		runInBlock("Validate Channels Banking Agents response", () -> {
			callAndStopOnFailure(PrepareToGetProductsNChannelsApi.class, "banking-agents");
			preCallResource();
			callAndContinueOnFailure(DoNotStopOnFailure.class);
			callAndContinueOnFailure(BankingAgentsChannelValidator.class, Condition.ConditionResult.FAILURE);
		});
	}
}
