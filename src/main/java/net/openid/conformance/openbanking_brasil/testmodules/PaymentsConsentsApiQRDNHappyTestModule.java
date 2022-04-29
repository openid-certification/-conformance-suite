package net.openid.conformance.openbanking_brasil.testmodules;

import net.openid.conformance.condition.Condition;
import net.openid.conformance.condition.client.*;
import net.openid.conformance.openbanking_brasil.OBBProfile;
import net.openid.conformance.openbanking_brasil.paymentInitiation.PaymentFetchPixPaymentsValidator;
import net.openid.conformance.openbanking_brasil.testmodules.support.*;
import net.openid.conformance.openbanking_brasil.testmodules.support.payments.*;
import net.openid.conformance.openbanking_brasil.testmodules.support.warningMessages.TestTimedOut;
import net.openid.conformance.sequence.ConditionSequence;
import net.openid.conformance.testmodule.PublishTestModule;


@PublishTestModule(
	testName = "payments-api-qrdn-good-proxy-test",
	displayName = "Payments Consents API test module for QRDN local instrument with user provided details",
	summary = "The test will use the user provided QRDN fields: Payment consent request JSON with QRDN embedded;Initiators CNPJ for QRDN test;Remittance information for QRDN test, to create the request_body for both the Post Consents and the Post Payments. The Dynamic QRCode must be created by the organisation by using the PIX Tester environment and all the creditor details must be aligned with what is supplied on this field. The Test will first create a payment using the provided qrdn and, after it reaches an ACCC state, it will create another payment using the same QRDN and will expect a failure as the QRDN should be on a consumed state.",
	profile = OBBProfile.OBB_PROFIlE_PHASE3,
	configurationFields = {
		"server.discoveryUrl",
		"client.client_id",
		"client.jwks",
		"mtls.key",
		"mtls.cert",
		"mtls.ca",
		"resource.consentUrl",
		"resource.resourceUrl",
		"resource.brazilCpf",
		"resource.brazilPaymentConsent",
		"resource.brazilPixPayment",
		"resource.brazilQrdnPaymentConsent",
		"resource.brazilQrdnCnpj",
		"resource.brazilQrdnRemittance"
	}
)
public class PaymentsConsentsApiQRDNHappyTestModule extends AbstractOBBrasilQrCodePaymentFunctionalTestModule {

	private boolean secondAuthCodeFlow = false;

	@Override
	protected ConditionSequence createOBBPreauthSteps() {
		eventLog.log(getName(), "Payments scope present - protected resource assumed to be a payments endpoint");
		ConditionSequence steps = new OpenBankingBrazilPreAuthorizationErrorAgnosticSteps(addTokenEndpointClientAuthentication)
			.replace(FAPIBrazilCreatePaymentConsentRequest.class, condition(SelectPaymentConsentWithQrdnCode.class))
		    .insertBefore(CreateTokenEndpointRequestForClientCredentialsGrant.class, condition(RememberOriginalScopes.class))
		    .insertBefore(FAPIBrazilValidateResourceResponseTyp.class, condition(EnsureConsentStatusIsAwaitingAuthorisation.class))
			.replace(OptionallyAllow201Or422.class, condition(EnsureConsentResponseCodeWas201.class)
							.dontStopOnFailure()
							.onFail(Condition.ConditionResult.FAILURE)
							.skipIfStringMissing("proceed_with_test"));
		return steps;
	}

	@Override
	protected void postProcessResourceSequence(ConditionSequence pixSequence) {
		pixSequence.replace(CreatePaymentRequestEntityClaims.class, condition(CreatePaymentRequestEntityClaimsFromQrdnConfig.class));
	}

	@Override
	protected void configureDictInfo() {
		callAndStopOnFailure(SelectQRDNCodeLocalInstrumentWithQrdnConfig.class);
		callAndStopOnFailure(SelectQRDNCodePixLocalInstrument.class);
		callAndStopOnFailure(ValidateQrdnConfig.class);
	}

	@Override
	protected void validateResponse() {
		super.validateResponse();
		env.removeNativeValue("proceed_with_test");
		eventLog.startBlock("Try to re-use QR code");
		callAndContinueOnFailure(SubsequentPixPaymentEditorCondition.class);

		ConditionSequence createNewConsent = new OpenBankingBrazilPreAuthorizationErrorAgnosticSteps(addTokenEndpointClientAuthentication)
			.insertBefore(CreateTokenEndpointRequestForClientCredentialsGrant.class, condition(ResetScopesToConfigured.class))
			.replace(FAPIBrazilCreatePaymentConsentRequest.class, condition(SelectPaymentConsentWithQrdnCode.class))
			.skip(EnsureHttpStatusCodeIs201.class, "Skipping because we may proceed here and reject later");
		call(createNewConsent);
		if(env.getString("proceed_with_test") == null) {
			eventLog.log(getName(), "Consent call failed early - test finished");
			callAndStopOnFailure(EnsureConsentErrorWasDetalhePgtoInvalido.class);
			fireTestFinished();
		}
		performSecondFlow();
	}

	protected void performSecondFlow() {

		eventLog.startBlock(currentClientString() + "Make request to authorization endpoint");

		createAuthorizationRequest();

		createAuthorizationRequestObject();

		if (isPar.isTrue()) {
			callAndStopOnFailure(BuildRequestObjectPostToPAREndpoint.class);
			addClientAuthenticationToPAREndpointRequest();
			performParAuthorizationRequestFlow();
		} else {
			buildRedirect();
			performRedirect();
		}
	}


	@Override
	protected void onPostAuthorizationFlowComplete() {
		if(!secondAuthCodeFlow) {
			secondAuthCodeFlow = true;
			return;
		}
		requestProtectedResourceAgain();

	}

	protected void requestProtectedResourceAgain() {
		ConditionSequence pixSequence = new CallPixPaymentsEndpointSequence()
			.skip(EnsureResponseCodeWas201.class, "Skipping 201 check");
		postProcessResourceSequence(pixSequence);
		resourceCreationErrorMessageCondition().ifPresent(c -> {
			pixSequence.insertAfter(CallProtectedResource.class, condition(c));
		});
		call(pixSequence);
		pollForStatusChange();
		if (!env.getBoolean("proxy_payment_422")) {
			callAndStopOnFailure(EnsurePaymentIsRejected.class);
			callAndStopOnFailure(PaymentFetchPixPaymentsValidator.class);
		}

		fireTestFinished();
	}

	private void pollForStatusChange() {
		callAndStopOnFailure(ProxyTestCheckForPass.class);
		callAndStopOnFailure(EnsureProxyTestResourceResponseCodeWas422.class);
		if (!env.getBoolean("proxy_payment_422")) {
			int count = 1;
			boolean keepPolling = true;
			while (keepPolling) {
				call(sequence(PollPaymentsSequence.class));

				call(sequenceOf(
					condition(PaymentsProxyCheckForRejectedStatus.class),
					condition(PaymentsProxyCheckEnsureNoRejectionReasonUnlessRejected.class),
					condition(PaymentsProxyCheckForInvalidStatus.class)));

				if (env.getBoolean("payment_proxy_check_for_reject")) {
					if (env.getBoolean("consent_rejected")) {
						keepPolling = false;
					}
				}

				if (count >= 8) {
					keepPolling = false;
					callAndStopOnFailure(TestTimedOut.class);
					callAndStopOnFailure(ChuckWarning.class, Condition.ConditionResult.FAILURE);
				} else {
					count++;
				}
			}
		}
	}

}
