package org.imt.tournamentmaster.controller.match;

import org.imt.tournamentmaster.model.match.Round;
import org.imt.tournamentmaster.service.match.RoundService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/round")
public class RoundController {

    private final RoundService roundService;

    @Autowired
    public RoundController(RoundService roundService) {
        this.roundService = roundService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<Round> getById(@PathVariable long id) {
        Optional<Round> round = roundService.getById(id);

        return round.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping
    public List<Round> getAll() {
        return roundService.getAll();
    }

    @GetMapping("/max/{scoreA}")
    public List<Round> getMaxScore(@PathVariable int scoreA) {
       return roundService.getByScoreAGreaterThanEqual(scoreA);
    }

    @PostMapping("/new")
    public ResponseEntity<Round> creerRound(@RequestBody Round round) {
        Round nouveauRound = roundService.creerRound(round);
        return new ResponseEntity<>(nouveauRound, HttpStatus.CREATED);
    }
}
