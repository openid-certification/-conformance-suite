package net.openid.conformance.openbanking_brasil.testmodules.support;

import com.google.gson.JsonObject;
import net.openid.conformance.condition.Condition;
import net.openid.conformance.condition.client.*;
import net.openid.conformance.fapi1advancedfinal.SetApplicationJwtAcceptHeaderForResourceEndpointRequest;
import net.openid.conformance.fapi1advancedfinal.SetApplicationJwtContentTypeHeaderForResourceEndpointRequest;
import net.openid.conformance.openbanking_brasil.OBBProfile;
import net.openid.conformance.openbanking_brasil.testmodules.AbstractOBBrasilFunctionalTestModule;
import net.openid.conformance.testmodule.PublishTestModule;
import net.openid.conformance.variant.FAPI1FinalOPProfile;


@PublishTestModule(
	testName = "payments-api-consumed-consent-test",
	displayName = "Payments API basic consumed consent test module",
	summary = "Payments API basic test module",
	profile = OBBProfile.OBB_PROFILE,
	configurationFields = {
		"server.discoveryUrl",
		"client.client_id",
		"client.jwks",
		"mtls.key",
		"mtls.cert",
		"mtls.ca",
		"resource.consentUrl",
		"resource.brazilCpf",
		"resource.resourceUrl"
	}
)
public class PaymentsConsumedConsentsTestModule extends AbstractOBBrasilFunctionalTestModule {
	@Override
	protected void validateClientConfiguration() {
		eventLog.startBlock("Adding Payment scope");
		callAndStopOnFailure(AddPaymentScope.class);
		eventLog.startBlock("Validating config");
		super.validateClientConfiguration();
	}

	@Override
	protected void onConfigure(JsonObject config, String baseUrl) {
		eventLog.startBlock("Prepping consent request");
		callAndStopOnFailure(PrepareToPostConsentRequest.class);
		eventLog.startBlock("Set resource URL");
		callAndStopOnFailure(SetProtectedResourceUrlToPaymentsEndpoint.class);
	}

	protected void makeRequest(boolean fail){
		callAndStopOnFailure(CreateEmptyResourceEndpointRequestHeaders.class);
		if (isSecondClient()) {
			if (getVariant(FAPI1FinalOPProfile.class) == FAPI1FinalOPProfile.CONSUMERDATARIGHT_AU) {
				// CDR requires this header for all authenticated resource server endpoints
				callAndStopOnFailure(AddFAPIAuthDateToResourceEndpointRequest.class, "FAPI1-BASE-6.2.2-3", "CDR-http-headers");
			}
		} else {
			// these are optional; only add them for the first client
			callAndStopOnFailure(AddFAPIAuthDateToResourceEndpointRequest.class, "FAPI1-BASE-6.2.2-3");
			callAndStopOnFailure(AddIpV4FapiCustomerIpAddressToResourceEndpointRequest.class, "FAPI1-BASE-6.2.2-4");
			if (getVariant(FAPI1FinalOPProfile.class) == FAPI1FinalOPProfile.CONSUMERDATARIGHT_AU) {
				// CDR requires this header when the x-fapi-customer-ip-address header is present
				callAndStopOnFailure(AddCdrXCdsClientHeadersToResourceEndpointRequest.class, "CDR-http-headers");
			}
			callAndStopOnFailure(CreateRandomFAPIInteractionId.class);
			callAndStopOnFailure(AddFAPIInteractionIdToResourceEndpointRequest.class, "FAPI1-BASE-6.2.2-5");
		}
		if (getVariant(FAPI1FinalOPProfile.class) == FAPI1FinalOPProfile.CONSUMERDATARIGHT_AU) {
			callAndStopOnFailure(AddCdrXvToResourceEndpointRequest.class, "CDR-http-headers");
		}
		if (getVariant(FAPI1FinalOPProfile.class) == FAPI1FinalOPProfile.OPENBANKING_BRAZIL) {
			// setup to call the payments initiation API, which requires a signed jwt request body
			call(sequenceOf(condition(CreateIdempotencyKey.class), condition(AddIdempotencyKeyHeader.class)));
			callAndStopOnFailure(SetApplicationJwtContentTypeHeaderForResourceEndpointRequest.class);
			callAndStopOnFailure(SetApplicationJwtAcceptHeaderForResourceEndpointRequest.class);
			callAndStopOnFailure(SetResourceMethodToPost.class);
			callAndStopOnFailure(CreatePaymentRequestEntityClaims.class);

			// we reuse the request object conditions to add various jwt claims; it would perhaps make sense to make
			// these more generic.
			call(exec().mapKey("request_object_claims", "resource_request_entity_claims"));

			// aud (in the JWT request): the Resource Provider (eg the institution holding the account) must validate if the value of the aud field matches the endpoint being triggered;
			callAndStopOnFailure(AddAudAsPaymentInitiationUriToRequestObject.class, "BrazilOB-6.1");

			//iss (in the JWT request and in the JWT response): the receiver of the message shall validate if the value of the iss field matches the organisationId of the sender;
			callAndStopOnFailure(AddIssAsCertificateOuToRequestObject.class, "BrazilOB-6.1");

			//jti (in the JWT request and in the JWT response): the value of the jti field shall be filled with the UUID defined by the institution according to [RFC4122] version 4;
			callAndStopOnFailure(AddJtiAsUuidToRequestObject.class, "BrazilOB-6.1");

			//iat (in the JWT request and in the JWT response): the iat field shall be filled with the message generation time and according to the standard established in [RFC7519](https:// datatracker.ietf.org/doc/html/rfc7519#section-2) to the NumericDate format.
			callAndStopOnFailure(AddIatToRequestObject.class, "BrazilOB-6.1");
			call(exec().unmapKey("request_object_claims"));
			callAndStopOnFailure(FAPIBrazilSignPaymentInitiationRequest.class);
		}
		// if expecting fail
		if(fail){
			callAndContinueOnFailure(CallProtectedResourceAndExpectFailure.class);
			eventLog.startBlock("Validating response, expecting 422 jwt with code: CONSENTIMENTO_INVALIDO");
			callAndContinueOnFailure(EnsureResponseCodeWas422.class);
			callAndStopOnFailure(EnsureCodeIsInvalidConsent.class);
		}
		// if expecting pass
		else {
			callAndContinueOnFailure(CallProtectedResourceWithBearerTokenAndCustomHeaders.class, "FAPI1-BASE-6.2.1-1", "FAPI1-BASE-6.2.1-3");
			callAndContinueOnFailure(CheckForDateHeaderInResourceResponse.class, Condition.ConditionResult.FAILURE, "FAPI1-BASE-6.2.1-11");
			callAndContinueOnFailure(CheckForFAPIInteractionIdInResourceResponse.class, Condition.ConditionResult.FAILURE, "FAPI1-BASE-6.2.1-11");
			if (!isSecondClient()) {
				callAndContinueOnFailure(EnsureMatchingFAPIInteractionId.class, Condition.ConditionResult.FAILURE, "FAPI1-BASE-6.2.1-11");
			}
			validateBrazilPaymentInitiationSignedResponse();
		}
	}

	@Override
	protected void requestProtectedResource(){
		// verify the access token against a protected resource
		eventLog.startBlock("Making first valid request");
		makeRequest(false);
		eventLog.endBlock();
		validateResponse();

		eventLog.startBlock("Attempting a call with a consumed consent - should fail");
		makeRequest(true);
		eventLog.endBlock();

	}


	@Override
	protected void validateResponse() {
	}


}
