package net.openid.conformance.condition.client;

import com.google.gson.JsonObject;
import net.openid.conformance.condition.AbstractCondition;
import net.openid.conformance.condition.PostEnvironment;
import net.openid.conformance.condition.PreEnvironment;
import net.openid.conformance.testmodule.Environment;
import org.apache.commons.lang3.RandomStringUtils;

public class AddRandomDpopJktToAuthorizationEndpointRequest extends AbstractCondition {

	@Override
	@PreEnvironment(required = "authorization_endpoint_request")
	@PostEnvironment(required = "authorization_endpoint_request")
	public Environment evaluate(Environment env) {



		String dpopJkt = RandomStringUtils.randomAlphanumeric(10);

		JsonObject authorizationEndpointRequest = env.getObject("authorization_endpoint_request");

		authorizationEndpointRequest.addProperty("dpop_jkt", dpopJkt);

		env.putObject("authorization_endpoint_request", authorizationEndpointRequest);

		logSuccess("Added random dpop_jkt parameter to request", authorizationEndpointRequest);

		return env;

	}

}
