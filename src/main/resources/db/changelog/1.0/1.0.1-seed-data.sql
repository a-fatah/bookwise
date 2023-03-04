-- liquibase formatted sql
-- changeset abdul:insert_authors_seed_data

INSERT INTO authors (id, name) VALUES
                               (1, 'Harper Lee'),
                               (2, 'F. Scott Fitzgerald'),
                               (3, 'Jane Austen'),
                               (4, 'George Orwell'),
                               (5, 'J.D. Salinger'),
                               (6, 'Gabriel Garcia Marquez'),
                               (7, 'Aldous Huxley'),
                               (8, 'J.R.R. Tolkien'),
                               (9, 'Virginia Woolf'),
                               (10, 'Oscar Wilde'),
                               (11, 'William Shakespeare'),
                               (12, 'Mark Twain'),
                               (13, 'Ernest Hemingway'),
                               (14, 'Charles Dickens'),
                               (15, 'Agatha Christie'),
                               (16, 'Stephen King'),
                               (17, 'J.K. Rowling'),
                               (18, 'Harper Lee');

-- changeset abdul:insert_books_seed_data

INSERT INTO books (id, title, author_id, pages) VALUES (1, 'To Kill a Mockingbird', 1, 281);
INSERT INTO books (id, title, author_id, pages) VALUES (2, 'The Great Gatsby', 2, 180);
INSERT INTO books (id, title, author_id, pages) VALUES (3, 'Pride and Prejudice', 3, 279);
INSERT INTO books (id, title, author_id, pages) VALUES (4, '1984', 4, 328);
INSERT INTO books (id, title, author_id, pages) VALUES (5, 'The Catcher in the Rye', 5, 277);
INSERT INTO books (id, title, author_id, pages) VALUES (6, 'One Hundred Years of Solitude', 6, 417);
INSERT INTO books (id, title, author_id, pages) VALUES (7, 'Brave New World', 7, 311);
INSERT INTO books (id, title, author_id, pages) VALUES (8, 'The Lord of the Rings', 8, 1178);
INSERT INTO books (id, title, author_id, pages) VALUES (9, 'Mrs. Dalloway', 9, 216);
INSERT INTO books (id, title, author_id, pages) VALUES (10, 'The Picture of Dorian Gray', 10, 189);
INSERT INTO books (id, title, author_id, pages) VALUES (11, 'Hamlet', 11, 432);
INSERT INTO books (id, title, author_id, pages) VALUES (12, 'The Adventures of Huckleberry Finn', 12, 224);
INSERT INTO books (id, title, author_id, pages) VALUES (13, 'The Old Man and the Sea', 13, 127);
INSERT INTO books (id, title, author_id, pages) VALUES (14, 'A Tale of Two Cities', 14, 544);
INSERT INTO books (id, title, author_id, pages) VALUES (15, 'And Then There Were None', 15, 288);
INSERT INTO books (id, title, author_id, pages) VALUES (16, 'The Shining', 16, 447);
INSERT INTO books (id, title, author_id, pages) VALUES (17, 'Harry Potter and the Sorcerer''s Stone', 17, 309);
INSERT INTO books (id, title, author_id, pages) VALUES (18, 'Go Set a Watchman', 18, 281);
