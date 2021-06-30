package net.openid.conformance.apis.creditOperations.advances;

import net.openid.conformance.apis.AbstractJsonResponseConditionUnitTest;
import net.openid.conformance.condition.ConditionError;
import net.openid.conformance.openbanking_brasil.creditOperations.advances.AdvancesContractInstallmentsResponseValidator;
import net.openid.conformance.util.UseResurce;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;

@UseResurce("jsonResponses/creditOperations/advances/installments/advancesInstallmentsResponse.json")
public class AdvancesContractInstallmentsResponseValidatorTest extends AbstractJsonResponseConditionUnitTest {

	@Test
	public void validateStructure() {
		AdvancesContractInstallmentsResponseValidator condition = new AdvancesContractInstallmentsResponseValidator();
		run(condition);
	}

	@Test
	@UseResurce("jsonResponses/creditOperations/advances/installments/advancesInstallmentsResponseWithError.json")
	public void validateStructureWithMissingField() {
		AdvancesContractInstallmentsResponseValidator condition = new AdvancesContractInstallmentsResponseValidator();
		ConditionError error = runAndFail(condition);
		assertThat(error.getMessage(), containsString(condition.createElementNotFoundMessage("typeContractRemaining")));
	}


	@Test
	@UseResurce("jsonResponses/creditOperations/advances/installments/advancesInstallmentsResponse(WrongEnum).json")
	public void validateStructureWrongEnum() {
		AdvancesContractInstallmentsResponseValidator condition = new AdvancesContractInstallmentsResponseValidator();
		ConditionError error = runAndFail(condition);
		assertThat(error.getMessage(), containsString(condition.createFieldValueNotMatchEnumerationMessage("typeNumberOfInstalments")));
	}

	@Test
	@UseResurce("jsonResponses/creditOperations/advances/installments/advancesInstallmentsResponse(WrongPattern).json")
	public void validateStructureWrongRegexp() {
		AdvancesContractInstallmentsResponseValidator condition = new AdvancesContractInstallmentsResponseValidator();
		ConditionError error = runAndFail(condition);
		assertThat(error.getMessage(), containsString(condition.createFieldValueNotMatchPatternMessage("dueDate")));
	}

	@Test
	@UseResurce("jsonResponses/creditOperations/advances/installments/advancesInstallmentsResponse(WrongMaxLength).json")
	public void validateStructureWrongMaxLength() {
		AdvancesContractInstallmentsResponseValidator condition = new AdvancesContractInstallmentsResponseValidator();
		ConditionError error = runAndFail(condition);
		assertThat(error.getMessage(), containsString(condition.createFieldValueIsMoreThanMaxLengthMessage("paidInstalments")));
	}
}
