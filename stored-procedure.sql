-- This procedure adds a new star and returns the id in one call
DROP PROCEDURE IF EXISTS InsertStar;

USE moviedb;
DELIMITER $$
CREATE PROCEDURE InsertStar(
    IN star_name  VARCHAR(100),
    IN birth_year INT,
    OUT newStarId VARCHAR(10)
)
BEGIN
    DECLARE next_id INT DEFAULT 0;
    DECLARE new_id VARCHAR(10);

    -- Get the maximum numeric part of the id, assuming the format 'nm<number>'
SELECT CAST(SUBSTRING(max(id), 3) AS UNSIGNED) INTO next_id FROM stars;

-- Increment the maximum numeric part to get the next id and add it to nm
SET new_id = CONCAT('nm', IFNULL(next_id, 0) + 1);

	-- Insert the new star into the table
INSERT INTO stars(id, name, birthyear)
VALUES (new_id, star_name, birth_year);

SET newStarId = new_id;
SELECT newStarId;
END $$
DELIMITER ;