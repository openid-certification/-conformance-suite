package net.openid.conformance.openbanking_brasil.testmodules.v2.resources;

import com.google.gson.JsonObject;
import net.openid.conformance.condition.Condition;
import net.openid.conformance.condition.client.CallProtectedResource;
import net.openid.conformance.openbanking_brasil.OBBProfile;
import net.openid.conformance.openbanking_brasil.generic.ErrorValidator;
import net.openid.conformance.openbanking_brasil.resourcesAPI.v1.PrepareUrlForResourcesCall;
import net.openid.conformance.openbanking_brasil.resourcesAPI.v2.*;
import net.openid.conformance.openbanking_brasil.testmodules.AbstractOBBrasilFunctionalTestModule;
import net.openid.conformance.openbanking_brasil.testmodules.creditOperations.financing.AddScopesForFinancingsApi;
import net.openid.conformance.openbanking_brasil.testmodules.customerAPI.AddScopesForCustomerApi;
import net.openid.conformance.openbanking_brasil.testmodules.support.*;
import net.openid.conformance.testmodule.PublishTestModule;
import net.openid.conformance.variant.FAPI1FinalOPProfile;
import net.openid.conformance.variant.VariantHidesConfigurationFields;

@PublishTestModule(
	testName = "resources-api-test-unavailable-v2",
	displayName = "Validates that the server has correctly implemented the expected behaviour for temporarily blocked resources",
	summary = "Validates that the server has correctly implemented the expected behaviour for temporarily blocked resources\n" +
		"\u2022 Creates a CONSENT with all the existing permissions including either business or personal data, depending on what has been provided on the test configuration\n" +
		"\u2022 Expects a Success 201\n" +
		"\u2022 Redirect the user to authorize the CONSENT - Redirect URI must contain all phase 2 scopes\n" +
		"\u2022 Expect a Successful authorization with an authorization code created\n" +
		"\u2022 Call the RESOURCES API with the authorized consent\n" +
		"\u2022 Expect a 200 - Validate that AT LEAST one Resource has been returned and is on the state TEMPORARY_UNAVAILABLE/UNAVAILABLE\n" +
		"\u2022 Evaluate which Resource is on the TEMPORARY_UNAVAILABLE/UNAVAILABLE state, fetch the resource id, create the base request URI for said resource\n" +
		"\u2022 Call either the CONTRACTS or the ACCOUNTS list API for this Resource\n" +
		"\u2022 Expect a 200 - Make sure the Server returns a 200 without that TEMPORARY_UNAVAILABLE/UNAVAILABLE resource on it's list\n" +
		"\u2022 Depending on the unavailable resource, call one of the following APIs depending: (1) /contracts/{contractId}/warranties for credit operations, (2) /accounts/{creditCardAccountId}/bills for credit cards, or (3) /accounts/{accountId}/balances for accounts\n" +
		"\u2022 Expect a 403 - Validate that the field response.errors.code is STATUS_RESOURCE_TEMPORARY_UNAVAILABLE/STATUS_RESOURCE_UNAVAILABLE\n" +
		"\u2022 \n",
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
		"consent.productType"
	}
)
@VariantHidesConfigurationFields(parameter = FAPI1FinalOPProfile.class, value = "openbanking_brazil", configurationFields = {
	"client.org_jwks",
	"directory.client_id"
})
public class ResourcesApiTestModuleUnavailableV2 extends AbstractOBBrasilFunctionalTestModule {

	@Override
	protected void configureClient() {
		callAndStopOnFailure(BuildResourcesConfigResourceUrlFromConsentUrl.class);
		super.configureClient();
	}

	@Override
	protected void onConfigure(JsonObject config, String baseUrl) {
		callAndStopOnFailure(AddAccountScope.class);
		callAndStopOnFailure(AddCreditCardScopes.class);
		callAndStopOnFailure(AddScopesForCustomerApi.class);
		callAndStopOnFailure(AddInvoiceFinancingsScope.class);
		callAndStopOnFailure(AddScopesForFinancingsApi.class);
		callAndStopOnFailure(AddLoansScope.class);
		callAndStopOnFailure(AddUnarrangedOverdraftScope.class);
		callAndStopOnFailure(AddResourcesScope.class);

		callAndStopOnFailure(PrepareAllResourceRelatedConsentsForHappyPathTest.class);
		callAndStopOnFailure(PrepareUrlForResourcesCall.class);
		super.onConfigure(config, baseUrl);
	}

	@Override
	protected void validateResponse() {
		callAndContinueOnFailure(ResourcesResponseValidatorV2.class, Condition.ConditionResult.FAILURE);
		callAndStopOnFailure(SaveUnavailableResourceData.class);

		runInBlock("Ensure we cannot see the given unavailable resource in its api list.", () -> {
			callAndStopOnFailure(UpdateSavedResourceData.class);
			callAndStopOnFailure(PrepareUrlForApiListForSavedResourceCall.class);
			callAndContinueOnFailure(CallProtectedResource.class, Condition.ConditionResult.FAILURE);
			callAndContinueOnFailure(EnsureResponseCodeWas200.class, Condition.ConditionResult.FAILURE);
			callAndContinueOnFailure(EnsureUnavailableResourceIsNotOnList.class, Condition.ConditionResult.FAILURE);
		});

		runInBlock("Ensure we cannot access the unavailable resource.", () -> {
			callAndStopOnFailure(PrepareUrlForSavedResourceCall.class);
			callAndContinueOnFailure(CallProtectedResource.class, Condition.ConditionResult.FAILURE);
			callAndContinueOnFailure(EnsureResponseCodeWas403.class);
			callAndContinueOnFailure(ErrorValidator.class, Condition.ConditionResult.FAILURE);
		});
	}
}
