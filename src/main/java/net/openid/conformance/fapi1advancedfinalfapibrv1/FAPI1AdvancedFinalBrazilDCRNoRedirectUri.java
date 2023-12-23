package net.openid.conformance.fapi1advancedfinalfapibrv1;

import net.openid.conformance.condition.Condition;
import net.openid.conformance.condition.client.CallDynamicRegistrationEndpoint;
import net.openid.conformance.condition.client.CheckErrorFromDynamicRegistrationEndpointIsInvalidRedirectUriOrInvalidClientMetadata;
import net.openid.conformance.condition.client.EnsureContentTypeJson;
import net.openid.conformance.condition.client.EnsureHttpStatusCodeIs400;
import net.openid.conformance.condition.client.RemoveRedirectUrisFromDynamicClientRegistrationEndpointRequest;
import net.openid.conformance.testmodule.PublishTestModule;

@PublishTestModule(
	testName = "fapi1-advanced-final-brazil-dcr-no-redirect-uri",
	displayName = "FAPI1-Advanced-Final: Brazil DCR no redirect uri",
	summary = "Perform the DCR flow, but without including a redirect uri in the request body - the server must reject the registration attempt.",
	profile = "FAPI1-Advanced-Final",
	configurationFields = {
		"server.discoveryUrl",
		"client.scope",
		"client.jwks",
		"mtls.key",
		"mtls.cert",
		"mtls.ca",
		"directory.discoveryUrl",
		"directory.client_id",
		"directory.apibase",
		"resource.resourceUrl"
	}
)
public class FAPI1AdvancedFinalBrazilDCRNoRedirectUri extends AbstractFAPI1AdvancedFinalBrazilDCR {

	@Override
	protected void setupResourceEndpoint() {
		// not needed as resource endpoint won't be called
	}

	@Override
	protected void callRegistrationEndpoint() {
		callAndStopOnFailure(RemoveRedirectUrisFromDynamicClientRegistrationEndpointRequest.class, "BrazilOBDCR-7.1-6");

		callAndStopOnFailure(CallDynamicRegistrationEndpoint.class);

		env.mapKey("endpoint_response", "dynamic_registration_endpoint_response");
		callAndContinueOnFailure(EnsureContentTypeJson.class, Condition.ConditionResult.FAILURE, "RFC7591-3.2.2");
		callAndContinueOnFailure(EnsureHttpStatusCodeIs400.class, Condition.ConditionResult.FAILURE, "RFC7591-3.2.2");
		callAndContinueOnFailure(CheckErrorFromDynamicRegistrationEndpointIsInvalidRedirectUriOrInvalidClientMetadata.class, Condition.ConditionResult.FAILURE, "RFC7591-3.2.2");
	}

	@Override
	public void start() {
		fireTestFinished();
	}
}
