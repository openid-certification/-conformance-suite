package net.openid.conformance.openbanking_brasil.testmodules.support;

import com.google.gson.JsonObject;
import net.openid.conformance.condition.client.jsonAsserting.AbstractJsonAssertingCondition;
import net.openid.conformance.testmodule.Environment;

public class ReplaceInitiatorCnpjWithUnknownValue extends AbstractJsonAssertingCondition {

	@Override
	public Environment evaluate(Environment environment) {
		JsonObject obj = environment.getObject("resource");
		obj = obj.getAsJsonObject("brazilPixPayment");
		obj = obj.getAsJsonObject("data");
		obj.addProperty("cnpjInitiator", "62176752000165");
		return environment;
	}

}
