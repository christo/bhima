document.addEventListener('init', function(event) {
    const page = event.target;
    let mb = page.querySelector('#menu_button');
    if (mb != null) {
        mb.onclick = function () {
            document.querySelector('#menu').open();
        };
    }

  if (page.id === 'settings') {

    //The postchange event listener is used for changing the dots when the carousel is changed
    page.querySelector('#carousel').addEventListener("postchange", function() {
      page.querySelector('#dot0').classList.remove("circle_current");
      page.querySelector('#dot1').classList.remove("circle_current");
      page.querySelector('#dot2').classList.remove("circle_current");

      page.querySelector('#dot' + page.querySelector('#carousel').getActiveIndex()).classList.add("circle_current");

    });
  } else if (page.id === 'movie_details') {
    page.querySelector('#movie_title').innerHTML = page.data.title;
  } else if (page.id === 'login') {
    getStatus();
    getCurrentProgram();
  } else if (page.id === 'control') {
    getCurrentProgram();
    $.get("api/bhima/programs", function(data) {
      const carousel = $("#allPrograms");
      carousel.empty();
      data.forEach(function(pi) {
          const id = pi.id;
          const name = pi.name;
          const type = pi.type;
          const thumbnail = pi.thumbnail;
          const item = '<ons-carousel-item tappable id="'+ id +'" onclick="requestProgram(this.id)" class="programItem"><div>' + type + " " + name + "</div><img src='data:image/jpg;charset=utf-8;base64," + thumbnail + "'/></ons-carousel-item>";
          carousel[0].innerHTML += '\n' + item;

      });

      carousel[0].refresh();
    });
  } else if (page.id === 'wiring') {
    $.get("api/bhima/effectiveWiring", function(data) {
        const ports = $("#portList");
        ports.empty();
        for (s in data) {
            const item = '<li><div class="port circle">'+s+ ' : '+data[s]+'</div></li>'
            ports[0].innerHTML += '\n' + item;
        }
        ports[0].refresh && ports[0].refresh();
    });
  }


});

let pid; // TODO remove global state

function openMovieDetails(id) {
  document.querySelector('#myNavigator').pushPage('movie_details.html', {data: {title: id}});
}

function requestProgram(id) {
  const dialog = document.getElementById('run-program-dialog');
  pid = id;
  if (dialog) {
    dialog.show();
  } else {
    ons.createElement('runProgramDialog.html', { append: true })
      .then(function(dialog) {
        dialog.show();
      });
  }
}

const okProgramDialog = function(id) {
  const dialog = document.getElementById(id)
  dialog.hide();
  runProgram(pid);
};


function openSettings() {
  document.querySelector('#myNavigator').pushPage('settings_splitter.html');
}

function openControl() {
  document.querySelector('#myNavigator').pushPage('control_splitter.html');
}

function openWiring() {
    document.querySelector("#myNavigator").pushPage('wiring_splitter.html');
}

function goBack() {
  document.querySelector('#menu').close().then(function() {
    document.querySelector('#myNavigator').popPage()
  });
}

function getStatus() {
    $.get("api/bhima/status", function(data) {
       $("#status").text(data);
    }, "json").fail(function(data, status) {
        $("#status").text("error " + status);
    });
}

function getCurrentProgram() {
    $.get("api/bhima/program", function(data) {
        $("#currentProgramName").text(data.name);
        $("#currentProgramType").text(data.type);
        $("#currentProgramThumbnail").html("<img src='data:image/jpg;charset=utf-8;base64," + data.thumbnail + "'/>");

    });
}

function runProgram(id) {
    console.log("requesting to run program " + id);
    $.post("/api/bhima/runProgram", {id: id}, function(data) {
        console.log("program running: " + data.id);
    });
}