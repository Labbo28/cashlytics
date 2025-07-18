function toggleAccountForm() {
	const form = document.getElementById('addAccountForm');
	if (form.style.display === 'none' || form.style.display === '') {
		form.style.display = 'block';
		form.scrollIntoView({ behavior: 'smooth' });
	} else {
		form.style.display = 'none';
	}
}

function toggleTransactionForm() {
	const form = document.getElementById('addTransactionForm');
	const noTransDiv = document.getElementById('noTransactionDiv');
	if (form.style.display === 'none' || form.style.display === '') {
		form.style.display = 'block';
		form.scrollIntoView({ behavior: 'smooth' });
		// Nascondi il messaggio "Nessuna transazione"
		if (noTransDiv) {
			noTransDiv.style.display = 'none';
		}
	} else {
		form.style.display = 'none';
		// Mostra di nuovo il messaggio se la lista è vuota
		if (noTransDiv) {
			noTransDiv.style.display = 'block';
		}
	}
}

function toggleBudgetForm() {
	const form = document.getElementById('addBudgetForm');
	const div = document.getElementById('noBudgetDiv');
	if (form.style.display === 'none' || form.style.display === '') {
		form.style.display = 'block';
		form.scrollIntoView({ behavior: 'smooth' });
		if (div) {
			div.style.display = 'none';
		}
	} else {
		form.style.display = 'none';
		if (div) {
			div.style.display = 'block';
		}
	}
}

document.addEventListener('DOMContentLoaded', function () {
	const alerts = document.querySelectorAll('.alert');
	alerts.forEach(alert => {
		setTimeout(() => {
			alert.style.transition = 'opacity 0.5s ease';
			alert.style.opacity = '0';
			setTimeout(() => alert.remove(), 500);
		}, 5000);
	});
});

function toggleMenu(button) {
	const dropdown = button.nextElementSibling;
	const allDropdowns = document.querySelectorAll('.menu-dropdown');
	allDropdowns.forEach(menu => {
		if (menu !== dropdown) {
			menu.style.display = 'none';
		}
	});
	dropdown.style.display = dropdown.style.display === 'block' ? 'none' : 'block';
}

// Chiude il menu cliccando fuori
document.addEventListener('click', function (e) {
	const isMenu = e.target.closest('.menu-wrapper');
	if (!isMenu) {
		document.querySelectorAll('.menu-dropdown').forEach(menu => {
			menu.style.display = 'none';
		});
	}
});

function handleMerchantSelection(value) {
	const newMerchantForm = document.getElementById('newMerchantForm');
	const merchantNameInput = document.getElementById('merchantName');

	if (value === 'add_new') {
		// Mostra il form per nuovo merchant
		newMerchantForm.style.display = 'block';
		merchantNameInput.focus();

		// Resetta il valore del select per evitare problemi di validazione
		document.getElementById('merchantId').value = '';
	} else {
		// Nascondi il form per nuovo merchant
		newMerchantForm.style.display = 'none';
		merchantNameInput.value = '';
	}
}

function cancelNewMerchant() {
	const newMerchantForm = document.getElementById('newMerchantForm');
	const merchantSelect = document.getElementById('merchantId');
	const merchantNameInput = document.getElementById('merchantName');

	// Nascondi il form
	newMerchantForm.style.display = 'none';

	// Resetta i valori
	merchantSelect.value = '';
	merchantNameInput.value = '';
}
