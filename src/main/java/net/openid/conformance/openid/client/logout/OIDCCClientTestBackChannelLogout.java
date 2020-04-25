package net.openid.conformance.openid.client.logout;

import net.openid.conformance.condition.Condition;
import net.openid.conformance.condition.as.logout.EnsureBackChannelLogoutEndpointResponseContainsCacheHeaders;
import net.openid.conformance.condition.as.logout.EnsureBackChannelLogoutUriResponseStatusCodeIs200;
import net.openid.conformance.testmodule.PublishTestModule;

@PublishTestModule(
	testName = "oidcc-client-test-rp-backchannel-rpinitlogout",
	displayName = "OIDCC: Relying party test, back channel logout.",
	summary = "The client is expected to make an authorization request " +
		"(also a token request and a optionally a userinfo request when applicable)," +
		" then the RP terminates the session by calling the end_session_endpoint (RP-Initiated Logout)," +
		" then Handle Post Logout URI Redirect" +
		" then the OP(the test suite) will send a back channel logout request." +
		" Corresponds to rp-backchannel-rpinitlogout in the old test suite.",
	profile = "OIDCC",
	configurationFields = {
	}
)
public class OIDCCClientTestBackChannelLogout extends AbstractOIDCCClientBackChannelLogoutTest
{

	@Override
	protected void validateBackChannelLogoutResponse() {
		super.validateBackChannelLogoutResponse();
		callAndStopOnFailure(EnsureBackChannelLogoutUriResponseStatusCodeIs200.class, "OIDCBCL-2.8");
	}

}
