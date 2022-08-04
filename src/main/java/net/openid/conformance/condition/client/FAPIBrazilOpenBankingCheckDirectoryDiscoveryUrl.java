package net.openid.conformance.condition.client;

import net.openid.conformance.condition.AbstractCondition;
import net.openid.conformance.condition.PreEnvironment;
import net.openid.conformance.testmodule.Environment;

public class FAPIBrazilOpenBankingCheckDirectoryDiscoveryUrl extends AbstractFAPIBrazilCheckDirectoryDiscoveryUrl {

	@Override
	String getErrorMessage() {
		return "Testing for Brazil certification must be done using the Brazil directory. If you do not have access to the directory an example client is available in the conformance suite instructions.";
	}

	@Override
	String getExpectedUrl() {
		return "https://auth.sandbox.directory.openbankingbrasil.org.br/.well-known/openid-configuration";
	}

}
