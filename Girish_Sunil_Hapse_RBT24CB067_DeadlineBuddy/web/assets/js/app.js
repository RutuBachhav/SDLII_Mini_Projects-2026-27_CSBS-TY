/* ===========================================================================
   DeadlineBuddy - progressive enhancement only.

   Every page works with JavaScript disabled: the filter bar is a GET form with
   an Apply button, and the destructive actions each have their own confirmation
   page. What follows only removes clicks.
   =========================================================================== */
(function () {
    "use strict";

    document.documentElement.classList.add("has-js");

    /* Anything marked .needs-js is hidden by CSS until we get here. */
    document.querySelectorAll(".needs-js").forEach(function (el) {
        el.style.display = "";
    });

    /* ---------------------------------------------------------------------
       Filter bar: changing a dropdown applies it, instead of having to reach
       for Apply. The button stays, for keyboards and for no-JS.
       --------------------------------------------------------------------- */
    var filterForm = document.querySelector("[data-autosubmit]");
    if (filterForm) {
        filterForm.querySelectorAll("select").forEach(function (select) {
            select.addEventListener("change", function () {
                filterForm.submit();
            });
        });
    }

    /* ---------------------------------------------------------------------
       A second guard on destructive submits. The server still has its own
       confirmation page, so declining here just leaves the page as it was.
       --------------------------------------------------------------------- */
    document.querySelectorAll("form[data-confirm]").forEach(function (form) {
        form.addEventListener("submit", function (event) {
            if (!window.confirm(form.getAttribute("data-confirm"))) {
                event.preventDefault();
            }
        });
    });

    /* ---------------------------------------------------------------------
       Colour swatches on the subject form: clicking one ticks its radio and
       updates the preview dot. Without JS the radios work on their own.
       --------------------------------------------------------------------- */
    var swatches = document.querySelectorAll("[data-swatch]");
    var preview = document.querySelector("[data-swatch-preview]");
    if (swatches.length && preview) {
        var paint = function () {
            swatches.forEach(function (input) {
                if (input.checked) {
                    preview.style.background = input.value;
                }
            });
        };
        swatches.forEach(function (input) {
            input.addEventListener("change", paint);
        });
        paint();
    }

    /* ---------------------------------------------------------------------
       Dismiss a flash message without a round trip.
       --------------------------------------------------------------------- */
    document.querySelectorAll("[data-dismiss-flash]").forEach(function (button) {
        button.addEventListener("click", function () {
            var alert = button.closest(".alert");
            if (alert) {
                alert.remove();
            }
        });
    });
}());
