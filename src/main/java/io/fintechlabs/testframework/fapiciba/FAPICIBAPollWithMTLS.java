package io.fintechlabs.testframework.fapiciba;

import com.google.gson.JsonObject;
import io.fintechlabs.testframework.condition.ConditionError;
import io.fintechlabs.testframework.condition.client.AddRequestedExp300SToAuthorizationEndpointRequest;
import io.fintechlabs.testframework.testmodule.PublishTestModule;

@PublishTestModule(
	testName = "fapi-ciba-poll-with-mtls",
	displayName = "FAPI-CIBA: Poll mode (MTLS client authentication)",
	summary = "This test requires two different clients registered to use MTLS client authentication under the FAPI-CIBA profile for the 'poll' mode. The test authenticates the user twice (using different variations on the authorisation request etc), tests that certificate bound access tokens are implemented correctly. Do not respond to the request until the test enters the 'WAITING' state.",
	profile = "FAPI-CIBA",
	configurationFields = {
		"server.discoveryUrl",
		"client.client_id",
		"client.scope",
		"client.jwks",
		"client.hint_type",
		"client.hint_value",
		"mtls.key",
		"mtls.cert",
		"mtls.ca",
		"client2.client_id",
		"client2.scope",
		"client2.jwks",
		"mtls2.key",
		"mtls2.cert",
		"mtls2.ca",
		"resource.resourceUrl"
	}
)
/** Temporary wrapper for FAPICIBAPoll until MTLS test plan is migrated to variants */
public class FAPICIBAPollWithMTLS extends FAPICIBAPoll {

	public void configure(JsonObject config, String baseUrl) {
		setupMTLS();
		super.configure(config, baseUrl);

	}
}
