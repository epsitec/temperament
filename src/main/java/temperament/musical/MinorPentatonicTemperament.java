package temperament.musical;

import java.util.ArrayList;
import java.util.List;

public class MinorPentatonicTemperament implements ITemperament {

    // Notes for A Minor Pentatonic: A, C, D, E, G
    private static final String[] NOTE_NAMES = {"La", "Do", "Re", "Mi", "Sol"};
    private static final double[] RATIOS = {
        1.0,                            // La (Root)
        Math.pow(2, 3.0/12.0),          // Do (Minor Third, 3 semitones)
        Math.pow(2, 5.0/12.0),          // Re (Perfect Fourth, 5 semitones)
        Math.pow(2, 7.0/12.0),          // Mi (Perfect Fifth, 7 semitones)
        Math.pow(2, 10.0/12.0)          // Sol (Minor Seventh, 10 semitones)
    };

    @Override
    public int getNbNotesGamme() {
        return 5;
    }

    @Override
    public int getNbNotesToPlayGamme() {
        return 5;
    }

    @Override
    public double getNoteFrequencyRatio(int noteIndex) {
        if (noteIndex < 0 || noteIndex >= RATIOS.length) {
            return 1.0; // Default or throw exception
        }
        return RATIOS[noteIndex];
    }

    @Override
    public double getNoteFrequencyRatioInFifthsCirle(int noteIndex) {
        // For pentatonic, this might just be the octave-reduced ratio
        return getNoteFrequencyRatio(noteIndex);
    }

    @Override
    public String getNoteName(int noteIndex) {
        if (noteIndex < 0 || noteIndex >= NOTE_NAMES.length) {
            return "Unknown";
        }
        return NOTE_NAMES[noteIndex];
    }

    @Override
    public int La() {
        // "La" is the root (1st note) in our A-based Minor Pentatonic scale (index 0)
        for (int i = 0; i < NOTE_NAMES.length; i++) {
            if ("La".equals(NOTE_NAMES[i])) {
                return i;
            }
        }
        return 0; // Should be found
    }

    @Override
    public double getFrequenceDo(double frequenceLa) {
        // In this A minor pentatonic scale, "La" is the root (RATIOS[0] = 1.0).
        // "Do" is the second note (RATIOS[1]).
        // The question is: what is the frequency of the "Do" that is part of *this specific scale*,
        // given that the *root of this scale* ("La") has frequency `frequenceLa`.
        // So, F_La_root = frequenceLa.
        // F_Do_in_scale = F_La_root * RATIOS[1] (since RATIOS[0] for La is 1.0)
        // This seems to be what's intended, but the method name "getFrequenceDo" is a bit ambiguous.
        // If it meant a generic "Do C4" based on "La A4", that's different.
        // Given it's inside a temperament, it should refer to notes *within* that temperament.
        // The `MajorPentatonicTemperament` assumed `getFrequenceDo` means frequency of the scale's *root*.
        // Let's stick to that for consistency: "Do" in "getFrequenceDo" refers to the *root of the scale*.
        // Our root is "La". So we want the frequency of "La".
        // frequenceLa is already the frequency of "La" (the root).
        // So, F_ScaleRoot = frequenceLa.
        // This means `getNoteFrequencyRatio(La())` should be 1.0 for this scale.
        // F_ScaleRoot = frequenceLa / getNoteFrequencyRatio(La()) = frequenceLa / 1.0 = frequenceLa.
        return frequenceLa / getNoteFrequencyRatio(La());
    }

    @Override
    public int getNbNotes() {
        return 5;
    }

    @Override
    public List<NotesInterval> getFifthsIntervals() {
        return new ArrayList<>();
    }

    @Override
    public List<NotesInterval> getMajorThirdsIntervals() {
        return new ArrayList<>();
    }

    @Override
    public String getNoteFullName(int noteIndex) {
        return getNoteName(noteIndex);
    }

    @Override
    public int findNoteIndexByFullName(String fullName) {
        for (int i = 0; i < NOTE_NAMES.length; i++) {
            if (NOTE_NAMES[i].equalsIgnoreCase(fullName)) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public int findNoteIndexByRatio(double ratio) {
        double minDiff = Double.MAX_VALUE;
        int bestIndex = -1;
        for (int i = 0; i < RATIOS.length; i++) {
            double diff = Math.abs(RATIOS[i] - ratio);
            if (diff < minDiff) {
                minDiff = diff;
                bestIndex = i;
            }
        }
        if (minDiff < 0.01) { // Threshold
            return bestIndex;
        }
        return -1;
    }

    @Override
    public boolean isModernTemperament() {
        return true;
    }

    @Override
    public String toString() {
        return "Minor Pentatonic";
    }
}
