# Mobile web client 

Application for controlling the dragon effects and monitoring the system written in `React.js`. The server-side REST-API
is in the `dragonmind-web` module which provides in-process access to the Java API behind a REST API and this module
calls those endpoints. For dev-mode, run this with `npm start` or `gradle npm_run_start` and interact with it on port
3000. In production-like builds, find it on the same port as the REST API (probably port 9000), being served by
`dragonmind-web`.

## Tech

Pretty standard mobile-first, React Single Page App, using
Material UI with a dark-mode theme.

Code is ES6, transpiled with Babel and unit-tested with jest.

## TODO

* view / modify settings, especially global brightness slider
* view effective wiring
* web vitals & health check 
* admin authentication (stretch)
