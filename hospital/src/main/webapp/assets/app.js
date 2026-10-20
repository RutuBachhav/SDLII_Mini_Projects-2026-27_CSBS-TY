/* Small progressive enhancements. All actions work without this file. */
(function () {
  "use strict";

  // Dismissible notices (success only auto-keeps a button; errors persist until dismissed).
  document.addEventListener("click", function (e) {
    var btn = e.target.closest("[data-dismiss]");
    if (btn) {
      var note = btn.closest(".notice");
      if (note) {
        note.remove();
      }
    }
  });

  // Busy state on submit so double-clicks do not double-post.
  // Also announce it in the polite live region for screen-reader users.
  document.addEventListener("submit", function (e) {
    var form = e.target;
    var btn = form.querySelector('button[type="submit"]');
    if (btn && !btn.disabled) {
      btn.setAttribute("aria-busy", "true");
    }
    var status = document.getElementById("status-region");
    if (status) {
      status.textContent = "Sending your request…";
    }
  });

  // After a failed submit, land focus on the first invalid field so keyboard
  // and screen-reader users start where the fix is needed.
  var firstInvalid = document.querySelector("[aria-invalid='true']");
  if (firstInvalid && firstInvalid.focus) { firstInvalid.focus(); }

  // Confirm dialog for destructive actions (replaces window.confirm).
  var dialog = document.getElementById("confirm-dialog");
  var dialogTitle = document.getElementById("confirm-title");
  var dialogText = document.getElementById("confirm-text");
  var dialogOk = document.getElementById("confirm-ok");
  var pendingForm = null;
  var lastTrigger = null;

  document.addEventListener("click", function (e) {
    var trigger = e.target.closest("[data-confirm]");
    if (!trigger) {
      return;
    }
    var form = trigger.closest("form");
    if (!form || !dialog || typeof dialog.showModal !== "function") {
      return; // Without dialog support, let the form submit normally.
    }
    e.preventDefault();
    pendingForm = form;
    lastTrigger = trigger;
    var what = trigger.getAttribute("data-confirm");
    if (dialogTitle) {
      dialogTitle.textContent = trigger.getAttribute("data-confirm-title") || "Are you sure?";
    }
    if (dialogText) {
      dialogText.textContent = what || "This cannot be undone.";
    }
    if (dialogOk) {
      dialogOk.textContent = trigger.getAttribute("data-confirm-ok") || "Confirm";
    }
    dialog.showModal();
  });

  if (dialogOk) {
    dialogOk.addEventListener("click", function () {
      if (pendingForm) {
        var btn = pendingForm.querySelector('button[type="submit"]');
        if (btn) {
          btn.setAttribute("aria-busy", "true");
        }
        pendingForm.submit();
      }
    });
  }

  // Return focus to the trigger when the dialog closes, so keyboard
  // users do not land back at the top of the page.
  if (dialog) {
    dialog.addEventListener("close", function () {
      pendingForm = null;
      if (lastTrigger && lastTrigger.focus) {
        lastTrigger.focus();
        lastTrigger = null;
      }
    });
  }
})();
