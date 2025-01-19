package org.imt.tournamentmaster.service.match;

import jakarta.persistence.EntityManager;
import org.imt.tournamentmaster.model.equipe.Equipe;
import org.imt.tournamentmaster.model.match.Match;
import org.imt.tournamentmaster.repository.match.MatchRepository;
import org.imt.tournamentmaster.service.equipe.EquipeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.StreamSupport;

@Service
public class MatchService {

    private final MatchRepository matchRepository;
    private final EquipeService equipeService;
    private final EntityManager entityManager;

    @Autowired
    public MatchService(MatchRepository matchRepository, EquipeService equipeService, EntityManager entityManager) {
        this.matchRepository = matchRepository;
        this.equipeService = equipeService;
        this.entityManager = entityManager;
    }

    @Transactional(readOnly = true)
    public Optional<Match> getById(long id) {
        return matchRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<Match> getAll() {
        return StreamSupport.stream(matchRepository.findAll().spliterator(), false)
                .toList();
    }

    @Transactional
    public Match creerMatch(Match match) {
        /*
        Equipe equipeA = renvoyerOuCreerEquipe(match.getEquipeA());
        equipeA = entityManager.merge(equipeA);
        match.setEquipeA(equipeA);

        Equipe equipeB = renvoyerOuCreerEquipe(match.getEquipeB());
        equipeB = entityManager.merge(equipeB);
        match.setEquipeB(equipeB);
         */
        return matchRepository.save(match);
    }

    private Equipe renvoyerOuCreerEquipe(Equipe equipe) {
        if (equipe != null && equipe.getId() != 0) {
            Optional<Equipe> equipeExistante = equipeService.getById(equipe.getId());
            if (equipeExistante.isPresent()) {
                return equipeExistante.get();
            }
        }
        return equipeService.creerEquipe(equipe);
    }
}
