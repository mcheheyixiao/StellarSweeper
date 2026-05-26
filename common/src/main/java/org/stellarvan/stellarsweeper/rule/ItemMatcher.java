package org.stellarvan.stellarsweeper.rule;

@FunctionalInterface
public interface ItemMatcher {
    boolean matches(CleanupContext context);
}
