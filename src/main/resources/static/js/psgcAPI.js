const regionSelect = document.getElementById('region');
const provinceSelect = document.getElementById('province');
const citySelect = document.getElementById('city');
const barangaySelect = document.getElementById('barangay');
const fullAddressInput = document.getElementById('fullAddress');

// Fetch and populate regions
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

//Address Combiner
document.querySelector("form").addEventListener("submit", function () {
    const lot = document.getElementById("lotNumber").value.split(',').map(s => s.trim()).filter(Boolean).join(', ');
    const street = document.getElementById("street").value.trim();

    const selectedAddress = [
        lot,
        street,
        barangaySelect.selectedOptions[0]?.textContent,
        citySelect.selectedOptions[0]?.textContent,
        provinceSelect.selectedOptions[0]?.textContent,
        regionSelect.selectedOptions[0]?.textContent
    ].filter(Boolean).join(", ");

    fullAddressInput.value = selectedAddress;
});

const emailInput = document.getElementById("email");
const usernameInput = document.getElementById("username");
const phoneInput = document.getElementById("phone");

// === Helpers ===
function isValidEmail(email) {
    return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email);
}

function isValidUsername(username) {
    return /^[a-zA-Z0-9]{3,20}$/.test(username);
}

function isValidPhone(phone) {
    return /^(09\d{9}|\+639\d{9})$/.test(phone);
}

// === Email Check ===
emailInput.addEventListener("blur", () => {
    const email = emailInput.value.trim();
    if (!email) {
        emailInput.setCustomValidity(""); // allow empty if not required
        return;
    }

    if (!isValidEmail(email)) {
        emailInput.setCustomValidity("Invalid email format.");
        return;
    }

    fetch(`/api/check/email?email=${encodeURIComponent(email)}`)
        .then(res => res.json())
        .then(data => {
            emailInput.setCustomValidity(data.exists ? "Email is already in use." : "");
        })
        .catch(() => {
            emailInput.setCustomValidity("Server error during email check.");
        });
});

// === Username Check ===
usernameInput.addEventListener("blur", () => {
    const username = usernameInput.value.trim();
    if (!username) {
        usernameInput.setCustomValidity(""); // allow empty if not required
        return;
    }

    if (!isValidUsername(username)) {
        usernameInput.setCustomValidity("Invalid username (3-20 alphanumeric only).");
        return;
    }

    fetch(`/api/check/username?username=${encodeURIComponent(username)}`)
        .then(res => res.json())
        .then(data => {
            usernameInput.setCustomValidity(data.exists ? "Username is already taken." : "");
        })
        .catch(() => {
            usernameInput.setCustomValidity("Server error during username check.");
        });
});

// === Phone Check ===
phoneInput.addEventListener("blur", function () {
    const phone = this.value.trim();
    if (!phone) {
        this.setCustomValidity(""); // don't block if it's empty (let "required" handle it)
        return;
    }

    if (!isValidPhone(phone)) {
        this.setCustomValidity("Please enter a valid PH number (e.g. 09123456789 or +639123456789)");
    } else {
        this.setCustomValidity("");
    }
});
