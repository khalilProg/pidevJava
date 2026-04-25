package tn.esprit.tools;

public class ComboItem {
    private int id;
    private String label;

    public ComboItem(int id, String label) {
        this.id = id;
        this.label = label;
    }

    public int getId() {
        return id;
    }

    public String getLabel() {
        return label;
    }

    // THIS IS THE MAGIC METHOD! It tells the Dropdown what text to show to the Doctor.
    @Override
    public String toString() {
        return label;
    }
}