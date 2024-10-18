(function () {
  [...document.querySelectorAll('[data-bs-toggle="popover"]')].map(
    (popoverTriggerEl) => new bootstrap.Popover(popoverTriggerEl),
  );
})();
