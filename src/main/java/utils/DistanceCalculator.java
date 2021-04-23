package utils;
/*::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/
/*::                                                                         :*/
/*::  This routine calculates the distance between two points (given the     :*/
/*::  latitude/longitude of those points). It is being used to calculate     :*/
/*::  the distance between two locations using GeoDataSource (TM) products   :*/
/*::                                                                         :*/
/*::  Definitions:                                                           :*/
/*::    Southern latitudes are negative, eastern longitudes are positive     :*/
/*::                                                                         :*/
/*::  Function parameters:                                                   :*/
/*::    lat1, lon1 = Latitude and Longitude of point 1 (in decimal degrees)  :*/
/*::    lat2, lon2 = Latitude and Longitude of point 2 (in decimal degrees)  :*/
/*::    unit = the unit you desire for results                               :*/
/*::           where: 'M' is statute miles (default)                         :*/
/*::                  'K' is kilometers                                      :*/
/*::                  'N' is nautical miles                                  :*/
/*::  Worldwide cities and other features databases with latitude longitude  :*/
/*::  are available at https://www.geodatasource.com                         :*/
/*::                                                                         :*/
/*::  For enquiries, please contact sales@geodatasource.com                  :*/
/*::                                                                         :*/
/*::  Official Web site: https://www.geodatasource.com                       :*/
/*::                                                                         :*/
/*::           GeoDataSource.com (C) All Rights Reserved 2019                :*/
/*::                                                                         :*/
/*::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/

import data.Problem;
import objects.Installation;
import objects.Order;

public class DistanceCalculator {

    public static double distance(Installation instOne, Order orderTwo, String unit) {
        return distance(instOne, Problem.getInstallation(orderTwo), unit);
    }

    public static double distance(Order orderOne, Order orderTwo, String unit) {
        return distance(Problem.getInstallation(orderOne), Problem.getInstallation(orderTwo), unit);
    }

    public static double distance(Installation instOne, Installation instTwo, String unit) {
        return distance(instOne.getLatitude(), instOne.getLongitude(), instTwo.getLatitude(), instTwo.getLongitude(), unit);
    }

    public static double distance(double latOne, double lonOne, Order orderTwo, String unit) {
        Installation installationTwo = Problem.getInstallation(orderTwo);
        return distance(latOne, lonOne, installationTwo.getLatitude(), installationTwo.getLongitude(), unit);
    }

    public static double distance(double latOne, double lonOne, double latTwo, double lonTwo, String unit) {
        if ((latOne == latTwo) && (lonOne == lonTwo)) {
            return 0;
        } else {
            double theta = lonOne - lonTwo;
            double dist = Math.sin(Math.toRadians(latOne)) * Math.sin(Math.toRadians(latTwo)) + Math.cos(Math.toRadians(latOne)) * Math.cos(Math.toRadians(latTwo)) * Math.cos(Math.toRadians(theta));
            dist = Math.acos(dist);
            dist = Math.toDegrees(dist);
            dist = dist * 60 * 1.1515;
            if (unit.equals("K")) {
                dist = dist * 1.609344;
            } else if (unit.equals("N")) {
                dist = dist * 0.8684;
            }
            return (dist);
        }
    }





}
