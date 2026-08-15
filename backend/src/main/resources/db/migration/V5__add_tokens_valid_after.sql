-- Backs server-side logout: a token issued before this timestamp no longer
-- counts as authenticated, even if it hasn't naturally expired yet. NULL
-- (the default for every existing row) means "no restriction" - nobody's
-- current session gets force-logged-out just by this column existing.
ALTER TABLE app_user
    ADD COLUMN tokens_valid_after TIMESTAMP;
