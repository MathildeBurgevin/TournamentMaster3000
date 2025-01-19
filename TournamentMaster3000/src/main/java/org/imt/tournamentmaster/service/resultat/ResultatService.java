package org.imt.tournamentmaster.service.resultat;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.imt.tournamentmaster.model.equipe.Equipe;
import org.imt.tournamentmaster.model.resultat.Resultat;
import org.imt.tournamentmaster.repository.resultat.ResultatRepository;
import org.imt.tournamentmaster.service.reporting.JsonReportingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.StreamSupport;

@Service
public class ResultatService {

    private final ResultatRepository resultatRepository;
    private final JsonReportingService jsonReportingService;

    public ResultatService(ResultatRepository resultatRepository, JsonReportingService jsonReportingService) {
        this.resultatRepository = resultatRepository;
        this.jsonReportingService = jsonReportingService;
    }

    @Transactional(readOnly = true)
    public Optional<Resultat> getById(long id) {
        return resultatRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<Resultat> getAll() {
        return StreamSupport.stream(resultatRepository.findAll().spliterator(), false)
                .toList();
    }

    @Transactional
    public String creerResultats(List<Resultat> resultats) throws JsonProcessingException {

        StringBuilder rapport = new StringBuilder();
        List<String> erreurs = new ArrayList<>();
        List<Long> resultatsNonValide = new ArrayList<>();
        int nbEvenements = 0;
        Equipe equipeGagnante = null;

        for (Resultat resultat : resultats) {
            try {
                // Vérification si le résultat existe déjà dans la base (éviter les doublons)
                Optional<Resultat> resultatExistant = resultatRepository.findById(resultat.getId());
                if (resultatExistant.isPresent()) {
                    resultatsNonValide.add(resultat.getId());
                    throw new IllegalStateException("Doublon détecté pour le résultat avec l'ID " + resultat.getId());
                }

                // Enregistrer le résultat si pas de doublon
                resultatRepository.save(resultat);
                nbEvenements++;

                // Récupérer l'équipe gagnante du match
                if (equipeGagnante == null || resultat.getMatch().determineWinner().equals(equipeGagnante)) {
                    equipeGagnante = resultat.getMatch().determineWinner();
                }
            } catch (Exception e) {
                // En cas d'erreur (ex. doublon ou autre), on ajoute à la liste des erreurs
                erreurs.add("Erreur lors de l'ajout d'un résultat : " + e.getMessage());
            }
        }

        // Générer le rapport
        rapport.append("Rapport de traitement des résultats :\n");
        rapport.append("Date de traitement : ").append(LocalDateTime.now()).append("\n");
        rapport.append("Nombre d'événements traités : ").append(nbEvenements).append("\n");

        for (int i = 0; i < resultats.size(); i++) {
            Resultat resultat = resultats.get(i);
            if (! resultatsNonValide.contains(resultat.getId())) {
                rapport.append("Resultat ").append(i + 1).append(" :\n");
                rapport.append("- Match : ").append(resultat.getMatch().getEquipeA().getNom())
                        .append(" contre ").append(resultat.getMatch().getEquipeB().getNom()).append("\n");
                rapport.append("- Équipe gagnante : ").append(resultat.getMatch().determineWinner().getNom()).append("\n");
                rapport.append("- Score : ").append(resultat.getMatch().getScoreTotal()).append("\n");
            }
        }

        if (!erreurs.isEmpty()) {
            rapport.append("Erreurs survenues :\n");
            for (String erreur : erreurs) {
                rapport.append("- ").append(erreur).append("\n");
            }
            rapport.append("Status : KO\n");
        } else {
            rapport.append("Status : OK\n");
        }

        String rapportJson = "";

        for (Resultat resultat : resultats) {
            rapportJson += "\n" + jsonReportingService.report(resultat);
        }
        rapport.append("\nDétails JSON : ").append(rapportJson);

        return rapport.toString();
    }
}
