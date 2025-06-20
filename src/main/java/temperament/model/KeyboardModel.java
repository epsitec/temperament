package temperament.model;

import java.awt.Color;
import java.awt.Point;
import java.awt.Polygon;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

import temperament.Commons;
import temperament.musical.ITemperament;
import temperament.musical.TemperamentAbbatialePayerne;

/**
 * modèle de données pour les touches d'un clavier de piano
 */
public class KeyboardModel extends SelectableNotesModel {
	/** dimensions pour une touche de piano standard, en mm */
	private static final double	DX1	= 24;
	private static final double	DX2	= 14;
	private static final double	DY1	= 150;
	private static final double	DY2	= 100;
	private static final double	DY3	= 50;

	private int					dx1;
	private int					dx2;
	private int					dy1;
	private int					dy2;
	private int					dy3;
	private List<KeyboardKey>	keys;
	private Color				whiteKey;
	private Color				blackKey;

	public KeyboardModel(ApplicationState appState) {
		super(appState);
		selectKeyboardColor();

		appState.addPropertyChangeListener(ApplicationState.TEMPERAMENT_PROPERTY, new PropertyChangeListener() {

			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				selectKeyboardColor();
			}
		});
	}

	private void selectKeyboardColor() {
		ITemperament t = getTemperament();
		boolean blackAndWhite = null != t && t.isModernTemperament();
		;
		whiteKey = blackAndWhite ? Color.white : new Color(252, 197, 109);
		blackKey = blackAndWhite ? Color.black : new Color(102, 69, 17);
	}

	@Override
	protected List<? extends ISelectableNote> getNotes() {
		return keys;
	}

	@Override
	protected void afterPanelDimensionChanged() {
		ITemperament t = getTemperament();
		int notesToDisplayTotal = t.getNbNotesGamme() * 2; // Assuming 2 octaves/repetitions visible
		if (notesToDisplayTotal == 0) notesToDisplayTotal = 1; // Avoid division by zero

		// DX1 is the original design width for a standard white key.
		// We want to scale this so that 'notesToDisplayTotal' keys fit in getWidth().
		double calculatedKeyWidth = (double) getWidth() / notesToDisplayTotal;

		// We still use DX1, DY1 etc. for proportions of a single key,
		// but the overall width of a key (dx1) will be based on calculatedKeyWidth.
		// Let's make dx1 equal to this calculatedKeyWidth.
		// The internal proportions of the key shape (like where the cutouts for black keys are)
		// might look odd if we just stretch/squash DX1, but for pentatonic using only DoFa shape, it's simpler.

		dx1 = (int) calculatedKeyWidth;
		// Make other dx/dy values proportional to dx1 if they were originally based on DX1.
		// Or, for pentatonic where we might only use one key shape, this is less critical.
		// For simplicity, let's keep other DY dimensions scaled by a consistent factor for now.
		// This part might need refinement based on visual results.
		// double originalTotalWidth = 14 * DX1; // Original design: 2 octaves of 7 white keys
		// double scaleFactor = getWidth() / originalTotalWidth; // General scale based on original design

		if (t.getNbNotesGamme() < 12) { // Simpler scaling for non-12-tone
			 // dx1 is already set to calculatedKeyWidth
			 // dy1, dy2, dy3 can maintain original proportions relative to a fixed height or scaled height
			 dy1 = (int) (DY1 * getHeight() / DY1); // Full height
			 dy2 = (int) (DY2 * getHeight() / DY1);
			 dy3 = (int) (DY3 * getHeight() / DY1);
			 dx2 = (int) (DX2 * dx1 / DX1); // dx2 proportional to new dx1
			 if (DX1 == 0) dx2 = 0; // Avoid division by zero if DX1 is somehow 0
		} else { // Original scaling for 12-tone
			double scaleX = getWidth() / (14 * DX1); // 2 octaves, 7 white keys each
			double scaleY = getHeight() / DY1;
			double scale = Math.min(scaleX, scaleY);
			dx1 = (int) (DX1 * scale);
			dx2 = (int) (DX2 * scale);
			dy1 = (int) (DY1 * scale);
			dy2 = (int) (DY2 * scale);
			dy3 = (int) (DY3 * scale);
		}
	}

	/**
	 * retourne la largeur d'une touche "blanche"
	 * 
	 * @return
	 */
	public int getWhiteKeyWidth() {
		return dx1;
	}

	/**
	 * retourne la largeur d'une touche "noire"
	 * 
	 * @return
	 */
	public int getBlackKeyWidth() {
		return dx2;
	}

	@Override
	protected void initNotes() {
		keys = new ArrayList<KeyboardKey>();
		initOctave(0);
		initOctave(1);
		updateNotes();
	}

	@Override
	protected void updateNotes() {
		if (null != keys) {
			for (KeyboardKey k : keys) {
				k.updateKeyPosition(dx1);
			}
		}

	}

	private void initOctave(int octave) {
		ITemperament t = getTemperament();
		int notesInGamme = t.getNbNotesGamme();
		// For 12-note gammes, an "octave" in terms of key layout spans 7 white key positions.
		// For other gammes (e.g. pentatonic), an "octave" or repetition of the gamme
		// will span `notesInGamme` key positions.
		int xOffsetMultiplier = (notesInGamme == 12) ? 7 : notesInGamme;
		int xStart = octave * xOffsetMultiplier;

		int baseNoteIndexForOctave = octave * notesInGamme;

		if (notesInGamme == 5) { // Specific handling for pentatonic
			for (int i = 0; i < notesInGamme; i++) {
				// Each key occupies one position sequentially.
				// The currentNoteIndexInTemperament should correctly map to the temperament's notes.
				// For pentatonic, octave handling in noteIndex might need to be t.getNbNotes() if it differs from gamme
				// but usually for pentatonic, gamme is the full set of notes.
				keys.add(new KeyboardKey(KeyType.DoFa, xStart + i, baseNoteIndexForOctave + i));
			}
		} else if (notesInGamme == 12) { // Standard 12-note handling (existing logic)
			// This is a simplified version of the original logic, ensuring idx maps correctly.
			// Original logic had complex idx increments, let's use direct mapping from ITemperament.
			// This part recreates the standard piano layout based on 12 notes.
			// White keys: C, D, E, F, G, A, B (indices 0,2,4,5,7,9,11 in a 12-note scale)
			// Black keys: C#, D#, F#, G#, A# (indices 1,3,6,8,10 in a 12-note scale)
			// The notePosition for KeyboardKey refers to its visual placement slot.

			keys.add(new KeyboardKey(KeyType.DoFa,    xStart + 0, baseNoteIndexForOctave + 0)); // Do
			keys.add(new KeyboardKey(KeyType.ReSolLa, xStart + 1, baseNoteIndexForOctave + 2)); // Re
			keys.add(new KeyboardKey(KeyType.MiSi,    xStart + 2, baseNoteIndexForOctave + 4)); // Mi
			keys.add(new KeyboardKey(KeyType.DoFa,    xStart + 3, baseNoteIndexForOctave + 5)); // Fa
			keys.add(new KeyboardKey(KeyType.ReSolLa, xStart + 4, baseNoteIndexForOctave + 7)); // Sol
			keys.add(new KeyboardKey(KeyType.ReSolLa, xStart + 5, baseNoteIndexForOctave + 9)); // La - ReSolLa type often used for La
			keys.add(new KeyboardKey(KeyType.MiSi,    xStart + 6, baseNoteIndexForOctave + 11)); // Si

			keys.add(new KeyboardKey(KeyType.NoireComplete, xStart + 0, baseNoteIndexForOctave + 1));  // Do# (place relative to Do)
			keys.add(new KeyboardKey(KeyType.NoireComplete, xStart + 1, baseNoteIndexForOctave + 3));  // Re# (place relative to Re)
			keys.add(new KeyboardKey(KeyType.NoireComplete, xStart + 3, baseNoteIndexForOctave + 6));  // Fa# (place relative to Fa)
			keys.add(new KeyboardKey(KeyType.NoireComplete, xStart + 4, baseNoteIndexForOctave + 8));  // Sol# (place relative to Sol)
			keys.add(new KeyboardKey(KeyType.NoireComplete, xStart + 5, baseNoteIndexForOctave + 10)); // La# (place relative to La)
		} else {
			// Fallback for other numbers of notes: similar to pentatonic, lay them out sequentially.
			for (int i = 0; i < notesInGamme; i++) {
				keys.add(new KeyboardKey(KeyType.DoFa, xStart + i, baseNoteIndexForOctave + i));
			}
		}
	}

	public List<KeyboardKey> getKeys() {
		return keys;
	}

	public class KeyboardKey implements ISelectableNote {
		private Polygon	polygon;
		private boolean	selected;
		/** index de la note dans le tempérament */
		private int		noteIndex;
		private KeyType	keyType;
		private int		notePosition;
		private Point	textPosition;
		private int		width;

		public KeyboardKey(KeyType keyType, int notePosition, int noteIndex) {
			this.keyType = keyType;
			this.notePosition = notePosition;
			this.noteIndex = noteIndex;
			this.selected = false;
			this.polygon = null;
			this.textPosition = null;
		}

		/**
		 * retourne le polygon qui représente le contour de la touche du clavier
		 * 
		 * @return
		 */
		public Polygon getPolygon() {
			return polygon;
		}

		/**
		 * retourne la position d'affichage du nom de la note associée à la touche
		 * 
		 * @return
		 */
		public Point getTextPosition() {
			return textPosition;
		}

		@Override
		public String getTooltipText() {
			return null;
		}

		/**
		 * met à jour la position de la touche, suite à un changement de dimensions du
		 * panel qui représente le clavier
		 * 
		 * @param dx
		 */
		public void updateKeyPosition(int dx) {
			switch (keyType) {
			case DoFa:
				updateKeyPositionTypeDoFa(notePosition * dx);
				break;
			case ReSolLa:
				updateKeyPositionTypeReSolLa(notePosition * dx);
				break;
			case MiSi:
				updateKeyPositionTypeMiSi(notePosition * dx);
				break;
			case NoireComplete:
				updateKeyPositionTypeNoireComplete(notePosition * dx);
				break;
			case BriseeArriere:
				updateKeyPositionTypeNoireBriseeArriere(notePosition * dx);
				break;
			case BriseeAvant:
				updateKeyPositionTypeNoireBriseeAvant(notePosition * dx);
				break;

			}
		}

		/**
		 * retourne true si la touche est une touche blanche
		 * 
		 * @return
		 */
		public boolean isWhiteKey() {
			return keyType == KeyType.DoFa || keyType == KeyType.MiSi || keyType == KeyType.ReSolLa;
		}

		@Override
		public boolean isSelected() {
			return selected;
		}

		@Override
		public void setSelected(boolean pressed) {
			this.selected = pressed;
		}

		/**
		 * retourne l'index de la note associée à la touche, dans le tempérament actuel
		 */
		public int getNoteIndex() {
			return noteIndex;
		}

		/**
		 * détermine si la forme de la touche contient le point (x,y)
		 */
		public boolean containsPoint(int x, int y) {
			return polygon.contains(x, y);
		}

		/**
		 * retourne le nom de la note associée à la touche
		 * 
		 * @return
		 */
		public String getNoteName() {
			return getTemperament().getNoteName(noteIndex);
		}

		/**
		 * retourne la largeur de la touche, en pixels
		 * 
		 * @return
		 */
		public int getWidth() {
			return width;
		}

		public Color getFillColor() {
			Color result;
			if (isSelected()) {
				int selRank = KeyboardModel.this.getSelectionRank(noteIndex);
				result = Commons.getSelectionColor(selRank);
			} else {
				if (isWhiteKey()) {
					result = whiteKey;
				} else {
					result = blackKey;
				}
			}
			return result;
		}

		private void updateKeyPositionTypeDoFa(int xStart) {
			int[] x = new int[6];
			int[] y = new int[6];

			x[0] = xStart;
			y[0] = 0;

			x[1] = x[0];
			y[1] = dy1;

			x[2] = x[1] + dx1;
			y[2] = y[1];

			x[3] = x[2];
			y[3] = dy2;

			x[4] = x[3] - dx2 / 2;
			y[4] = y[3];

			x[5] = x[4];
			y[5] = 0;

			polygon = new Polygon(x, y, x.length);
			textPosition = new Point((x[1] + x[2]) / 2, y[2]);
			width = dx1;
		}

		private void updateKeyPositionTypeReSolLa(int xStart) {
			int[] x = new int[8];
			int[] y = new int[8];

			x[0] = xStart + dx2 / 2;
			y[0] = 0;

			x[1] = x[0];
			y[1] = dy2;

			x[2] = xStart;
			y[2] = y[1];

			x[3] = x[2];
			y[3] = dy1;

			x[4] = x[3] + dx1;
			y[4] = y[3];

			x[5] = x[4];
			y[5] = dy2;

			x[6] = x[5] - dx2 / 2;
			y[6] = y[5];

			x[7] = x[6];
			y[7] = 0;
			polygon = new Polygon(x, y, x.length);
			textPosition = new Point((x[3] + x[4]) / 2, y[4]);
			width = dx1;
		}

		private void updateKeyPositionTypeMiSi(int xStart) {
			int[] x = new int[6];
			int[] y = new int[6];

			x[0] = xStart + dx2 / 2;
			y[0] = 0;

			x[1] = x[0];
			y[1] = dy2;

			x[2] = xStart;
			y[2] = y[1];

			x[3] = x[2];
			y[3] = dy1;

			x[4] = x[3] + dx1;
			y[4] = y[3];

			x[5] = x[4];
			y[5] = 0;

			polygon = new Polygon(x, y, x.length);
			textPosition = new Point((x[3] + x[4]) / 2, y[4]);
			width = dx1;
		}

		private void updateKeyPositionTypeNoireComplete(int xStart) {
			int[] x = new int[4];
			int[] y = new int[4];

			x[0] = xStart - dx2 / 2;
			y[0] = 0;

			x[1] = x[0];
			y[1] = dy2;

			x[2] = x[1] + dx2;
			y[2] = y[1];

			x[3] = x[2];
			y[3] = 0;

			polygon = new Polygon(x, y, x.length);
			textPosition = new Point((x[1] + x[2]) / 2, y[2]);
			width = dx2;
		}

		private void updateKeyPositionTypeNoireBriseeAvant(int xStart) {
			int[] x = new int[4];
			int[] y = new int[4];

			x[0] = xStart - dx2 / 2;
			y[0] = dy3 + 1;

			x[1] = x[0];
			y[1] = dy2;

			x[2] = x[1] + dx2;
			y[2] = y[1];

			x[3] = x[2];
			y[3] = y[0];

			polygon = new Polygon(x, y, x.length);
			textPosition = new Point((x[1] + x[2]) / 2, y[2]);
			width = dx2;
		}

		private void updateKeyPositionTypeNoireBriseeArriere(int xStart) {
			int[] x = new int[4];
			int[] y = new int[4];

			x[0] = xStart - dx2 / 2;
			y[0] = 0;

			x[1] = x[0];
			y[1] = dy3 - 1;

			x[2] = x[1] + dx2;
			y[2] = y[1];

			x[3] = x[2];
			y[3] = 0;

			polygon = new Polygon(x, y, x.length);
			textPosition = new Point((x[1] + x[2]) / 2, y[2]);
			width = dx2;
		}

	}

	private enum KeyType {
		DoFa, ReSolLa, MiSi, NoireComplete, BriseeArriere, BriseeAvant
	}
}
