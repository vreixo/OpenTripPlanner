package org.opentripplanner.routing.constraints;

public class EnvironmentalFactorThreshold {

    public final EnvironmentalFactorType type;
    public final Double maxAverage;
    public final Double maxPeak;

    public boolean softAverageLimiting = true;
    public double softAverageOverageRate = 5.0; // a jump in cost for every meter over the average pollution limit
    public double softAveragePenalty = 60.0; // a jump in cost when stepping over the average pollution limit

    public boolean softPeakLimiting = true;
    public double softPeakOverageRate = 5.0; // a jump in cost for every meter over the peak pollution limit
    public double softPeakPenalty = 60.0; // a jump in cost when stepping over the peak pollution limit

    public EnvironmentalFactorThreshold(EnvironmentalFactorType type, Double maxAverage,
                                        Double maxPeak) {
        this.type = type;
        this.maxAverage = maxAverage;
        this.maxPeak = maxPeak;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((type == null) ? 0 : type.hashCode());
        result = prime * result + ((maxAverage == null) ? 0 : maxAverage.hashCode());
        result = prime * result + ((maxPeak == null) ? 0 : maxPeak.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        EnvironmentalFactorThreshold other = (EnvironmentalFactorThreshold) obj;
        if (type != other.type)
            return false;
        if (maxAverage == null) {
            if (other.maxAverage != null)
                return false;
        } else if (!maxAverage.equals(other.maxAverage))
            return false;
        if (maxPeak == null) {
            if (other.maxPeak != null)
                return false;
        } else if (!maxPeak.equals(other.maxPeak))
            return false;
        return true;
    }


}