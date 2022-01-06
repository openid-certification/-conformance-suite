package net.openid.conformance.openbanking_brasil.testmodules.opendata.utils;

import net.openid.conformance.condition.AbstractCondition;
import net.openid.conformance.testmodule.Environment;

public class PrepareToGetOpenDataApi extends AbstractCondition {

	@Override
	public Environment evaluate(Environment env) {
		String resourceUrl = env.getString("config", "resource.resourceUrl");
		env.putString("protected_resource_url", resourceUrl);
		return env;
	}
}
