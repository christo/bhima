# common functions to be sourced by other scripts

function fail() {
  printf "  \\033[31mfailed\\033[0m\n"
}

function die() {
  fail
  exit 1
}

function ok() {
  printf " \\033[32mok\\033[0m\n"
}

# ping $1 and report colour result
function pingtest() {
  ping -c 2 "$1" >/dev/null 2>&1 && ok || fail
}
