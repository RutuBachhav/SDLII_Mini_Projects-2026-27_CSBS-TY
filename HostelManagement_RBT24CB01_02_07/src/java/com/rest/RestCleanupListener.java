package com.rest;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

@WebListener
public class RestCleanupListener implements ServletContextListener {

    @Override
    public void contextDestroyed(ServletContextEvent sce) {

        try {
            HostelResource.closeFactory();
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            RoomResource.closeFactory();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
