package temperament.musical;

import java.util.ArrayList;
import java.util.List;

public class MajorPentatonicTemperament implements ITemperament {

    private static final String[] NOTE_NAMES = {"Do", "Re", "Mi", "Sol", "La"};
    private static final double[] RATIOS = {
        1.0,             // Do
        Math.pow(2, 2.0/12.0), // Re (2 semitones)
        Math.pow(2, 4.0/12.0), // Mi (4 semitones)
        Math.pow(2, 7.0/12.0), // Sol (7 semitones)
        Math.pow(2, 9.0/12.0)  // La (9 semitones)
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
            // Or throw an exception, or return a default
            return 1.0;
        }
        return RATIOS[noteIndex];
    }

    @Override
    public double getNoteFrequencyRatioInFifthsCirle(int noteIndex) {
        // This method might need more thought for pentatonic scales
        // For now, returning the basic ratio within one octave
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
        // "La" is the 5th note in our C-based Major Pentatonic scale (index 4)
        for (int i = 0; i < NOTE_NAMES.length; i++) {
            if ("La".equals(NOTE_NAMES[i])) {
                return i;
            }
        }
        return 4; // Default to A if C is root
    }

    @Override
    public double getFrequenceDo(double frequenceLa) {
        // If La is f0, and Do is at -9 semitones from La (or +3 semitones if La is the A above G)
        // Our La is RATIOS[4] relative to Do (RATIOS[0]=1.0)
        // So, frequenceLa = F_Do * RATIOS[4]
        // F_Do = frequenceLa / RATIOS[4]
        // However, the interface defines getNoteFrequencyRatio(La()) as the ratio of La to the scale's root.
        // And getFrequenceDo is asking for the frequency of the scale's root if 'La' has frequenceLa.
        return frequenceLa / getNoteFrequencyRatio(La());
    }

    @Override
    public int getNbNotes() {
        return 5;
    }

    @Override
    public List<NotesInterval> getFifthsIntervals() {
        // Pentatonic scales don't typically have a full circle of fifths in the same way
        return new ArrayList<>(); // Return empty list
    }

    @Override
    public List<NotesInterval> getMajorThirdsIntervals() {
        // Similar to fifths, standard major thirds might not be explicitly defined across the scale
        return new ArrayList<>(); // Return empty list
    }

    @Override
    public String getNoteFullName(int noteIndex) {
        // For simplicity, octave is not explicitly handled here for this 5-note scale
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
        // Set a threshold for matching
        if (minDiff < 0.01) { // Threshold for ratio difference
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
        return "Major Pentatonic";
    }
}
