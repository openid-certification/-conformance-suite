package net.openid.conformance.fapi1advancedfinal;

import net.openid.conformance.condition.Condition;
import net.openid.conformance.condition.client.AddAudToRequestObject;
import net.openid.conformance.condition.client.AddBadAudToRequestObject;
import net.openid.conformance.condition.client.CheckForUnexpectedParametersInErrorResponseFromAuthorizationEndpoint;
import net.openid.conformance.condition.client.CheckStateInAuthorizationResponse;
import net.openid.conformance.condition.client.EnsureErrorFromAuthorizationEndpointResponse;
import net.openid.conformance.condition.client.EnsureInvalidRequestObjectError;
import net.openid.conformance.condition.client.EnsureInvalidRequestUriError;
import net.openid.conformance.condition.client.EnsurePARInvalidRequestObjectError;
import net.openid.conformance.condition.client.ExpectRequestObjectWithBadAudClaimErrorPage;
import net.openid.conformance.sequence.ConditionSequence;
import net.openid.conformance.testmodule.PublishTestModule;

@PublishTestModule(
	testName = "fapi1-advanced-final-ensure-request-object-with-bad-aud-fails",
	displayName = "FAPI1-Advanced-Final: ensure request object with bad values of aud fails",
	summary = "This test should end with the authorization server showing an error message that the request object is invalid (a screenshot of which should be uploaded) or with the user being redirected back to the conformance suite with a correct error response.",
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
public class FAPI1AdvancedFinalEnsureRequestObjectWithBadAudFails extends AbstractFAPI1AdvancedFinalPARExpectingAuthorizationEndpointPlaceholderOrCallback {

	@Override
	protected void createPlaceholder() {
		callAndStopOnFailure(ExpectRequestObjectWithBadAudClaimErrorPage.class, "OIDCC-6.1", "RFC7519-4.1.3");

		env.putString("error_callback_placeholder", env.getString("request_object_unverifiable_error"));
	}

	@Override
	protected ConditionSequence makeCreateAuthorizationRequestObjectSteps() {
		return super.makeCreateAuthorizationRequestObjectSteps()
				.replace(AddAudToRequestObject.class,
						condition(AddBadAudToRequestObject.class).requirements("OIDCC-6.1", "RFC7519-4.1.3"));
	}

	@Override
	protected void processParErrorResponse() {
		callAndContinueOnFailure(EnsurePARInvalidRequestObjectError.class, Condition.ConditionResult.FAILURE, "PAR-2.3");
	}

	@Override
	protected void onAuthorizationCallbackResponse() {

		// We now have callback_query_params and callback_params (containing the hash) available, as well as authorization_endpoint_response (which test conditions should use if they're looking for the response)

		/* If we get an error back from the authorization server:
		 * - It must be a 'invalid_request_object' error
		 * - It must have the correct state we supplied
		 */

		callAndContinueOnFailure(CheckStateInAuthorizationResponse.class, Condition.ConditionResult.FAILURE);
		callAndContinueOnFailure(EnsureErrorFromAuthorizationEndpointResponse.class, Condition.ConditionResult.FAILURE, "OIDCC-3.1.2.6");
		callAndContinueOnFailure(CheckForUnexpectedParametersInErrorResponseFromAuthorizationEndpoint.class, Condition.ConditionResult.WARNING, "OIDCC-3.1.2.6");
		if (isPar.isTrue()) {
			callAndContinueOnFailure(EnsureInvalidRequestUriError.class, Condition.ConditionResult.FAILURE, "JAR-4");
		} else {
			callAndContinueOnFailure(EnsureInvalidRequestObjectError.class, Condition.ConditionResult.FAILURE, "OIDCC-3.1.2.6");
		}
		fireTestFinished();

	}
}
