// https://github.com/bigskysoftware/htmx-extensions/blob/main/src/json-enc/json-enc.js
// form to json + xsrf token
(function () {
  let api;
  htmx.defineExtension("json-enc", {
    init: function (apiRef) {
      api = apiRef;
    },
    onEvent: function (name, evt) {
      if (name === "htmx:configRequest") {
        evt.detail.headers["Content-Type"] = "application/json";
        var xsrf = document.querySelector('input[name="_csrf"]').value;
        evt.detail.headers["X-XSRF-TOKEN"] = xsrf;
      }
    },
    encodeParameters: function (xhr, parameters, elt) {
      xhr.overrideMimeType("text/json");
      const vals = api.getExpressionVars(elt);
      const object = {};
      parameters.forEach(function (value, key) {
        const typedValue = Object.hasOwn(vals, key) ? vals[key] : value;
        if (Object.hasOwn(object, key)) {
          if (!Array.isArray(object[key])) {
            object[key] = [object[key]];
          }
          object[key].push(typedValue);
        } else {
          object[key] = typedValue;
        }
      });
      return JSON.stringify(object);
    },
  });
})();
