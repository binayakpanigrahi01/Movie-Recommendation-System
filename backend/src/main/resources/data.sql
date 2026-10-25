-- Movie Recommender Seed Data
-- Pre-populates standard TMDB movie & TV genres and required keywords (intensity markers & mood proxies)

-- 1. Standard TMDB Movie Genres
INSERT INTO genres (tmdb_genre_id, name, media_type) VALUES
    (28, 'Action', 'movie'),
    (12, 'Adventure', 'movie'),
    (16, 'Animation', 'movie'),
    (35, 'Comedy', 'movie'),
    (80, 'Crime', 'movie'),
    (99, 'Documentary', 'movie'),
    (18, 'Drama', 'movie'),
    (10751, 'Family', 'movie'),
    (14, 'Fantasy', 'movie'),
    (36, 'History', 'movie'),
    (27, 'Horror', 'movie'),
    (10402, 'Music', 'movie'),
    (9648, 'Mystery', 'movie'),
    (10749, 'Romance', 'movie'),
    (878, 'Science Fiction', 'movie'),
    (10770, 'TV Movie', 'movie'),
    (53, 'Thriller', 'movie'),
    (10752, 'War', 'movie'),
    (37, 'Western', 'movie')
ON DUPLICATE KEY UPDATE name = VALUES(name);

-- 2. Standard TMDB TV Genres
INSERT INTO genres (tmdb_genre_id, name, media_type) VALUES
    (10759, 'Action & Adventure', 'tv'),
    (16, 'Animation', 'tv'),
    (35, 'Comedy', 'tv'),
    (80, 'Crime', 'tv'),
    (99, 'Documentary', 'tv'),
    (18, 'Drama', 'tv'),
    (10751, 'Family', 'tv'),
    (10762, 'Kids', 'tv'),
    (9648, 'Mystery', 'tv'),
    (10763, 'News', 'tv'),
    (10764, 'Reality', 'tv'),
    (10765, 'Sci-Fi & Fantasy', 'tv'),
    (10766, 'Soap', 'tv'),
    (10767, 'Talk', 'tv'),
    (10768, 'War & Politics', 'tv'),
    (37, 'Western', 'tv')
ON DUPLICATE KEY UPDATE name = VALUES(name);

-- 3. Core Keywords: Intensity Markers (Q7) and Mood Proxies (TV Series)
INSERT INTO keywords (tmdb_keyword_id, name, is_intensity_marker) VALUES
    (10987, 'graphic violence', TRUE),
    (9826, 'gore', TRUE),
    (233, 'sexual violence', TRUE),
    (1264, 'torture', TRUE),
    (9799, 'horror', FALSE),
    (9840, 'romance', FALSE)
ON DUPLICATE KEY UPDATE name = VALUES(name), is_intensity_marker = VALUES(is_intensity_marker);
