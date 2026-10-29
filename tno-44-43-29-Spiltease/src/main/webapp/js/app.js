/* =====================================================================
   SplitEase front end
   - AngularJS 1.8 : routing (ngRoute), controllers, services, filters,
                     two-way data binding and form validation
   - jQuery 3.7    : AJAX for dashboard stats, animated counters,
                     toast notifications and the confirm dialog
   ===================================================================== */
(function () {
    'use strict';

    var API = 'api';   // relative: works at http://localhost:8080/SplitEase/

    var app = angular.module('splitEase', ['ngRoute']);

    /* ---------------- routes (single-page application) ---------------- */
    app.config(['$routeProvider', function ($routeProvider) {
        $routeProvider
            .when('/', { templateUrl: 'views/dashboard.html', controller: 'DashboardCtrl' })
            .when('/groups/:id', { templateUrl: 'views/group.html', controller: 'GroupCtrl' })
            .when('/people', { templateUrl: 'views/people.html', controller: 'PeopleCtrl' })
            .otherwise({ redirectTo: '/' });
    }]);

    /* ---------------- filters ---------------- */
    var inrFormat = new Intl.NumberFormat('en-IN', { style: 'currency', currency: 'INR', maximumFractionDigits: 2 });
    app.filter('inr', function () {
        return function (v) {
            var n = Math.abs(Number(v) || 0);
            return inrFormat.format(n);
        };
    });
    app.filter('initials', function () {
        return function (name) {
            if (!name) { return ''; }
            // use only words that start with a letter: "Goa Trip 2026" -> "GT"
            var p = name.trim().split(/\s+/).filter(function (w) { return /^[A-Za-z]/.test(w); });
            if (!p.length) { return name.trim().charAt(0).toUpperCase(); }
            return (p[0][0] + (p.length > 1 ? p[p.length - 1][0] : '')).toUpperCase();
        };
    });

    /* ---------------- jQuery UI helpers (wrapped as an Angular service) ---------------- */
    app.factory('UI', ['$q', function ($q) {
        return {
            toast: function (msg, kind) {
                var $t = $('<div class="toast"></div>').addClass(kind || 'ok').text(msg);
                $('#toasts').append($t);
                $t.fadeIn(200).delay(2600).fadeOut(400, function () { $t.remove(); });
            },
            confirm: function (title, text, yesLabel) {
                var d = $q.defer();
                $('#confirmTitle').text(title);
                $('#confirmText').text(text);
                $('#confirmYes').text(yesLabel || 'Delete');
                $('#overlay, #confirmModal').fadeIn(150);
                function close(ok) {
                    $('#overlay, #confirmModal').fadeOut(150);
                    $('#confirmYes, #confirmNo, #overlay').off('click');
                    if (ok) { d.resolve(); } else { d.reject(); }
                }
                $('#confirmYes').on('click', function () { close(true); });
                $('#confirmNo, #overlay').on('click', function () { close(false); });
                return d.promise;
            }
        };
    }]);

    /* ---------------- REST client ($http) ---------------- */
    app.factory('Api', ['$http', function ($http) {
        function data(r) { return r.data; }
        return {
            users: function () { return $http.get(API + '/users').then(data); },
            addUser: function (u) { return $http.post(API + '/users', u).then(data); },
            groups: function () { return $http.get(API + '/groups').then(data); },
            group: function (id) { return $http.get(API + '/groups/' + id).then(data); },
            createGroup: function (g) { return $http.post(API + '/groups', g).then(data); },
            addMember: function (id, userId) { return $http.post(API + '/groups/' + id + '/members', { userId: userId }).then(data); },
            expenses: function (id) { return $http.get(API + '/groups/' + id + '/expenses').then(data); },
            addExpense: function (id, e) { return $http.post(API + '/groups/' + id + '/expenses', e).then(data); },
            deleteExpense: function (eid) { return $http['delete'](API + '/expenses/' + eid); },
            plan: function (id) { return $http.get(API + '/groups/' + id + '/balances').then(data); },
            settlements: function (id) { return $http.get(API + '/groups/' + id + '/settlements').then(data); },
            settle: function (id, s) { return $http.post(API + '/groups/' + id + '/settlements', s).then(data); },
            exportCsv: function (id) { return $http.get(API + '/groups/' + id + '/export', { responseType: 'text' }).then(data); }
        };
    }]);

    function errText(r) {
        return (r && r.data && r.data.error) ? r.data.error : 'Something went wrong. Is the server running?';
    }

    /* ---------------- navigation ---------------- */
    app.controller('NavCtrl', ['$scope', '$location', function ($scope, $location) {
        $scope.isActive = function (p) {
            var cur = $location.path();
            return p === '/' ? (cur === '/' || cur.indexOf('/groups') === 0) : cur.indexOf(p) === 0;
        };
    }]);

    /* ---------------- dashboard ---------------- */
    app.controller('DashboardCtrl', ['$scope', 'Api', 'UI', '$location', function ($scope, Api, UI, $location) {
        $scope.groups = [];
        $scope.users = [];
        $scope.showNew = false;

        Api.groups().then(function (g) { $scope.groups = g; });

        // jQuery AJAX + jQuery.animate() counters for the stat cards
        $.getJSON(API + '/stats').done(function (s) {
            animateCount('#statSpent', s.totalSpent);
            animateCount('#statGroups', s.groups);
            animateCount('#statExpenses', s.expenses);
            animateCount('#statSettled', s.totalSettled);
        }).fail(function () { UI.toast('Could not load statistics', 'err'); });

        $scope.openNewGroup = function () {
            Api.users().then(function (u) {
                $scope.users = u;
                $scope.newG = { name: '', description: '', pick: {} };
                $scope.showNew = true;
            });
        };
        $scope.selectedCount = function () {
            if (!$scope.newG) { return 0; }
            return Object.keys($scope.newG.pick).filter(function (k) { return $scope.newG.pick[k]; }).length;
        };
        $scope.createGroup = function () {
            var ids = Object.keys($scope.newG.pick).filter(function (k) { return $scope.newG.pick[k]; }).map(Number);
            Api.createGroup({ name: $scope.newG.name, description: $scope.newG.description, memberIds: ids })
                .then(function (g) {
                    UI.toast('Group "' + g.name + '" created');
                    $location.path('/groups/' + g.id);
                }, function (r) { UI.toast(errText(r), 'err'); });
        };
    }]);

    function animateCount(sel, target) {
        var $el = $(sel);
        $({ n: 0 }).animate({ n: Number(target) || 0 }, {
            duration: 900,
            easing: 'swing',
            step: function (now) { $el.text(Math.round(now).toLocaleString('en-IN')); },
            complete: function () { $el.text(Math.round(Number(target) || 0).toLocaleString('en-IN')); }
        });
    }

    /* ---------------- group page ---------------- */
    app.controller('GroupCtrl', ['$scope', '$routeParams', 'Api', 'UI', '$q', function ($scope, $routeParams, Api, UI, $q) {
        var id = Number($routeParams.id);
        $scope.categories = ['Food', 'Travel', 'Stay', 'Shopping', 'Entertainment', 'Utilities', 'General'];
        $scope.splitTypes = [
            { key: 'EQUAL', label: 'Equally' },
            { key: 'EXACT', label: 'Exact ₹' },
            { key: 'PERCENT', label: 'By %' }
        ];

        function today() {
            var d = new Date();
            d.setMinutes(d.getMinutes() - d.getTimezoneOffset());
            return new Date(d.toISOString().slice(0, 10));
        }

        function resetForm() {
            $scope.ex = {
                description: '', amount: null, splitType: 'EQUAL', category: 'Food', date: today(),
                paidBy: $scope.group && $scope.group.members.length ? $scope.group.members[0].id : null
            };
            $scope.parts = ($scope.group ? $scope.group.members : []).map(function (m) {
                return { id: m.id, name: m.name, on: true, value: null, share: 0 };
            });
            $scope.recalc();
        }

        function load() {
            return $q.all([Api.group(id), Api.expenses(id), Api.plan(id), Api.settlements(id), Api.users()])
                .then(function (r) {
                    var first = !$scope.group;
                    $scope.group = r[0];
                    $scope.expenses = r[1];
                    $scope.plan = r[2];
                    $scope.settlements = r[3];
                    var memberIds = r[0].members.map(function (m) { return m.id; });
                    $scope.nonMembers = r[4].filter(function (u) { return memberIds.indexOf(u.id) < 0; });
                    var maxAbs = 0;
                    $scope.plan.balances.forEach(function (b) { maxAbs = Math.max(maxAbs, Math.abs(b.net)); });
                    $scope.maxAbs = maxAbs;
                    if (first || $scope.parts.length !== r[0].members.length) { resetForm(); }
                }, function (r) { UI.toast(errText(r), 'err'); });
        }

        $scope.barWidth = function (net) {
            if (!$scope.maxAbs) { return '0%'; }
            return (Math.abs(net) / $scope.maxAbs * 50).toFixed(1) + '%';
        };

        $scope.setType = function (t) {
            $scope.ex.splitType = t;
            $scope.parts.forEach(function (p) { p.value = null; });
            $scope.recalc();
        };

        // Live preview of the split. Mirrors the server logic exactly (all in paise).
        $scope.recalc = function () {
            if (!$scope.ex) { return; }
            var total = Math.round((Number($scope.ex.amount) || 0) * 100);
            var on = $scope.parts.filter(function (p) { return p.on; });
            $scope.parts.forEach(function (p) { p.share = 0; });
            var ok = on.length > 0 && total > 0, msg = '', sum = 0;

            if ($scope.ex.splitType === 'EQUAL' && on.length) {
                var base = Math.floor(total / on.length), rem = total - base * on.length;
                on.forEach(function (p, i) { p.share = (base + (i < rem ? 1 : 0)) / 100; });
                msg = on.length ? 'Split equally between ' + on.length : '';
            } else if ($scope.ex.splitType === 'EXACT') {
                on.forEach(function (p) { p.share = Number(p.value) || 0; sum += Math.round(p.share * 100); });
                ok = ok && sum === total;
                msg = ok ? 'Amounts match the bill' : '₹' + ((total - sum) / 100).toFixed(2) + ' left to assign';
            } else if ($scope.ex.splitType === 'PERCENT') {
                var pct = 0, assigned = 0;
                on.forEach(function (p) { pct += Number(p.value) || 0; });
                var shares = on.map(function (p) { var s = Math.floor(total * (Number(p.value) || 0) / 100); assigned += s; return s; });
                var left = total - assigned;
                on.forEach(function (p, i) { p.share = (shares[i] + (i < left ? 1 : 0)) / 100; });
                pct = Math.round(pct * 100) / 100;
                ok = ok && pct === 100;
                msg = ok ? 'Percentages total 100%' : 'Percentages total ' + pct + '% (need 100%)';
            }
            if (!on.length) { msg = 'Select at least one person'; }
            if (total <= 0) { msg = 'Enter the bill amount'; }
            $scope.shareTotal = on.reduce(function (a, p) { return a + p.share; }, 0);
            $scope.splitOk = ok;
            $scope.splitMsg = msg;
        };

        $scope.addExpense = function () {
            var d = $scope.ex.date, iso = d.getFullYear() + '-' + ('0' + (d.getMonth() + 1)).slice(-2) + '-' + ('0' + d.getDate()).slice(-2);
            var body = {
                description: $scope.ex.description,
                amount: Number($scope.ex.amount),
                paidBy: $scope.ex.paidBy,
                splitType: $scope.ex.splitType,
                category: $scope.ex.category,
                expenseDate: iso,
                participants: $scope.parts.filter(function (p) { return p.on; }).map(function (p) {
                    return { userId: p.id, value: $scope.ex.splitType === 'EQUAL' ? null : Number(p.value) || 0 };
                })
            };
            $scope.saving = true;
            Api.addExpense(id, body).then(function (e) {
                UI.toast('Added "' + e.description + '"');
                resetForm();
                if ($scope.ef) { $scope.ef.$setPristine(); $scope.ef.$setUntouched(); }
                return load();
            }, function (r) { UI.toast(errText(r), 'err'); })['finally'](function () { $scope.saving = false; });
        };

        $scope.removeExpense = function (e) {
            UI.confirm('Delete this expense?', '"' + e.description + '" will be removed and balances recalculated.')
                .then(function () { return Api.deleteExpense(e.id); })
                .then(function () { UI.toast('Expense deleted'); return load(); },
                      function (r) { if (r) { UI.toast(errText(r), 'err'); } });
        };

        $scope.settle = function (p) {
            UI.confirm('Record this payment?', p.fromName + ' paid ' + p.toName + ' ₹' + Number(p.amount).toFixed(2) + '.', 'Mark paid')
                .then(function () {
                    return Api.settle(id, { fromUser: p.fromUser, toUser: p.toUser, amount: p.amount, note: 'Settle-up suggestion' });
                })
                .then(function () { UI.toast('Payment recorded'); return load(); },
                      function (r) { if (r) { UI.toast(errText(r), 'err'); } });
        };

        $scope.addMember = function () {
            Api.addMember(id, $scope.sel.memberId).then(function () {
                UI.toast('Member added');
                $scope.sel.memberId = null;
                return load();
            }, function (r) { UI.toast(errText(r), 'err'); });
        };

        $scope.exportCsv = function () {
            Api.exportCsv(id).then(function (csv) {
                var blob = new Blob([csv], { type: 'text/csv' });
                var a = document.createElement('a');
                a.href = URL.createObjectURL(blob);
                a.download = $scope.group.name.replace(/\W+/g, '_') + '_summary.csv';
                a.click();
                UI.toast('CSV downloaded');
            });
        };

        $scope.parts = [];
        $scope.sel = { memberId: null };
        load();
    }]);

    /* ---------------- people ---------------- */
    app.controller('PeopleCtrl', ['$scope', 'Api', 'UI', function ($scope, Api, UI) {
        $scope.nu = {};
        function load() { Api.users().then(function (u) { $scope.users = u; }); }
        $scope.addUser = function () {
            Api.addUser($scope.nu).then(function (u) {
                UI.toast(u.name + ' added');
                $scope.nu = {};
                $scope.uf.$setPristine();
                $scope.uf.$setUntouched();
                load();
            }, function (r) { UI.toast(errText(r), 'err'); });
        };
        load();
    }]);
})();
