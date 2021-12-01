package net.openid.conformance.openbanking_brasil.plans.productsNServicesApiTestPlans;


import net.openid.conformance.openbanking_brasil.OBBProfile;
import net.openid.conformance.openbanking_brasil.plans.PlanNames;
import net.openid.conformance.openbanking_brasil.testmodules.productsNServices.BusinessInvoiceFinancingsApiTestModule;
import net.openid.conformance.plan.PublishTestPlan;
import net.openid.conformance.plan.TestPlan;
import net.openid.conformance.variant.ClientAuthType;

import java.util.List;

@PublishTestPlan(
	testPlanName = "ProductsNServices - BusinessInvoiceFinancings API test plan",
	profile = OBBProfile.OBB_PROFIlE_PHASE1,
	displayName = PlanNames.BUSINESS_INVOICE_FINANCINGS_API_TEST_PLAN,
	summary = "Structural and logical tests for ProductsNServices - BusinessInvoiceFinancings API"
)
public class BusinessInvoiceFinancingsApiTestPlan implements TestPlan {
	public static List<ModuleListEntry> testModulesWithVariants() {
		return List.of(
			new ModuleListEntry(
				List.of(
					BusinessInvoiceFinancingsApiTestModule.class
				),
				List.of(
					new Variant(ClientAuthType.class, "none")
				)
			)
		);
	}
}
