-- Update existing status values to match enum values
UPDATE todo_item SET status = 'TODO' WHERE status = 'PENDING' OR status = 'pending' OR status = 'To Do' OR status IS NULL;
UPDATE todo_item SET status = 'IN_PROGRESS' WHERE status = 'in_progress' OR status = 'In Progress' OR status = 'PROGRESS';
UPDATE todo_item SET status = 'COMPLETED' WHERE status = 'completed' OR status = 'Complete' OR status = 'DONE';
UPDATE todo_item SET status = 'CANCELLED' WHERE status = 'cancelled' OR status = 'canceled' OR status = 'Cancelled';

-- Set default status for any remaining null values
UPDATE todo_item SET status = 'TODO' WHERE status IS NULL OR status = '';

-- Add constraint to ensure only valid enum values
ALTER TABLE todo_item ADD CONSTRAINT check_status_enum 
CHECK (status IN ('TODO', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED'));
