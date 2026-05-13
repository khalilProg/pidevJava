package tn.esprit.services;

import tn.esprit.entities.DossierMed;

import java.util.*;

public class KNNModelService {

    // Number of neighbors to calculate for majority voting
    private static final int K_VALUE = 5;

    /**
     * Nested class just to hold distance calculations temporarily.
     */
    private static class DonorDistance implements Comparable<DonorDistance> {
        DossierMed dossier;
        double distance;
        String recommendedDonationLabel; // Whole Blood, Plasma, or Double Red Cells

        DonorDistance(DossierMed d, double dist, String label) {
            this.dossier = d;
            this.distance = dist;
            this.recommendedDonationLabel = label;
        }

        @Override
        public int compareTo(DonorDistance o) {
            return Double.compare(this.distance, o.distance);
        }
    }

    /**
     * Machine Learning Prediction function (Custom Euclidean Native Math).
     * Compares the target person against the entire Database history.
     */
    public String predictOptimalDonation(DossierMed currentUser, List<DossierMed> databaseHistory) {

        if(databaseHistory == null || databaseHistory.size() < K_VALUE) {
            return "WHOLE BLOOD (System defaulting: insufficient dataset size)";
        }

        List<DonorDistance> neighborList = new ArrayList<>();

        for (DossierMed historicalDossier : databaseHistory) {
            // Do not compare against yourself!
            if (historicalDossier.getId() == currentUser.getId()) continue;

            // Mathematical calculation of Euclidean Proximity between traits (Weight and Age primarily for safety limits)
            double ageDifference = Math.pow((currentUser.getAge() - historicalDossier.getAge()), 2);
            double bmiDifference = Math.pow((currentUser.calculateBMI() - historicalDossier.calculateBMI()), 2);

            // Standard Euclidean math algorithm -> c = √ (a² + b²)
            double actualDistance = Math.sqrt(ageDifference + bmiDifference);

            /* We fake the dataset training labels by evaluating past entries algorithmically.
             * (If you add a field "successful_donation_type" to DB later, grab that instead of calculating here).
             * Heavy users usually donate double red. Young/Lean donate Plasma. Normal is Whole. */
            String historyCategoryAssigned = classifyHistoricalModelLabel(historicalDossier);

            // Add distance profile
            neighborList.add(new DonorDistance(historicalDossier, actualDistance, historyCategoryAssigned));
        }

        // IMPORTANT STEP: ML Sorting! Who is closest mathematically?
        Collections.sort(neighborList);

        // Gather votes for K = 5 neighbors.
        Map<String, Integer> classVotes = new HashMap<>();
        for (int i = 0; i < Math.min(K_VALUE, neighborList.size()); i++) {
            String predictedCategory = neighborList.get(i).recommendedDonationLabel;
            classVotes.put(predictedCategory, classVotes.getOrDefault(predictedCategory, 0) + 1);
        }

        // Returns whatever highest cluster prediction holds
        return Collections.max(classVotes.entrySet(), Map.Entry.comparingByValue()).getKey();
    }

    /**
     * Generates a structural classification type based on medical safety rules for clustering algorithms.
     */
    private String classifyHistoricalModelLabel(DossierMed oldData) {
        if (oldData.getPoid() > 80 && oldData.getTaille() > 175) {
            return "POWER DOUBLE RED CELLS";
        } else if (oldData.getAge() >= 18 && oldData.getAge() <= 28) {
            return "LIQUID PLASMA";
        } else {
            return "STANDARD WHOLE BLOOD";
        }
    }
}