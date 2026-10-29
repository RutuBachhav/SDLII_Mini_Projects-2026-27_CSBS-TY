/* CampusCare - restrained motion (decorative only; no dependency on App internals) */
(function () {
    'use strict';
    var reduce = window.matchMedia && window.matchMedia('(prefers-reduced-motion: reduce)').matches;

    /* Rotating word in the hero title */
    function buildRotator() {
        var el = document.querySelector('.rotator');
        if (!el || reduce) return;
        var words = ['fixed', 'heard', 'tracked', 'resolved'], i = 0;
        setInterval(function () {
            i = (i + 1) % words.length;
            el.classList.remove('swap');
            void el.offsetWidth;
            el.textContent = words[i];
            el.classList.add('swap');
        }, 2400);
    }

    /* Count-up numbers & staggered reveal in the portal */
    function countUp(el) {
        if (el.dataset.counted) return;
        el.dataset.counted = '1';
        var target = parseInt(el.textContent.replace(/[^\d]/g, ''), 10);
        if (isNaN(target) || reduce) return;
        var start = null, dur = 900;
        function tick(ts) {
            if (start === null) start = ts;
            var p = Math.min((ts - start) / dur, 1), e = 1 - Math.pow(1 - p, 3);
            el.textContent = Math.round(target * e);
            if (p < 1) requestAnimationFrame(tick);
        }
        el.textContent = '0';
        requestAnimationFrame(tick);
    }

    var io = 'IntersectionObserver' in window ? new IntersectionObserver(function (entries) {
        entries.forEach(function (en) {
            if (en.isIntersecting) { en.target.classList.add('in'); io.unobserve(en.target); }
        });
    }, { threshold: 0.05 }) : null;

    function enhanceContent(root) {
        root.querySelectorAll('.stat-value').forEach(countUp);
        var i = 0;
        root.querySelectorAll('.stat-card, .action-card, .card, .profile-card').forEach(function (n) {
            if (n.classList.contains('reveal')) return;
            n.classList.add('reveal');
            n.style.transitionDelay = Math.min(i++ * 60, 480) + 'ms';
            if (io) io.observe(n); else n.classList.add('in');
        });
        root.querySelectorAll('tbody tr').forEach(function (tr, k) {
            tr.style.animationDelay = Math.min(k * 40, 400) + 'ms';
        });
    }

    function watchPortal() {
        var main = document.getElementById('main-view-content');
        if (!main) return;
        var busy = false;
        new MutationObserver(function () {
            if (busy) return;
            busy = true;
            requestAnimationFrame(function () { busy = false; enhanceContent(main); });
        }).observe(main, { childList: true });
    }

    /* Button ripple */
    document.addEventListener('click', function (e) {
        var b = e.target.closest && e.target.closest('.btn');
        if (!b || reduce) return;
        var r = b.getBoundingClientRect(), s = Math.max(r.width, r.height) / 2;
        var d = document.createElement('span');
        d.className = 'ripple';
        d.style.cssText = 'width:' + s + 'px;height:' + s + 'px;left:' + (e.clientX - r.left - s / 2) + 'px;top:' + (e.clientY - r.top - s / 2) + 'px';
        b.appendChild(d);
        setTimeout(function () { d.remove(); }, 600);
    });

    function init() { buildRotator(); watchPortal(); }
    if (document.readyState === 'loading') document.addEventListener('DOMContentLoaded', init); else init();
})();
