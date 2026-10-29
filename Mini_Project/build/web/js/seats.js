document.addEventListener("DOMContentLoaded", function () {
    var form = document.getElementById("seatForm");
    if (!form) {
        return;
    }
    var price = Number(form.getAttribute("data-price") || "0");
    var max = 8;
    var boxes = form.querySelectorAll('input[name="seats"]');
    var countEl = document.getElementById("seatCount");
    var totalEl = document.getElementById("seatTotal");
    var button = document.getElementById("bookBtn");

    function selectedCount() {
        var count = 0;
        boxes.forEach(function (box) {
            if (box.checked) {
                count++;
            }
        });
        return count;
    }

    function refresh() {
        var count = selectedCount();
        if (count > max) {
            boxes.forEach(function (box) {
                if (box === document.activeElement) {
                    box.checked = false;
                }
            });
            count = selectedCount();
        }
        countEl.textContent = String(count);
        totalEl.textContent = "₹" + Math.round(price * count).toLocaleString("en-IN");
        if (button) {
            button.disabled = count === 0;
        }
    }

    boxes.forEach(function (box) {
        box.addEventListener("change", refresh);
    });
    refresh();
});
