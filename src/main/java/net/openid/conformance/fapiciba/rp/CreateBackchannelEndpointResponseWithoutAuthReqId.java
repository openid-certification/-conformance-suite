package net.openid.conformance.fapiciba.rp;

import com.google.gson.JsonObject;
import net.openid.conformance.condition.AbstractCondition;
import net.openid.conformance.condition.PostEnvironment;
import net.openid.conformance.condition.PreEnvironment;
import net.openid.conformance.testmodule.Environment;

import java.time.Instant;
import java.time.format.DateTimeFormatter;

public class CreateBackchannelEndpointResponseWithoutAuthReqId extends AbstractCondition {

	public static final int EXPIRES_IN = 180;

	@Override
	@PreEnvironment(required = { "backchannel_endpoint_http_request", "backchannel_request_object" })
	@PostEnvironment(required = "backchannel_endpoint_response")
	public Environment evaluate(Environment env) {
		JsonObject backchannelResponse = new JsonObject();

		backchannelResponse.addProperty("expires_in", EXPIRES_IN);

		env.putObject("backchannel_endpoint_response", backchannelResponse);
		logSuccess("Created backchannel response", backchannelResponse);

		return env;
	}
}
