package io.fintechlabs.testframework.condition.client;

import com.google.gson.JsonObject;
import io.fintechlabs.testframework.condition.Condition;
import io.fintechlabs.testframework.condition.ConditionError;
import io.fintechlabs.testframework.logging.TestInstanceEventLog;
import io.fintechlabs.testframework.testmodule.Environment;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Date;

import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class ValidateJARMExp_Recommendations_UnitTest {
	@Spy
	private Environment env = new Environment();

	@Mock
	private TestInstanceEventLog eventLog;

	private ValidateJARMExpRecommendations cond;

	@Before
	public void setUp() throws Exception {
		cond = new ValidateJARMExpRecommendations();
		cond.setProperties("UNIT_TEST", eventLog, Condition.ConditionResult.INFO);
	}

	private void createResponse(Environment env, int expiresIn) {
		Date now = new Date();
		long nowSeconds = now.getTime() / 1000;

		JsonObject claims = new JsonObject();
		claims.addProperty("exp", nowSeconds + expiresIn);

		JsonObject response = new JsonObject();
		response.add("claims", claims);
		env.putObject("jarm_response",response);
	}

	@Test
	public void testEvaluate_noError() {
		createResponse(env,240); // 4 minute offset
		cond.evaluate(env);
		verify(env, atLeastOnce()).getLong("jarm_response", "claims.exp");
	}

	@Test
	public void testEvaluate_noErrorMaxLength() {
		createResponse(env,15*60); // 15 minutes (10 minutes allowable in spec + 5 minutes allowed time skew)
		cond.evaluate(env);
	}

	@Test(expected = ConditionError.class)
	public void testEvaluate_tooLong() {
		createResponse(env,16*60); // 16 minutes (10 minutes allowable in spec + 5 minutes allowed time skew)
		cond.evaluate(env);
	}

	@Test(expected = ConditionError.class)
	public void testEvaluate_tooShort() {
		createResponse(env,5); // 30 seconds; fairly random
		cond.evaluate(env);
	}

	@Test(expected = ConditionError.class)
	public void testEvaluate_noExp() {
		JsonObject claims = new JsonObject();
		JsonObject response = new JsonObject();
		response.add("claims", claims);
		env.putObject("heart_response",response);
		cond.evaluate(env);
	}
}
