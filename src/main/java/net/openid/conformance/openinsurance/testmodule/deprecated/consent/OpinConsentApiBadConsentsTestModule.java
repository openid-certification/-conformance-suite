package net.openid.conformance.openinsurance.testmodule.deprecated.consent;

import net.openid.conformance.condition.Condition;
import net.openid.conformance.condition.client.FAPIBrazilAddExpirationToConsentRequest;
import net.openid.conformance.condition.client.FAPIBrazilCreateBadConsentRequest;
import net.openid.conformance.openbanking_brasil.OBBProfile;
import net.openid.conformance.openbanking_brasil.generic.ErrorValidator;
import net.openid.conformance.openbanking_brasil.testmodules.AbstractClientCredentialsGrantFunctionalTestModule;
import net.openid.conformance.openbanking_brasil.testmodules.support.AddConsentScope;
import net.openid.conformance.openbanking_brasil.testmodules.support.CallConsentApiWithBearerToken;
import net.openid.conformance.openbanking_brasil.testmodules.support.EnsureResponseCodeWas400;
import net.openid.conformance.openbanking_brasil.testmodules.support.IgnoreResponseError;
import net.openid.conformance.openbanking_brasil.testmodules.support.PrepareToPostConsentRequest;
import net.openid.conformance.openbanking_brasil.testmodules.support.SetContentTypeApplicationJson;
import net.openid.conformance.testmodule.PublishTestModule;

@PublishTestModule(
	testName = "consent-api-bad-consents-test-v1",
	displayName = "Validate that requests with incompatible consents return HTTP 400",
	summary = "Validate that requests with incompatible consents return HTTP 400" +
	"\u2022 Calls the Token Endpoint using the consents scope \n" +
		"\u2022 Creates a Consent v1 with incompatible permissions\n" +
		"\u2022 Checks if server returns 400 status code\n",
	profile = OBBProfile.OBB_PROFILE_OPEN_INSURANCE_PHASE2,
	configurationFields = {
		"server.discoveryUrl",
		"client.client_id",
		"client.jwks",
		"mtls.key",
		"mtls.cert",
		"mtls.ca",
		"resource.consentUrl",
		"resource.brazilCpf"
	}
)
public class OpinConsentApiBadConsentsTestModule extends AbstractClientCredentialsGrantFunctionalTestModule {

	@Override
	protected void runTests() {

		runInBlock("Create incompatible consents v1", () -> {
			callAndStopOnFailure(PrepareToPostConsentRequest.class);
			callAndStopOnFailure(AddConsentScope.class);
			callAndStopOnFailure(FAPIBrazilCreateBadConsentRequest.class);
			callAndStopOnFailure(FAPIBrazilAddExpirationToConsentRequest.class);
			callAndStopOnFailure(SetContentTypeApplicationJson.class);
			callAndStopOnFailure(IgnoreResponseError.class);
			callAndContinueOnFailure(CallConsentApiWithBearerToken.class);
			callAndContinueOnFailure(ErrorValidator.class, Condition.ConditionResult.FAILURE);
			callAndStopOnFailure(EnsureResponseCodeWas400.class);
		});
	}


}
