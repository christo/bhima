import React, {useEffect, useState} from 'react';
import './App.css';
import {
    Alert,
    Box,
    Button,
    CircularProgress,
    Container,
    createTheme,
    CssBaseline,
    Dialog,
    DialogActions,
    DialogTitle,
    Drawer,
    FormControl,
    FormControlLabel,
    IconButton,
    List,
    ListItem,
    ListItemIcon,
    ListItemText,
    Paper, Slide,
    Slider, Snackbar,
    Stack,
    Switch, Table, TableBody, TableCell, TableContainer, TableHead, TableRow,
    ThemeProvider
} from "@mui/material";
import {
    AccessTime,
    Apps,
    BrightnessHigh,
    BrightnessLow,
    Category,
    ConnectedTv,
    Details, Favorite,
    Image,
    Info,
    Label,
    LocalMovies, Margin, NetworkCheck,
    QuestionMark,
    Settings, SettingsInputComponent,
    TextFields,
    TextRotationNone, Timer,
} from "@mui/icons-material";
import {pink, purple} from "@mui/material/colors";
import Logo from "./dragon-head-neg.png";

/** Iff true, limits the rate of updates as a result of continuous settings changes, i.e. brightness */
const DO_SPAM_LIMIT = false;

// see com.chromosundrift.bhima.api.ProgramType
/** Movie program, playing video file at configured speed, loops for duration if shorter than duration. */
const TYPE_MOVIE = "Movie";
/** Code that produces animation, possibly interactive, possibly sound-responsive. */
const TYPE_ALGORITHM = "Algorithm";
/** Image with configured animation, e.g. kaliedoscope, rotozoom, scroll etc. */
const TYPE_IMAGE = "Image";
/** Video Stream */
const TYPE_STREAM = "Stream";
/** Text scroller */
const TYPE_TEXT = "Text";

const bhimaTheme = createTheme({
    palette: {
        mode: 'dark',
        primary: {
            main: purple[400],
        },
        secondary: {
            main: pink[400],
        },
    },
});

/** Converts seconds to human-readable duration format */
function secondsToHuman(totalSecs) {
    let days    = Math.floor(totalSecs / 86400);
    let hours   = Math.floor((totalSecs - (days * 86400)) / 3600);
    let minutes = Math.floor((totalSecs - (days * 86400) - (hours * 3600)) / 60);
    let seconds = Math.floor(totalSecs - (days * 86400) - (hours * 3600) - (minutes * 60));

    let daysStr = (days > 0) ? (days + (days === 1 ? " day " : " days ")) : ("");
    if (hours < 10) {
        hours = "0" + hours;
    }
    if (minutes < 10) {
        minutes = "0" + minutes;
    }
    if (seconds < 10) {
        seconds = "0" + seconds;
    }
    return daysStr + hours+':'+minutes+':'+seconds;
}

function getEndpoint(name) {
    // TODO derive from config with devmode / production
    return '//' + window.location.hostname + ":9000/api/bhima/" + name;
}

function getHeaders() {
    return {
        'Content-Type': 'application/json',
        'Accept': 'application/json'
    };
}

function bhimaFetch(name) {
    return fetch(getEndpoint(name), {
        headers: getHeaders()
    }).then(res => res.json());
}

/**
 * Shows the icon for the given program type (e.g. Movie, Program, ...future types).
 *
 * @param props
 * @returns {JSX.Element}
 * @constructor
 */
const ProgramTypeIcon = (props) => {
    const {type, fontSize} = props;
    switch(type) {
        case TYPE_MOVIE: return <LocalMovies className="programType" fontSize={fontSize}/>;
        case TYPE_ALGORITHM: return <Apps className="programType" fontSize={fontSize} />;
        case TYPE_IMAGE: return <Image className="programType" fontSize={fontSize} />;
        case TYPE_STREAM: return <ConnectedTv className="programType" fontSize={fontSize} />;
        case TYPE_TEXT: return <TextFields className="programType" fontSize={fontSize} />;
        default: return <QuestionMark className="programType" fontSize={fontSize} />;
    }
};

function NetworkError(props) {
    return <Alert severity="error">Dragonmind link failure: {props.message}</Alert>;
}

