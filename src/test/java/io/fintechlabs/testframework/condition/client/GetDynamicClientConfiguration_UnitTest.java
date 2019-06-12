package io.fintechlabs.testframework.condition.client;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.fintechlabs.testframework.condition.Condition;
import io.fintechlabs.testframework.condition.ConditionError;
import io.fintechlabs.testframework.logging.TestInstanceEventLog;
import io.fintechlabs.testframework.testmodule.Environment;
import io.fintechlabs.testframework.testmodule.OIDFJSON;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

import static org.assertj.core.api.Java6Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class GetDynamicClientConfiguration_UnitTest {

	@Spy
	private Environment env = new Environment();

	@Mock
	private TestInstanceEventLog eventLog;

	private GetDynamicClientConfiguration cond;

	/**
	 * @throws Exception
	 */
	@Before
	public void setUp() throws Exception {
		cond = new GetDynamicClientConfiguration();
		cond.setProperties("UNIT-TEST", eventLog, Condition.ConditionResult.INFO);
	}

	/**
	 * Test method for {@link io.fintechlabs.testframework.condition.client.GetDynamicClientConfiguration#evaluate(Environment)}
	 */
	@Test(expected = ConditionError.class)
	public void testEvaluate_noClientConfig() {
		env.putObject("config", new JsonObject());

		cond.evaluate(env);
	}

	/**
	 * Test method for {@link io.fintechlabs.testframework.condition.client.GetDynamicClientConfiguration#evaluate(Environment)}
	 */
	@Test
	public void testEvaluate_noClientNameInConfig() {
		JsonObject config = new JsonParser().parse("{" +
			"\"client\":{}" +
			"}").getAsJsonObject();
		env.putObject("config", config);

		cond.evaluate(env);

		assertThat(env.getObject("dynamic_client_registration_template")).isInstanceOf(JsonObject.class);
		assertThat(env.getObject("dynamic_client_registration_template").get("client_name")).isNull();
		assertThat(env.getObject("client_name")).isNull();
	}

	/**
	 * Test method for {@link io.fintechlabs.testframework.condition.client.GetDynamicClientConfiguration#evaluate(Environment)}
	 */
	@Test
	public void testEvaluate_ClientNameInConfig() {
		JsonObject config = new JsonParser().parse("{" +
			"\"client\":{\"client_name\":\"foo\"}" +
			"}").getAsJsonObject();
		env.putObject("config", config);

		cond.evaluate(env);

		assertThat(env.getObject("dynamic_client_registration_template")).isInstanceOf(JsonObject.class);
		assertThat(env.getObject("dynamic_client_registration_template").get("client_name")).isNotNull();
		assertThat(OIDFJSON.getString(env.getObject("dynamic_client_registration_template").get("client_name"))).isEqualTo("foo");
		assertThat(env.getString("client_name")).isNotNull();
		assertThat(env.getString("client_name")).isEqualTo("foo");
	}
}
