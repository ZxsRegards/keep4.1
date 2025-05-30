window.onload = function () {
  var STORAGE_SWITCH_KEY = "STORAGE_SWITCH_KEY";

  var switchEl = document.querySelector("#switch-loud");
  var mainItemEl = document.querySelector("#main-item");
  var tipsDialogEl = document.querySelector(".example-headline-slot");
  var moreBtnEl = document.querySelector(".more-btn");
  var githubEl = document.querySelector(".github-item");
  var zxsEl = document.querySelector(".github-zxs");
  // var hideBgItemEl = document.querySelector("#hide-bg-item");
  var switchHideEl = document.querySelector("#switch-hide");
  var canSwitch = true;
  var switchCache = true;
  try {
    var c = JSON.parse(localStorage.getItem(STORAGE_SWITCH_KEY));
    switchCache = typeof c === "boolean" ? c : true;
  } catch (err) {
    console.log(err);
  }
  switchEl.checked = switchCache;

  if (switchCache) {
    switchProcessStatus("1");
  } else {
    switchProcessStatus("0");
  }

  function debounce(func, wait) {
    let timeout;
    return function (...args) {
      const context = this;
      clearTimeout(timeout);
      timeout = setTimeout(() => func.apply(context, args), wait);
    };
  }

  var debounceSwitchProcessStatus = debounce(switchProcessStatus, 30);

  mainItemEl.addEventListener("click", () => {
    switchEl.checked = !switchEl.checked;
    localStorage.setItem(STORAGE_SWITCH_KEY, JSON.stringify(switchEl.checked));
    switchProcessStatus(switchEl.checked ? "1" : "0");
  });

  switchEl.addEventListener("click", (event) => {
    event.stopPropagation();
    if (!canSwitch) return;
    switchHideEl = setTimeout(() => {
      canSwitch = true;
    });
    setTimeout(() => {
      try {
        localStorage.setItem(
          STORAGE_SWITCH_KEY,
          JSON.stringify(switchEl.checked)
        );
      } catch (e) {
        console.log(e);
      }
      debounceSwitchProcessStatus(switchEl.checked ? "1" : "0");
    });
  });

   //hideBgItemEl.addEventListener("click", () => {
   //  switchHideEl.checked = !switchHideEl.checked;
   //  switchBgHideStatus(switchHideEl.checked ? "1" : "0");
   //});

  switchHideEl.addEventListener("click", (event) => {
    event.stopPropagation();
    setTimeout(() => {
      switchBgHideStatus(switchHideEl.checked ? "1" : "0");
    });
  });

  moreBtnEl.addEventListener("click", () => {
    //AndroidFunction.openWebUrl("https://baidu.com");
  });

  githubEl.addEventListener("click", () => {
    AndroidFunction.openWebUrl("https://github.com/aimmarc/keep_4");
  });

 zxsEl.addEventListener("click", () => {
    AndroidFunction.openWebUrl("https://github.com/ZxsRegards");
  });
  function switchProcessStatus(status = "0") {
    try {
      AndroidFunction.setLoudspeaker(status);
    } catch (err) {
      console.log(err);
    }
  }

  function switchBgHideStatus(status = "0") {
    try {
      AndroidFunction.setAppBackground(status);
    } catch (err) {
      console.log(err);
    }
  }
};
