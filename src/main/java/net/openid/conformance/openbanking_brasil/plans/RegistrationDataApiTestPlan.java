package net.openid.conformance.openbanking_brasil.plans;

import net.openid.conformance.openbanking_brasil.OBBProfile;
import net.openid.conformance.plan.PublishTestPlan;
import net.openid.conformance.plan.TestPlan;

@PublishTestPlan(
	testPlanName = "Registration Data api test",
	profile = OBBProfile.OBB_PROFILE,
	displayName = "Functional tests for registration data API (WIP)",
	summary = "Structural and logical tests for OpenBanking Brasil-conformant Registration Data API",
	testModules = {
		RegistrationDataApiTestModule.class
	})
public class RegistrationDataApiTestPlan implements TestPlan {
}
