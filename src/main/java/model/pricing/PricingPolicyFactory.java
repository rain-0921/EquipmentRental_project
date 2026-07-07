package model.pricing;

import java.util.HashMap;
import java.util.Map;

/**
 * Singleton registry that maps strategy keys (matching the
 * {@code equipment.pricing_strategy} ENUM in the DB) to concrete
 * {@link PricingPolicy} instances. Decouples the persistence layer
 * from concrete implementations and makes adding a new strategy
 * a single-class change.
 */
public final class PricingPolicyFactory {

    private static final PricingPolicyFactory INSTANCE = new PricingPolicyFactory();
    private final Map<String, PricingPolicy> registry = new HashMap<>();

    private PricingPolicyFactory() {
        register(new StandardPricing());
        register(new TieredPricing());
        register(new PromotionalPricing());
    }

    public static PricingPolicyFactory getInstance() {
        return INSTANCE;
    }

    private void register(PricingPolicy policy) {
        registry.put(policy.getStrategyKey(), policy);
    }

    /**
     * Look up a pricing strategy by its database key.
     *
     * @throws IllegalArgumentException if the key is unknown - this
     *         indicates either a data-integrity bug or a new strategy
     *         was added to the DB without a corresponding Java class.
     */
    public PricingPolicy fromKey(String key) {
        PricingPolicy policy = registry.get(key);
        if (policy == null) {
            throw new IllegalArgumentException(
                "Unknown pricing strategy: " + key
                + ". Known: " + registry.keySet());
        }
        return policy;
    }

    /** All registered strategies - used by admin tooling. */
    public Map<String, PricingPolicy> all() {
        return Map.copyOf(registry);
    }
}