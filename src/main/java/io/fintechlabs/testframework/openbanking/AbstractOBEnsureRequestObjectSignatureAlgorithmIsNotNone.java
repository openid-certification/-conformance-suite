package io.fintechlabs.testframework.openbanking;

import java.util.Map;

import io.fintechlabs.testframework.condition.client.BuildRequestObjectRedirectToAuthorizationEndpoint;
import io.fintechlabs.testframework.condition.client.ConvertAuthorizationEndpointRequestToRequestObject;
import io.fintechlabs.testframework.condition.client.ExpectRequestObjectUnverifiableErrorPage;
import io.fintechlabs.testframework.condition.client.SerializeRequestObjectWithNullAlgorithm;
import io.fintechlabs.testframework.frontChannel.BrowserControl;
import io.fintechlabs.testframework.info.TestInfoService;
import io.fintechlabs.testframework.logging.TestInstanceEventLog;
import io.fintechlabs.testframework.runner.TestExecutionManager;

public abstract class AbstractOBEnsureRequestObjectSignatureAlgorithmIsNotNone extends AbstractOBServerTestModule {

	public AbstractOBEnsureRequestObjectSignatureAlgorithmIsNotNone(String id, Map<String, String> owner, TestInstanceEventLog eventLog, BrowserControl browser, TestInfoService testInfo, TestExecutionManager executionManager) {
		super(id, owner, eventLog, browser, testInfo, executionManager);
	}

	@Override
	protected void performAuthorizationFlow() {

		requestClientCredentialsGrant();

		createAccountRequest();

		createAuthorizationRequest();

		createAuthorizationRedirect();

		String redirectTo = env.getString("redirect_to_authorization_endpoint");

		eventLog.log(getName(), "Redirecting to url " + redirectTo);

		callAndStopOnFailure(ExpectRequestObjectUnverifiableErrorPage.class, "FAPI-2-7.3-1");

		setStatus(Status.WAITING);

		browser.goToUrl(redirectTo);
	}

	@Override
	protected void createAuthorizationRedirect() {

		callAndStopOnFailure(ConvertAuthorizationEndpointRequestToRequestObject.class);

		callAndStopOnFailure(SerializeRequestObjectWithNullAlgorithm.class);

		callAndStopOnFailure(BuildRequestObjectRedirectToAuthorizationEndpoint.class);
	}

}
