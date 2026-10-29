package com.splitease.service;

import com.splitease.dao.StatsDAO;
import com.splitease.model.DashboardStats;

/** Dashboard totals. */
public class StatsService {

    private final StatsDAO statsDAO = new StatsDAO();

    public DashboardStats load() {
        return statsDAO.load();
    }
}
