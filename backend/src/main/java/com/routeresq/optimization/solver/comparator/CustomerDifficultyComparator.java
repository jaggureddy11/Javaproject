package com.routeresq.optimization.solver.comparator;

import com.routeresq.optimization.solver.model.TimefoldCustomer;

import java.util.Comparator;

public class CustomerDifficultyComparator implements Comparator<TimefoldCustomer> {

    @Override
    public int compare(TimefoldCustomer a, TimefoldCustomer b) {
        // 1. Earlier delivery window start is harder
        int res = Integer.compare(a.getWindowStartMinutes(), b.getWindowStartMinutes());
        if (res != 0) return res;

        // 2. Earlier delivery window end is harder
        res = Integer.compare(a.getWindowEndMinutes(), b.getWindowEndMinutes());
        if (res != 0) return res;

        // 3. Heavier order weight is harder
        return b.getWeightKg().compareTo(a.getWeightKg());
    }
}
