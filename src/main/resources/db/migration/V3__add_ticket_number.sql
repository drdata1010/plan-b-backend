-- Create sequence_generators table
CREATE TABLE sequence_generators (
    sequence_name VARCHAR(50) PRIMARY KEY,
    next_value BIGINT NOT NULL
);

-- Add ticket_number column to tickets table
ALTER TABLE tickets ADD COLUMN ticket_number VARCHAR(20) UNIQUE;

-- Initialize existing tickets with ticket numbers
WITH numbered_tickets AS (
    SELECT id, ROW_NUMBER() OVER (ORDER BY created_at) as row_num
    FROM tickets
)
UPDATE tickets t
SET ticket_number = CONCAT('TK-', nt.row_num)
FROM numbered_tickets nt
WHERE t.id = nt.id;

-- Insert initial sequence value (start with 2 since we'll have TK-1)
INSERT INTO sequence_generators (sequence_name, next_value)
VALUES ('TICKET_SEQUENCE', 2);

-- Add an index for faster lookups by ticket number
CREATE INDEX idx_tickets_ticket_number ON tickets(ticket_number);
