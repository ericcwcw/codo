ALTER TABLE "user" ADD COLUMN email_verified BOOLEAN NOT NULL DEFAULT FALSE;

CREATE INDEX idx_user_email_verified ON "user" (email_verified);
