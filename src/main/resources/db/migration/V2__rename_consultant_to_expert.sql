-- Rename consultations table to expert_sessions
ALTER TABLE consultations RENAME TO expert_sessions;

-- Rename consultation_id column to expert_session_id in chat_sessions table
ALTER TABLE chat_sessions RENAME COLUMN consultation_id TO expert_session_id;

-- Update indexes
ALTER INDEX idx_consultation_user RENAME TO idx_expert_session_user;
ALTER INDEX idx_consultation_expert RENAME TO idx_expert_session_expert;
ALTER INDEX idx_consultation_ticket RENAME TO idx_expert_session_ticket;
ALTER INDEX idx_consultation_status RENAME TO idx_expert_session_status;
ALTER INDEX idx_consultation_scheduled_at RENAME TO idx_expert_session_scheduled_at;

-- Update foreign key constraints
ALTER TABLE chat_sessions DROP CONSTRAINT IF EXISTS fk_chat_sessions_consultation;
ALTER TABLE chat_sessions ADD CONSTRAINT fk_chat_sessions_expert_session FOREIGN KEY (expert_session_id) REFERENCES expert_sessions(id);

-- Update session_type values in chat_sessions table
UPDATE chat_sessions SET session_type = 'EXPERT_SESSION' WHERE session_type = 'CONSULTATION';
