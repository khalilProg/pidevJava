package tn.esprit.tools;

public class MockSession {
    // ID of the user's Medical Record (from dossier_med table)
    private static final int MOCKED_DOSSIER_MED_ID = 1;

    // ID of the user as a Client (from don table)
    private static final int MOCKED_CLIENT_ID = 1;

    public static int getDossierMedId() {
        return MOCKED_DOSSIER_MED_ID;
    }

    public static int getClientId() {
        return MOCKED_CLIENT_ID;
    }
}