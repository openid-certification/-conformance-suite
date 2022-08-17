package net.openid.conformance.raidiam.validators.organisationDomainUsers;

import com.google.gson.JsonElement;
import net.openid.conformance.condition.client.jsonAsserting.AbstractJsonAssertingCondition;
import net.openid.conformance.logging.ApiName;
import net.openid.conformance.raidiam.validators.CommonParts;
import net.openid.conformance.testmodule.Environment;
import net.openid.conformance.util.field.ObjectArrayField;

/**
 * Api url: ****
 * Api endpoint: GET /organisations/{OrganisationId}/{AuthorisationDomainName}/users
 * Api git hash: ****
 *
 */
@ApiName("Raidiam Directory GET Organisation Domain Users")
public class GetOrganisationDomainUsersValidator extends AbstractJsonAssertingCondition {

	private final CommonParts parts;

	public GetOrganisationDomainUsersValidator() {
		parts = new CommonParts(this);
	}

	@Override
	public Environment evaluate(Environment environment) {
		JsonElement body = bodyFrom(environment);
		assertField(body,
			new ObjectArrayField
				.Builder("content")
				.setValidator(parts::organisationDomainUsersContent)
				.setOptional()
				.build());
		parts.assertDefaultResponseFields(body);
		return environment;
	}
}
