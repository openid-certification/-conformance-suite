package net.openid.conformance.fapi1advancedfinal;

import net.openid.conformance.condition.Condition.ConditionResult;
import net.openid.conformance.condition.as.AddIatValueIsWeekInPastToIdToken;
import net.openid.conformance.condition.as.ClientContinuedAfterReceivingIdTokenIssuedInPast;
import net.openid.conformance.testmodule.PublishTestModule;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@PublishTestModule(
	testName = "fapi1-advanced-final-client-test-iat-is-week-in-past",
	displayName = "FAPI1-Advanced-Final: client test - iat value which is a week in the past in id_token from authorization_endpoint, should be rejected",
	summary = "This test should end with the client displaying an error message that the iat value in the id_token (from the authorization_endpoint) has expired (in the request object)",
	profile = "FAPI1-Advanced-Final",
	configurationFields = {
		"server.jwks",
		"client.client_id",
		"client.scope",
		"client.redirect_uri",
		"client.certificate",
		"client.jwks",
	}
)

public class FAPI1AdvancedFinalClientTestIatIsWeekInPast extends AbstractFAPI1AdvancedFinalClientExpectNothingAfterAuthorizationEndpoint {

	@Override
	protected void addCustomValuesToIdToken() {

		callAndStopOnFailure(AddIatValueIsWeekInPastToIdToken.class, "OIDCC-3.1.3.7-10");
	}

	@Override
	protected Object authorizationCodeGrantType(String requestId) {

		callAndContinueOnFailure(ClientContinuedAfterReceivingIdTokenIssuedInPast.class, ConditionResult.WARNING);
		setStatus(Status.WAITING);
		fireTestFinished();
		return new ResponseEntity<Object>("Client has incorrectly called token_endpoint after receiving an id_token with an iat value which is a week in the past from the authorization_endpoint.", HttpStatus.BAD_REQUEST);
	}

}
