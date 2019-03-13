package de.dfki.pdp.sc;

/**
 * Used to save the position of states in a graphical view
 *
 * e.g. <qt:editorinfo geometry="0;-200;-106;-50;100;100" scenegeometry="283.19;110.47;177.19;60.47;164.29;100"/>
 */
public class Geometry {
    private double x,y,w,h;

    public Geometry(String qtGeo) {
        try {
            String[] split = qtGeo.split(";");
            setX(Double.valueOf(split[0]));
            setY(Double.valueOf(split[1]));
            setW(Double.valueOf(split[4]));
            setH(Double.valueOf(split[5]));
        } catch (Exception ex) {
            throw new IllegalArgumentException("could not read size from qt geomtry string", ex);
        }
    }

    public double getX() {
        return x;
    }

    protected void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    protected void setY(double y) {
        this.y = y;
    }

    public double getW() {
        return w;
    }

    protected void setW(double w) {
        this.w = w;
    }

    public double getH() {
        return h;
    }

    protected void setH(double h) {
        this.h = h;
    }
}
