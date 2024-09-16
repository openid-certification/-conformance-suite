package net.openid.conformance.openid.client;

import net.openid.conformance.condition.as.InvalidateIdTokenSignature;
import net.openid.conformance.condition.as.OIDCCGenerateServerConfigurationIdTokenSigningAlgRS256Only;
import net.openid.conformance.condition.as.SetServerSigningAlgToRS256;
import net.openid.conformance.sequence.ConditionSequence;
import net.openid.conformance.sequence.as.OIDCCRegisterClientWithIdTokenSignedResponseAlgRS256;
import net.openid.conformance.testmodule.PublishTestModule;

@PublishTestModule(
	testName = "oidcc-client-test-invalid-sig-rs256",
	displayName = "OIDCC: Relying party test. Invalid id_token signature using RS256.",
	summary = """
		The client must identify the invalid signature and reject the ID Token after doing ID Token validation. The client may skip this validation if the id token was received from the token endpoint as per https://openid.net/specs/openid-connect-core-1_0.html#IDTokenValidation
		
		Corresponds to rp-id_token-bad-sig-rs256 test in the old test suite\
		""",
	profile = "OIDCC",
	configurationFields = {
		"waitTimeoutSeconds"
	}
)
public class OIDCCClientTestInvalidIdTokenSignatureWithRS256 extends AbstractOIDCCClientTestExpectingNothingInvalidIdToken {

	@Override
	protected boolean isInvalidSignature() {
		return true;
	}

	@Override
	public void customizeIdTokenSignature() {
		callAndStopOnFailure(InvalidateIdTokenSignature.class, "OIDCC-3.1.3.7", "OIDCC-3.2.2.11");
	}

	@Override
	protected void configureServerConfiguration() {
		callAndStopOnFailure(OIDCCGenerateServerConfigurationIdTokenSigningAlgRS256Only.class);
	}

	@Override
	protected void setServerSigningAlgorithm() {
		callAndStopOnFailure(SetServerSigningAlgToRS256.class);
	}

	@Override
	protected Class<? extends ConditionSequence> getAdditionalClientRegistrationSteps() {
		return OIDCCRegisterClientWithIdTokenSignedResponseAlgRS256.class;
	}


	@Override
	protected String getAuthorizationCodeGrantTypeErrorMessage() {
		return "Client has incorrectly called token_endpoint after receiving an id_token with an invalid signature from the authorization_endpoint.";
	}

	@Override
	protected String getHandleUserinfoEndpointRequestErrorMessage() {
		return "Client has incorrectly called userinfo_endpoint after receiving an id_token with an invalid signature.";
	}

}
