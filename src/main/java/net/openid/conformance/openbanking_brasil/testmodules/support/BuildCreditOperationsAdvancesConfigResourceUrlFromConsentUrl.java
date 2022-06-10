package net.openid.conformance.openbanking_brasil.testmodules.support;

import net.openid.conformance.condition.AbstractCondition;
import net.openid.conformance.condition.PreEnvironment;
import net.openid.conformance.testmodule.Environment;

public class BuildCreditOperationsAdvancesConfigResourceUrlFromConsentUrl extends AbstractCondition {

	@Override
	@PreEnvironment(required = "config")
	public Environment evaluate(Environment env) {
		String consentUrl = env.getString("config","resource.consentUrl");
		String apiReplacement = "unarranged-accounts-overdraft";
		String endpointReplacement = "contracts";

		String validatorRegex = "^(https://)(.*?)(consents/v[0-9]/consents)";
		if(!consentUrl.matches(validatorRegex)) {
			throw error("consentUrl is not valid, please ensure that url matches " + validatorRegex, args("consentUrl", consentUrl));
		}

		String resourceUrl = consentUrl.replaceFirst("consents", apiReplacement).replaceFirst("consents", endpointReplacement);
		env.putString("config", "resource.resourceUrl", resourceUrl);

		logSuccess("resourceUrl for account set up", args("resourceUrl", resourceUrl));
		return env;
	}
}
