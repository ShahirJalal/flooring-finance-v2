package com.flooring.finance.common;

/** What a job entry counts toward: income (the price side) or one of four cost buckets. */
public enum EntryCategory {
    INCOME,
    MATERIALS,
    WORKER,
    DELIVERY,
    OTHER
}