const CurrentProgram = (props) => {
    const {program, setProgram} = props;

    const [loaded, setLoaded] = useState(false);
    const [error, setError] = useState(null);

    // TODO get latest liveUpdate setting by properly depending on systemInfo.settings.liveVideo
    // doesn't currently work because of the following message. Maybe fix with useEffect()
    //
    // react_devtools_backend.js:3973 Warning: The final argument passed to useEffect changed size between renders.
    // The order and size of this array must remain constant.
    //
    // Previous: []
    // Incoming: [[object Object], function () { [native code] }, [object Object]]
    // at CurrentProgram (http://localhost:3000/static/js/bundle.js:257:5)
    // at div
    // at http://localhost:3000/static/js/bundle.js:2169:66
    //     at Container (http://localhost:3000/static/js/bundle.js:9565:82)
    // at HomePage (http://localhost:3000/static/js/bundle.js:475:80)
    // at div
    // at http://localhost:3000/static/js/bundle.js:2169:66
    //     at Container (http://localhost:3000/static/js/bundle.js:9565:82)
    // at InnerThemeProvider (http://localhost:3000/static/js/bundle.js:19937:70)
    // at ThemeProvider (http://localhost:3000/static/js/bundle.js:19644:5)
    // at ThemeProvider (http://localhost:3000/static/js/bundle.js:19957:5)
    // at App (http://localhost:3000/static/js/bundle.js:1432:86)
    let liveUpdate = false;
    if (props.systemInfo && props.systemInfo.settings) {
        liveUpdate = props.systemInfo.settings.liveVideo;
    }
    const deps = liveUpdate ? [program, setProgram, props.systemInfo] : [props.systemInfo];

    useEffect(() => {
        bhimaFetch("program")
            .then(
                (result) => {
                    setProgram(result);
                    setLoaded(true);
                },
                (error) => {
                    setError(error);
                    setLoaded(true);
                }
            );
    }, deps);
    if (error) {
        return NetworkError(error);
    } else if (!loaded) {
        return <CircularProgress color="secondary" />;
    } else {
        if (program === null) {
            return null;
        } else {
            return <ProgramCard program={program}/>;
        }
    }
}

const ProgramCard = (props) => {
    const {program, setProgram} = props;

    let [open, setOpen] = React.useState(false);

    const handleOpen = () => {
        setOpen(true);
    }
    const handleCancel = () => {
        setOpen(false);
    }
    const handleRun = () => {
        setOpen(false);
        let data = new FormData();
        data.append("id", program.id);
        fetch(getEndpoint("runProgram2?id=" + encodeURI(program.id)), {
            method: 'POST',
            headers: getHeaders()
        }).then(res => res.json())
            .then(() => {
                setProgram(program);
                window.scrollTo(0, 0);
            });
    };

    return <React.Fragment>
        <Dialog open={open} onClose={handleCancel}>
            <DialogTitle>{`Run ${program.name} now?`}</DialogTitle>
            <DialogActions>
                <Button onClick={handleCancel}>Cancel</Button>
                <Button onClick={handleRun} autoFocus>Run</Button>
            </DialogActions>
        </Dialog>
        <Paper className="program" onClick={handleOpen}>
            <img className="thumbnail" alt={`thumbnail for ${program.name}`}
                 src={"data:image/jpg;charset=utf-8;base64," + program.thumbnail}/>
            <ProgramTypeIcon type={program.type.name} fontSize="large"/>
            <span className="programName">{program.name}</span>
            {/*<span className="duration">{program.durationSeconds}</span>*/}
        </Paper>
    </React.Fragment>;
};

const ProgramList = (props) => {
    const {setProgram} = props;
    const [loaded, setLoaded] = useState(false);
    const [programs, setPrograms] = useState([]);
    const [error, setError] = useState(null);
    useEffect(() => {
        bhimaFetch("programs").then(
            (result) => {
                setLoaded(true);
                setPrograms(result);
            },
            (error) => {
                setLoaded(true);
                setError(error);
            }
        );
    }, []);
    if (error) {
        return null;
    } else if (!loaded) {
        return <CircularProgress color="secondary" />;
    } else {
        return (
            <Stack spacing={2} sx={{margin: "auto"}}>
                {programs.map(program => (
                    <ProgramCard key={program.id} program={program} setProgram={setProgram}/>
                ))}
            </Stack>
        );
    }
};

