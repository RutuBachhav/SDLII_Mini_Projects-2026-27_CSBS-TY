
async function load() {
    const me = await fetch('api/auth').then(r => r.json());

    document.getElementById('welcome').textContent =
        'Welcome, ' + me.name;

    const p = await fetch('api/properties').then(r => r.json());

    document.getElementById('count').textContent = p.length;

    document.getElementById('table').innerHTML = p.map(x =>
        `<tr>
            <td>${x.id}</td>
            <td>${x.title}</td>
            <td>${x.city}</td>
            <td>${x.type}</td>
            <td>₹${x.price || x.rent}</td>
            <td><span class="tag">${x.status}</span></td>
        </tr>`
    ).join('');
}

async function addProperty(e) {
    e.preventDefault();

    let f = new FormData(e.target);
    let o = Object.fromEntries(f);

    // Validate contact number
    const contactNumber = (o.contactNumber || '').trim();

    if (
        !/^[0-9+() -]{7,20}$/.test(contactNumber) ||
        contactNumber.replace(/[^0-9]/g, '').length < 7
    ) {
        alert('Please enter a valid contact number.');
        return;
    }

    o.contactNumber = contactNumber;
    o.rent = +o.rent || 0;
    o.price = +o.price || 0;
    o.area = +o.area || 0;

    const r = await fetch('api/properties', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(o)
    });

    if (r.ok) {
        e.target.reset();
        await load();
        alert('Property saved to MySQL');
    } else {
        const result = await r.json().catch(() => ({}));
        alert(result.error || 'Could not save property');
    }
}

load();
