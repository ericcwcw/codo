-- Add user_id column to user_todo_list table for collaborators feature
ALTER TABLE user_todo_list ADD COLUMN user_id UUID;

-- Add foreign key constraint
ALTER TABLE user_todo_list ADD CONSTRAINT fk_user_todo_list_user_id 
    FOREIGN KEY (user_id) REFERENCES "user"(id) ON DELETE CASCADE;

-- Add unique constraint to prevent duplicate collaborations
ALTER TABLE user_todo_list ADD CONSTRAINT uk_user_todo_list_user_list 
    UNIQUE (user_id, list_id);

-- Create index for better query performance
CREATE INDEX idx_user_todo_list_user_id ON user_todo_list(user_id);
CREATE INDEX idx_user_todo_list_list_id ON user_todo_list(list_id);
