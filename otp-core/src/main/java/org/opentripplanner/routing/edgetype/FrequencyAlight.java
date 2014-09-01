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

package org.opentripplanner.routing.edgetype;

import org.onebusaway.gtfs.model.Stop;
import org.onebusaway.gtfs.model.Trip;
import org.opentripplanner.routing.core.RoutingContext;
import org.opentripplanner.routing.core.RoutingRequest;
import org.opentripplanner.routing.core.ServiceDay;
import org.opentripplanner.routing.core.State;
import org.opentripplanner.routing.core.StateEditor;
import org.opentripplanner.routing.core.StopTransfer;
import org.opentripplanner.routing.core.TransferTable;
import org.opentripplanner.routing.core.TraverseMode;
import org.opentripplanner.routing.core.TraverseModeSet;
import org.opentripplanner.routing.graph.Edge;
import org.opentripplanner.routing.vertextype.TransitVertex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vividsolutions.jts.geom.LineString;

public class FrequencyAlight extends Edge {
    private static final long serialVersionUID = 3388162982920747289L;

    private static final Logger LOG = LoggerFactory.getLogger(FrequencyAlight.class);
            
    private int stopIndex;
    private FrequencyBasedTripPattern pattern;
    private int modeMask;

    private int serviceId;


    public FrequencyAlight(TransitVertex from, TransitVertex to,
            FrequencyBasedTripPattern pattern, int stopIndex, TraverseMode mode, int serviceId) {
        super(from, to);
        this.pattern = pattern;
        this.stopIndex = stopIndex;
        this.modeMask = new TraverseModeSet(mode).getMask();
        this.serviceId = serviceId;
    }

    @Override
    public Trip getTrip() {
        return pattern.getTrip();
    }

    public String getDirection() {
        return pattern.getHeadsign(stopIndex);
    }

    public double getDistance() {
        return 0;
    }

    public LineString getGeometry() {
        return null;
    }

    public String getName() {
        return "leave street network for transit network";
    }

