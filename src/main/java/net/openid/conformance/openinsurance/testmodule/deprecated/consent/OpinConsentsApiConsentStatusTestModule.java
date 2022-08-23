package net.openid.conformance.openinsurance.testmodule.deprecated.consent;

import com.google.common.base.Strings;
import com.google.gson.JsonObject;
import net.openid.conformance.condition.Condition;
import net.openid.conformance.condition.client.CallProtectedResource;
import net.openid.conformance.openbanking_brasil.OBBProfile;
import net.openid.conformance.openbanking_brasil.generic.ErrorValidator;
import net.openid.conformance.openbanking_brasil.testmodules.customerAPI.AddScopesForCustomerApi;
import net.openid.conformance.openbanking_brasil.testmodules.customerAPI.PrepareToGetCustomCustomerIdentifications;
import net.openid.conformance.openbanking_brasil.testmodules.support.AbstractOBBrasilFunctionalTestModuleOptionalErrors;
import net.openid.conformance.openbanking_brasil.testmodules.support.BuildCustomCustomersConfigResourceUrlFromConsentUrl;
import net.openid.conformance.openbanking_brasil.testmodules.support.EnsureConsentWasAuthorised;
import net.openid.conformance.openbanking_brasil.testmodules.support.EnsureResponseCodeWas200;
import net.openid.conformance.openbanking_brasil.testmodules.support.EnsureResponseCodeWas404;
import net.openid.conformance.openbanking_brasil.testmodules.support.ObtainAccessTokenWithClientCredentials;
import net.openid.conformance.openbanking_brasil.testmodules.support.PrepareAllCustomerRelatedConsentsForResource404HappyPathTest;
import net.openid.conformance.openbanking_brasil.testmodules.support.PrepareToCallCustomerDataEndpoint;
import net.openid.conformance.openbanking_brasil.testmodules.support.PrepareToFetchConsentRequest;
import net.openid.conformance.openbanking_brasil.testmodules.support.TransformConsentRequestForProtectedResource;
import net.openid.conformance.sequence.ConditionSequence;
import net.openid.conformance.testmodule.PublishTestModule;

@PublishTestModule(
	testName = "opin-consent-api-status-test-v1",
	displayName = "Validate that consents V1 are actually authorised on redirect",
	summary = "Validates that consents are actually authorised on redirect\n" +
		"\u2022 Creates a Consent V1 with all of the existing permissions\n" +
		"\u2022 Checks all of the fields sent on the consent API are specification compliant\n" +
		"\u2022 Expects a valid consent creation 201\n" +
		"\u2022 Calls the GET Resources endpoint\n" +
		"\u2022 Expects either a 200 or an error\n" +
		"\u2022 Calls the GET Consents endpoint\n" +
		"\u2022 Expects a 200 with the Consent being authorised",
	profile = OBBProfile.OBB_PROFILE_OPEN_INSURANCE_PHASE2,
	configurationFields = {
		"server.discoveryUrl",
		"client.client_id",
		"client.jwks",
		"mtls.key",
		"mtls.cert",
		"mtls.ca",
		"resource.consentUrl",
		"resource.brazilCpf",
		"resource.customerUrl",
		"consent.productType"
	}
)
public class OpinConsentsApiConsentStatusTestModule extends AbstractOBBrasilFunctionalTestModuleOptionalErrors {

	@Override
	protected void configureClient() {
		callAndStopOnFailure(BuildCustomCustomersConfigResourceUrlFromConsentUrl.class);
		super.configureClient();
	}

	@Override
	protected void onConfigure(JsonObject config, String baseUrl) {
		callAndStopOnFailure(AddScopesForCustomerApi.class);
		callAndStopOnFailure(PrepareAllCustomerRelatedConsentsForResource404HappyPathTest.class);
		callAndStopOnFailure(PrepareToGetCustomCustomerIdentifications.class);
	}

	@Override
	protected void validateResponse() {
		String responseError = env.getString("resource_endpoint_error_code");
		if (Strings.isNullOrEmpty(responseError)) {
			runInBlock("Validating get consent response", () -> {
				callAndStopOnFailure(PrepareToFetchConsentRequest.class);
				callAndStopOnFailure(TransformConsentRequestForProtectedResource.class);
				call(createGetAccessTokenWithClientCredentialsSequence(addTokenEndpointClientAuthentication));
				preCallProtectedResource("Fetch consent");
				callAndStopOnFailure(EnsureConsentWasAuthorised.class);
			});
		} else {
			callAndContinueOnFailure(ErrorValidator.class, Condition.ConditionResult.FAILURE);
			callAndStopOnFailure(EnsureResponseCodeWas404.class);
			String logMessage = "Call personal endpoint";
			runInBlock(logMessage, () -> {
				callAndStopOnFailure(PrepareToCallCustomerDataEndpoint.class);
				callAndStopOnFailure(CallProtectedResource.class);
				callAndContinueOnFailure(EnsureResponseCodeWas200.class, Condition.ConditionResult.WARNING);
			});
		}
	}

	protected ConditionSequence createGetAccessTokenWithClientCredentialsSequence(Class<? extends ConditionSequence> clientAuthSequence) {
		return new ObtainAccessTokenWithClientCredentials(clientAuthSequence);
	}

}
