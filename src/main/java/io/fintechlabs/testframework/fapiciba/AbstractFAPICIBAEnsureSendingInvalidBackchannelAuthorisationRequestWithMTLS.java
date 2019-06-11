package io.fintechlabs.testframework.fapiciba;

import com.google.gson.JsonObject;
import io.fintechlabs.testframework.condition.Condition;
import io.fintechlabs.testframework.condition.ConditionError;
import io.fintechlabs.testframework.condition.client.CheckErrorFromBackchannelAuthenticationEndpointErrorInvalidRequest;

// Send invalid request to backchannel authorisation endpoint and the response is invalid_request
public abstract class AbstractFAPICIBAEnsureSendingInvalidBackchannelAuthorisationRequestWithMTLS extends AbstractFAPICIBAWithMTLS {
	@Override
	protected void performPostAuthorizationResponse() {

		validateErrorFromBackchannelAuthorizationRequestResponse();

		callAndContinueOnFailure(CheckErrorFromBackchannelAuthenticationEndpointErrorInvalidRequest.class, Condition.ConditionResult.FAILURE, "CIBA-13");

		fireTestFinished();
	}

	@Override
	protected void performAuthorizationFlow() {
		performPreAuthorizationSteps();

		eventLog.startBlock(currentClientString() + "Call backchannel authentication endpoint");

		createAuthorizationRequest();

		performAuthorizationRequest();

		eventLog.endBlock();

		performPostAuthorizationResponse();
	}

	@Override
	protected void waitForAuthenticationToComplete(long delaySeconds) {
		//Not called in this test

	}

	@Override
	protected void processNotificationCallback(JsonObject requestParts) {
		fireTestFailure();
		throw new ConditionError(getId(), "Notification endpoint was called while it shouldn't");
	}
}
