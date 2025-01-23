package net.openid.conformance.fapi1advancedfinal;

import net.openid.conformance.condition.client.ExpectRedirectUriMissingErrorPage;
import net.openid.conformance.testmodule.PublishTestModule;

@PublishTestModule(
	testName = "fapi1-advanced-final-ensure-redirect-uri-in-authorization-request",
	displayName = "FAPI1-Advanced-Final: ensure redirect URI in authorization request",
	summary = "This test should result an the authorization server showing an error page saying the redirect url is missing from the request (a screenshot of which should be uploaded)",
	profile = "FAPI1-Advanced-Final",
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
public class FAPI1AdvancedFinalEnsureRedirectUriInAuthorizationRequest extends AbstractFAPI1AdvancedFinalPARInvalidRequestOrInvalidRequestObject {

	@Override
	protected void createPlaceholder() {
		callAndStopOnFailure(ExpectRedirectUriMissingErrorPage.class, "FAPI1-BASE-5.2.2-9");

		env.putString("error_callback_placeholder", env.getString("redirect_uri_missing_error"));
	}

	@Override
	protected void createAuthorizationRequest() {
		super.createAuthorizationRequest();

		// Remove the redirect URL
		env.getObject("authorization_endpoint_request").remove("redirect_uri");
	}

}