const HomePage = (props) => {
    const [program, setProgram] = useState(null);
    const [sysOpen, setSysOpen] = useState(false);
    const [lofOpen, setLofOpen] = useState(false);
    const toggleDrawer = (open) => (event) => {
        if (
            event &&
            event.type === 'keydown' &&
            (event.key === 'Tab' || event.key === 'Shift')
        ) {
            return;
        }
        setSysOpen(open);
    };
    const lofSnack = () => {
        setLofOpen(true);
    };
    const handleLofClose = () => {
        setLofOpen(false);
    };
    function SlideTransition(props) {
        return <Slide {...props} direction="up" />;
    }
    return (
        <Container className="page">
            <Box display="flex" justifyContent="space-between">
                <h3>Bhima</h3>
                {/*TODO fix highlight circle is oblate */}
                <IconButton
                    onClick={toggleDrawer(true)}
                    size="large"
                    edge="start"
                    color="primary"
                >
                    <Settings/>
                </IconButton>
            </Box>
            <CurrentProgram program={program} setProgram={setProgram} systemInfo={props.systemInfo}/>
            <h3>All Programs</h3>
            <ProgramList setProgram={setProgram}/>
            <Box sx={{display: "flex", justifyContent: "center", margin: 4}} onClick={lofSnack}>
                <Button onClick={lofSnack} size="large"><Favorite fontSize="large"/></Button></Box>

            <Snackbar open={lofOpen} autoHideDuration={1600} onClose={handleLofClose}
                TransitionComponent={SlideTransition}>
                <Alert severity="success" sx={{ width: '100%' }} onClose={handleLofClose}>
                    Love Over Fear
                </Alert>
            </Snackbar>

            <Drawer
                anchor="right"
                open={sysOpen}
                onClose={toggleDrawer(false)}
            >
                <SystemPage {...props}/>
            </Drawer>
        </Container>
    );
};

/** Show global settings controls */
function SettingsPanel(props) {
    const [settings, setSettings] = useState(props.systemInfo.settings);
    const setSystemInfo = props.setSystemInfo;
    // TODO trigger update on systemInfo
    let lastUpdate = Date.now();

    /** create a handler for the given switch */
    const updateSettings = (switchName) => (event, newValue) => {
        const thisUpdate = Date.now();
        let spamLimited = thisUpdate - lastUpdate > 100;
        if (!DO_SPAM_LIMIT || spamLimited) {
            if (event && event.target !== undefined) {
                let newSettings = {
                    mute: settings.mute,
                    sleep: settings.sleep,
                    luminanceCorrection: settings.luminanceCorrection,
                    brightness: settings.brightness,
                    autoThrottle: settings.autoThrottle,
                    liveVideo: settings.liveVideo
                };
                newSettings[switchName] = newValue;
                let body = JSON.stringify(newSettings);
                fetch(getEndpoint("settings"), {
                    method: 'POST',
                    headers: getHeaders(),
                    body: body
                }).then(res => res.json()).then((result, error) => {
                    if (error) {
                        console.error(error.message);
                    } else {
                        setSettings(result);
                        setSystemInfo(prevSi => {
                            prevSi.settings = settings
                            return prevSi;
                        })
                    }
                });
            }
        }
    }

    return <React.Fragment>

        <Stack className="settings">
            <FormControl>
                <FormControlLabel control={<Switch checked={settings.luminanceCorrection}/>}
                                  label="Gamma Correction" onChange={updateSettings("luminanceCorrection")}/>
                <FormControlLabel control={<Switch checked={settings.autoThrottle}
                                   onChange={updateSettings("autoThrottle")}/>} label="Autothrottle"/>
                <FormControlLabel control={<Switch checked={settings.mute}/>} label="Mute"
                                  onChange={updateSettings("mute")}/>
                <FormControlLabel control={<Switch checked={settings.liveVideo}/>} label="Live Video"
                                  onChange={updateSettings("liveVideo")}/>

                <Stack spacing={2} direction="row" sx={{mb: 1, width: "90%"}} alignItems="center">
                    <BrightnessLow/>
                    <Slider aria-label="Brightness"
                            value={settings.brightness}
                            valueLabelDisplay="auto"
                            min={0.0}
                            max={1.0}
                            step={0.02}
                            onChange={updateSettings("brightness")}/>
                    <BrightnessHigh/>
                </Stack>
                <FormControlLabel control={<Switch checked={settings.sleep} disabled
                                   onChange={updateSettings("sleep")}/>} label="Sleep"/>
            </FormControl>
        </Stack>
    </React.Fragment>;
}

