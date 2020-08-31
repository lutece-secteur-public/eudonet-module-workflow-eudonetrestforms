package fr.paris.lutece.plugins.workflow.modules.eudonetrestforms.utils;

import static java.lang.Math.*;

public class EudonetConversion {
	
	
	private final static double LAMBERT_N = 0.7256077650;
	private final static double LAMBERT_C = 11754255.426;
	private final static double LAMBERT_XS = 700000.0;
	private final static double LAMBERT_YS = 12655612.050;
	
	public final static double M_PI_2 = Math.PI/2.0;

    public final static double DEFAULT_EPS = 1e-10 ;
    public final static double E_CLARK_IGN =  0.08248325676  ;
    public final static double E_WGS84 =  0.08181919106  ;

    public final static double LON_MERID_PARIS = 0  ;
    public final static double LON_MERID_GREENWICH = 0.04079234433 ;
    public final static double LON_MERID_IERS = 3.0*Math.PI/180.0;
    
    /**
     * Convert Lambert 93 to POINT (lat lon)
     * @param x 
     * @param y
     * @return POINT (lat lon)
     */
    public static String lambertToGeographic(double x, double y) {
    	return lambertToGeographic(x, y, LON_MERID_IERS, E_WGS84);
    }

	public static String lambertToGeographic(double x, double y, double lonMeridian, double e) {
        double n = LAMBERT_N;
        double C = LAMBERT_C;
        double xs = LAMBERT_XS;
        double ys = LAMBERT_YS;

        double lon, gamma, R, latIso;

        R = sqrt((x - xs) * (x - xs) + (y - ys) * (y - ys));

        gamma = atan((x - xs) / (ys - y));

        lon = lonMeridian + gamma / n;

        latIso = -1 / n * log(abs(R / C));

        double lat = latitudeFromLatitudeISO(latIso, e, DEFAULT_EPS);

        return String.format("POINT (%f %f)", lon * 180/Math.PI, lat * 180/Math.PI);
    }
	
    private static double latitudeFromLatitudeISO(double latISo, double e, double eps) {

        double phi0 = 2 * atan(exp(latISo)) - M_PI_2;
        double phiI = 2 * atan(pow((1 + e * sin(phi0)) / (1 - e * sin(phi0)), e / 2d) * exp(latISo)) - M_PI_2;
        double delta = abs(phiI - phi0);

        while (delta > eps) {
            phi0 = phiI;
            phiI = 2 * atan(pow((1 + e * sin(phi0)) / (1 - e * sin(phi0)), e / 2d) * exp(latISo)) - M_PI_2;
            delta = abs(phiI - phi0);
        }

        return phiI;
    }

}
