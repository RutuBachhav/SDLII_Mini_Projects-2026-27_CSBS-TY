package com.splitease.rest;

import com.splitease.dto.ExpenseRequest;
import com.splitease.dto.GroupRequest;
import com.splitease.dto.MemberRequest;
import com.splitease.model.Expense;
import com.splitease.model.Group;
import com.splitease.model.Settlement;
import com.splitease.model.SettlementPlan;
import com.splitease.service.BalanceService;
import com.splitease.service.ExpenseService;
import com.splitease.service.ExportService;
import com.splitease.service.GroupService;
import com.splitease.service.SettlementService;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import java.util.List;

/** REST endpoints for groups and everything inside a group:  /api/groups */
@Path("groups")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class GroupResource {

    private final GroupService groups = new GroupService();
    private final ExpenseService expenses = new ExpenseService();
    private final BalanceService balances = new BalanceService();
    private final SettlementService settlements = new SettlementService();
    private final ExportService export = new ExportService();

    @GET
    public List<Group> list() {
        return groups.listGroups();
    }

    @POST
    public Group create(GroupRequest req) {
        return groups.createGroup(req);
    }

    @GET
    @Path("{id}")
    public Group get(@PathParam("id") int id) {
        return groups.getGroup(id);
    }

    @DELETE
    @Path("{id}")
    public void delete(@PathParam("id") int id) {
        groups.deleteGroup(id);
    }

    /** POST /api/groups/3/members {"userId": 7} */
    @POST
    @Path("{id}/members")
    public Group addMember(@PathParam("id") int id, MemberRequest req) {
        return groups.addMember(id, req.getUserId());
    }

    @GET
    @Path("{id}/expenses")
    public List<Expense> listExpenses(@PathParam("id") int id) {
        return expenses.listExpenses(id);
    }

    /** POST /api/groups/3/expenses  (see ExpenseRequest for the JSON shape) */
    @POST
    @Path("{id}/expenses")
    public Expense addExpense(@PathParam("id") int id, ExpenseRequest req) {
        return expenses.addExpense(id, req);
    }

    /** GET /api/groups/3/balances : balances + simplified settle-up plan */
    @GET
    @Path("{id}/balances")
    public SettlementPlan balances(@PathParam("id") int id) {
        return balances.buildPlan(id);
    }

    @GET
    @Path("{id}/settlements")
    public List<Settlement> listSettlements(@PathParam("id") int id) {
        return settlements.listSettlements(id);
    }

    /** POST /api/groups/3/settlements {"fromUser":2,"toUser":1,"amount":450.00} */
    @POST
    @Path("{id}/settlements")
    public Settlement settle(@PathParam("id") int id, Settlement s) {
        return settlements.record(id, s);
    }

    /** GET /api/groups/3/export : CSV summary for Excel */
    @GET
    @Path("{id}/export")
    @Produces("text/csv")
    public String exportCsv(@PathParam("id") int id) {
        return export.groupCsv(id);
    }
}
