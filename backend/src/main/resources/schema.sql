-- Movie Recommender Database Schema
-- Defines all 10 core tables with proper constraints, foreign keys, and indexes.

CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS user_profiles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    birth_year SMALLINT NULL,
    subtitles_ok BOOLEAN NOT NULL DEFAULT FALSE,
    languages JSON NOT NULL,
    favorite_genres JSON NOT NULL,
    never_want_genres JSON NOT NULL,
    loved_title_ids JSON NULL,
    era_preference ENUM('mostly_newer', 'any_era', 'love_classics') NOT NULL DEFAULT 'any_era',
    CONSTRAINT fk_user_profiles_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS password_reset_tokens (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    token VARCHAR(255) NOT NULL UNIQUE,
    expires_at DATETIME NOT NULL,
    used BOOLEAN NOT NULL DEFAULT FALSE,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_password_reset_tokens_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_password_reset_tokens_token (token)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS genres (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tmdb_genre_id INT NOT NULL,
    name VARCHAR(50) NOT NULL,
    media_type ENUM('movie', 'tv') NOT NULL,
    UNIQUE KEY uk_genres_tmdb_media (tmdb_genre_id, media_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS keywords (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tmdb_keyword_id INT NOT NULL UNIQUE,
    name VARCHAR(50) NOT NULL,
    is_intensity_marker BOOLEAN NOT NULL DEFAULT FALSE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS titles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tmdb_id BIGINT NOT NULL UNIQUE,
    media_type ENUM('movie', 'tv') NOT NULL,
    title VARCHAR(255) NOT NULL,
    overview TEXT NULL,
    poster_path VARCHAR(255) NULL,
    backdrop_path VARCHAR(255) NULL,
    release_date DATE NULL,
    runtime_minutes INT NULL,
    series_status ENUM('ongoing', 'ended') NULL,
    original_language VARCHAR(10) NULL,
    vote_average DECIMAL(3,1) NULL,
    vote_count INT NULL DEFAULT 0,
    certification_raw VARCHAR(20) NULL,
    certification_band ENUM('U', 'UA7', 'UA13', 'UA16', 'A') NULL,
    genre_ids JSON NULL,
    keyword_ids JSON NULL,
    cast JSON NULL,
    imdb_rating DECIMAL(3,1) NULL,
    rotten_tomatoes_rating TINYINT NULL,
    metacritic_rating TINYINT NULL,
    cached_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    refreshed_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_titles_certification_band (certification_band),
    INDEX idx_titles_original_language (original_language),
    INDEX idx_titles_runtime_minutes (runtime_minutes),
    INDEX idx_titles_series_status (series_status),
    INDEX idx_titles_vote_average (vote_average),
    INDEX idx_titles_vote_count (vote_count),
    INDEX idx_titles_media_type (media_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS sessions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    media_type ENUM('movie', 'series', 'either') NOT NULL,
    group_size TINYINT NOT NULL,
    time_limit ENUM('under_90', '90_120', '2_2.5', 'no_limit', 'episode_or_two', 'binge', 'new_show') NOT NULL,
    occasion ENUM('friends_hangout', 'date_night', 'family_time', 'solo_chill') NULL,
    status ENUM('pending', 'completed', 'failed') NOT NULL DEFAULT 'pending',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_sessions_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_sessions_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS session_person_answers (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    session_id BIGINT NOT NULL,
    person_index TINYINT NOT NULL,
    name VARCHAR(100) NULL,
    age_band ENUM('under_7', '7_12', '13_15', '16_17', '18_plus') NULL,
    moods JSON NOT NULL,
    genres_up_for JSON NOT NULL,
    hard_no_genres JSON NOT NULL,
    languages_ok JSON NOT NULL,
    intensity ENUM('light', 'balanced', 'intense') NOT NULL DEFAULT 'balanced',
    something_like_title_id BIGINT NULL,
    CONSTRAINT fk_session_person_answers_session FOREIGN KEY (session_id) REFERENCES sessions(id) ON DELETE CASCADE,
    CONSTRAINT fk_session_person_answers_title FOREIGN KEY (something_like_title_id) REFERENCES titles(id) ON DELETE SET NULL,
    UNIQUE KEY uk_session_person (session_id, person_index)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS session_results (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    session_id BIGINT NOT NULL,
    title_id BIGINT NOT NULL,
    `rank` TINYINT NOT NULL,
    group_score DECIMAL(5,2) NOT NULL,
    badge ENUM('none', 'compromise', 'not_rated') NOT NULL DEFAULT 'none',
    ai_explanation TEXT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_session_results_session FOREIGN KEY (session_id) REFERENCES sessions(id) ON DELETE CASCADE,
    CONSTRAINT fk_session_results_title FOREIGN KEY (title_id) REFERENCES titles(id) ON DELETE CASCADE,
    UNIQUE KEY uk_session_title (session_id, title_id),
    INDEX idx_session_results_session_rank (session_id, `rank`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS session_result_person_scores (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    session_result_id BIGINT NOT NULL,
    person_index TINYINT NOT NULL,
    score DECIMAL(5,2) NOT NULL,
    CONSTRAINT fk_result_person_scores_result FOREIGN KEY (session_result_id) REFERENCES session_results(id) ON DELETE CASCADE,
    UNIQUE KEY uk_result_person (session_result_id, person_index)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Added after v1 schema: idempotent column add (MySQL has no ADD COLUMN IF NOT EXISTS)
SET @add_imdb_id = (SELECT IF(COUNT(*) = 0, 'ALTER TABLE titles ADD COLUMN imdb_id VARCHAR(20) NULL', 'SELECT 1') FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'titles' AND column_name = 'imdb_id');
PREPARE add_imdb_id_stmt FROM @add_imdb_id;
EXECUTE add_imdb_id_stmt;
DEALLOCATE PREPARE add_imdb_id_stmt;
