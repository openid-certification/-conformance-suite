package net.openid.conformance.openbanking_brasil.testmodules.support;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.openid.conformance.condition.PreEnvironment;
import net.openid.conformance.condition.client.jsonAsserting.AbstractJsonAssertingCondition;
import net.openid.conformance.testmodule.Environment;
import net.openid.conformance.util.field.StringField;

public class EnsureErrorResponseCodeIsStatusResourceAwaitingAuthorization extends AbstractJsonAssertingCondition {

	@Override
	@PreEnvironment(required = "resource_endpoint_response")
	public Environment evaluate(Environment env) {
		JsonObject body = bodyFrom(env).getAsJsonObject();
		JsonArray errors = findByPath(body, "$.errors").getAsJsonArray();
		JsonObject error = errors.get(0).getAsJsonObject();
		assertField(error, new StringField
			.Builder("code")
			.setPattern("STATUS_RESOURCE_AWAITING_AUTHORIZATION")
			.build());
		return env;
	}
}
