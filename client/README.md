# Mobile web client 

Application for controlling the dragon effects and monitoring
the system. Written in `React.js`, this replaces the previous
web client in the `dragonmind-web` module. The server-side of
`dragonmind-web` remains, providing in-process access to the 
Java API behind a REST API and this module calls those 
endpoints.

## Tech

Pretty standard mobile-first, React Single Page App, using
Material UI with a dark-mode theme and `react-router-dom`.

Code is ES6, transpiled with Babel and unit-tested with jest.

## TODO

* run program call (parity functionality)
* modify build to put the client-side bundle in place for the 
* view / modify settings, especially global brightness slider
* view effective wiring
* web vitals & health check 
* full system status including uptime, load, disk, relevant log 
tails etc.
* admin authentication (stretch)
