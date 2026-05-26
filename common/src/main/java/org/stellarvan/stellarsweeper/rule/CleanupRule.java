package org.stellarvan.stellarsweeper.rule;

@FunctionalInterface
public interface CleanupRule {
    CleanupDecision evaluate(CleanupContext context);
}
