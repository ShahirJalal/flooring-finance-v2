package com.flooring.finance.dto;

import com.flooring.finance.common.EntryCategory;

/** A description + category the owner has typed before, offered back as a one-tap suggestion. */
public record EntrySuggestion(String description, EntryCategory category) {
}
