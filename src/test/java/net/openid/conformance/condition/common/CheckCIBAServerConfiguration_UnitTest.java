package net.openid.conformance.condition.common;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.openid.conformance.condition.Condition.ConditionResult;
import net.openid.conformance.condition.ConditionError;
import net.openid.conformance.logging.TestInstanceEventLog;
import net.openid.conformance.testmodule.Environment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class CheckCIBAServerConfiguration_UnitTest {

	@Spy
	private Environment env = new Environment();

	@Mock
	private TestInstanceEventLog eventLog;

	private JsonObject goodConfig;

	private JsonObject badConfigWithoutAuthorizationEndpoint;

	private JsonObject badConfigWithoutTokenEndpoint;

	private JsonObject badConfigWithoutIssuer;

	private CheckCIBAServerConfiguration cond;

	/**
	 * @throws Exception
	 */
	@BeforeEach
	public void setUp() throws Exception {

		cond = new CheckCIBAServerConfiguration();

		cond.setProperties("UNIT-TEST", eventLog, ConditionResult.INFO);

		goodConfig = JsonParser.parseString("{"
			+ "\"backchannel_authentication_endpoint\":\"https://example.com/oauth/backauth\","
			+ "\"token_endpoint\":\"https://example.com/api/oauth/token\","
			+ "\"issuer\":\"https://example.com/\""
			+ "}").getAsJsonObject();

		badConfigWithoutAuthorizationEndpoint = JsonParser.parseString("{"
			+ "\"token_endpoint\":\"https://example.com/api/oauth/token\","
			+ "\"issuer\":\"https://example.com/\""
			+ "}").getAsJsonObject();

		badConfigWithoutTokenEndpoint = JsonParser.parseString("{"
			+ "\"backchannel_authentication_endpoint\":\"https://example.com/oauth/backauth\","
			+ "\"issuer\":\"https://example.com/\""
			+ "}").getAsJsonObject();

		badConfigWithoutIssuer = JsonParser.parseString("{"
			+ "\"backchannel_authentication_endpoint\":\"https://example.com/oauth/authorize\","
			+ "\"token_endpoint\":\"https://example.com/api/oauth/token\""
			+ "}").getAsJsonObject();
	}

	@Test
	public void testEvaluate_noError() {

		env.putObject("server", goodConfig);

		cond.execute(env);

		verify(env, atLeastOnce()).getString("server", "backchannel_authentication_endpoint");
		verify(env, atLeastOnce()).getString("server", "token_endpoint");
		verify(env, atLeastOnce()).getString("server", "issuer");
	}

	@Test
	public void testEvaluate_missingAuthorizationEndpoint() {
		assertThrows(ConditionError.class, () -> {

			env.putObject("server", badConfigWithoutAuthorizationEndpoint);

			cond.execute(env);
		});
	}

	@Test
	public void testEvaluate_missingTokenEndpoint() {
		assertThrows(ConditionError.class, () -> {

			env.putObject("server", badConfigWithoutTokenEndpoint);

			cond.execute(env);
		});
	}

	@Test
	public void testEvaluate_missingIssuer() {
		assertThrows(ConditionError.class, () -> {

			env.putObject("server", badConfigWithoutIssuer);

			cond.execute(env);
		});
	}

}
