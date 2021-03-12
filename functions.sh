# common functions to be sourced by other scripts

function die() {
  printf "  \\033[31mfailed\\033[0m\n"
  exit 1
}

function ok() {
  printf " \\033[32mok\\033[0m\n"
}

