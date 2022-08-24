package net.openid.conformance.openinsurance.validator.insuranceAviation.v1;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.openid.conformance.condition.PreEnvironment;
import net.openid.conformance.condition.client.jsonAsserting.AbstractJsonAssertingCondition;
import net.openid.conformance.logging.ApiName;
import net.openid.conformance.openinsurance.validator.OpenInsuranceLinksAndMetaValidator;
import net.openid.conformance.testmodule.Environment;
import net.openid.conformance.util.SetUtils;
import net.openid.conformance.util.field.*;

import java.util.Set;

/**
 * Api Source: swagger/openinsurance/insuranceAviation/v1/swagger-insurance-aviation.yaml
 * Api endpoint: /{policyId}/premium
 * Api version: 1.0.0
 */

@ApiName("Insurance Aviation Premium V1")
public class OpinInsuranceAviationPremiumValidatorV1 extends AbstractJsonAssertingCondition {

	public static final Set<String> CODE = SetUtils.createSet("CASCO, RESPONSABILIDADE_CIVIL_FACULTATIVA, RESPONSABILIDADE_CIVIL_AEROPORTUARIA, RESPONSABILIDADE_DO_EXPLORADOR_E_TRANSPORTADOR_AEREO, OUTRAS");
	public static final Set<String> MOVEMENT_TYPE = SetUtils.createSet("LIQUIDACAO_DE_PREMIO, LIQUIDACAO_DE_RESTITUICAO_DE_PREMIO, LIQUIDACAO_DE_CUSTO_DE_AQUISICAO, LIQUIDACAO_DE_RESTITUICAO_DE_CUSTO_DE_AQUISICAO, ESTORNO_DE_PREMIO, ESTORNO_DE_RESTITUICAO_DE_PREMIO, ESTORNO_DE_CUSTO_DE_AQUISICAO, EMISSAO_DE_PREMIO, CANCELAMENTO_DE_PARCELA, EMISSAO_DE_RESTITUICAO_DE_PREMIO, REABERTURA_DE_PARCELA, BAIXA_POR_PERDA");
	public static final Set<String> MOVEMENT_ORIGIN = SetUtils.createSet("EMISSAO_DIRETA, EMISSAO_ACEITA_DE_COSSEGURO, EMISSAO_CEDIDA_DE_COSSEGURO");
	public static final Set<String> TELLERID_TYPE = SetUtils.createSet("CPF, CNPJ, OUTROS");
	public static final Set<String> PAYMENT_TYPE = SetUtils.createSet("BOLETO, TED, TEF, CARTAO, DOC, CHEQUE, DESCONTO_EM_FOLHA, PIX, DINHEIRO_EM_ESPECIE, OUTROS");
	private final OpenInsuranceLinksAndMetaValidator linksAndMetaValidator = new OpenInsuranceLinksAndMetaValidator(this);

	@Override
	@PreEnvironment(strings = "resource_endpoint_response")
	public Environment evaluate(Environment environment) {
		JsonElement body = bodyFrom(environment);
		assertField(body,
			new ObjectField
				.Builder(ROOT_PATH)
				.setValidator(this::assertData)
				.build());
		linksAndMetaValidator.assertMetaAndLinks(body);
		return environment;
	}

	private void assertData(JsonObject data) {
		assertField(data,
			new NumberField
				.Builder("paymentsQuantity")
				.setMaxLength(3)
				.build());

		assertField(data,
			new ObjectField
				.Builder("amount")
				.setValidator(this::assertAmount)
				.build());

		assertField(data,
			new ObjectArrayField
				.Builder("coverages")
				.setValidator(this::assertCoverages)
				.build());

		assertField(data,
			new ObjectArrayField
				.Builder("payments")
				.setValidator(this::assertPayments)
				.build());
	}

	private void assertPayments(JsonObject payments) {
		assertField(payments,
			new StringField
				.Builder("movementDate")
				.build());

		assertField(payments,
			new StringField
				.Builder("movementType")
				.setMaxLength(47)
				.setEnums(MOVEMENT_TYPE)
				.build());

		assertField(payments,
			new StringField
				.Builder("movementOrigin")
				.setMaxLength(27)
				.setEnums(MOVEMENT_ORIGIN)
				.setOptional()
				.build());

		assertField(payments,
			new DoubleField
				.Builder("movementPaymentsNumber")
				.setMaxLength(3)
				.build());

		assertField(payments,
			new ObjectField
				.Builder("amount")
				.setValidator(this::assertAmount)
				.build());

		assertField(payments,
			new StringField
				.Builder("maturityDate")
				.build());

		assertField(payments,
			new StringField
				.Builder("tellerId")
				.setMaxLength(1024)
				.setOptional()
				.build());

		assertField(payments,
			new StringField
				.Builder("tellerIdType")
				.setMaxLength(6)
				.setEnums(TELLERID_TYPE)
				.setOptional()
				.build());

		assertField(payments,
			new StringField
				.Builder("tellerName")
				.setMaxLength(100)
				.setOptional()
				.build());

		assertField(payments,
			new StringField
				.Builder("financialInstitutionCode")
				.setMaxLength(100)
				.setOptional()
				.build());

		assertField(payments,
			new StringField
				.Builder("paymentType")
				.setMaxLength(19)
				.setEnums(PAYMENT_TYPE)
				.setOptional()
				.build());
	}

	private void assertCoverages(JsonObject coverages) {
		assertField(coverages,
			new StringField
				.Builder("branch")
				.setMaxLength(4)
				.build());

		assertField(coverages,
			new StringField
				.Builder("code")
				.setMaxLength(52)
				.setEnums(CODE)
				.build());

		assertField(coverages,
			new StringField
				.Builder("description")
				.setMaxLength(500)
				.setOptional()
				.build());

		assertField(coverages,
			new ObjectField
				.Builder("premiumAmount")
				.setValidator(this::assertAmount)
				.build());
	}

	private void assertAmount(JsonObject data) {
		assertField(data,
			new DoubleField
				.Builder("amount")
				.setPattern("^\\d{1,16}\\.\\d{2}$")
				.build());

		assertField(data,
			new StringField
				.Builder("currency")
				.setPattern("^(\\w{3}){1}$")
				.build());
	}
}