    public State traverse(State state0) {
        RoutingContext rctx = state0.getContext();
        RoutingRequest options = state0.getOptions();
        Trip trip = pattern.getTrip();

        if (options.isArriveBy()) {
            /* backward traversal: find a transit trip on this pattern */

            if (!options.getModes().get(modeMask)) {
                return null;
            }
            
            /*
             * Check transfer rules. This is possible here because the pattern always returns the
             * same trip. 
             */
            
            // Current time is used to find the next trip
            long currentTime = state0.getTimeSeconds();
            
            int transferPenalty = 0;
            if (state0.isEverBoarded()) {
                // This is not the first boarding, thus a transfer
                TransferTable transferTable = options.getRoutingContext().transferTable;
                // Get the current stop
                Stop currentStop = ((TransitVertex) tov).getStop(); 
                // Get the transfer time
                int transferTime = transferTable.getTransferTime(state0.getPreviousStop(),
                        currentStop, state0.getPreviousTrip(), trip, false);
                if (transferTime > 0) {
                    // There is a minimum transfer time to make this transfer
                    // Decrease current time if necessary
                    long tableAlightBefore = state0.getLastAlightedTimeSeconds() - transferTime;
                    if (tableAlightBefore < currentTime) {
                        currentTime = tableAlightBefore;
                    }
                } else if (transferTime == StopTransfer.FORBIDDEN_TRANSFER) {
                    // This transfer is not allowed
                    return null;
                }
                
                // Determine transfer penalty
                transferPenalty = transferTable.determineTransferPenalty(transferTime, options.nonpreferredTransferPenalty);
                
                // Check whether back edge is TimedTransferEdge
                if (state0.getBackEdge() instanceof TimedTransferEdge) {
                    // Transfer must be of type TIMED_TRANSFER
                    if (transferTime != StopTransfer.TIMED_TRANSFER) {
                        return null;
                    }
                }
            }
            
            
            /* find next boarding time */
            /*
             * check lists of transit serviceIds running yesterday, today, and tomorrow (relative to
             * initial state) if this pattern's serviceId is running look for the next boarding time
             * choose the soonest boarding time among trips starting yesterday, today, or tomorrow
             */
            int bestWait = -1;
            TraverseMode mode = state0.getNonTransitMode();
            if (options.bannedTrips.containsKey(trip.getId())) {
                //This behaves a little differently than with ordinary trip patterns,
                //because trips don't really have strong identities in frequency-based
                //plans.  I expect that reasonable plans will still be produced, since
                //we used to use route banning and that was not so bad.  Also, 
                //partial trip banning is unsupported (the whole trip will be treated
                //as banned)
                return null;
            }
            ServiceDay serviceDay = null;
            for (ServiceDay sd : rctx.serviceDays) {
                int secondsSinceMidnight = sd.secondsSinceMidnight(currentTime);
                // only check for service on days that are not in the future
                // this avoids unnecessarily examining tomorrow's services
                if (secondsSinceMidnight < 0)
                    continue;
                if (sd.serviceIdRunning(serviceId)) {
                    int startTime = pattern.getPreviousArrivalTime(stopIndex, secondsSinceMidnight,
                            options.wheelchairAccessible, mode == TraverseMode.BICYCLE, true);
                    if (startTime >= 0) {
                        // a trip was found, wait will be non-negative
                        
                        int wait = (int) (state0.getTimeSeconds() - sd.time(startTime));
                        if (wait < 0)
                            LOG.error("negative wait time on alight");
                        if (bestWait < 0 || wait < bestWait) {
                            // track the soonest departure over all relevant schedules
                            bestWait = wait;
                            serviceDay = sd;
                        }
                    }

                }
            }
            if (bestWait < 0) {
                return null;
            }
            
            /* check if trip is banned for this plan */
            if (options.tripIsBanned(trip))
            	return null;

            /* check if route is preferred for this plan */
            long preferences_penalty = options.preferencesPenaltyForTrip(trip);

            StateEditor s1 = state0.edit(this);
            int type = pattern.getBoardType(stopIndex);
            if (TransitUtils.handleBoardAlightType(s1, type)) {
                return null;
            }
            //s1.setTrip(bestPatternIndex); is this necessary? (AMB)
            s1.setServiceDay(serviceDay);
            s1.incrementTimeInSeconds(bestWait);
            s1.incrementNumBoardings();
            s1.setTripId(trip.getId());
            s1.setPreviousTrip(trip);
            s1.setZone(pattern.getZone(stopIndex));
            s1.setRoute(trip.getRoute().getId());

            long wait_cost = bestWait;
            if (!state0.isEverBoarded()) {
                wait_cost *= options.waitAtBeginningFactor;
            } else {
                wait_cost *= options.waitReluctance;
            }
            s1.incrementWeight(preferences_penalty);
            s1.incrementWeight(transferPenalty);
            s1.incrementWeight(wait_cost + options.getBoardCost(mode));
            s1.setBackMode(TraverseMode.LEG_SWITCH);
            return s1.makeState();
        } else {
            /* forward traversal: not so much to do */
            // do not alight immediately when arrive-depart dwell has been eliminated
            // this affects multi-itinerary searches
            if (state0.getBackEdge() instanceof FrequencyAlight) {
                return null;
            }
            StateEditor s1 = state0.edit(this);
            int type = pattern.getBoardType(stopIndex);
            if (TransitUtils.handleBoardAlightType(s1, type)) {
                return null;
            }
            s1.setTripId(null);
            s1.setLastAlightedTimeSeconds(state0.getTimeSeconds());
            s1.setPreviousStop(((TransitVertex) fromv).getStop());
            s1.setBackMode(TraverseMode.LEG_SWITCH);
            return s1.makeState();
        }
    }

    public State optimisticTraverse(State state0) {
        StateEditor s1 = state0.edit(this);
        // no cost (see patternalight)
        s1.setBackMode(TraverseMode.LEG_SWITCH);
        return s1.makeState();
    }

    /* See weightLowerBound comment. */
    public double timeLowerBound(RoutingContext rctx) {
        if (rctx.opt.isArriveBy()) {
            if (! rctx.opt.getModes().get(modeMask)) {
                return Double.POSITIVE_INFINITY;
            }
            int serviceId = pattern.getServiceId();
            for (ServiceDay sd : rctx.serviceDays)
                if (sd.serviceIdRunning(serviceId))
                    return 0;
            return Double.POSITIVE_INFINITY;
        } else {
            return 0;
        }
    }

    /*
     * If the main search is proceeding backward, the lower bound search is proceeding forward.
     * Check the mode or serviceIds of this pattern at board time to see whether this pattern is
     * worth exploring. If the main search is proceeding forward, board cost is added at board
     * edges. The lower bound search is proceeding backward, and if it has reached a board edge the
     * pattern was already deemed useful.
     */
    public double weightLowerBound(RoutingRequest options) {
        if (options.isArriveBy())
            return timeLowerBound(options);
        else
            return options.getBoardCostLowerBound();
    }

    
    public int getStopIndex() {
        return stopIndex;
    }

    public String toString() {
        return "FrequencyAlight(" + getFromVertex() + ", " + getToVertex() + ")";
    }
}
