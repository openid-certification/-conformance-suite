package io.fintechlabs.testframework.variant;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.flatMapping;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Stream;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.stereotype.Component;

import com.google.common.collect.Sets;

import io.fintechlabs.testframework.plan.PublishTestPlan;
import io.fintechlabs.testframework.plan.TestPlan;
import io.fintechlabs.testframework.testmodule.PublishTestModule;
import io.fintechlabs.testframework.testmodule.TestModule;
import io.fintechlabs.testframework.testmodule.Variant;

@Component
public class VariantService {

	// TODO: Remove (most) legacy-support code once all test modules have been converted

	private static final String SEARCH_PACKAGE = "io.fintechlabs";

	private final Map<Class<?>, ParameterHolder<? extends Enum<?>>> variantParametersByClass;
	private final Map<String, ParameterHolder<? extends Enum<?>>> variantParametersByName;
	private final Map<Class<?>, TestModuleHolder> testModulesByClass;
	private final SortedMap<String, TestModuleHolder> testModulesByName;
	private final SortedMap<String, TestPlanHolder> testPlansByName;

	public VariantService() {
		this.variantParametersByClass = inClassesWithAnnotation(VariantParameter.class)
				.collect(toMap(identity(), c -> wrapParameter(c)));

		this.variantParametersByName = variantParametersByClass.values().stream()
				.collect(toMap(p -> p.name, identity()));

		this.testModulesByClass = inClassesWithAnnotation(PublishTestModule.class)
				.collect(toMap(identity(), c -> wrapModule(c)));

		this.testModulesByName = testModulesByClass.values().stream()
				.collect(toSortedMap(m -> m.info.testName(), identity()));

		this.testPlansByName = inClassesWithAnnotation(PublishTestPlan.class)
				.map(c -> wrapPlan(c))
				.collect(toSortedMap(holder -> holder.info.testPlanName(), identity()));
	}

	public TestPlanHolder getTestPlan(String name) {
		return testPlansByName.get(name);
	}

	public Collection<TestPlanHolder> getTestPlans() {
		return testPlansByName.values();
	}

	public TestModuleHolder getTestModule(String name) {
		return testModulesByName.get(name);
	}

	public Collection<TestModuleHolder> getTestModules() {
		return testModulesByName.values();
	}

	private ParameterHolder<? extends Enum<?>> parameter(Class<?> c) {
		ParameterHolder<? extends Enum<?>> p = variantParametersByClass.get(c);
		if (p == null) {
			throw new IllegalArgumentException("Not a variant parameter: " + c.getName());
		}
		return p;
	}

	private ParameterHolder<? extends Enum<?>> parameter(String name) {
		ParameterHolder<? extends Enum<?>> p = variantParametersByName.get(name);
		if (p == null) {
			throw new IllegalArgumentException("Not a variant parameter: " + name);
		}
		return p;
	}

	private Map<ParameterHolder<? extends Enum<?>>, ? extends Enum<?>> typedVariant(VariantSelection variant) {
		return variant.getVariant().entrySet().stream()
				.map(e -> {
					ParameterHolder<?> p = parameter(e.getKey());
					return Map.entry(p, p.valueOf(e.getValue()));
				})
				.collect(toOrderedMap(Map.Entry::getKey, Map.Entry::getValue));
	}

