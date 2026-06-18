// src/main/java/de/novateams/models/TeamRole.java
package de.novateams.models;

public enum TeamRole {
    MEMBER(0, "Mitglied"),
    MODERATOR(1, "Moderator"),
    CO_OWNER(2, "Co-Owner"),
    OWNER(3, "Owner");
    
    private final int level;
    private final String displayName;
    
    TeamRole(int level, String displayName) {
        this.level = level;
        this.displayName = displayName;
    }
    
    public int getLevel() { return level; }
    public String getDisplayName() { return displayName; }
    
    public boolean isAtLeast(TeamRole role) {
        return this.level >= role.level;
    }
    
    public TeamRole getNextHigher() {
        return switch (this) {
            case MEMBER -> MODERATOR;
            case MODERATOR -> CO_OWNER;
            case CO_OWNER -> OWNER;
            case OWNER -> OWNER;
        };
    }
    
    public TeamRole getNextLower() {
        return switch (this) {
            case OWNER -> CO_OWNER;
            case CO_OWNER -> MODERATOR;
            case MODERATOR -> MEMBER;
            case MEMBER -> MEMBER;
        };
    }
}
