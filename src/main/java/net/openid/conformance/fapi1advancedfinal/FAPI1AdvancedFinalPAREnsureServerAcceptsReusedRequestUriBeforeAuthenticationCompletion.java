package net.openid.conformance.fapi1advancedfinal;

import net.openid.conformance.condition.Condition;
import net.openid.conformance.condition.client.AuthorizationEndpointGet;
import net.openid.conformance.testmodule.PublishTestModule;
import net.openid.conformance.variant.FAPIAuthRequestMethod;
import net.openid.conformance.variant.VariantNotApplicable;

@PublishTestModule(
	testName = "fapi1-advanced-final-par-ensure-reused-request-uri-prior-to-auth-completion-succeeds",
	displayName = "FAPI1-Advanced-Final:  PAR - ensure reused request uri prior to auth completion succeeds",
	summary = "This test checks that authorization servers that enforce one-time use of `request_uri` do so at the point of authorization, not at the point of loading an authorization page. Authorization requests are issued that do not complete the authorization process. These should succeed. Then a regular authorization flow is executed which must succeed",
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

@VariantNotApplicable(parameter = FAPIAuthRequestMethod.class, values = {
	"by_value"
})

public class FAPI1AdvancedFinalPAREnsureServerAcceptsReusedRequestUriBeforeAuthenticationCompletion extends AbstractFAPI1AdvancedFinalServerTestModule {

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
