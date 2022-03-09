package net.openid.conformance.condition.client;

import com.google.gson.JsonParser;
import net.openid.conformance.condition.Condition;
import net.openid.conformance.condition.ConditionError;
import net.openid.conformance.logging.TestInstanceEventLog;
import net.openid.conformance.testmodule.Environment;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class CheckErrorFromBackchannelAuthenticationEndpointError_UnitTest {

	@Spy
	private Environment env = new Environment();

	@Mock
	private TestInstanceEventLog eventLog;

	private CheckErrorFromBackchannelAuthenticationEndpointError cond;

	@Before
	public void setUp() throws Exception {
		cond = new CheckErrorFromBackchannelAuthenticationEndpointError();
		cond.setProperties("UNIT-TEST", eventLog, Condition.ConditionResult.INFO);
	}

	@Test
	public void testEvaluate_caseInvalidRequest() {
		env.putObject("backchannel_authentication_endpoint_response", JsonParser.parseString("{\"error\":\"invalid_request\"}").getAsJsonObject());

		cond.execute(env);
	}

	@Test
	public void testEvaluate_caseAccessDenied() {
		env.putObject("backchannel_authentication_endpoint_response", JsonParser.parseString("{\"error\":\"access_denied\"}").getAsJsonObject());

		cond.execute(env);
	}

	@Test
	public void testEvaluate_caseInvalidClient() {
		env.putObject("backchannel_authentication_endpoint_response", JsonParser.parseString("{\"error\":\"invalid_client\"}").getAsJsonObject());

		cond.execute(env);
	}

	@Test(expected = ConditionError.class)
	public void testEvaluate_caseInvalidRequestObject() {
		env.putObject("backchannel_authentication_endpoint_response", JsonParser.parseString("{\"error\":\"invalid_request_object\"}").getAsJsonObject());

		cond.execute(env);
	}
}
