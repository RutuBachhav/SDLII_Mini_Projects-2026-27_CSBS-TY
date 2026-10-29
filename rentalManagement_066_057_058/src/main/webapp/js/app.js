document.addEventListener('DOMContentLoaded',function(){
 document.querySelectorAll('[data-confirm]').forEach(function(el){el.addEventListener('click',function(e){if(!confirm(el.getAttribute('data-confirm')))e.preventDefault();});});
 document.querySelectorAll('[data-filter]').forEach(function(input){input.addEventListener('input',function(){var q=input.value.toLowerCase();var table=document.querySelector(input.getAttribute('data-filter'));if(!table)return;table.querySelectorAll('tbody tr').forEach(function(row){row.style.display=row.innerText.toLowerCase().includes(q)?'':'none';});});});
});
