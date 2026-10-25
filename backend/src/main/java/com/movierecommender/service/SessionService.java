package com.movierecommender.service;

import com.movierecommender.dto.session.PersonAnswerRequest;
import com.movierecommender.dto.session.PersonScoreDto;
import com.movierecommender.dto.session.PersonSummaryDto;
import com.movierecommender.dto.session.ResultDto;
import com.movierecommender.dto.session.SessionResultsResponse;
import com.movierecommender.dto.session.SessionStartRequest;
import com.movierecommender.dto.session.SessionStartResponse;
import com.movierecommender.dto.session.SessionSubmitRequest;
import com.movierecommender.entity.Session;
import com.movierecommender.entity.SessionPersonAnswer;
import com.movierecommender.entity.SessionResult;
import com.movierecommender.entity.SessionResultPersonScore;
import com.movierecommender.entity.Title;
import com.movierecommender.entity.User;
import com.movierecommender.entity.UserProfile;
import com.movierecommender.entity.enums.AgeBand;
import com.movierecommender.entity.enums.EraPreference;
import com.movierecommender.entity.enums.ResultBadge;
import com.movierecommender.entity.enums.SessionStatus;
import com.movierecommender.exception.ApiException;
import com.movierecommender.repository.SessionPersonAnswerRepository;
import com.movierecommender.repository.SessionRepository;
import com.movierecommender.repository.SessionResultPersonScoreRepository;
import com.movierecommender.repository.SessionResultRepository;
import com.movierecommender.repository.TitleRepository;
import com.movierecommender.repository.UserProfileRepository;
import com.movierecommender.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class SessionService {

    private final SessionRepository sessionRepository;
    private final SessionPersonAnswerRepository sessionPersonAnswerRepository;
    private final SessionResultRepository sessionResultRepository;
    private final SessionResultPersonScoreRepository sessionResultPersonScoreRepository;
    private final TitleRepository titleRepository;
    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final MatchingEngineService matchingEngineService;
    private final GroqExplanationService groqExplanationService;

    @Transactional
    public SessionStartResponse start(Long userId, SessionStartRequest request) {
        User user = getUser(userId);
        Session session = Session.builder()
                .user(user)
                .mediaType(request.mediaType())
                .groupSize(request.groupSize())
                .timeLimit(request.timeLimit())
                .occasion(request.occasion())
                .build();
        session = sessionRepository.save(session);
        return new SessionStartResponse(session.getId());
    }

    @Transactional
    public SessionResultsResponse submit(Long userId, Long sessionId, SessionSubmitRequest request) {
        Session session = getOwnedSession(userId, sessionId);
        if (session.getStatus() == SessionStatus.COMPLETED) {
            return getResults(userId, sessionId);
        }
        if (request.people().size() != session.getGroupSize()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Number of people must match the session's group size.");
        }

        AgeBand hostAgeBand = resolveHostAgeBand(userId);
        EraPreference hostEra = resolveHostEraPreference(userId);
        List<Long> hostLovedTitleIds = resolveHostLovedTitleIds(userId);

        List<SessionPersonAnswer> people = new ArrayList<>();
        for (PersonAnswerRequest p : request.people()) {
            people.add(SessionPersonAnswer.builder()
                    .session(session)
                    .personIndex(p.personIndex())
                    .name(p.name())
                    .ageBand(p.personIndex() == 0 ? hostAgeBand : p.ageBand())
                    .moods(p.moods())
                    .genresUpFor(p.genresUpFor())
                    .hardNoGenres(p.hardNoGenres())
                    .languagesOk(p.languagesOk())
                    .intensity(p.intensity())
                    .somethingLikeTitle(p.somethingLikeTitleId() == null ? null : titleRepository.findById(p.somethingLikeTitleId()).orElse(null))
                    .build());
        }
        sessionPersonAnswerRepository.saveAll(people);

        List<MatchingEngineService.MatchResult> matches = matchingEngineService.match(session, people, hostEra, hostLovedTitleIds, Set.of());
        persistResults(session, people, matches.stream().limit(5).toList(), 1);

        session.setStatus(SessionStatus.COMPLETED);
        sessionRepository.save(session);

        return buildResponse(session, people);
    }

    @Transactional
    public SessionResultsResponse more(Long userId, Long sessionId) {
        Session session = getOwnedSession(userId, sessionId);
        List<SessionPersonAnswer> people = sessionPersonAnswerRepository.findBySessionIdOrderByPersonIndex(sessionId);
        if (people.isEmpty()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Session hasn't been submitted yet.");
        }

        EraPreference hostEra = resolveHostEraPreference(userId);
        List<Long> hostLovedTitleIds = resolveHostLovedTitleIds(userId);
        Set<Long> excludeTitleIds = new HashSet<>(sessionResultRepository.findTitleIdsBySessionId(sessionId));
        List<MatchingEngineService.MatchResult> matches = matchingEngineService.match(session, people, hostEra, hostLovedTitleIds, excludeTitleIds);

        int nextRank = sessionResultRepository.findBySessionIdOrderByRankAsc(sessionId).size() + 1;
        persistResults(session, people, matches.stream().limit(5).toList(), nextRank);

        return buildResponse(session, people);
    }

    @Transactional(readOnly = true)
    public SessionResultsResponse getResults(Long userId, Long sessionId) {
        Session session = getOwnedSession(userId, sessionId);
        List<SessionPersonAnswer> people = sessionPersonAnswerRepository.findBySessionIdOrderByPersonIndex(sessionId);
        return buildResponse(session, people);
    }

    private void persistResults(Session session, List<SessionPersonAnswer> people, List<MatchingEngineService.MatchResult> matches, int startRank) {
        Map<Long, String> explanations = groqExplanationService.explain(matches, session, people);
        int rank = startRank;
        for (MatchingEngineService.MatchResult match : matches) {
            SessionResult unsaved = SessionResult.builder()
                    .session(session)
                    .title(match.title())
                    .rank(rank++)
                    .groupScore(match.groupScore())
                    .badge(resolveBadge(match))
                    .aiExplanation(explanations.get(match.title().getId()))
                    .build();
            SessionResult saved = sessionResultRepository.save(unsaved);

            List<SessionResultPersonScore> scores = match.personScores().entrySet().stream()
                    .map(e -> SessionResultPersonScore.builder()
                            .sessionResult(saved)
                            .personIndex(e.getKey())
                            .score(e.getValue())
                            .build())
                    .toList();
            sessionResultPersonScoreRepository.saveAll(scores);
        }
    }

    private ResultBadge resolveBadge(MatchingEngineService.MatchResult match) {
        if (match.title().getCertificationBand() == null) return ResultBadge.NOT_RATED;
        return match.compromise() ? ResultBadge.COMPROMISE : ResultBadge.NONE;
    }

    private SessionResultsResponse buildResponse(Session session, List<SessionPersonAnswer> people) {
        List<PersonSummaryDto> personDtos = people.stream()
                .map(p -> new PersonSummaryDto(p.getPersonIndex(), p.getName()))
                .toList();
        List<ResultDto> resultDtos = sessionResultRepository.findBySessionIdOrderByRankAsc(session.getId()).stream()
                .map(this::toResultDto)
                .toList();
        return new SessionResultsResponse(session.getId(), personDtos, resultDtos);
    }

    private ResultDto toResultDto(SessionResult r) {
        Title t = r.getTitle();
        List<PersonScoreDto> scores = sessionResultPersonScoreRepository.findBySessionResultId(r.getId()).stream()
                .map(s -> new PersonScoreDto(s.getPersonIndex(), s.getScore()))
                .toList();
        Integer year = t.getReleaseDate() == null ? null : t.getReleaseDate().getYear();
        return new ResultDto(r.getRank(), t.getId(), t.getTitle(), t.getPosterPath(), t.getMediaType(), year,
                t.getRuntimeMinutes(), t.getCertificationRaw(), t.getCertificationBand(),
                r.getGroupScore(), r.getBadge(), r.getAiExplanation(), scores);
    }

    private AgeBand resolveHostAgeBand(Long userId) {
        return userProfileRepository.findByUserId(userId)
                .map(UserProfile::getBirthYear)
                .map(AgeBandMapper::fromBirthYear)
                .orElse(null);
    }

    private EraPreference resolveHostEraPreference(Long userId) {
        return userProfileRepository.findByUserId(userId)
                .map(UserProfile::getEraPreference)
                .orElse(EraPreference.ANY_ERA);
    }

    private List<Long> resolveHostLovedTitleIds(Long userId) {
        return userProfileRepository.findByUserId(userId)
                .map(UserProfile::getLovedTitleIds)
                .orElse(List.of());
    }

    private Session getOwnedSession(Long userId, Long sessionId) {
        return sessionRepository.findByIdAndUserId(sessionId, userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "SESSION_NOT_FOUND", "Session not found."));
    }

    private User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "AUTH_INVALID", "User not found."));
    }
}