function StopWatch(props) {
    const [elapsed, setElapsed] = useState();

    const elapsedInitial = props.seconds;
    const timeInitial = new Date();
    function refreshClock() {
        setElapsed((new Date().getTime() - timeInitial) / 1000 + elapsedInitial);
    }
    useEffect(() => {
        const timerId = setInterval(refreshClock, 1000);
        return function cleanup() {
            clearInterval(timerId);
        };
    }, []);
    return <React.Fragment>{secondsToHuman(elapsed)}</React.Fragment>;
}

function PortList(props) {
    const {ports} = {...props};
    return <React.Fragment>
    {ports.map(i => (
        <Box key={i} sx={{marginLeft: 1, color: "secondary"}}>{i}</Box>
    ))}
    </React.Fragment>;
}

function LedControllers(props) {
    const controllers = props.controllers;
    if (controllers.length === 0) {
        return <p><i>None detected</i></p>
    } else {
        return <React.Fragment>
            {controllers.map(c => (
                <Paper key={c.name} sx={{padding: 1, marginBottom: 2}}>
                    <Stack>
                        <Box>
                            <List>
                                <ListItem>
                                    <ListItemIcon><Margin fontSize="large"/></ListItemIcon>
                                    <ListItemText primary={`${c.species} ${c.name}`} secondary={c.address}/>
                                </ListItem>
                                <ListItem>
                                    <ListItemIcon><Timer fontSize="large"/></ListItemIcon>
                                    <ListItemText primary="Latency" secondary={c.latency}/>
                                </ListItem>
                                <ListItem>
                                    <ListItemIcon><NetworkCheck fontSize="large"/></ListItemIcon>
                                    <ListItemText primary="Bandwidth" secondary={c.bandwidth}/>
                                </ListItem>

                            </List>
                        </Box>
                        {Object.entries(c.stats).map((kv, i) => (
                            <Box key={i} sx={{marginLeft: 3, marginBottom: 2}}>
                                <ListItemText primary={kv[0]} secondary={kv[1]}/>
                            </Box>
                        ))}
                    </Stack>

                </Paper>

            ))}
        </React.Fragment>;
    }
}

function Wiring(props) {
    const wiring = props.wiring;
    return <TableContainer component={Paper} sx={{marginBottom: 5}}>
        <Table sx={{ minWidth: "100%" }} aria-label="simple table">
            <TableHead>
                <TableRow>
                    <TableCell>Panel Group</TableCell>
                    <TableCell align="right"><SettingsInputComponent fontSize="small"/></TableCell>
                </TableRow>
            </TableHead>
            <TableBody>
                {Object.entries(wiring).map((kv, i) => (
                    <TableRow key={i}
                              sx={{ '&:last-child td, &:last-child th': { border: 0 } }}
                    >
                        <TableCell component="th" scope="row">
                            {kv[0]}
                        </TableCell>

                        <TableCell align="right"><PortList ports={kv[1]}/></TableCell>
                    </TableRow>
                ))}
            </TableBody>
        </Table>
    </TableContainer>;
}

