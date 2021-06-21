package net.openid.conformance.openbanking_brasil;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.openid.conformance.condition.AbstractCondition;
import net.openid.conformance.condition.PostEnvironment;
import net.openid.conformance.testmodule.Environment;
import net.openid.conformance.testmodule.OIDFJSON;

public class CardAccountSelector extends AbstractCondition {

	@Override
	@PostEnvironment(strings = "accountId")
	public Environment evaluate(Environment env) {
		String entityString = env.getString("resource_endpoint_response");
		JsonObject accountList = new JsonParser().parse(entityString).getAsJsonObject();
		JsonArray data = accountList.getAsJsonArray("data");
		JsonObject firstAccount = data.get(0).getAsJsonObject();
		String creditCardAccountId = OIDFJSON.getString(firstAccount.get("creditCardAccountId"));
		env.putString("accountId", creditCardAccountId);
		return env;
	}

}
