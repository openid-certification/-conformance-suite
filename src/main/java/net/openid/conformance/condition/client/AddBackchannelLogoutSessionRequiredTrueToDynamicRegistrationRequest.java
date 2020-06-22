package net.openid.conformance.condition.client;

import com.google.gson.JsonObject;
import net.openid.conformance.condition.AbstractCondition;
import net.openid.conformance.condition.PostEnvironment;
import net.openid.conformance.condition.PreEnvironment;
import net.openid.conformance.testmodule.Environment;

public class AddBackchannelLogoutSessionRequiredTrueToDynamicRegistrationRequest extends AbstractCondition {

	@Override
	@PreEnvironment(required = "dynamic_registration_request")
	@PostEnvironment(required = "dynamic_registration_request")
	public Environment evaluate(Environment env) {
		JsonObject dynamicRegistrationRequest = env.getObject("dynamic_registration_request");

		dynamicRegistrationRequest.addProperty("backchannel_logout_session_required", true);

		env.putObject("dynamic_registration_request", dynamicRegistrationRequest);

		log("Added backchannel_logout_session_required: true to dynamic registration request", args("dynamic_registration_request", dynamicRegistrationRequest));

		return env;
	}

}