	private static Stream<Class<?>> inClassesWithAnnotation(Class<? extends Annotation> annotationClass) {
		ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);
		scanner.addIncludeFilter(new AnnotationTypeFilter(annotationClass));
		Stream.Builder<Class<?>> builder = Stream.builder();
		try {
			for (BeanDefinition bd : scanner.findCandidateComponents(SEARCH_PACKAGE)) {
				builder.accept(Class.forName(bd.getBeanClassName()));
			}
		} catch (ClassNotFoundException e) {
			// Not expected to happen
			throw new RuntimeException("Error loading class", e);
		}
		return builder.build();
	}

	private static <A extends Annotation> Stream<A> inCombinedAnnotations(
			Class<? extends TestModule> testClass,
			Class<A> annotationClass) {

		// Walk the class hierarchy and collect annotations - we do this because
		// combining @Repeatable with @Inherited doesn't give all annotations (in general).

		LinkedList<Class<?>> classes = new LinkedList<Class<?>>();
		for (Class<?> c = testClass; TestModule.class.isAssignableFrom(c); c = c.getSuperclass()) {
			classes.addFirst(c);
		}

		return classes.stream()
				.flatMap(c -> Arrays.stream(c.getDeclaredAnnotationsByType(annotationClass)));
	}

	private static <T, K, U> Collector<T, ?, LinkedHashMap<K, U>> toOrderedMap(
			Function<? super T, ? extends K> keyMapper,
			Function<? super T, ? extends U> valueMapper) {

		return Collector.of(LinkedHashMap::new,
				(m, t) -> m.put(keyMapper.apply(t), valueMapper.apply(t)),
				(m, r) -> { m.putAll(r); return m; });
	}

	private static <T, K, U> Collector<T, ?, SortedMap<K, U>> toSortedMap(
			Function<? super T, ? extends K> keyMapper,
			Function<? super T, ? extends U> valueMapper) {

		return Collector.of(TreeMap::new,
				(m, t) -> m.put(keyMapper.apply(t), valueMapper.apply(t)),
				(m, r) -> { m.putAll(r); return m; });
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private ParameterHolder<?> wrapParameter(Class<?> c) {
		if (!c.isEnum()) {
			throw new IllegalArgumentException("Variant parameters must be enums: " + c.getName());
		}
		return new ParameterHolder(c);
	}

	private TestModuleHolder wrapModule(Class<?> c) {
		if (!TestModule.class.isAssignableFrom(c)) {
			throw new RuntimeException("PublishTestModule annotation applied to a class which is not a test module: " + c.getName());
		}
		return new TestModuleHolder(c.asSubclass(TestModule.class));
	}

	private TestPlanHolder wrapPlan(Class<?> c) {
		if (!TestPlan.class.isAssignableFrom(c)) {
			throw new RuntimeException("PublishTestPlan annotation applied to a class which is not a test plan: " + c.getName());
		}
		return new TestPlanHolder(c.asSubclass(TestPlan.class));
	}

	public static class ParameterHolder<T extends Enum<T>> {

		public final String name;

		final Class<T> parameterClass;
		final Map<String, T> valuesByString;

		ParameterHolder(Class<T> parameterClass) {
			this.parameterClass = parameterClass;
			this.name = parameterClass.getAnnotation(VariantParameter.class).value();
			this.valuesByString = values().stream().collect(toMap(T::toString, identity()));
		}

		// We compare against the toString() value of each constant, so that variant values can include spaces etc.
		T valueOf(String s) {
			T v = valuesByString.get(s);
			if (v == null) {
				throw new IllegalArgumentException(String.format("Illegal value for variant parameter %s: \"%s\"", name, s));
			}
			return v;
		}

		List<T> values() {
			return List.of(parameterClass.getEnumConstants());
		}

	}

	public class TestPlanHolder {

		public final PublishTestPlan info;

		final List<TestModuleHolder> modules;
		final Class<? extends TestPlan> planClass;
		final List<String> legacyVariants;

		TestPlanHolder(Class<? extends TestPlan> planClass) {
			this.planClass = planClass;
			this.info = planClass.getDeclaredAnnotation(PublishTestPlan.class);
			this.legacyVariants = List.of(info.variants());
			this.modules = Arrays.stream(info.testModules())
					.map(c -> {
						TestModuleHolder m = testModulesByClass.get(c);
						if (m == null) {
							throw new RuntimeException(String.format("In annotation for %s: not a published test module: %s",
									planClass.getSimpleName(),
									c.getName()));
						}
						return m;
					})
					.collect(toList());
		}

		public List<String> getTestModules() {
			return modules.stream().map(m -> m.info.testName()).collect(toList());
		}

		public List<String> getTestModulesForVariant(VariantSelection variant) {
			if (variant.isLegacyVariant()) {
				String v = variant.getLegacyVariant();
				return modules.stream()
						.filter(m -> m.isApplicableForLegacyVariant(v))
						.map(m -> m.info.testName())
						.collect(toList());
			}
			Map<ParameterHolder<? extends Enum<?>>, ? extends Enum<?>> v = typedVariant(variant);
			return modules.stream()
					.filter(m -> m.isApplicableForVariant(v))
					.map(m -> m.info.testName())
					.collect(toList());
		}

		public Object getVariantSummary() {
			// TODO: modify API to return info on multidimensional variants
			if (!legacyVariants.isEmpty()) {
				Map<String, Set<String>> fields =
						modules.stream()
						.flatMap(m -> m.legacyConfigurationFields.entrySet().stream())
						.collect(groupingBy(e -> e.getKey(),
								flatMapping(e -> e.getValue().stream(),
										toSet())));
				return legacyVariants.stream()
						.map(v -> Map.of(
								"name", v,
								"configurationFields", fields.getOrDefault(v, Set.of())))
						.collect(toList());
			}
			return List.of();
		}

	}

	public class TestModuleHolder {

		public final PublishTestModule info;

		final Class<? extends TestModule> moduleClass;
		final Set<TestModuleVariantInfo<? extends Enum<?>>> parameters;
		final Map<String, List<String>> legacyConfigurationFields;
		final Map<String, Method> legacySetupMethods;

		TestModuleHolder(Class<? extends TestModule> moduleClass) {
			this.moduleClass = moduleClass;
			this.info = moduleClass.getDeclaredAnnotation(PublishTestModule.class);

			List<ParameterHolder<?>> declaredParameters = inCombinedAnnotations(moduleClass, VariantParameters.class)
					.flatMap(a -> Arrays.stream(a.value()))
					.map(c -> parameter(c))
					.collect(toList());

			Map<Class<?>, ParameterHolder<?>> declaredParametersByClass = declaredParameters.stream()
					.collect(toMap(c -> c.parameterClass, identity()));

			Function<Class<?>, ParameterHolder<?>> moduleParameter = c -> {
				ParameterHolder<?> p = declaredParametersByClass.get(c);
				if (p == null) {
					throw new IllegalArgumentException(String.format("In annotation for %s: not a declared variant parameter: %s",
							moduleClass.getSimpleName(),
							c.getName()));
				}
				return p;
			};

			Map<ParameterHolder<?>, Set<String>> allValuesNotApplicable =
					inCombinedAnnotations(moduleClass, VariantNotApplicable.class)
					.collect(groupingBy(a -> moduleParameter.apply(a.parameter()),
							flatMapping(a -> Arrays.stream(a.values()), toSet())));

			Map<ParameterHolder<?>, Map<String, List<String>>> allConfigurationFields =
					inCombinedAnnotations(moduleClass, VariantConfigurationFields.class)
					.collect(groupingBy(a -> moduleParameter.apply(a.parameter()),
							groupingBy(VariantConfigurationFields::value,
									flatMapping(a -> Arrays.stream(a.configurationFields()),
											toList()))));

			Map<ParameterHolder<?>, Map<String, List<Method>>> allSetupMethods =
					Arrays.stream(moduleClass.getMethods())
					.filter(m -> m.isAnnotationPresent(VariantSetup.class))
					.map(m -> Map.entry(m.getAnnotation(VariantSetup.class), m))
					.collect(groupingBy(e -> moduleParameter.apply(e.getKey().parameter()),
							groupingBy(e -> e.getKey().value(),
									mapping(e -> e.getValue(), toList()))));

			this.parameters = declaredParameters.stream()
					.map(p -> new TestModuleVariantInfo<>(
							p,
							allValuesNotApplicable.getOrDefault(p, Set.of()),
							allConfigurationFields.getOrDefault(p, Map.of()),
							allSetupMethods.getOrDefault(p, Map.of())))
					.collect(toSet());

			List<Method> legacyMethods =
					Arrays.stream(moduleClass.getDeclaredMethods())
					.filter(m -> m.isAnnotationPresent(Variant.class))
					.collect(toList());

			this.legacyConfigurationFields = legacyMethods.stream()
					.map(m -> m.getAnnotation(Variant.class))
					.collect(toOrderedMap(Variant::name, a -> List.of(a.configurationFields())));

			this.legacySetupMethods = legacyMethods.stream()
					.collect(toOrderedMap(m -> m.getAnnotation(Variant.class).name(), identity()));
		}

		public boolean isApplicableForVariant(VariantSelection variant) {
			if (variant.isLegacyVariant()) {
				return isApplicableForLegacyVariant(variant.getLegacyVariant());
			}
			return isApplicableForVariant(typedVariant(variant));
		}

		boolean isApplicableForVariant(Map<ParameterHolder<? extends Enum<?>>, ? extends Enum<?>> variant) {
			return parameters.stream()
					.allMatch(p -> {
						Object v = variant.get(p.parameter);
						return v != null && p.allowedValues.contains(v);
					});
		}

		boolean isApplicableForLegacyVariant(String variant) {
			return legacySetupMethods.containsKey(variant);
		}

		public Object getVariantSummary() {
			// TODO: modify API to return info on multidimensional variants
			if (!legacySetupMethods.isEmpty()) {
				return legacyConfigurationFields.entrySet().stream()
						.map(e -> Map.of(
								"name", e.getKey(),
								"configurationFields", e.getValue()))
						.collect(toList());
			}
			return List.of();
		}

		public TestModule newInstance(VariantSelection variant) {
			if (!legacySetupMethods.isEmpty()) {
				if (!variant.isLegacyVariant()) {
					throw new RuntimeException("Only legacy variants supported for test: " + info.testName());
				}
				return newLegacyInstance(variant.getLegacyVariant());
			}

			Map<ParameterHolder<? extends Enum<?>>, ? extends Enum<?>> typedVariant = typedVariant(variant);

			// Validate the supplied parameters

			Set<ParameterHolder<?>> declaredParameters = parameters.stream().map(p -> p.parameter).collect(toSet());

			Set<ParameterHolder<?>> missingParameters = Sets.difference(declaredParameters, typedVariant.keySet());
			if (!missingParameters.isEmpty()) {
				throw new IllegalArgumentException("Missing values for required variant parameters: " +
						missingParameters.stream().map(p -> p.name).collect(joining(", ")));
			}

			// Note: supplying extra variant parameters is not an error

			parameters.forEach(p -> {
				Object v = typedVariant.get(p.parameter);
				if (!p.allowedValues.contains(v)) {
					throw new RuntimeException(String.format("Not an allowed value for variant parameter %s: %s",
							p.parameter.name,
							v));
				}
			});

			// Create the module
			TestModule module;
			try {
				module = moduleClass.getDeclaredConstructor().newInstance();
			} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
					| InvocationTargetException | NoSuchMethodException | SecurityException e) {
				throw new RuntimeException("Couldn't create test module", e);
			}

			module.setVariant(typedVariant.entrySet().stream()
					.collect(toMap(e -> e.getKey().parameterClass, e -> e.getValue())));

			// Invoke any setup methods for the configured variant
			try {
				for (TestModuleVariantInfo<?> p : parameters) {
					for (Method setup : p.setupMethods.getOrDefault(typedVariant.get(p.parameter), List.of())) {
						setup.invoke(module);
					}
				}
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				throw new RuntimeException("Failed to initialize test module: " + info.testName(), e);
			}

			return module;
		}

		private TestModule newLegacyInstance(String variant) {
			Method setup = legacySetupMethods.get(variant);
			if (setup == null) {
				throw new IllegalArgumentException("Not a recognized variant: " + variant);
			}

			try {
				TestModule module = moduleClass.getDeclaredConstructor().newInstance();
				module.setVariant(Map.of());
				setup.invoke(module);
				return module;
			} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
					| InvocationTargetException | NoSuchMethodException | SecurityException e) {
				throw new RuntimeException("Couldn't create test module", e);
			}
		}

	}

	class TestModuleVariantInfo<T extends Enum<T>> {

		final ParameterHolder<T> parameter;
		final Set<T> allowedValues;
		final Map<T, List<String>> configurationFields;
		final Map<T, List<Method>> setupMethods;

		TestModuleVariantInfo(
				ParameterHolder<T> parameter,
				Set<String> valuesNotApplicable,
				Map<String, List<String>> configurationFields,
				Map<String, List<Method>> setupMethods) {

			this.parameter = parameter;

			this.allowedValues = EnumSet.allOf(parameter.parameterClass);
			valuesNotApplicable.forEach(s -> this.allowedValues.remove(parameter.valueOf(s)));

			this.configurationFields = configurationFields.entrySet().stream()
					.collect(toMap(e -> parameter.valueOf(e.getKey()), e -> e.getValue()));

			this.setupMethods = setupMethods.entrySet().stream()
					.collect(toMap(e -> parameter.valueOf(e.getKey()), e -> e.getValue()));

			// Sanity-check the setup methods
			setupMethods.values().stream()
					.flatMap(List::stream)
					.forEach(m -> {
							if (!Modifier.isPublic(m.getModifiers())) {
								throw new RuntimeException("Variant setup methods must be public: " + m);
							}

							if (m.getParameterCount() != 0) {
								throw new RuntimeException("Variant setup methods cannot take parameters: " + m);
							}
					});
		}

	}

}
