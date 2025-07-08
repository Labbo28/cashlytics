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
