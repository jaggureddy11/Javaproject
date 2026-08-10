# Constraint Specification Catalog

## 1. Hard Constraints (Infeasibility Penalties)

### H1: Vehicle Capacity Constraint
- **Rule**: The total weight of all orders assigned to a vehicle must not exceed the vehicle's `maxWeightKg`.
- **Score Impact**: `-1000 Hard Score` per kg overloaded.
- **Implementation**:
  $$\text{Penalty} = \sum_{v \in V} \max(0, \sum_{s \in S_v} \text{weight}(s) - \text{cap}(v))$$

### H2: Delivery Time Window Constraint
- **Rule**: Arrival time at order location $s$ must be $\le$ `windowEndMinutes`.
- **Score Impact**: `-100 Hard Score` per minute of lateness.
- **Implementation**:
  $$\text{Penalty} = \sum_{s \in S} \max(0, \text{arrivalTime}(s) - \text{windowEnd}(s))$$

### H3: Driver Shift Duration Constraint
- **Rule**: Total route duration (from depot exit to depot return) must not exceed driver `maxShiftMinutes`.
- **Score Impact**: `-500 Hard Score` per hour over shift limit.

### H4: Single Depot Origin/Return Constraint
- **Rule**: Every vehicle route must originate from its assigned depot and return to the same depot upon route completion.

---

## 2. Soft Constraints (Optimization Objectives)

### S1: Minimize Total Route Distance
- **Rule**: Minimize aggregate travel distance across all active vehicles.
- **Score Impact**: `-1 Soft Score` per meter traveled.

### S2: Minimize Total Travel & Service Duration
- **Rule**: Minimize total fleet operational time.
- **Score Impact**: `-10 Soft Score` per minute of driving/waiting.

### S3: Minimize Fleet Size (Vehicle Count)
- **Rule**: Prefer utilizing fewer vehicles fully over deploying unnecessary extra vehicles.
- **Score Impact**: `-50,000 Soft Score` per active vehicle deployed.

### S4: Route Disruption Penalty (Route Stability during Re-Optimization)
- **Rule**: When re-optimizing due to an incident, changing an unaffected future stop on an existing route incurs a penalty cost to prevent driver sequence confusion.
- **Score Impact**: `-5,000 Soft Score` per un-forced stop sequence change.