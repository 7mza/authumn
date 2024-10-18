(function () {
  [...document.querySelectorAll('[data-bs-toggle="tooltip"]')].map(
    (tooltipTriggerEl) => new bootstrap.Tooltip(tooltipTriggerEl),
  );
})();
