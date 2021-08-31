package net.openid.conformance.openbanking_brasil.creditCard;

import com.google.common.collect.Sets;
import com.google.gson.JsonObject;
import net.openid.conformance.condition.PreEnvironment;
import net.openid.conformance.condition.client.AbstractJsonAssertingCondition;
import net.openid.conformance.logging.ApiName;
import net.openid.conformance.testmodule.Environment;
import net.openid.conformance.util.field.DatetimeField;
import net.openid.conformance.util.field.DoubleField;
import net.openid.conformance.util.field.IntField;
import net.openid.conformance.util.field.StringField;

import java.util.Set;

/**
 * This is validator for API-Cartão de Crédito | Transações de cartão de crédito
 * https://openbanking-brasil.github.io/areadesenvolvedor/#limites-de-cartao-de-credito
 */

@ApiName("Credit Card Accounts Transaction")
public class CreditCardAccountsTransactionResponseValidator extends AbstractJsonAssertingCondition {

	@Override
	@PreEnvironment(strings = "resource_endpoint_response")
	public Environment evaluate(Environment environment) {

		JsonObject body = bodyFrom(environment);
		assertHasField(body, ROOT_PATH);
		assertJsonArrays(body, ROOT_PATH, this::assertInnerFields);

		return environment;
	}

	private void assertInnerFields(JsonObject data) {
		Set<String> enumLineName = Sets.newHashSet("CREDITO_A_VISTA", "CREDITO_PARCELADO", "SAQUE_CREDITO_BRASIL", "SAQUE_CREDITO_EXTERIOR", "EMPRESTIMO_CARTAO_CONSIGNADO", "OUTROS");
		Set<String> enumCreditDebitType = Sets.newHashSet("CREDITO", "DEBITO");
		Set<String> enumTransactionType = Sets.newHashSet("PAGAMENTO", "TARIFA", "OPERACOES_CREDITO_CONTRATADAS_CARTAO", "ESTORNO", "CASHBACK", "OUTROS");
		Set<String> enumPaymentType = Sets.newHashSet("A_VISTA", "A_PRAZO");
		Set<String> enumFeeType = Sets.newHashSet("ANUIDADE", "SAQUE_CARTAO_BRASIL", "SAQUE_CARTAO_EXTERIOR", "AVALIACAO_EMERGENCIAL_CREDITO", "EMISSAO_SEGUNDA_VIA", "TARIFA_PAGAMENTO_CONTAS", "SMS", "OUTRA");
		Set<String> enumCreditsType = Sets.newHashSet("CREDITO_ROTATIVO", "PARCELAMENTO_FATURA", "EMPRESTIMO", "OUTROS");

		assertField(data,
			new StringField
				.Builder("transactionId")
				.setPattern("^[a-zA-Z0-9][a-zA-Z0-9\\-]{0,99}$")
				.setMaxLength(100)
				.setOptional()
				.build());

		assertField(data,
			new StringField
				.Builder("identificationNumber")
				.setPattern("^[a-zA-Z0-9][a-zA-Z0-9\\-]{0,99}$")
				.setMaxLength(100)
				.build());

		assertField(data,
			new StringField
				.Builder("lineName")
				.setEnums(enumLineName)
				.setOptional()
				.build());

		assertField(data,
			new StringField
				.Builder("transactionName")
				.setPattern("[\\w\\W\\s]*")
				.setMaxLength(100)
				.build());

		assertField(data,
			new StringField
				.Builder("billId")
				.setPattern("^[a-zA-Z0-9][a-zA-Z0-9\\-]{0,99}$")
				.setMaxLength(100)
				.setOptional()
				.build());

		assertField(data,
			new StringField
				.Builder("creditDebitType")
				.setMaxLength(7)
				.setEnums(enumCreditDebitType)
				.build());

		assertField(data,
			new StringField
				.Builder("transactionType")
				.setEnums(enumTransactionType)
				.build());

		assertField(data,
			new StringField
				.Builder("transactionalAdditionalInfo")
				.setMaxLength(140)
				.build());

		assertField(data,
			new StringField
				.Builder("paymentType")
				.setEnums(enumPaymentType)
				.build());

		assertField(data,
			new StringField
				.Builder("feeType")
				.setEnums(enumFeeType)
				.build());

		assertField(data,
			new StringField
				.Builder("feeTypeAdditionalInfo")
				.setMaxLength(140)
				.build());

		assertField(data,
			new StringField
				.Builder("otherCreditsType")
				.setEnums(enumCreditsType)
				.build());

		assertField(data,
			new StringField
				.Builder("otherCreditsAdditionalInfo")
				.setMaxLength(50)
				.build());

		assertField(data,
			new StringField
				.Builder("chargeIdentificator")
				.setMaxLength(50)
				.setPattern("\\w*\\W*")
				.build());

		assertField(data,
			new IntField
				.Builder("chargeNumber")
				.setMaxLength(2)
				.build());

		assertField(data,
			new DoubleField
				.Builder("chargeNumber")
				.setMaxLength(20)
				.build());

		assertField(data,
			new DoubleField
				.Builder("brazilianAmount")
				.setMaxLength(20)
				.build());

		assertField(data,
			new DoubleField
				.Builder("amount")
				.setMaxLength(20)
				.build());

		assertField(data,
			new StringField
				.Builder("currency")
				.setMaxLength(3)
				.setPattern("^(\\w{3}){1}$|^NA$")
				.build());

		assertField(data,
			new DatetimeField
				.Builder("transactionDate")
				.setPattern("^(\\d{4})-(1[0-2]|0?[1-9])-(3[01]|[12][0-9]|0?[1-9])$|^NA$")
				.build());

		assertField(data,
			new DatetimeField
				.Builder("billPostDate")
				.setPattern("^(\\d{4})-(1[0-2]|0?[1-9])-(3[01]|[12][0-9]|0?[1-9])$|^NA$")
				.build());

		assertField(data,
			new IntField
				.Builder("payeeMCC")
				.setNullable()
				.setMaxLength(4).build());
	}
}
