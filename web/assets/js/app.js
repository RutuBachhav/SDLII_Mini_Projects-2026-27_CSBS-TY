/* A small amount of progressive polish. The application works with scripting
   switched off: every action is a plain form or a link. */

(function () {
  'use strict';

  // Stop a double click from posting a claim, a decision or a return twice.
  // The server also guards against it, by checking the status it is coming
  // from, but a disabled button is clearer feedback than an error message.
  document.querySelectorAll('form[method="post"]').forEach(function (form) {
    form.addEventListener('submit', function () {
      var buttons = form.querySelectorAll('button[type="submit"]');
      window.setTimeout(function () {
        buttons.forEach(function (b) {
          b.disabled = true;
          if (!b.dataset.kept) {
            b.dataset.kept = b.textContent;
            b.textContent = 'Working…';
          }
        });
      }, 0);
    });
  });

  // Ask before closing a report or suspending an account.
  document.querySelectorAll('button.btn-danger').forEach(function (button) {
    button.addEventListener('click', function (event) {
      var label = (button.textContent || 'continue').trim().toLowerCase();
      if (!window.confirm('Are you sure you want to ' + label + '?')) {
        event.preventDefault();
      }
    });
  });

  // Keep the date inputs out of the future; the server enforces this too.
  var today = new Date().toISOString().slice(0, 10);
  document.querySelectorAll('input[type="date"]').forEach(function (input) {
    if (!input.max) {
      input.max = today;
    }
  });
})();
