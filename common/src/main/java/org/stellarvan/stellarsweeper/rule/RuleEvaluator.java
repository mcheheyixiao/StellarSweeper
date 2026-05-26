package org.stellarvan.stellarsweeper.rule;

import java.util.Set;

public final class RuleEvaluator {
    private final CleanupRule exactItemRule;

    public RuleEvaluator(Set<String> activeItemIds) {
        ItemMatcher matcher = context -> activeItemIds.contains(context.itemId());
        this.exactItemRule = context -> matcher.matches(context) ? CleanupDecision.clean() : CleanupDecision.skip();
    }

    public CleanupDecision evaluate(CleanupContext context) {
        return exactItemRule.evaluate(context);
    }
}
