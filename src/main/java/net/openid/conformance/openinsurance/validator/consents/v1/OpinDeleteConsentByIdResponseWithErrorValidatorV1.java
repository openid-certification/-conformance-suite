package net.openid.conformance.openinsurance.validator.consents.v1;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.openid.conformance.condition.PreEnvironment;
import net.openid.conformance.condition.client.jsonAsserting.AbstractJsonAssertingCondition;
import net.openid.conformance.logging.ApiName;
import net.openid.conformance.testmodule.Environment;
import net.openid.conformance.util.field.DatetimeField;
import net.openid.conformance.util.field.IntField;
import net.openid.conformance.util.field.ObjectArrayField;
import net.openid.conformance.util.field.ObjectField;
import net.openid.conformance.util.field.StringField;

/**
 * Api url: swagger/openinsurance/consents/v1/swagger-consents-api-v1.yaml
 * Api endpoint: /consents/{consentId}
 * Api version: 1.04
 **/
@ApiName("Delete Consent By Id V1")
public class OpinDeleteConsentByIdResponseWithErrorValidatorV1 extends AbstractJsonAssertingCondition {

	@Override
	@PreEnvironment(strings = "resource_endpoint_response")
	public Environment evaluate(Environment environment) {
		JsonElement body = bodyFrom(environment);

		assertField(body,
			new ObjectArrayField
				.Builder("errors")
				.setValidator(this::assertInnerFields)
				.setMinItems(1)
				.setMaxItems(13)
				.build());

		assertField(body,
			new ObjectField
				.Builder("meta")
				.setValidator(this::assertMeta)
				.setOptional()
				.build());

		return environment;
	}

	private void assertInnerFields(JsonObject body) {
		assertField(body,
			new StringField
				.Builder("code")
				.setPattern("[\\w\\W\\s]*")
				.setMaxLength(255)
				.build());

		assertField(body,
			new StringField
				.Builder("title")
				.setPattern("[\\w\\W\\s]*")
				.setMaxLength(255)
				.build());

		assertField(body,
			new StringField
				.Builder("detail")
				.setPattern("[\\w\\W\\s]*")
				.setMaxLength(2048)
				.build());
	}

	public void assertMeta(JsonObject meta) {
		assertField(meta,
			new IntField
				.Builder("totalRecords")
				.build());

		assertField(meta,
			new IntField
				.Builder("totalPages")
				.build());

		assertField(meta,
			new DatetimeField
				.Builder("requestDateTime")
				.setMaxLength(20)
				.build());
	}
}
