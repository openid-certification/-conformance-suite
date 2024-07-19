package net.openid.conformance.fapi2spid2;

import com.google.gson.JsonObject;
import net.openid.conformance.condition.AbstractCondition;
import net.openid.conformance.condition.PreEnvironment;
import net.openid.conformance.testmodule.Environment;
import org.springframework.util.StringUtils;

public class EnsureIdTokenContainsMandatoryFAPI2Claims extends AbstractCondition {

	@Override
	@PreEnvironment(required = {"id_token"} )
	public Environment evaluate(Environment env) {

		JsonObject idTokenClaims = env.getElementFromObject("id_token", "claims").getAsJsonObject();
		boolean txnValuePresent = idTokenClaims.has("txn");
		if (!txnValuePresent) {
			throw error("id_token does not contain txn claim.");
		}
		String txnValue = idTokenClaims.get("txn").getAsString();
		if (!StringUtils.hasText(txnValue)) {
			throw error("id_token contains an empty txn claim.");
		}

		logSuccess("id_token contained required FAPI2 claims.", args("txn", txnValue));

		return env;
	}
}
