-- Server-side JWT revocation, take two: comparing a stored cutoff instant
-- against the token's floored-to-seconds `iat` claim can never fully avoid
-- a race window right around the cutoff (see BUG_REPORT.txt #4). An
-- incrementing version number sidesteps clocks entirely - a token is valid
-- only if its embedded version still matches the user's current one.
ALTER TABLE app_user
    DROP COLUMN tokens_valid_after;

ALTER TABLE app_user
    ADD COLUMN token_version INTEGER NOT NULL DEFAULT 0;
