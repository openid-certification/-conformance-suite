package net.openid.conformance.openbanking_brasil.testmodules.creditOperations.advances.testmodules.v2;

import com.google.gson.JsonObject;
import net.openid.conformance.condition.Condition;
import net.openid.conformance.openbanking_brasil.OBBProfile;
import net.openid.conformance.openbanking_brasil.creditOperations.advances.v2.AdvancesContractInstallmentsResponseValidatorV2;
import net.openid.conformance.openbanking_brasil.creditOperations.advances.v2.AdvancesContractResponseValidatorV2;
import net.openid.conformance.openbanking_brasil.creditOperations.advances.v2.AdvancesGuaranteesResponseValidatorV2;
import net.openid.conformance.openbanking_brasil.creditOperations.advances.v2.AdvancesPaymentsResponseValidatorV2;
import net.openid.conformance.openbanking_brasil.creditOperations.advances.v1.AdvancesResponseValidator;
import net.openid.conformance.openbanking_brasil.testmodules.AbstractOBBrasilFunctionalTestModule;
import net.openid.conformance.openbanking_brasil.testmodules.creditOperations.PrepareAllCreditOperationsPermissionsForHappyPath;
import net.openid.conformance.openbanking_brasil.testmodules.creditOperations.advances.*;
import net.openid.conformance.openbanking_brasil.testmodules.support.AddUnarrangedOverdraftScope;
import net.openid.conformance.openbanking_brasil.testmodules.support.BuildCreditOperationsAdvancesConfigResourceUrlFromConsentUrl;
import net.openid.conformance.openbanking_brasil.testmodules.support.EnsureResponseHasLinks;
import net.openid.conformance.openbanking_brasil.testmodules.support.ValidateResponseMetaData;
import net.openid.conformance.openbanking_brasil.testmodules.support.ValidateSelfEndpoint;
import net.openid.conformance.testmodule.PublishTestModule;

@PublishTestModule(
	testName = "unarranged-overdraft-api-test-v2",
	displayName = "Validate structure of all unarranged overdraft API resources V2",
	summary = "Validates the structure of all unarranged overdraft API resources V2\n" +
		"\u2022 Creates a consent with all the permissions needed to access the Credit Operations API  (\"LOANS_READ\", \"LOANS_WARRANTIES_READ\", \"LOANS_SCHEDULED_INSTALMENTS_READ\", \"LOANS_PAYMENTS_READ\", \"FINANCINGS_READ\", \"FINANCINGS_WARRANTIES_READ\", \"FINANCINGS_SCHEDULED_INSTALMENTS_READ\", \"FINANCINGS_PAYMENTS_READ\", \"UNARRANGED_ACCOUNTS_OVERDRAFT_READ\", \"UNARRANGED_ACCOUNTS_OVERDRAFT_WARRANTIES_READ\", \"UNARRANGED_ACCOUNTS_OVERDRAFT_SCHEDULED_INSTALMENTS_READ\", \"UNARRANGED_ACCOUNTS_OVERDRAFT_PAYMENTS_READ\", \"INVOICE_FINANCINGS_READ\", \"INVOICE_FINANCINGS_WARRANTIES_READ\", \"INVOICE_FINANCINGS_SCHEDULED_INSTALMENTS_READ\", \"INVOICE_FINANCINGS_PAYMENTS_READ\", \"RESOURCES_READ\")\n" +
		"\u2022 Expects 201 - Expects Success on Redirect - Validates all of the fields sent on the consents API\n" +
		"\u2022 Calls GET Unarranged Overdraft Contracts API V2\n" +
		"\u2022 Expects 200 - Fetches one of the IDs returned\n" +
		"\u2022 Calls GET Unarranged Overdraft Contracts API with ID V2\n" +
		"\u2022 Expects 200\n" +
		"\u2022 Calls GET Unarranged Overdraft Warranties API V2\n" +
		"\u2022 Expects 200\n" +
		"\u2022 Calls GET Unarranged Overdraft Payments API V2\n" +
		"\u2022 Expects 200\n" +
		"\u2022 Calls GET Unarranged Overdraft Contracts Instalments API V2\n" +
		"\u2022 Expects 200\n",
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
public class CreditOperationsAdvancesApiTestModuleV2 extends AbstractOBBrasilFunctionalTestModule {

	@Override
	protected void configureClient() {
		callAndStopOnFailure(BuildCreditOperationsAdvancesConfigResourceUrlFromConsentUrl.class);
		super.configureClient();
	}

	@Override
	protected void onConfigure(JsonObject config, String baseUrl) {
		callAndStopOnFailure(PrepareAllCreditOperationsPermissionsForHappyPath.class);
		callAndStopOnFailure(AddUnarrangedOverdraftScope.class);
	}

	@Override
	protected void validateResponse() {
		callAndContinueOnFailure(AdvancesResponseValidator.class, Condition.ConditionResult.FAILURE);
		callAndContinueOnFailure(EnsureResponseHasLinks.class, Condition.ConditionResult.FAILURE);
		callAndContinueOnFailure(ValidateResponseMetaData.class, Condition.ConditionResult.FAILURE);
		call(sequence(ValidateSelfEndpoint.class));

		callAndStopOnFailure(CreditAdvanceSelector.class);

		callAndStopOnFailure(PrepareUrlForFetchingCreditAdvanceContracts.class);
		preCallProtectedResource("Contracts V2");
		callAndContinueOnFailure(AdvancesContractResponseValidatorV2.class, Condition.ConditionResult.FAILURE);

		callAndStopOnFailure(PrepareUrlForFetchingCreditAdvanceContractGuarantees.class);
		preCallProtectedResource("Contract Guarantees V2");
		callAndContinueOnFailure(AdvancesGuaranteesResponseValidatorV2.class, Condition.ConditionResult.FAILURE);

		callAndStopOnFailure(PrepareUrlForFetchingCreditAdvanceContractPayments.class);
		preCallProtectedResource("Contract Payments V2");
		callAndContinueOnFailure(AdvancesPaymentsResponseValidatorV2.class, Condition.ConditionResult.FAILURE);

		callAndStopOnFailure(PrepareUrlForFetchingCreditAdvanceContractInstallments.class);
		preCallProtectedResource("Contract Installments V2");
		callAndContinueOnFailure(AdvancesContractInstallmentsResponseValidatorV2.class, Condition.ConditionResult.FAILURE);
	}
}
