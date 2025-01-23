
package net.openid.conformance.fapi1advancedfinal;

import net.openid.conformance.condition.Condition;
import net.openid.conformance.condition.client.AddNonceToAuthorizationEndpointRequest;
import net.openid.conformance.condition.client.CheckForUnexpectedParametersInErrorResponseFromAuthorizationEndpoint;
import net.openid.conformance.condition.client.CheckStateInAuthorizationResponse;
import net.openid.conformance.condition.client.EnsureErrorFromAuthorizationEndpointResponse;
import net.openid.conformance.condition.client.EnsureInvalidRequestInvalidRequestObjectOrInvalidRequestUriError;
import net.openid.conformance.condition.client.ExpectRequestObjectMissingNonceErrorPage;
import net.openid.conformance.condition.client.SignRequestObject;
import net.openid.conformance.sequence.ConditionSequence;
import net.openid.conformance.testmodule.PublishTestModule;

@PublishTestModule(
	testName = "fapi1-advanced-final-ensure-request-object-without-nonce-fails",
	displayName = "FAPI1-Advanced-Final: ensure request object without nonce fails",
	summary = "This test includes nonce only outside the request object, where it must be ignored.\n\nThe test should end with the authorization server showing an error message that the request or request object is invalid (a screenshot of which should be uploaded) or with the user being redirected back to the conformance suite with an invalid_request error (in both cases due to the missing nonce).\n\nnonce is required for all openid flows as per FAPI1-Baseline section 5.2.2.2",
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
public class FAPI1AdvancedFinalEnsureRequestObjectWithoutNonceFails extends AbstractFAPI1AdvancedFinalPARInvalidRequestOrInvalidRequestObject {

	@Override
	protected void createPlaceholder() {
		callAndStopOnFailure(ExpectRequestObjectMissingNonceErrorPage.class, "FAPI1-ADV-5.2.3-8");

		env.putString("error_callback_placeholder", env.getString("request_object_unverifiable_error"));
	}

	@Override
	protected ConditionSequence makeCreateAuthorizationRequestSteps() {
		return super.makeCreateAuthorizationRequestSteps()
				.skip(AddNonceToAuthorizationEndpointRequest.class,
						"NOT adding nonce to request object");
	}

	@Override
	protected ConditionSequence makeCreateAuthorizationRequestObjectSteps() {
		return super.makeCreateAuthorizationRequestObjectSteps()
				.insertAfter(SignRequestObject.class,
						condition(AddNonceToAuthorizationEndpointRequest.class));
	}

	@Override
	protected void onAuthorizationCallbackResponse() {
		// We now have callback_query_params and callback_params (containing the hash) available, as well as authorization_endpoint_response (which test conditions should use if they're looking for the response)

		/* If we get an error back from the authorization server:
		 * - It must be a 'invalid_request_object' or 'invalid_request' error
		 * - It must have the correct state we supplied
		 */

		callAndContinueOnFailure(CheckStateInAuthorizationResponse.class, Condition.ConditionResult.FAILURE);
		callAndContinueOnFailure(EnsureErrorFromAuthorizationEndpointResponse.class, Condition.ConditionResult.FAILURE, "OIDCC-3.1.2.6");
		callAndContinueOnFailure(CheckForUnexpectedParametersInErrorResponseFromAuthorizationEndpoint.class, Condition.ConditionResult.WARNING, "OIDCC-3.1.2.6");
		callAndContinueOnFailure(EnsureInvalidRequestInvalidRequestObjectOrInvalidRequestUriError.class, Condition.ConditionResult.FAILURE, "OIDCC-3.1.2.6", "OIDCC-3.3.2.6");
		fireTestFinished();
	}
}
