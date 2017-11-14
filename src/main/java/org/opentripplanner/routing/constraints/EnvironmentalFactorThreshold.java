package org.opentripplanner.routing.constraints;

public class EnvironmentalFactorThreshold {

        private final EnvironmentalFactorType type;

        private final Double maxAverage;

        private final Double maxPeak;

		public EnvironmentalFactorThreshold(EnvironmentalFactorType type, Double maxAverage,
				Double maxPeak) {
			this.type = type;
			this.maxAverage = maxAverage;
			this.maxPeak = maxPeak;
		}

		public EnvironmentalFactorType getType() {
			return type;
		}

		public Double getMaxAverage() {
			return maxAverage;
		}

		public Double getMaxPeak() {
			return maxPeak;
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