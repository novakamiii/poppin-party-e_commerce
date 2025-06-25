// === Elements ===
const regionSelect = document.getElementById('region');
const provinceSelect = document.getElementById('province');
const citySelect = document.getElementById('city');
const barangaySelect = document.getElementById('barangay');
const lotNumberInput = document.getElementById('lotNumber');
const streetInput = document.getElementById('street');
const fullAddressInput = document.getElementById('fullAddress');
const addressDisplay = document.getElementById('addressDisplay');

// === Modal controls ===
const addressModal = document.getElementById('addressModal');
const saveAddressBtn = document.getElementById('saveAddressBtn');
const cancelAddressBtn = document.getElementById('cancelAddressBtn');

let addressSaved = false;


addressDisplay.addEventListener("click", () => {
    addressModal.style.display = "block";
    addressSaved = false; // ❌ user did not save anything
});

cancelAddressBtn.addEventListener("click", () => {
    addressModal.style.display = "none";
});

// === Save Address Logic ===
saveAddressBtn.addEventListener("click", () => {
    const lot = lotNumberInput.value.trim();
    const street = streetInput.value.trim();
    const composedAddress = [
        lot,
        street,
        barangaySelect.selectedOptions[0]?.textContent,
        citySelect.selectedOptions[0]?.textContent,
        provinceSelect.selectedOptions[0]?.textContent,
        regionSelect.selectedOptions[0]?.textContent
    ].filter(Boolean).join(", ");

    fullAddressInput.value = composedAddress;
    addressDisplay.value = composedAddress;
    addressModal.style.display = "none";
    addressSaved = true; // ✅ mark address as saved
});


// === PSGC Data Fetch ===
fetch('https://psgc.gitlab.io/api/regions/')
    .then(res => res.json())
    .then(regions => {
        regions.forEach(region => {
            const opt = document.createElement('option');
            opt.value = region.code;
            opt.textContent = region.name;
            regionSelect.appendChild(opt);
        });
    });

regionSelect.addEventListener('change', () => {
    provinceSelect.disabled = true;
    citySelect.disabled = true;
    barangaySelect.disabled = true;
    provinceSelect.innerHTML = '<option>Select Province</option>';

    fetch(`https://psgc.gitlab.io/api/regions/${regionSelect.value}/provinces/`)
        .then(res => res.json())
        .then(provinces => {
            provinceSelect.disabled = false;
            provinces.forEach(prov => {
                const opt = document.createElement('option');
                opt.value = prov.code;
                opt.textContent = prov.name;
                provinceSelect.appendChild(opt);
            });
        });
});

provinceSelect.addEventListener('change', () => {
    citySelect.disabled = true;
    barangaySelect.disabled = true;
    citySelect.innerHTML = '<option>Select City/Municipality</option>';

    fetch(`https://psgc.gitlab.io/api/provinces/${provinceSelect.value}/cities-municipalities/`)
        .then(res => res.json())
        .then(cities => {
            citySelect.disabled = false;
            cities.forEach(city => {
                const opt = document.createElement('option');
                opt.value = city.code;
                opt.textContent = city.name;
                citySelect.appendChild(opt);
            });
        });
});

citySelect.addEventListener('change', () => {
    barangaySelect.disabled = true;
    barangaySelect.innerHTML = '<option>Select Barangay</option>';

    fetch(`https://psgc.gitlab.io/api/cities-municipalities/${citySelect.value}/barangays/`)
        .then(res => res.json())
        .then(barangays => {
            barangaySelect.disabled = false;
            barangays.forEach(bg => {
                const opt = document.createElement('option');
                opt.value = bg.name;
                opt.textContent = bg.name;
                barangaySelect.appendChild(opt);
            });
        });
});
