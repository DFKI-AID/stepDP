package de.dfki.tocalog.output.impp;


/**
 */
public class Score {
    private double score = 1.0;

    public void scale(double factor) {
        score *= factor;
    }

    public double getScore() {
        return score;
    }

    public static int compare(Score s1, Score s2) {
        //of value is better -> put better scores at the start
        return Double.compare(s2.score, s1.score);
    }
}
