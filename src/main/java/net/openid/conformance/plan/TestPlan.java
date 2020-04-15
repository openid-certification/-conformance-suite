package net.openid.conformance.plan;

import net.openid.conformance.testmodule.TestModule;

import java.util.List;

/**
 * A collection of test modules intended to be run with a single
 * test configuration.
 */
public interface TestPlan {
	interface ProfileNames {
		String rptest = "Test a Relying Party / OAuth2 Client";
		String optest = "Test an OpenID Provider / Authorization Server";
	}

	class Variant {
		public final Class<? extends Enum<?>> variant;
		public final String value;
		public Variant(Class<? extends Enum<?>> variant, String value) {
			this.variant = variant;
			this.value = value;
		}
	}

	/**
	 * A holder for one or more test modules and the variants they should be run with
	 *
	 * 	A list of these is returned by testModulesWithVariants() {
	 */
	class ModuleListEntry {
		public final List<Class<? extends TestModule>> testModules;
		public final List<Variant> variant;

		public ModuleListEntry(List<Class<? extends TestModule>> testModules,
							   List<Variant> variant) {
			this.testModules = testModules;
			this.variant = variant;
		}
	}

	/* Instead of defined test modules in the @PublishTestModule annotation, TestPlans can implement the
	testModulesWithVariants() method, which allows them to define that test modules will be run with multiple
	variants:

	public static List<ModuleListEntry> testModulesWithVariants()

	*/

}
