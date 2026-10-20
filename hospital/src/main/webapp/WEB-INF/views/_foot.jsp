<%@page contentType="text/html" pageEncoding="UTF-8"%>
    </div>
</main>
<footer class="site-footer">
    <div class="wrap">
        <p>CityCare Hospital — outpatient appointments. For emergencies, call your local emergency number.</p>
    </div>
</footer>
<dialog class="confirm" id="confirm-dialog" aria-labelledby="confirm-title">
    <h2 id="confirm-title">Are you sure?</h2>
    <p id="confirm-text">This cannot be undone.</p>
    <div class="row">
        <form method="dialog" class="inline-form"><button class="btn btn-secondary" value="cancel">Keep it</button></form>
        <button class="btn btn-danger" id="confirm-ok">Confirm</button>
    </div>
</dialog>
<script src="<%= request.getContextPath() %>/assets/app.js" defer></script>
</body>
</html>
