package com.splitease.rest;

import com.splitease.service.ExpenseService;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;

/** DELETE /api/expenses/{id} */
@Path("expenses")
public class ExpenseResource {

    private final ExpenseService service = new ExpenseService();

    @DELETE
    @Path("{id}")
    public void delete(@PathParam("id") int id) {
        service.deleteExpense(id);
    }
}
