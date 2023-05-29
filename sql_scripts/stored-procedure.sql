-- Adds a new star regardless of whether there exists a star
-- with the same name and birth year
DROP PROCEDURE IF EXISTS add_star;
USE moviedb;

DELIMITER $$
CREATE PROCEDURE add_star(
    IN starName  VARCHAR(100),
    IN starBirthYear INT,
    OUT starId VARCHAR(10)
)
BEGIN
    DECLARE starIdInt INT DEFAULT 0;
    DECLARE starTwoChar VARCHAR(10);
	-- Get the maximum numeric part of the id, assuming the format 'nm<number>'
	SELECT max(id) INTO starTwoChar FROM stars;
    -- if table is empty otherwise proceed --
    IF starTwoChar IS NULL THEN
		SET starTwoChar = 'nm0000000';
	ELSE
    -- Increment the maximum numeric part to get the next id and add it to twochar
		SET starIdInt = CAST(SUBSTRING(starTwoChar, 3) AS UNSIGNED) + 1;
		SET starTwoChar = SUBSTRING(starTwoChar, 1, 2);
	END IF;
    
    SET starId = CONCAT(starTwoChar, starIdInt);
	-- Insert the new star into the table --
	INSERT INTO stars(id, name, birthyear)
	VALUES (starId, starName, starBirthYear);
	SELECT starId;
END $$
DELIMITER ;

-- This adds a movie conditional on whether or not it exists
-- it also adds new stars and genres depending on whether
-- they exist

DROP PROCEDURE IF EXISTS add_movie;
USE moviedb;

DELIMITER $$
CREATE PROCEDURE add_movie(IN movieTitle VARCHAR(100), IN movieYear INT, IN movieDirector VARCHAR(100),
                           IN starName VARCHAR(100), IN starBirthYear INT, IN movieGenre VARCHAR(32))
BEGIN
    DECLARE movieIdInt INT;
    DECLARE movieTwoChar VARCHAR(10);
    DECLARE newMovieId VARCHAR(10);
    DECLARE newGenreId INT;
    DECLARE newStarId VARCHAR(10);

		-- Check if movie exists, else create a new movie id and insert--
	SELECT id INTO newMovieId FROM movies
	WHERE title = movieTitle AND year = movieYear AND director = movieDirector;

	IF newMovieId IS NULL THEN

			-- get max id --
		SELECT max(id) INTO movieTwoChar FROM movies;
        -- cast the numerical portion of the string to int and increment --
		SET movieIdInt = CAST(SUBSTRING(movieTwoChar, 3) AS UNSIGNED) + 1;
        SET movieTwoChar = SUBSTRING(movieTwoChar, 1, 2);
		SET newMovieId = CONCAT(movieTwoChar, movieIdInt);

				-- insert new movie --
		INSERT INTO movies(id, title, year, director)
		VALUES (newMovieId, movieTitle, movieYear, movieDirector);

		-- Check if genre exists, if not insert and get the ID --
		SELECT id INTO newGenreId FROM genres WHERE name = movieGenre;
		IF newGenreId IS NULL THEN
					INSERT INTO genres(name) VALUES (movieGenre);
					SET newGenreId = last_insert_id();
		END IF;

						-- insert into movies_in_genres --
		INSERT INTO genres_in_movies(genreId, movieId)
		VALUES (newGenreId, newMovieId);

		-- Check if star exists, if not insert and get the ID --
		SELECT id INTO newStarId
		FROM stars WHERE name = starName;
		IF newStarId IS NULL THEN
					CALL add_star(starName, starBirthYear, @outstarId);
					SET newStarId = @outStarId;
		END IF;

				-- insert into stars_in_movies --
		INSERT INTO stars_in_movies(starId, moviesId)
		VALUES (newStarId, newMovieId);
	ELSE
			SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Movie already exists';
	END IF;
END $$
DELIMITER ;