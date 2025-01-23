package net.openid.conformance.fapi2spid2;

import net.openid.conformance.condition.Condition;
import net.openid.conformance.condition.client.EnsurePARInvalidRequestOrInvalidRequestObjectError;
import net.openid.conformance.condition.client.ExpectRedirectUriMissingErrorPage;
import net.openid.conformance.testmodule.PublishTestModule;

@PublishTestModule(
	testName = "fapi2-security-profile-id2-ensure-redirect-uri-in-authorization-request",
	displayName = "FAPI2-Security-Profile-ID2: ensure redirect URI in authorization request",
	summary = "This test should result an the authorization server showing an error page saying the redirect url is missing from the request (a screenshot of which should be uploaded)",
	profile = "FAPI2-Security-Profile-ID2",
	configurationFields = {
		"server.discoveryUrl",
		"client.client_id",
		"client.scope",
		"client.jwks",
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
public class FAPI2SPID2EnsureRedirectUriInAuthorizationRequest extends AbstractFAPI2SPID2PARExpectingAuthorizationEndpointPlaceholderOrCallback {

	@Override
	protected void createPlaceholder() {
		callAndStopOnFailure(ExpectRedirectUriMissingErrorPage.class, "FAPI2-SP-ID2-5.3.1.2-6");

		env.putString("error_callback_placeholder", env.getString("redirect_uri_missing_error"));
	}

	@Override
	protected void createAuthorizationRequest() {
		super.createAuthorizationRequest();

		// Remove the redirect URL
		env.getObject("authorization_endpoint_request").remove("redirect_uri");
	}

	@Override
	protected void processParErrorResponse() {
		callAndContinueOnFailure(EnsurePARInvalidRequestOrInvalidRequestObjectError.class, Condition.ConditionResult.FAILURE, "PAR-2.3");
	}
}
