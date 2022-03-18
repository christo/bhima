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
    Paper,
    Slider,
    Stack,
    Switch,
    ThemeProvider
} from "@mui/material";
import {
    AccessTime,
    Apps,
    BrightnessHigh,
    BrightnessLow,
    Category,
    ConnectedTv,
    Details,
    Image,
    Info,
    Label,
    LocalMovies,
    QuestionMark,
    Settings,
    TextFields,
    TextRotationNone
} from "@mui/icons-material";
import {pink, purple} from "@mui/material/colors";
import Logo from "./dragon-head-neg.png";

// TODO read how to reduce bundle size from imports: https://mui.com/guides/minimizing-bundle-size/

/** Iff true, continuously fetch the current program info causing animated live thumbnail. */
const LIVE_UPDATE = true;  // TODO change to an update delay in ms

/** Iff true, limits the rate of updates as a result of continuous settings changes, i.e. brightness */
const DO_SPAM_LIMIT = false;  // TODO convert to update delay in ms

/** Movie program, playing video file at configured speed, loops for duration if shorter than duration. */
const TYPE_MOVIE = "Movie";
/** (planned) Code that produces animation, possibly interactive, possibly sound-responsive. */
const TYPE_ALGORITHM = "Algorithm";
/** Image with configured animation, e.g. kaliedoscope, rotozoom, scroll etc. */
const TYPE_IMAGE = "Image";
/** Video Stream */
const TYPE_STREAM = "Stream";
/** Text scroller */
const TYPE_TEXT = "Text";

/**
 * Returns value clamped between lowest and highest.
 */
function clamp(value, lowest, highest) {
    return Math.min(Math.max(lowest, value), highest);
}

// TODO complete colour scheme: purple/aqua/pink/lightgrey/black  (?)
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

    let daysStr = (days > 0) ? (days + " days ") : ("");
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

    const deps = LIVE_UPDATE ? [program, setProgram] : [];

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
    return (
        <Container className="page">
            <Box display="flex" justifyContent="space-between">
                <h3>Live</h3>

                <IconButton
                    onClick={toggleDrawer(true)}
                    size="large"
                    edge="start"
                    color="primary"
                >
                    <Settings/>
                </IconButton>
            </Box>
            <CurrentProgram program={program} setProgram={setProgram}/>
            <h3>All Programs</h3>
            <ProgramList setProgram={setProgram}/>
            <Drawer
                anchor="right"
                open={sysOpen}
                onClose={toggleDrawer(false)}
                onOpen={toggleDrawer(true)}
            >
                <SystemPage {...props}/>
            </Drawer>
        </Container>
    );
};

/** Show global settings controls */
function SettingsPanel(props) {
    const [settings, setSettings] = useState(props.settings);

    let lastUpdate = Date.now();

    /** create a handler for the given switch */
    const updateSettings = (switchName) => (event, newValue) => {
        const thisUpdate = Date.now();
        let spamLimited = thisUpdate - lastUpdate > 100;
        if (!DO_SPAM_LIMIT || spamLimited) {
            if (event && event.target !== undefined) {
                event.stopImmediatePropagation();
                let newSettings = {
                    mute: settings.mute,
                    sleep: settings.sleep,
                    luminanceCorrection: settings.luminanceCorrection,
                    brightness: settings.brightness,
                    autoThrottle: settings.autoThrottle
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
                    }
                });
            }
        }
    }

    return <React.Fragment>

        <h3>Global Settings</h3>
        <Stack className="settings">
            <FormControl>
                <FormControlLabel control={<Switch checked={settings.luminanceCorrection}/>}
                                  label="Gamma Correction" onChange={updateSettings("luminanceCorrection")}/>
                <FormControlLabel control={<Switch checked={settings.autoThrottle}
                                   onChange={updateSettings("autoThrottle")}/>} label="Autothrottle"/>
                <FormControlLabel control={<Switch checked={settings.mute}/>} label="Mute"
                                  onChange={updateSettings("mute")}/>

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
            <Container className="page" sx={{width: "80vw"}}>
                <Box
                    component="img"
                    sx={{objectFit: "contain", top: -80, overflow: "hidden", height: "200px", width: "100%", margin: 0}}
                    alt="Bhima logo"
                    className="logo"
                    src={Logo}
                />
                <h3>System</h3>
                <List>
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
                            <AccessTime fontSize="large"/>
                        </ListItemIcon>
                        <ListItemText primary="Uptime" secondary=<StopWatch seconds={systemInfo.uptimeSeconds}/>/>
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

                    {/* TODO: RAM, disk, load, temp, */}
                </List>
                <SettingsPanel settings={systemInfo.settings}/>

                <Box sx={{display: 'flex', justifyContent: 'end', mt: 2, mb: 4, mr: 2}}>
                    <Button variant="contained" href="#puge-cache" disabled>
                        Purge Cache
                    </Button>
                </Box>

                <h3>LED Controllers</h3>
                <p>TODO</p>

                <h3>Program Types</h3>
                <List sx={{paddingTop: 0}}>
                    {systemInfo.programTypes.map(pt => (
                        <ListItem key={pt.name}>
                            <ListItemIcon>
                                <ProgramTypeIcon type={pt.name} fontSize="large"/>
                            </ListItemIcon>
                            <ListItemText primary={pt.name} secondary={pt.description}/>
                        </ListItem>
                    ))}

                </List>

                <h3>Wiring</h3>

                <p>TODO</p>

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
