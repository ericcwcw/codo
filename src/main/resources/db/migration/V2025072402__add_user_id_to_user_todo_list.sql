ALTER TABLE user_todo_list ADD COLUMN user_id UUID;

ALTER TABLE user_todo_list ADD CONSTRAINT fk_user_todo_list_user_id 
    FOREIGN KEY (user_id) REFERENCES "user"(id) ON DELETE CASCADE;

ALTER TABLE user_todo_list ADD CONSTRAINT uk_user_todo_list_user_list 
    UNIQUE (user_id, list_id);

CREATE INDEX idx_user_todo_list_user_id ON user_todo_list(user_id);
CREATE INDEX idx_user_todo_list_list_id ON user_todo_list(list_id);
