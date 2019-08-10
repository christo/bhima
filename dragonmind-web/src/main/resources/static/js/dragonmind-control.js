document.addEventListener('init', function(event) {
  var page = event.target;

  if (page.id === 'home') {

    page.querySelector('#menu_button').onclick = function() {
      document.querySelector('#menu').open();
    };

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
      var carousel = $("#allPrograms");
      carousel.empty();
      data.forEach(function(pi) {
          var id = pi.id;
          var name = pi.name;
          var type = pi.type;
          var thumbnail = pi.thumbnail;
          var item = '<ons-carousel-item tappable id="'+ id +'" onclick="requestProgram(this.id)" class="programItem"><div>' + type + " " + name + "</div><img src='data:image/jpg;charset=utf-8;base64," + thumbnail + "'/></ons-carousel-item>";
          carousel[0].innerHTML += '\n' + item;

      });

      carousel[0].refresh();
    });
  }


});

var pid; // TODO remove global state

function openMovieDetails(id) {
  document.querySelector('#myNavigator').pushPage('movie_details.html', {data: {title: id}});
}

function requestProgram(id) {
  var dialog = document.getElementById('run-program-dialog');
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

var okProgramDialog = function(id) {
  var dialog = document.getElementById(id)
  dialog.hide();
  runProgram(pid);
};


function openHome() {
  document.querySelector('#myNavigator').pushPage('home_splitter.html');
}

function openControl() {
  document.querySelector('#myNavigator').pushPage('control_splitter.html');
}

function openWiring() {
    document.querySelector("#myNavigator").pushPage('wiring.html');
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