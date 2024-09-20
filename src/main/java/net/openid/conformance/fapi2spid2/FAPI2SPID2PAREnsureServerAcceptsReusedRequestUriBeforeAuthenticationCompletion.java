package net.openid.conformance.fapi2spid2;

import net.openid.conformance.condition.Condition;
import net.openid.conformance.condition.client.AuthorizationEndpointGet;

import net.openid.conformance.testmodule.PublishTestModule;

@PublishTestModule(
	testName = "fapi2-security-profile-id2-par-ensure-reused-request-uri-prior-to-auth-completion-succeeds",
	displayName = "FAPI2-Security-Profile-ID2: PAR - ensure reused request uri prior to auth completion succeeds",
	summary = "This test checks that authorization servers that enforce one-time use of `request_uri` do so at the point of authorization, not at the point of loading an authorization page. Authorization requests are issued that do not complete the authorization process. These should succeed. Then a regular authorization flow is executed which must succeed",
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
public class FAPI2SPID2PAREnsureServerAcceptsReusedRequestUriBeforeAuthenticationCompletion extends AbstractFAPI2SPID2ServerTestModule {

	@Override
	protected void performRedirect(String method) {
		// As per https://bitbucket.org/openid/fapi/issues/635/one-time-use-of-request_uri-causing-error
		// Issue a GET, including the 'request_uri', to the authorization endpoint prior to a
		// successful authorization flow.
		callAndStopOnFailure(AuthorizationEndpointGet.class);

		// Repeat the above, this should succeed.
		callAndContinueOnFailure(AuthorizationEndpointGet.class, Condition.ConditionResult.WARNING);

		// The second GET failed. Finish the test here.
		if (getResult() == Result.WARNING) {
			fireTestFinished();
		}

		// Proceed to complete the authorization flow.
		super.performRedirect(method);
	}
}
