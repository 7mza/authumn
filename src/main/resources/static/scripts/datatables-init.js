(function () {
  $("#table").dataTable({
    columnDefs: [
      {
        targets: "no-sort",
        orderable: false,
      },
    ],
  });
})();
