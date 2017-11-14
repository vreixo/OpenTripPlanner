/* This program is free software: you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public License
 as published by the Free Software Foundation, either version 3 of
 the License, or (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>. */

package org.opentripplanner.updater.environmental;

import com.thanglequoc.aqicalculator.PollutantCode;

import static com.thanglequoc.aqicalculator.PollutantCode.*;

public enum ParameterAir {

    co(CO), so2(SO2), no2(NO2), o3(O3), pm10(PM10), pm25(PM25);

    private final PollutantCode pollutantCode;

    ParameterAir(PollutantCode pollutantCode) {
        this.pollutantCode = pollutantCode;
    }

    public PollutantCode getPollutantCode() {
        return pollutantCode;
    }
}