function SystemSummary(props) {
    const systemInfo = props.systemInfo;
    return <List>
        <ListItem>
            <ListItemIcon>
                <Info fontSize="large"/>
            </ListItemIcon>
            <ListItemText primary="Status" secondary={systemInfo.status}/>
        </ListItem>
        <ListItem>
            <ListItemIcon>
                <Label fontSize="large"/>
            </ListItemIcon>
            <ListItemText primary="Version" secondary={systemInfo.version}/>
        </ListItem>
        <ListItem>
            <ListItemIcon>
                <Label fontSize="large"/>
            </ListItemIcon>
            <ListItemText primary="System Name" secondary={systemInfo.name}/>
        </ListItem>
        <ListItem>
            <ListItemIcon>
                <AccessTime fontSize="large"/>
            </ListItemIcon>
            <ListItemText primary="Uptime" secondary=<StopWatch seconds={systemInfo.uptimeSeconds}/>/>
        </ListItem>
        <ListItem>
            <ListItemIcon>
                <AccessTime fontSize="large"/>
            </ListItemIcon>
            <ListItemText primary="Load Average" secondary={systemInfo.loadAverage}/>
        </ListItem>
        <ListItem>
            <ListItemIcon>
                <TextRotationNone fontSize="large"/>
            </ListItemIcon>
            <ListItemText primary="Scroll Text" secondary={systemInfo.scrollText}/>
        </ListItem>
        <ListItem>
            <ListItemIcon>
                <ProgramTypeIcon type={systemInfo.currentProgram.type.name} fontSize="large"/>
            </ListItemIcon>
            <ListItemText primary="Current Program" secondary={systemInfo.currentProgram.name}/>
        </ListItem>
        <ListItem>
            <ListItemIcon>
                <Category fontSize="large"/>
            </ListItemIcon>
            <ListItemText primary="Project" secondary={systemInfo.configProject}/>
        </ListItem>
        <ListItem>
            <ListItemIcon>
                <Details fontSize="large"/>
            </ListItemIcon>
            <ListItemText primary="Project Version" secondary={systemInfo.configVersion}/>
        </ListItem>
    </List>;
}

function ProgramTypesList(props) {
    const programTypes = props.programTypes;
    return <List sx={{paddingTop: 0}}>
        {programTypes.map(pt => (
            <ListItem key={pt.name}>
                <ListItemIcon>
                    <ProgramTypeIcon type={pt.name} fontSize="large"/>
                </ListItemIcon>
                <ListItemText primary={pt.name} secondary={pt.description}/>
            </ListItem>
        ))}

    </List>;
}

const SystemPage = (props) => {
    const {systemInfo, setSystemInfo, error, loaded} = props;
    useEffect(() => {
    }, [systemInfo, setSystemInfo, error, loaded]);
    if (error) {
        return NetworkError(error);
    } else if (!loaded) {
        return <CircularProgress color="secondary" />;
    } else {
        return (
            <Container className="page" sx={{width: "80vw", backgroundColor: "black"}}>
                <Box
                    component="img"
                    sx={{objectFit: "contain", top: -80, overflow: "hidden", height: "200px", width: "100%", margin: 0}}
                    alt="Bhima logo"
                    className="logo"
                    src={Logo}
                />
                <h3>System</h3>
                <SystemSummary systemInfo={systemInfo}/>

                <h3>Global Settings</h3>
                <SettingsPanel systemInfo={systemInfo} setSystemInfo={setSystemInfo}/>

                <Box sx={{display: 'flex', justifyContent: 'end', mt: 2, mb: 4, mr: 2}}>
                    <Button variant="contained" href="#puge-cache" disabled>
                        Purge Cache
                    </Button>
                </Box>

                <h3>LED Controllers</h3>
                <LedControllers controllers={systemInfo.ledControllers}/>

                <h3>Program Types</h3>
                <ProgramTypesList programTypes={systemInfo.programTypes}/>

                <h3>Wiring</h3>
                <Wiring wiring={systemInfo.effectiveWiring}/>

            </Container>
        );
    }
};

function App() {
    const [systemInfo, setSystemInfo] = useState(null);
    const [loaded, setLoaded] = useState(false);
    const [error, setError] = useState(null);
    useEffect(() => {
        bhimaFetch("systemInfo").then(
            (result) => {
                setLoaded(true);
                setSystemInfo(result);
            },
            (error) => {
                setLoaded(true);
                setError(error);
            }
        );
    }, []);
    return <ThemeProvider theme={bhimaTheme}>
            <CssBaseline enableColorScheme />
            <Container sx={{ display: 'flex', justifyItems: "space-around",
                justifyContent: "center", padding: 0, backgroundColor: "#434152", margin: 0, width: 1 }}>
                <HomePage systemInfo={systemInfo} setSystemInfo={setSystemInfo} error={error} loaded={loaded}/>
            </Container>
        </ThemeProvider>;
}

export default App;
